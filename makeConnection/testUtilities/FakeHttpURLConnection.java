package org.schabi.newpipe.player.datasource.testdoubles;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class FakeHttpURLConnection extends HttpURLConnection {

    public Map<String, String> headers = new HashMap<>();
    public ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    public boolean connected = false;
    public String requestMethod;
    public boolean doOutput = false;
    public int fixedLength = -1;
    public int connectTimeout = -1;
    public int readTimeout = -1;
    public boolean redirectEnabled = true;

    public FakeHttpURLConnection(final URL url) {
        super(url);
    }

    @Override
    public void setRequestProperty(final String key, final String value) {
        headers.put(key, value);
    }

    @Override
    public void setRequestMethod(final String method) {
        this.requestMethod = method;
    }

    @Override
    public void setDoOutput(final boolean doOutput) {
        this.doOutput = doOutput;
    }

    @Override
    public void setFixedLengthStreamingMode(final int contentLength) {
        this.fixedLength = contentLength;
    }

    @Override
    public void setConnectTimeout(final int timeout) {
        this.connectTimeout = timeout;
    }

    @Override
    public void setReadTimeout(final int timeout) {
        this.readTimeout = timeout;
    }

    @Override
    public void setInstanceFollowRedirects(final boolean followRedirects) {
        this.redirectEnabled = followRedirects;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public void connect() {
        connected = true;
    }

    @Override public void disconnect() {

    }
    @Override public boolean usingProxy() {
        return false;
    }
}


