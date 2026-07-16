package com.ruan.medieval_fantasy.combat.heat.client;

public class ClientHeatData {

    private static int heat;

    public static int getHeat() {
        return heat;
    }

    public static void setHeat(int heat) {
        ClientHeatData.heat = Math.max(0, Math.min(100, heat));
    }
}
