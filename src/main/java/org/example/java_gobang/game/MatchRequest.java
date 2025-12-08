package org.example.java_gobang.game;

//这表示一个websocket的匹配响应
public class MatchRequest {
    private String message = "";

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
