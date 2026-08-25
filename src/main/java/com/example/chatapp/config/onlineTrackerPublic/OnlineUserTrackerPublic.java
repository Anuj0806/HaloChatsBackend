package com.example.chatapp.config.onlineTrackerPublic;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class OnlineUserTrackerPublic {

    private static final Map<String, String> SESSION_USER_MAP_PUBLIC = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> USER_SESSION_MAP_PUBLIC = new ConcurrentHashMap<>();

    public static void userOnlineInPublic(String username, String sessionId) {
        SESSION_USER_MAP_PUBLIC.put(sessionId, username);
        USER_SESSION_MAP_PUBLIC
                .computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet())
                .add(sessionId);
    }

    public static void userOfflineInPublic(String sessionId) {
        String username = SESSION_USER_MAP_PUBLIC.remove(sessionId);
        if (username != null) {
            Set<String> sessions = USER_SESSION_MAP_PUBLIC.get(username);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    USER_SESSION_MAP_PUBLIC.remove(username);
                }
            }
        }
    }

    public static boolean isOnlineInPublic(String username) {
        return USER_SESSION_MAP_PUBLIC.containsKey(username);
    }

    public static Set<String> getUserSessionsInPublic(String username) {
        return USER_SESSION_MAP_PUBLIC.getOrDefault(username, Collections.emptySet());
    }

    public static Set<String> getAllOnlineUsersInPublic() {
        return USER_SESSION_MAP_PUBLIC.keySet();
    }
}
