package com.example.rubikssolver;


import java.time.Duration;

public class Solve {
    int durationMinutes,durationSeconds,durationMilliseconds;
    String username;
    Boolean solved;
    String userId;
    public Solve() {
    }

    public Solve(int durationMinutes, int durationSeconds, int durationMilliseconds, String username, Boolean solved, String userId) {
        this.durationMinutes = durationMinutes;
        this.durationSeconds = durationSeconds;
        this.durationMilliseconds = durationMilliseconds;
        this.username = username;
        this.solved = solved;
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Solve{" +
                "durationMinutes=" + durationMinutes +
                ", durationSeconds=" + durationSeconds +
                ", durationMilliseconds=" + durationMilliseconds +
                ", username='" + username + '\'' +
                ", solved=" + solved +
                ", userId='" + userId + '\'' +
                '}';
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public int getDurationMilliseconds() {
        return durationMilliseconds;
    }

    public void setDurationMilliseconds(int durationMilliseconds) {
        this.durationMilliseconds = durationMilliseconds;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getSolved() {
        return solved;
    }

    public void setSolved(Boolean solved) {
        this.solved = solved;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
