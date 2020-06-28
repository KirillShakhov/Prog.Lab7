package lab;

import lab.Commands.Command;
import lab.Commands.ConcreteCommands.Auth;
import lab.Commands.SerializedCommands.Message;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Thread.sleep;


public class ServerController implements Runnable {

	private static final int BUFF_SIZE = 1000000;
	static final String host = "127.0.0.1";
	static int port = 1010;
	private static final List<Message> incomingMessages = new LinkedList<>();

	ServerController(String port){
		ServerController.port = Integer.parseInt(port);
	}

	public void run() {
		//noinspection ResultOfMethodCallIgnored
		Runtime.getRuntime().addShutdownHook(new Thread(BD::save));
		try {
			Selector selector = Selector.open();
			ServerSocketChannel server = ServerSocketChannel.open();
			server.configureBlocking(false);
			server.socket().bind(new InetSocketAddress(host, port));
			server.register(selector, SelectionKey.OP_ACCEPT);

			System.out.println("Server started");

			StringBuilder console_line = new StringBuilder();
			//noinspection InfiniteLoopStatement
			while (true) {
				if (System.in.available() > 0) {
					int ch;
					while (true){
						ch = System.in.read();
						if(ch == 10){
							break;
						}
						else {
							console_line.append((char) ch);
						}
					}
					server_commands(console_line.toString());
					console_line = new StringBuilder();
				}
				selector.select();
				Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					iterator.remove();
					if (selector.isOpen()) {
						if(connectThread(key, selector, server)){continue;}
						if(readThread(key, selector)){continue;}
						if(writeThread(key, selector)){continue;}
						sleep(100);
					}
				}
			}
		} catch (SocketException e){
			System.out.println("Порт занят");
			System.exit(0);
		} catch(Exception e){
			System.out.println("Приложение завершено");
			e.printStackTrace();
		}
	}

	public static Message getSocketObjet(SocketChannel socketChannel) throws IOException, ClassNotFoundException {
		ByteBuffer data = ByteBuffer.allocate(BUFF_SIZE);
		socketChannel.read(data);
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data.array());
		ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
		Message message = (Message)objectInputStream.readObject();
		message.setSocketChannel(socketChannel);
		return message;
	}

	private static void sendSocketObject(SocketChannel client, Message message) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
		objectOutputStream.writeObject(message);
		objectOutputStream.flush();
		client.write(ByteBuffer.wrap(byteArrayOutputStream.toByteArray()));
	}

	private static void server_commands(String s){
		if(s.equals("save")){
			if (BD.save()) {
				System.out.println("Успешное сохранение");
			}else{
				System.out.println("Сохранение не удалось");
			}
		}
		else if(s.equals("end")){
			System.out.println("Выход из программы");
			if (!BD.save()) {
				System.out.println("Сохранение не удалось");
			}
			System.exit(0);
		}
	}

	private static boolean writeThread(SelectionKey key, Selector selector) throws IOException {
		if (key.isWritable() && key.isValid()) {
			SocketChannel channel = (SocketChannel) key.channel();
			String result = "Неизвестная команда";
			for (Message mes : incomingMessages){
				if(BD.checkPass(mes.getUser_name(), mes.getPass()) || (mes.getCommand().getClass() == Auth.class)) {
					if(mes.getSocketChannel().equals(channel)){
						Command command = mes.getCommand();
						result = command.execute(mes);
					}
				}
				incomingMessages.remove(mes);
			}
			sendSocketObject(channel, new Message(result));
			channel.register(selector, SelectionKey.OP_READ);
			return true;
		}
		return false;
	}
	private static boolean readThread(SelectionKey key, Selector selector) throws IOException {
		if (key.isReadable() && key.isValid()) {
			SocketChannel channel = (SocketChannel) key.channel();
			try {
				Message message = getSocketObjet(channel);
				message.setSocketChannel(channel);
				incomingMessages.add(message);
				channel.register(selector, SelectionKey.OP_WRITE);
			}
			catch (IOException | ClassNotFoundException e){
				System.out.println("Клиент отключился");
				channel.close();
			}
			return true;
		}
		return false;
	}
	private static boolean connectThread(SelectionKey key, Selector selector, ServerSocketChannel server) throws IOException {
		if (key.isAcceptable() && key.isValid()) {
			SocketChannel client = server.accept();
			client.configureBlocking(false);
			client.register(selector, SelectionKey.OP_READ);
			System.out.println("Новое подключение");
			return true;
		}
		return false;
	}
}