package ro.itexpert;

import java.util.List;

public class User {

    private String id;
    private String userName;
    private List<String> profiles;
    private Long credits;
    private boolean dirty = false;

    public User(String id, String userName, List<String> profile, Long credits) {
        this.id= id;
        this.userName = userName;
        this.profiles = profile;
        this.credits = credits;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }

    public Long getCredits() {
        return credits;
    }

    public void setCredits(Long credits) {
        this.credits = credits;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void spendCredit() {
        credits--;
        dirty = true;
    }
}
