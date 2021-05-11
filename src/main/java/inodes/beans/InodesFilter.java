package inodes.beans;

import inodes.models.Credential;
import inodes.models.User;
import inodes.service.api.UserGroupService;
import inodes.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Component
public class InodesFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    Logger LOG = LoggerFactory.getLogger(InodesFilter.class);

    @Autowired
    UserGroupService AS;

    static Random R = new Random();
    Map<String, SessionHeader> sessMap = new HashMap<>();

    private static class SessionHeader {
        String user;
        String tok = R.nextDouble() + "";

        public SessionHeader(String user) {
            this.user = user;
        }

        public SessionHeader(String user, String tok) {
            this.user = user;
            this.tok = tok;
        }

        static SessionHeader fromHeader(String s) {
            return new SessionHeader(s.split(":")[0], s.split(":")[1]);
        }

        @Override
        public String toString() {
            return String.format("%s:%s", user, tok);
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getTok() {
            return tok;
        }

        public void setTok(String tok) {
            this.tok = tok;
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = ((HttpServletResponse) servletResponse);
        String url = req.getServletPath();
        String method = req.getMethod();
        String ip = req.getRemoteAddr();

        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        resp.setHeader("Access-Control-Expose-Headers", "*");
        resp.setHeader("Access-Control-Allow-Headers", "authorization,authinfo,content-type");
        resp.setHeader("Access-Control-Allow-Methods", "POST,PUT,DELETE");

        try {
            if (!method.equals("OPTIONS")) {
                if (url.equals("/auth/login")) {
                    doLogin(req, resp, true);
                    return;
                } else if (url.equals("/auth/logout")) {
                    doLogout(req, resp);
                    return;
                } else if (url.equals("/auth/register") || url.equals("/nocors") || url.startsWith("/h2-console")) {
                    // leave these guys
                } else {
                    if (!isLoggedIn(req, resp)) {
                        if(!method.equals("GET")) {
                            sendNoAuth(req, resp);
                            return;
                        }
                    }
                }
            }
            filterChain.doFilter(servletRequest, servletResponse);

            Object user = SecurityUtil.getCurrentUser();
            LOG.info("[{}] - ({}) - {} {} - {}", user == null ? "--" : user.toString(), ip, method, url, resp.getStatus());
        } finally {
            SecurityUtil.unsetCurrentUser();
        }
    }

    private boolean isLoggedIn(HttpServletRequest req, HttpServletResponse resp) {
        String sessHdr = req.getHeader("AuthInfo");
        String authHdr = req.getHeader("Authorization");
        if (authHdr != null) {
            return doLogin(req, resp, false);
        } else if (sessHdr != null) {
            SessionHeader sh = SessionHeader.fromHeader(sessHdr);
            boolean ret = sessMap.containsKey(sh.getUser()) && sessMap.get(sh.getUser()).getTok().equals(sh.getTok());
            if (ret) {
                SecurityUtil.setCurrentUser(sh.getUser());
                return ret;
            }
        }
        return false;
    }

    private void doLogout(HttpServletRequest req, HttpServletResponse resp) {
        String sessHdr = req.getHeader("AuthInfo");
        if (sessHdr != null) {
            SessionHeader sh = SessionHeader.fromHeader(sessHdr);
            if (isLoggedIn(req, resp) && sessMap.containsKey(sh.getUser())) {
                sessMap.remove(sh.getUser());
            }
        }
    }

    private boolean doLogin(HttpServletRequest req, HttpServletResponse resp, boolean makeSess) {
        String authHdr = req.getHeader("Authorization");
        if (authHdr != null) {
            Credential cred = makeCredential(authHdr);
            try {
                if (AS.authenticate(cred)) {
                    if (makeSess) {
                        SessionHeader sh = new SessionHeader(cred.getUserName());
                        sessMap.put(cred.getUserName(), sh);
                        resp.setHeader("AuthInfo", sh.toString());
                    }
                    SecurityUtil.setCurrentUser(cred.getUserName());
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private Credential makeCredential(String authHdr) {
        String chunks[] = new String(Base64.getDecoder().decode(authHdr.split(" ")[1].getBytes())).split(":");
        return Credential.builder().userName(chunks[0]).password(chunks[1]).build();
    }

    private void sendNoAuth(HttpServletRequest req, HttpServletResponse r) throws IOException {
        r.setStatus(401);
        r.setHeader("401", "Unauthorized");
        r.setHeader("WWW-Authenticate", "Basic");
        r.getOutputStream().write("{\"message\": \"Unauthorized, login first!\"}".getBytes());
    }

    @Override
    public void destroy() {

    }
}
