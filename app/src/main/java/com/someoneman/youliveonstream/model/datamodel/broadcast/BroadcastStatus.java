package com.someoneman.youliveonstream.model.datamodel.broadcast;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nik on 16.04.2016.
 */
public enum BroadcastStatus {
    Abandoned(0),
    Complete(1),
    Created(2),
    Live(3),
    Ready(4),
    Reclaimed(5),
    Revoked(7),
    Starting(8),
    TestStarting(9),
    Testing(10);

    private final int value;
    private static Map<Integer, BroadcastStatus> map = new HashMap<>();

    static {
        for (BroadcastStatus broadcastStatus : BroadcastStatus.values()) {
            map.put(broadcastStatus.value, broadcastStatus);
        }
    }
    BroadcastStatus(int value) {
        this.value = value;
    }

    public int toInt() {
        return value;
    }

    public static BroadcastStatus getStatusFromInt(int lifeCycleStatus) {
        return map.get(lifeCycleStatus);
    }

    public static BroadcastStatus getStatusFromString(String lifeCycleStatus) {
        switch (lifeCycleStatus) {
            case "abandoned":
                return Abandoned;
            case "complete":
                return Complete;
            case "created":
                return Created;
            case "live":
                return Live;
            case "liveStarting":
                return Starting;
            case "ready":
                return Ready;
            case "reclaimed":
                return Reclaimed;
            case "revoked":
                return Revoked;
            case "testStarting":
                return TestStarting;
            case "testing":
                return Testing;
        }
        return null;
    }
}
