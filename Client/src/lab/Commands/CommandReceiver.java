package lab.Commands;

import lab.BasicClasses.Album;
import lab.BasicClasses.MusicBand;
import lab.ClientController;
import lab.Commands.ConcreteCommands.*;
import lab.Commands.SerializedCommands.Message;
import lab.Commands.Utils.Creaters.ElementCreator;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import static lab.ClientController.getSocketObject;
import static lab.ClientController.sendSocketObject;

/**
 * Ресивер(получатель), отправляет серилизованные объекты на сервер.
 */
public class CommandReceiver {
    private final CommandInvoker commandInvoker;
    private final ElementCreator elementCreator;

    public CommandReceiver(CommandInvoker commandInvoker, ElementCreator elementCreator) {
        this.commandInvoker = commandInvoker;
        this.elementCreator = elementCreator;
    }

    public Message help() {
        commandInvoker.getCommandMap().forEach((name, command) -> command.writeInfo());
        return null;
    }

    public Message info() {
        //sender.sendObject(new Message("Хей"));
        ////Thread.sleep(delay);
        return new Message(new Info());
    }

    public Message show() {
        //sender.sendObject(new Message(new Show()));
        ////Thread.sleep(delay);
        return new Message(new Show());
    }

    public Message add() {
        return new Message(new Add(), elementCreator.createMusicBand());
    }

    /**
     *
     * @param ID - апдейт элемента по ID.
     */
    public Message update(String ID) {
        //sender.sendObject(new SerializedCombinedCommand(new Update(), elementCreator.createMusicBand(), ID));
        ////Thread.sleep(delay);
        return new Message(new Update(), elementCreator.createMusicBand(), ID);
    }

    /**
     *
     * @param ID - удаление по ID.
     */
    public Message removeById(String ID) {
        //sender.sendObject(new SerializedArgumentCommand(new RemoveByID(), ID));
        ////Thread.sleep(delay);
        return new Message(new RemoveByID(), ID);
    }

    public Message removeByDescription(String des) {
        //sender.sendObject(new SerializedArgumentCommand(new RemoveByID(), ID));
        ////Thread.sleep(delay);
        return new Message(new RemoveByDescription(), des);
    }

    public Message clear() {
        //sender.sendObject(new SerializedSimplyCommand(new Clear()));
        ////Thread.sleep(delay);
        return new Message(new Clear());
    }

    public Message exit() {
        System.out.println("Завершение работы клиента.");
        System.exit(0);
        return null;
    }

    public Message filter_contains_name(String arg){
        return new Message(new FilterContainsName(), arg);
    }

    public Message removeGreater() {
        return new Message(new RemoveGreater(), elementCreator.createMusicBand());
    }

    public Message removeLower() {
        return new Message(new RemoveLower(), elementCreator.createMusicBand());
    }

    public Message executeScript(String path) {
        String line;
        String command;
        ArrayList<String> parameters = new ArrayList<>();
        ClientController.level_list.add("execute_script " + path);
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) {
            while ((line = bufferedReader.readLine()) != null) {
                if (line.split(" ")[0].matches("add|update|remove_lower|remove_greater")) {
                    parameters.clear();
                    command = line;
                    for (int i = 0; i < 11; i++) {
                        if (line != null) {
                            line = bufferedReader.readLine();
                            parameters.add(line);
                        } else { System.out.println("Не хватает параметров для создания объекта."); break; }
                    }
                    MusicBand musicBand = elementCreator.createScriptMusicBand(parameters);
                    if (musicBand != null) {
                        switch (command.split(" ")[0]) {
                            case "add":
                                sendSocketObject(new Message(new Add(), musicBand));
                                Thread.sleep(100);
                                try {
                                    Message message = getSocketObject();
                                    System.out.println(message.getString());
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                    System.out.println("Команда не сработала");
                                }
                                break;
                            case "update":
                                sendSocketObject(new Message(new Update(), musicBand));
                                Thread.sleep(100);
                                try {
                                    Message message = getSocketObject();
                                    System.out.println(message.getString());
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                    System.out.println("Команда не сработала");
                                }
                                break;
                            case "remove_greater":
                                sendSocketObject(new Message(new RemoveGreater(), musicBand));
                                Thread.sleep(100);
                                try {
                                    Message message = getSocketObject();
                                    System.out.println(message.getString());
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                    System.out.println("Команда не сработала");
                                }
                                break;
                            case "remove_lower":
                                sendSocketObject(new Message(new RemoveLower(), musicBand));
                                Thread.sleep(100);
                                try {
                                    Message message = getSocketObject();
                                    System.out.println(message.getString());
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                    System.out.println("Команда не сработала");
                                }
                                break;
                        }
                    }
                } else if (line.split(" ")[0].equals("execute_script")
                        && line.split(" ")[1].equals(ExecuteScript.getPath())) { System.out.println("Пресечена попытка рекурсивного вызова скрипта."); }
                else { commandInvoker.executeCommand(line.split(" ")); }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Ошибка! " + e.getMessage());
        }
        return null;

    }

    public Message countGreaterThanBestAlbum() {
        return new Message(new CountGreaterThanBestAlbum(), new MusicBand(elementCreator.createAlbum()));
    }

    public Message reorder() {
        return new Message(new Reorder());
    }

    public Message auth() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите имя:");
        System.out.print(">");
        String name = null;
        if(scanner.hasNextLine()) {
            name = scanner.nextLine();
        }
        System.out.println("Введите пароль:");
        System.out.print(">");
        String pass = null;
        if(scanner.hasNextLine()) {
            pass = scanner.nextLine();
        }
        return new Message(new Auth(), name+":::"+pass);
    }

    public Message register() {
        Scanner scanner = new Scanner(System.in);
        String name = "";
        String pass = "";
        String pass2 = "";

        System.out.println("Введите имя:");
        System.out.print(">");
        if(scanner.hasNextLine()) {
            name = scanner.nextLine();
        }
        while (true) {
            try {
                System.out.println("Введите пароль:");
                System.out.print(">");
                if (scanner.hasNextLine()) {
                    pass = scanner.nextLine();
                    if (pass.equals("")) {
                        System.out.println("Пароль не может быть пустым");
                        continue;
                    }
                }
                System.out.println("Введите ещё раз пароль:");
                System.out.print(">");
                if (scanner.hasNextLine()) {
                    pass2 = scanner.nextLine();
                    if (pass.equals("")) {
                        System.out.println("Пароль не может быть пустым");
                        continue;
                    }
                }
                if (pass.equals(pass2)) {
                    break;
                }
            } catch (Exception ignored) { }
        }
        return new Message(new Register(), name+":::"+pass);
    }
}
