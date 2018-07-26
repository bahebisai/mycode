package com.jrdcom.filemanager.singleton;

import android.app.Notification;

import java.util.HashMap;

/**
 * Created by user on 16-11-1.
 */
public class NotificationMap<K,T> extends HashMap {
    private NotificationMap() {
    }

    private static NotificationMap<Long, Notification.Builder> runSingle = new NotificationMap<Long, Notification.Builder>();

    public static NotificationMap<Long, Notification.Builder> getInstance() {
        return runSingle;
    }

    public static void addNotification(long createTime, Notification.Builder notification) {
        runSingle.put(createTime, notification);
    }

    public static void removeNotification(long createTime) {
        runSingle.remove(createTime);
    }

    public static void clearAllNotification() {
        runSingle.clear();
    }

    public static Notification.Builder getNotification(long createTime) {
        return (Notification.Builder)runSingle.get(createTime);
    }

    public static int getNotificationSize() {
        return runSingle.size();
    }
}
