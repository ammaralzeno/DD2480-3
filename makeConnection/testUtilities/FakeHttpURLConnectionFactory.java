package com.example.testhandlers.https;

import org.schabi.newpipe.player.datasource.testdoubles.FakeHttpURLConnection;

public final class FakeHttpURLConnectionFactory {
    private FakeHttpURLConnectionFactory() { }

    private static FakeHttpURLConnection connection;

    public static void set(final FakeHttpURLConnection conn) {
        connection = conn;
    }

    public static FakeHttpURLConnection get() {
        return connection;
    }
}
