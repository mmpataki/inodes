package inodes.util;

public class SecurityUtil {

    private static ThreadLocal<String> user = new ThreadLocal<>();

    public static String getCurrentUser() {
        return user.get();
    }

    public static void setCurrentUser(String value) {
        user.set(value);
    }

    public static void unsetCurrentUser() {
        user.remove();
    }

}
