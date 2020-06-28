package lab;

import lab.Commands.CommandInvoker;
import lab.Commands.CommandReceiver;
import lab.Commands.ConcreteCommands.*;
import lab.Commands.SerializedCommands.Message;
import lab.Commands.Utils.Creaters.ElementCreator;

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static java.lang.Thread.sleep;


public class ClientController implements Runnable {
	private static final int BUFF_SIZE = 1000000;
	public static String name = null;
	public static String pass = null;
	static String hostname;
	static int port;

	static Scanner scanner = new Scanner(System.in);
	static boolean isAuth = false;
	static int reconect_schetchick = 1;
	static SocketChannel client;

	static CommandInvoker commandInvoker = new CommandInvoker();
	ElementCreator elementCreator = new ElementCreator();
	CommandReceiver commandReceiver = new CommandReceiver(commandInvoker, elementCreator);

	Selector selector;
	static SocketChannel connectionClient;

	public static ArrayList<String> level_list = new ArrayList<>();




	public ClientController(String hostname, String port) {
		ClientController.hostname = hostname;
		ClientController.port = Integer.parseInt(port);
	}

	public void run() {
		if(hostname == null || port == -1){
			System.out.println("Класс не инициализирован");
			throw new RuntimeException("Не инициализирован hostname и port");
		}
		else {
			while (true) {
				try {
					selector = Selector.open();
					connectionClient = SocketChannel.open();
					connectionClient.connect(new InetSocketAddress("localhost", port));
					connectionClient.configureBlocking(false);
					//connectionClient.register(selector, SelectionKey.OP_CONNECT);
					connectionClient.register(selector, SelectionKey.OP_WRITE);

					commandInvoker.register("help", new Help(commandReceiver));
					commandInvoker.register("add", new Add(commandReceiver));
					commandInvoker.register("info", new Info(commandReceiver));
					commandInvoker.register("show", new Show(commandReceiver));
					commandInvoker.register("update", new Update(commandReceiver));
					commandInvoker.register("remove_by_id", new RemoveByID(commandReceiver));
					commandInvoker.register("remove_by_description", new RemoveByDescription(commandReceiver));
					commandInvoker.register("filter_contains_name", new FilterContainsName(commandReceiver));
					commandInvoker.register("reorder", new Reorder(commandReceiver));
					commandInvoker.register("clear", new Clear(commandReceiver));
					commandInvoker.register("exit", new Exit(commandReceiver));
					commandInvoker.register("remove_greater", new RemoveGreater(commandReceiver));
					commandInvoker.register("remove_lower", new RemoveLower(commandReceiver));
					commandInvoker.register("execute_script", new ExecuteScript(commandReceiver));


					while (true) {
						selector.select();
						for (SelectionKey key : selector.selectedKeys()) {
							//iterator.remove();
							if (key.isValid()) {
								client = (SocketChannel) key.channel();
								if (client != null) {
									try {
										if (connectThread(key, selector)) {
											continue;
										}
										if (writeThread(key, selector)) {
											continue;
										}
										if (readThread(key, selector)) {
											continue;
										}
										sleep(10);
									} catch (ConnectException e) {
										System.out.println("В данный момент сервер не доступен, повторная попытка: " + reconect_schetchick);
										if (reconect_schetchick > 20) {
											System.exit(0);
										}
										selector = Selector.open();
										connectionClient = SocketChannel.open();
										connectionClient.configureBlocking(false);
										connectionClient.connect(new InetSocketAddress(hostname, port));
										connectionClient.register(selector, SelectionKey.OP_WRITE);
										reconect_schetchick++;
										sleep(1000);
									} catch (IOException ex) {
										System.out.println("Сервер закрыл соединение");
										System.out.println("Повторное подлючение");
										//client.register(selector, SelectionKey.OP_CONNECT);
										//connectionClient.register(selector, SelectionKey.OP_CONNECT);
										selector = Selector.open();
										connectionClient = SocketChannel.open();
										connectionClient.connect(new InetSocketAddress("localhost", port));
										connectionClient.configureBlocking(false);
										//connectionClient.register(selector, SelectionKey.OP_CONNECT);
										connectionClient.register(selector, SelectionKey.OP_WRITE);
									} catch (NoSuchElementException | InterruptedException e) {
										System.out.println("Завершение работы.");
										client.close();
										e.printStackTrace();

										System.exit(0);
									}
								}
							}
						}
					}
				} catch (ConnectException e) {
					//System.out.println("Невозможно подключиться к данному хосту или порту");
					//System.out.println("Возможно сервер временно не доступен или указан неправильный адрес");
					System.out.println("В данный момент сервер не доступен, повторная попытка: " + reconect_schetchick);
					if (reconect_schetchick > 20) {
						System.exit(0);
					}
					try {
						selector = Selector.open();
						connectionClient = SocketChannel.open();
						connectionClient.configureBlocking(false);
						connectionClient.connect(new InetSocketAddress(hostname, port));
						connectionClient.register(selector, SelectionKey.OP_WRITE);
					} catch (IOException ignored) {}
					reconect_schetchick++;
					try {
						sleep(1000);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static Message getSocketObject() throws IOException, ClassNotFoundException {
		ByteBuffer data = ByteBuffer.allocate(BUFF_SIZE);
		client.read(data);
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data.array());
		ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
		return (Message) objectInputStream.readObject();
	}

	public static void sendSocketObject(Message message) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
		objectOutputStream.writeObject(message);
		objectOutputStream.flush();
		client.write(ByteBuffer.wrap(byteArrayOutputStream.toByteArray()));
	}

	private static boolean readThread(SelectionKey key, Selector selector) throws IOException, ClassNotFoundException, InterruptedException {
		if ((key.interestOps() & SelectionKey.OP_READ) != 0) {
			if (!isAuth) {
				Message message = getSocketObject();
				System.out.println(message.getString());
				message.setUserPass(name, pass);
				switch (message.getString()) {
					case "Неправильный пароль":
						isAuth = false;
						name = null;
						pass = null;
						key.interestOps(SelectionKey.OP_WRITE);
						client.register(selector, SelectionKey.OP_WRITE);
						sleep(500);
						break;
					case "Успешная авторизация":
					case "Пользователь зарегистрирован":
						isAuth = true;
						System.out.println("Введите help");
						key.interestOps(SelectionKey.OP_WRITE);
						client.register(selector, SelectionKey.OP_WRITE);
						sleep(500);
						break;
					case "Пользователь не зарегистрировн, произошла ошибка":
						key.interestOps(SelectionKey.OP_WRITE);
						client.register(selector, SelectionKey.OP_WRITE);
						sleep(500);
						break;
				}
			} else {
				Message message = getSocketObject();
				System.out.println(message.getString());
				key.interestOps(SelectionKey.OP_WRITE);
				client.register(selector, SelectionKey.OP_WRITE);
				sleep(500);

			}
			return true;
		}
		return false;
	}

	private static boolean writeThread(SelectionKey key, Selector selector) throws IOException {
		if ((key.interestOps() & SelectionKey.OP_WRITE) != 0) {
			if (!isAuth) {
				System.out.println("Укажите имя пользователя:");
				name = scanner.nextLine().trim();
				System.out.println("Укажите пароль:");
				pass = scanner.nextLine().trim();
				Message message = new Message(new Auth(), name + ":::" + pass);
				sendSocketObject(message);
				key.interestOps(SelectionKey.OP_READ);
				client.register(selector, SelectionKey.OP_READ);
			} else {
				System.out.print(">");
				Message message = commandInvoker.executeCommand(scanner.nextLine().trim().split(" "));
				if (message != null) {
					message.setUserPass(ClientController.name, ClientController.pass);
					sendSocketObject(message);
					key.interestOps(SelectionKey.OP_READ);
					client.register(selector, SelectionKey.OP_READ);
				}
			}
			return true;
		}
		return false;
	}

	private static boolean connectThread(SelectionKey key, Selector selector) throws IOException, InterruptedException {
		if ((key.interestOps() & SelectionKey.OP_CONNECT) != 0) {
			try {
				if (client.finishConnect()) {
					key.interestOps(SelectionKey.OP_WRITE);
					client.register(selector, SelectionKey.OP_WRITE);
					System.out.println("Введите help");
				} else {
					System.out.println("В данный момент сервер не доступен, повторная попытка: " + reconect_schetchick);
					if (reconect_schetchick > 20) {
						System.exit(0);
					}
					selector = Selector.open();
					connectionClient = SocketChannel.open();
					connectionClient.configureBlocking(false);
					connectionClient.connect(new InetSocketAddress(hostname, port));
					connectionClient.register(selector, SelectionKey.OP_WRITE);
					reconect_schetchick++;
					sleep(1000);
				}
				return true;
			} catch (IOException | InterruptedException e) {
				System.out.println("В данный момент сервер не доступен, повторная попытка: " + reconect_schetchick);
				if (reconect_schetchick > 20) {
					System.exit(0);
				}
				selector = Selector.open();
				connectionClient = SocketChannel.open();
				connectionClient.configureBlocking(false);
				connectionClient.connect(new InetSocketAddress(hostname, port));
				connectionClient.register(selector, SelectionKey.OP_WRITE);
				reconect_schetchick++;
				sleep(1000);
				return true;
			}
		}
		return false;
	}
}