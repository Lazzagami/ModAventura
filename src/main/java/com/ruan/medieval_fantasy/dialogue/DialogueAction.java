package com.ruan.medieval_fantasy.dialogue;

public class DialogueAction {

    private String type;
    private String key;
    private String value;
    private int amount;
    private boolean booleanValue;
    private String animation;
    private int duration;

    public String getType() {
        return type == null ? "" : type;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public int getAmount() {
        return amount;
    }

    public boolean getBooleanValue() {
        return booleanValue;
    }

    public String getAnimation() {
        return animation;
    }

    public int getDuration() {
        return duration;
    }
}
