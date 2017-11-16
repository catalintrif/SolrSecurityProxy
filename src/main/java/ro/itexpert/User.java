package ro.itexpert;

import java.util.List;

public class User {
    public User(String userName, List<String> profile) {
        this.userName = userName;
        this.profiles = profile;
    }
    String userName;
    List<String> profiles;
}
