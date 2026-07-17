package com.ruan.medieval_fantasy.dialogue;

import java.util.List;

public class DialogueNode {

    private String id;
    private String speaker;
    private String text;
    private String voice;
    private String animation;
    private int animationTicks;
    private String next;
    private List<DialogueOption> options;
    private List<DialogueAction> actions;

    public String getId() {
        return id;
    }

    public String getSpeaker() {
        return speaker == null ? "" : speaker;
    }

    public String getText() {
        return text == null ? "" : text;
    }

    public String getVoice() {
        return voice;
    }

    public String getAnimation() {
        return animation;
    }

    public int getAnimationTicks() {
        return animationTicks;
    }

    public String getNext() {
        return next;
    }

    public List<DialogueOption> getOptions() {
        return options == null ? List.of() : options;
    }

    public List<DialogueAction> getActions() {
        return actions == null ? List.of() : actions;
    }
}
