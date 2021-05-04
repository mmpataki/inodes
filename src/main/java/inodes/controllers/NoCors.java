package inodes.controllers;

import org.springframework.web.bind.annotation.*;

import javax.net.ssl.*;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

@RestController
@CrossOrigin
public class NoCors {

    public NoCors() {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                            throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                            throws CertificateException {
                    }
                }
        };

        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        // Create all-trusting host name verifier
        HostnameVerifier validHosts = new HostnameVerifier() {
            @Override
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }
        };
        // All hosts will be valid
        HttpsURLConnection.setDefaultHostnameVerifier(validHosts);
    }
    
    @RequestMapping(value = "/nocors", method = RequestMethod.POST)
    public void noCors(@RequestBody NoCorsRequest req, HttpServletResponse resp) throws IOException {

        URL url = new URL(req.getUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(req.getMethod());
        if (req.getHeaders() != null) {
            req.getHeaders().entrySet().forEach(e -> connection.setRequestProperty(e.getKey(), e.getValue()));
        }
        if (req.getData() != null) {
            connection.setDoOutput(true);
            connection.getOutputStream().write(req.getData().getBytes());
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
        String method, url, data;
        Map<String, String> headers;

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

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
