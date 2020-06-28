package lab.Commands;


import lab.BD;
import lab.BasicClasses.Album;
import lab.BasicClasses.MusicBand;
import lab.BasicClasses.User;
import lab.Commands.SerializedCommands.Message;
import lab.Utils.Validator;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Ресивер(получатель), отправляет серилизованные объекты на сервер.
 */
public class CommandReceiver {
    public CommandReceiver() {

    }

    public String info() {
        System.out.println("Клиенту отправлен результат работы команды INFO");
        return "Коллекция типа: ArrayList\nДата инициализации: " + BD.getCreateTime() + "\nКоличество элементов: " + BD.size();
        //return "Коллекция типа: ArrayList\nДата инициализации: ";
    }

    public String show() {
        BD.sort();
        StringBuilder result = new StringBuilder();
        if (BD.size() != 0) {
            for (int i = 0; i < BD.size(); i++) {
                result.append(BD.get(i).toString()).append("\n");
            }
        } else {
            result = new StringBuilder(("Элементы отсутствуют"));
        }
        System.out.println("Клиенту отправлен результат работы команды SHOW");
        return result.toString();
    }

    public String add(MusicBand o) {
        o.setID(BD.giveID());
        o.setCreationDate(LocalDateTime.now());
        if (Validator.validateMusicBand(o)) {
            if (BD.add(o)) {
                return "Элемент добавлен в коллекцию.";
            } else {
                return "Элемент не добавлен в коллекцию.";
            }
        } else {
            return "Полученный элемент не прошел валидацию на стороне сервера.";
        }
    }

    /**
     *
     */
    public String update(Message mes) {
        int groupId;
        try {
            groupId = Integer.parseInt(mes.getArgs());
            if (BD.checkExist(groupId)) {
                if (Validator.validateMusicBand(mes.getMusicBand())) {
                    MusicBand o = mes.getMusicBand();
                    o.setID((long) groupId);
                    o.setCreationDate(LocalDateTime.now());
                    if (BD.get(groupId).getUser_creator().equals(o.getUser_creator())) {
                        if (BD.update(o, groupId)) {
                            return "Команда update выполнена.";
                        } else {
                            return "Команда update не выполнена.";
                        }
                    } else {
                        return "Команда update не выполнена. Вы не владелец этого объекта.";
                    }
                } else {
                    return "Полученный элемент не прошел валидацию на стороне сервера.";
                }
            } else {
                return "Элемента с таким ID нет в коллекции.";
            }
        } catch (NumberFormatException e) {
            return "Команда не выполнена. Вы ввели некорректный аргумент.";
        }
    }

    /**
     * @param ID - удаление по ID.
     */
    public String removeById(String ID, String name) {
        int groupId;
        try {
            groupId = Integer.parseInt(ID);
            if (BD.checkExist(groupId)) {
                if (BD.get(groupId).getUser_creator().equals(name)) {
                    if (BD.remove(groupId)) {
                        return "Элемент с ID " + groupId + " успешно удален.";
                    } else {
                        return "Команда remove не выполнена.";
                    }
                } else {
                    return "Элемент с ID " + groupId + " не принадлежит вам.";
                }
            } else {
                return "Элемента с таким ID нет в коллекции.";
            }
        } catch (NumberFormatException e) {
            return "Команда не выполнена. Вы ввели некорректный аргумент.";
        }
    }

    public String removeByDescription(String description, String name) {
        int j = 0;
        for (int i = 0; i < BD.size(); i++) {
            if (BD.get(i).getDescription().equals(description)) {
                if (BD.get(i).getUser_creator().equals(name)) {
                    if (BD.remove(i)) {
                        j++;
                    }
                }
            }
        }
        if (j == 0) {
            return "Таких элементов нет";
        } else {
            return "Удалено " + j + " объектов";
        }
    }

    public String clear() {
        BD.clean();

        return "Коллекция успешно очищена.";
    }


    public String removeLower(MusicBand musicBand) {

        if (Validator.validateMusicBand(musicBand)) {
            return BD.removeLower(musicBand);
        } else {
            return "Полученный элемент не прошел валидацию на стороне сервера.";
        }
    }

    public String filterContainsName(String arg) {
        StringBuilder result = new StringBuilder();
        int j = 0;
        for (int i = 0; i < BD.size(); i++) {
            if (BD.get(i).getName().contains(arg)) {
                result.append(BD.get(i).toString()).append("\n");
                j++;
            }
        }
        if (j != 0) {
            return result.toString();
        } else {
            return "Таких элементов нет";
        }
    }

    public String countGreaterThanBestAlbum(Album musicBand) {
        String result = "";
        int j = 0;
        for (int i = 0; i < BD.size(); i++) {
            if (BD.get(i).getSales() > musicBand.getSales()) {
                j++;
            }
        }
        if (j != 0) {
            return j + " элементов";
        } else {
            return "Таких элементов нет";
        }
    }

    public String reorder() {
        BD.reverse = !BD.reverse;
        BD.sort();
        return "Коллекция успешно отсортирована.";
    }

    public String removeGreater(MusicBand mb) {
        if (Validator.validateMusicBand(mb)) {
            return BD.removeGreater(mb);
        } else {
            return "Полученный элемент не прошел валидацию на стороне сервера.";
        }
    }

    public String auth(String string) {
        String[] s = string.split(":::", 2);
        int result = BD.findUser(s[0], s[1]);
        if (result == 1) {
            return "Успешная авторизация";
        } else if (result == -1) {
            return "Неправильный пароль";
        } else {
            if (BD.addUser(new User(s[0], s[1]))) {
                return "Пользователь зарегистрирован";
            } else {
                return "Пользователь не зарегистрировн, произошла ошибка";
            }
        }
    }
}
