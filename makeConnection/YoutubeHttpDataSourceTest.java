package org.schabi.newpipe.player.datasource;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.testhandlers.https.FakeHttpURLConnectionFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.schabi.newpipe.player.datasource.coverage.ManualCoverageRule;
import org.schabi.newpipe.player.datasource.testdoubles.FakeHttpURLConnection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import com.google.common.base.Predicate;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;

public class YoutubeHttpDataSourceTest {

    private Object downloader;
    private FakeHttpURLConnection fakeConnection;

    @ClassRule
    public static ManualCoverageRule coverage = new ManualCoverageRule();

    @Before
    public void setUp() throws Exception {
        System.setProperty("java.protocol.handler.pkgs", "com.example.testhandlers");

        // Create instance of the real class under test
        final Class<?> clazz =
                Class.forName("org.schabi.newpipe.player.datasource.YoutubeHttpDataSource");
        final Constructor<?> ctor = clazz.getDeclaredConstructor(
                int.class,
                int.class,
                boolean.class,
                boolean.class,
                boolean.class,
                HttpDataSource.RequestProperties.class,
                Predicate.class, boolean.class);
        ctor.setAccessible(true);
        downloader = ctor.newInstance(
                5000,
                5000,
                false,
                false,
                false,
                null,
                null,
                false);

        // Create fake connection
        fakeConnection = new FakeHttpURLConnection(new URL("https://example.com/videoplayback"));

        // Set private fields via reflection
        setPrivateField("connectTimeoutMillis", 5000);
        setPrivateField("readTimeoutMillis", 5000);
        setPrivateField("rangeParameterEnabled", false);
        setPrivateField("rnParameterEnabled", false);

        // Clear requestProperties
        final Field propsField = clazz.getDeclaredField("requestProperties");
        propsField.setAccessible(true);
        final Object requestProps = propsField.get(downloader);
        Assert.assertNotNull(requestProps);
        final Method clearMethod = requestProps.getClass().getDeclaredMethod("clear");
        clearMethod.setAccessible(true);
        clearMethod.invoke(requestProps);
    }

    private void setPrivateField(final String name, final Object value) throws Exception {
        final Field f = downloader.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(downloader, value);
    }

    private Object callMakeConnection(final URL url) throws Exception {
        final Method m = downloader.getClass().getDeclaredMethod(
                "makeConnection",
                URL.class,
                int.class,
                byte[].class,
                long.class,
                long.class,
                boolean.class,
                boolean.class,
                Map.class
        );
        m.setAccessible(true);

        return m.invoke(
                downloader,
                url,
                0,
                null,
                0L,
                -1L,
                true,
                false,
                new HashMap<>()
        );
    }

    private void callMakeConnection(
            final URL url,
            final boolean allowGzip,
            final boolean followRedirects
    ) throws Exception {

        final Method m = downloader.getClass().getDeclaredMethod(
                "makeConnection",
                URL.class,
                int.class,
                byte[].class,
                long.class,
                long.class,
                boolean.class,
                boolean.class,
                Map.class
        );
        m.setAccessible(true);

        m.invoke(
                downloader,
                url,
                0,          // httpMethod
                null,       // httpBody
                0L,         // position
                -1L,        // length
                allowGzip,
                followRedirects,
                new HashMap<>()
        );
    }

    private void callMakeConnection(
            final URL url,
            final long position,
            final long length
    ) throws Exception {

        final Method m = downloader.getClass().getDeclaredMethod(
                "makeConnection",
                URL.class,
                int.class,
                byte[].class,
                long.class,
                long.class,
                boolean.class,
                boolean.class,
                Map.class
        );
        m.setAccessible(true);

        m.invoke(
                downloader,
                url,
                0,
                null,
                position,
                length,
                true,       // allowGzip
                false,      // followRedirects
                new HashMap<>()
        );
    }


