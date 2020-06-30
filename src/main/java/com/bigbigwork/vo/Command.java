package com.bigbigwork.vo;

import java.io.Serializable;

public class Command implements Serializable {
    private int id;
    private String type;
    private String operation;
    private String keyCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(String keyCode) {
        this.keyCode = keyCode;
    }

    @Override
    public String toString() {
        return "Command{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", operation='" + operation + '\'' +
                ", keyCode='" + keyCode + '\'' +
                '}';
    }
}
