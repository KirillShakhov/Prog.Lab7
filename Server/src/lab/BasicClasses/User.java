package lab.BasicClasses;

public class User {
    private String name;
    private String pass;
    public User(String n, String p){
        name = n;
        pass = p;
    }

    public String getName() {
        return name;
    }

    public String getPass() {
        return pass;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", pass='" + pass + '\'' +
                '}';
    }
}
