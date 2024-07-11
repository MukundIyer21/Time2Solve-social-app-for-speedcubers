package com.example.rubikssolver;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ScrambleResponse {
    @SerializedName("scramble_string")
    private String scramble_string;
    @SerializedName("scrambled_cube")
    private List<List<String>> scrambled_cube;

    // Getters and setters
    public String getScrambleString() { return scramble_string; }
    public void setScrambleString(String scramble_string) { this.scramble_string = scramble_string; }
    public List<List<String>> getScrambledCube() { return scrambled_cube; }
    public void setScrambledCube(List<List<String>> scrambled_cube) { this.scrambled_cube = scrambled_cube; }

    @Override
    public String toString() {
        return "ScrambleResponse{" +
                "scramble_string='" + scramble_string + '\'' +
                ", scrambled_cube=" + scrambled_cube +
                '}';
    }
}
