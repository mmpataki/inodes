package inodes.service.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Unauthorized")
public class NoSuchUserException extends Exception {
    public NoSuchUserException(String userName) {
        super(userName);
    }
}
