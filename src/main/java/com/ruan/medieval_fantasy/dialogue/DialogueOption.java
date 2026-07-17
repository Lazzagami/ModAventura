package com.ruan.medieval_fantasy.dialogue;

import java.util.List;

public class DialogueOption {

    private String text;
    private String next;
    private List<DialogueAction> actions;
    private List<DialogueCondition> conditions;

    public String getText() {
        return text == null ? "" : text;
    }

    public String getNext() {
        return next;
    }

    public List<DialogueAction> getActions() {
        return actions == null ? List.of() : actions;
    }

    public List<DialogueCondition> getConditions() {
        return conditions == null ? List.of() : conditions;
    }
}
