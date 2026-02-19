package com.example.testhandlers.https;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

// prevents makeConnection from attempting to open a real connection

public class Handler extends URLStreamHandler {

    @Override
    protected URLConnection openConnection(final URL u) throws IOException {
        return FakeHttpURLConnectionFactory.get();
    }
}

