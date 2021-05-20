package inodes.util;

import inodes.Inodes;

public class UrlUtil {

    public static String getUserUrl(String userId) {
        return String.format("%s/?q=%%23inodesapp+%%23user+!%s", Inodes.getLocalAddr(), userId);
    }

    public static String getGroupUrl(String groupId) {
        return String.format("%s/?q=%%23inodesapp+%%23viewgroup+!%s", Inodes.getLocalAddr(), groupId);
    }

    public static String getDocUrl(String docId) {
        return String.format("%s/?q=@%s", Inodes.getLocalAddr(), docId);
    }

    public static String getRelativeDocPermApprovalLink(String docId, String userId) {
        return String.format("/data/%s/givePermission/%s", docId, userId);
    }

}
