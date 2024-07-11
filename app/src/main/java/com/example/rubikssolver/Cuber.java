package com.example.rubikssolver;

import java.util.List;

public class Cuber {
    private String uid;
    private String userName;
    private String bestSolve = "No solves yet";
    private List<Cuber> friends;

    public Cuber() {
    }

    public Cuber(String uid, String userName, List<Cuber> friends) {
        this.uid = uid;
        this.userName = userName;
        this.friends = friends;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<Cuber> getFriends() {
        return friends;
    }

    public void setFriends(List<Cuber> friends) {
        this.friends = friends;
    }

    public String getBestSolve() {
        return bestSolve;
    }

    public void setBestSolve(String bestSolve) {
        this.bestSolve = bestSolve;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "Cuber{" +
                "uid='" + uid + '\'' +
                ", userName='" + userName + '\'' +
                ", bestSolve='" + bestSolve + '\'' +
                ", friends=" + friends +
                '}';
    }
}
