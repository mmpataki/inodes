package inodes.service.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "User already exists")
public class UserExistsException extends Exception {
    UserExistsException(Exception e) {
        super(e);
    }
    public UserExistsException(String msg) {
        super(msg);
    }
}
