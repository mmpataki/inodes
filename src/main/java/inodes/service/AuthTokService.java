package inodes.service;

import inodes.models.AuthTok;
import inodes.repository.AuthTokRepo;
import inodes.service.api.UnAuthorizedException;
import inodes.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AuthTokService {

    @Autowired
    AuthTokRepo AR;

    Random R = new Random();

    public AuthTok getNewAuthTok() throws UnAuthorizedException {
        String usr = SecurityUtil.getCurrentUser();
        if(usr == null || usr.isEmpty()) {
            throw new UnAuthorizedException("login first");
        }
        AuthTok tok = AuthTok.builder().uId(usr).tok(makeNewTok()).build();
        AR.save(tok);
        return tok;
    }

    private String makeNewTok() {
        return R.nextLong() + "" + System.currentTimeMillis() + "-" + R.nextDouble();
    }

    public boolean verify(String usr, String tok) {
        AuthTok result = AR.findOne(AuthTok.AuthTokId.builder().uId(usr).tok(tok).build());
        if(result != null) {
            AR.delete(result);
        }
        return result != null;
    }

}
