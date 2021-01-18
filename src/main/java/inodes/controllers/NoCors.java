package inodes.controllers;

import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Map;

@RestController
@CrossOrigin
public class NoCors {

    @RequestMapping(value = "/nocors", method = RequestMethod.POST)
    public void noCors(@RequestBody NoCorsRequest req, HttpServletResponse resp) throws IOException {

        URL url = new URL(req.getUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(req.getMethod());
        if(req.getHeaders() != null) {
            req.getHeaders().entrySet().forEach(e -> connection.setRequestProperty(e.getKey(), e.getValue()));
        }

        InputStream in = connection.getInputStream();
        ServletOutputStream out = resp.getOutputStream();
        byte[] buf = new byte[4096];
        int length;
        while ((length = in.read(buf)) > 0) {
            out.write(buf, 0, length);
        }
    }

    public static class NoCorsRequest {
        String method, url;
        Map<String, String> headers;

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }
    }
}
