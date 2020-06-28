package lab.Commands.SerializedCommands;

import lab.BasicClasses.MusicBand;
import lab.Commands.Command;

import java.io.Serializable;
import java.nio.channels.SocketChannel;

public class Message implements Serializable {
    private static final long serialVersionUID = 34L;
    transient SocketChannel socketChannel;
    private String string;
    private Command command;
    private String args;
    private MusicBand musicBand;
    private String user_name;
    private String pass;

    public Message(String string) {
        this.string = string;
    }

    public Message(SocketChannel socketChannel, String string) {
        this.socketChannel = socketChannel;
        this.string = string;
    }

    public Message(SocketChannel socketChannel, Command command) {
        this.socketChannel = socketChannel;
        this.command = command;
    }

    public Message(Command command, MusicBand musicBand, String args) {
        this.command = command;
        this.musicBand = musicBand;
        this.args = args;
    }

    public Message(Command command, String args) {
        this.command = command;
        this.args = args;
    }

    public Message(Command command, MusicBand musicBand) {
        this.command = command;
        this.musicBand = musicBand;
    }

    public void setUserPass(String name, String pass){
        this.user_name = name;
        this.pass = pass;
    }

    public Message(Command command) {
        this.command = command;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getPass() {
        return pass;
    }

    public MusicBand getMusicBand() {
        return musicBand;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }
}
