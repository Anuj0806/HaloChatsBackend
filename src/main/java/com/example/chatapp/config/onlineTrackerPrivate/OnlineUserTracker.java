package com.example.chatapp.config.onlineTrackerPrivate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class OnlineUserTracker {

    private static final Map<String, String> SESSION_USER_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> USER_SESSION_MAP = new ConcurrentHashMap<>();

    public static void userOnline(String username, String sessionId) {
        SESSION_USER_MAP.put(sessionId, username);
        USER_SESSION_MAP
                .computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet())
                .add(sessionId);
    }

    public static void userOffline(String sessionId) {
        String username = SESSION_USER_MAP.remove(sessionId);
        if (username != null) {
            Set<String> sessions = USER_SESSION_MAP.get(username);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    USER_SESSION_MAP.remove(username);
                }
            }
        }
    }

    public static boolean isOnline(String username) {
        return USER_SESSION_MAP.containsKey(username);
    }

    public static Set<String> getUserSessions(String username) {
        return USER_SESSION_MAP.getOrDefault(username, Collections.emptySet());
    }

    public static Set<String> getAllOnlineUsers() {
        return USER_SESSION_MAP.keySet();
    }
}
