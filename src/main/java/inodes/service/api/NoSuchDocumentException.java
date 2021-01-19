package inodes.service.api;

public class NoSuchDocumentException extends Exception {
    public NoSuchDocumentException(String id) {
        super("No such document " + id);
    }
}