    @Test
    public void testMakeConnection() throws Exception {

        // Inject fake connection using custom URLStreamHandler
        final URL fakeUrl =
                new URL(null,
                        "https://example.com/videoplayback",
                        new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(final URL u) {
                return fakeConnection;
            }
        });

        FakeHttpURLConnectionFactory.set(fakeConnection);
        System.setProperty("java.protocol.handler.pkgs", "com.example.testhandlers");
        // Call private method
        final Object conn = callMakeConnection(fakeUrl);

        // Access POST_BODY via reflection
        final Field postBodyField = downloader.getClass().getDeclaredField("POST_BODY");
        postBodyField.setAccessible(true);
        final byte[] postBody = (byte[]) postBodyField.get(null);

        // Assertions
        assertEquals("POST", fakeConnection.requestMethod);
        assertArrayEquals(postBody, fakeConnection.outputStream.toByteArray());
        assertEquals(5000, fakeConnection.connectTimeout);
        assertEquals(5000, fakeConnection.readTimeout);
        assertTrue(fakeConnection.connected);
    }

    @Test
    public void testMakeConnectionGzipVsIdentity() throws Exception {
        // Arrange
        FakeHttpURLConnection fake = new FakeHttpURLConnection(new URL("https://example.com"));
        FakeHttpURLConnectionFactory.set(fake);
        System.setProperty("java.protocol.handler.pkgs", "com.example.testhandlers");

        final URL url = new URL("https://example.com/videoplayback");

        // --- Case 1: allowGzip = true ---
        callMakeConnection(url, true, false);

        assertEquals("gzip", fake.headers.get("Accept-Encoding"));

        // Reset fake connection for second case
        fake = new FakeHttpURLConnection(new URL("https://example.com"));
        FakeHttpURLConnectionFactory.set(fake);

        // --- Case 2: allowGzip = false ---
        callMakeConnection(url, false, false);

        assertEquals("identity", fake.headers.get("Accept-Encoding"));
    }

    @Test
    public void testMakeConnectionRangeHeaderLogic() throws Exception {
        FakeHttpURLConnection fake = new FakeHttpURLConnection(new URL("https://example.com"));
        FakeHttpURLConnectionFactory.set(fake);
        System.setProperty("java.protocol.handler.pkgs", "com.example.testhandlers");

        final URL url = new URL("https://example.com/videoplayback");

        // --- Case 1: No range (position=0, length=-1) ---
        callMakeConnection(url, 0L, -1L);

        assertFalse(fake.headers.containsKey("Range"));

        // Reset fake connection
        fake = new FakeHttpURLConnection(new URL("https://example.com"));
        FakeHttpURLConnectionFactory.set(fake);

        // --- Case 2: position only (position=1000, length=-1) ---
        callMakeConnection(url, 1000L, -1L);

        assertEquals("bytes=1000-", fake.headers.get("Range"));

        // Reset fake connection
        fake = new FakeHttpURLConnection(new URL("https://example.com"));
        FakeHttpURLConnectionFactory.set(fake);

        // --- Case 3: position + length (position=1000, length=500) ---
        callMakeConnection(url, 1000L, 500L);

        assertEquals("bytes=1000-1499", fake.headers.get("Range"));
    }

    @Test
    public void testMakeConnectionFollowRedirectsFlag() throws Exception {
        FakeHttpURLConnection fake = new FakeHttpURLConnection(new URL("https://example.com"));
        FakeHttpURLConnectionFactory.set(fake);
        System.setProperty("java.protocol.handler.pkgs", "com.example.testhandlers");

        final URL url = new URL("https://example.com/videoplayback");

        // --- Case 1: followRedirects = true ---
        callMakeConnection(url, true, true);

        assertTrue(fake.redirectEnabled);

        // Reset fake connection
        fake = new FakeHttpURLConnection(new URL("https://example.com"));
        FakeHttpURLConnectionFactory.set(fake);

        // --- Case 2: followRedirects = false ---
        callMakeConnection(url, true, false);

        assertFalse(fake.redirectEnabled);
    }

}
