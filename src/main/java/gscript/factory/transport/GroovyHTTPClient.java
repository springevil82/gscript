package gscript.factory.transport;

import gscript.Factory;
import gscript.GroovyException;
import gscript.factory.proxy.GroovyProxyFactory;
import gscript.util.Base64;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public final class GroovyHTTPClient {

    private final Factory factory;
    private final String URL;
    private String userAgent;
    private Integer timeout;
    private final Map<String, String> requestProperties = new HashMap<>();
    private final Map<String, String> responseProperties = new HashMap<>();

    private final ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();

    private RedirectTo redirectTo = RedirectTo.STDOUT;
    private File redirectFile;

    private int responseCode;
    private String responseContentType;
    private String responseContentEncoding;
    private int responseContentLength;

    public GroovyHTTPClient(Factory factory, String URL) {
        this.factory = factory;
        this.URL = URL;
    }

    public void setRequestProperty(String key, String value) {
        requestProperties.put(key, value);
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void regirectOutputToFile(Object file) {
        this.redirectTo = RedirectTo.FILE;
        this.redirectFile = file instanceof File ? (File) file : new File(file.toString());
        final File dir = redirectFile.getAbsoluteFile().getParentFile();
        if (!dir.exists())
            if (!dir.mkdirs())
                throw new GroovyException("Can't create dir: " + dir);
    }

    public void regirectOutputToStdout() {
        this.redirectTo = RedirectTo.STDOUT;
        this.redirectFile = null;
    }

    public void regirectOutputToBuffer() {
        this.redirectTo = RedirectTo.BUFFER;
        this.redirectFile = null;
    }

    public void get() throws Exception {
        internalPerform("GET");
    }

    public void post() throws Exception {
        internalPerform("POST");
    }

    private OutputStream createOutputStream() throws Exception {
        switch (redirectTo) {
            case FILE:
                return new FileOutputStream(redirectFile);
            case STDOUT:
                return System.out;
            case BUFFER:
                return responseBuffer;
        }

        return null;
    }

    private void doOutput(InputStream inputStream) throws Exception {
        if (inputStream == null)
            return;

        int length;
        byte[] buffer = new byte[1024];
        try (OutputStream outputStream = createOutputStream()) {
            while ((length = inputStream.read(buffer)) > 0)
                outputStream.write(buffer, 0, length);
        }
    }


    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    public String getResponseContentEncoding() {
        return responseContentEncoding;
    }

    public int getResponseContentLength() {
        return responseContentLength;
    }

    public Map<String, String> getResponseProperties() {
        return responseProperties;
    }

    public String getResponseBufferAsString(String encoding) throws Exception {
        return new String(responseBuffer.toByteArray(), encoding);
    }

    public byte[] getResponseBufferAsByteArray(String encoding) throws Exception {
        return responseBuffer.toByteArray();
    }

    private void internalPerform(String action) throws Exception {
        final HttpURLConnection connection = createConnection();
        connection.setRequestMethod(action);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        this.responseCode = connection.getResponseCode();
        this.responseContentType = connection.getContentType();
        this.responseContentEncoding = connection.getContentEncoding();
        this.responseContentLength = connection.getContentLength();

        this.responseProperties.clear();
        for (String key : connection.getHeaderFields().keySet())
            this.responseProperties.put(key, connection.getHeaderField(key));

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (final InputStream inputStream = connection.getInputStream()) {
                doOutput(inputStream);
            }
        } else {
            try (InputStream inputStream = connection.getErrorStream()) {
                doOutput(inputStream);
            }
        }

    }

    private HttpURLConnection createConnection() throws Exception {
        if (URL.toLowerCase().startsWith("https:"))
            initSSL();

        HttpURLConnection connection;
        final URL connectionURL = new URL(URL);

        final GroovyProxyFactory.Proxy proxy = factory.proxy.getProxy();

        if (proxy != null) {
            final Proxy p = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.getServer(), proxy.getPort()));

            connection = (HttpURLConnection) connectionURL.openConnection(p);
            if (proxy.getUsername() != null)
                connection.setRequestProperty("Proxy-Authorization", "Basic " + Base64.encodeBytes((proxy.getUsername() + ":" + proxy.getPassword()).getBytes()));

        } else
            connection = (HttpURLConnection) connectionURL.openConnection();

        if (userAgent != null)
            connection.setRequestProperty("User-Agent", userAgent);

        if (timeout != null)
            connection.setReadTimeout(timeout);

        return connection;
    }

    private void initSSL() throws Exception {
        final SSLContext sc = SSLContext.getInstance("SSLv3");
        final javax.net.ssl.TrustManager[] tma = {new TrustManager()};
        if (sc != null)
            sc.init(null, tma, null);

        SSLSocketFactory ssf = null;
        if (sc != null)
            ssf = sc.getSocketFactory();

        HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
    }

    private enum RedirectTo {
        FILE,
        STDOUT,
        BUFFER
    }

    private class TrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

}
