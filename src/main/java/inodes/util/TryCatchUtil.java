package inodes.util;

public class TryCatchUtil {
    public static interface Trier {
        public void doStuff() throws Exception;
    }
    public static void tc(Trier f) {
        try {
            f.doStuff();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
