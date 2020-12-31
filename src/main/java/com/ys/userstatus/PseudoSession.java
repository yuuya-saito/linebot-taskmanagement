package com.ys.userstatus;

import java.util.HashMap;
import java.util.Map;

public class PseudoSession {

    private static Map<String,Status> statusMap = new HashMap<>();

    public static Status getStatus(String userId) {
        return PseudoSession.statusMap.get(userId);
    }

    public static void putStatus(String userId, Status status) {
        PseudoSession.statusMap.put(userId, status);
    }

    public static String readContext(String userId) {
        return PseudoSession.statusMap.get(userId).getContext();
    }

    public static String readPlace(String userId) {
        return PseudoSession.statusMap.get(userId).getPlace();
    }

    public static void updateContext(String userId, String context) {
        Status newStatus = PseudoSession.statusMap.get(userId);
        newStatus.setContext(context);
        PseudoSession.statusMap.put(userId, newStatus);
    }

    public static void updatePlace(String userId, String place) {
        Status newStatus = PseudoSession.statusMap.get(userId);
        newStatus.setPlace(place);
        PseudoSession.statusMap.put(userId, newStatus);
    }

}