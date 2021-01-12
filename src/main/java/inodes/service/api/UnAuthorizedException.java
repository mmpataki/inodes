package inodes.service.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Unauthorized")
public class UnAuthorizedException extends Exception {
    public UnAuthorizedException(String s) {
        super(s);
    }
}
