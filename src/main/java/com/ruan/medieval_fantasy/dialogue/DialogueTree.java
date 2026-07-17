package com.ruan.medieval_fantasy.dialogue;

import java.util.LinkedHashMap;
import java.util.Map;

public class DialogueTree {

    private String id;
    private String start;
    private Map<String, DialogueNode> nodes = new LinkedHashMap<>();

    public String getId() {
        return id;
    }

    public String getStart() {
        return start == null || start.isBlank() ? "start" : start;
    }

    public Map<String, DialogueNode> getNodes() {
        return nodes == null ? Map.of() : nodes;
    }

    public DialogueNode getNode(String nodeId) {
        return getNodes().get(nodeId);
    }
}
