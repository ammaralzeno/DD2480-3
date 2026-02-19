import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.http.HttpHeaders;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * The chosen makeConnection() method.
 * 
 * CCN: 16
 * 
/**
 * Configures a connection and opens it.
 *
 * @param url               The url to connect to.
 * @param httpMethod        The http method.
 * @param httpBody          The body data, or {@code null} if not required.
 * @param position          The byte offset of the requested data.
 * @param length            The length of the requested data, or {@link C#LENGTH_UNSET}.
 * @param allowGzip         Whether to allow the use of gzip.
 * @param followRedirects   Whether to follow redirects.
 * @param requestParameters parameters (HTTP headers) to include in request.
 * @return the connection opened
 */
@SuppressWarnings("checkstyle:ParameterNumber")
@NonNull
private HttpURLConnection makeConnection(
        @NonNull final URL url,
        @HttpMethod final int httpMethod,
        @Nullable final byte[] httpBody,
        final long position,
        final long length,
        final boolean allowGzip,
        final boolean followRedirects,
        final Map<String, String> requestParameters) throws IOException {
    // This is the method that contains breaking changes with respect to DefaultHttpDataSource!
    // CCN: +1

    String requestUrl = url.toString();

    // Don't add the request number parameter if it has been already added (for instance in
    // DASH manifests) or if that's not a videoplayback URL
    final boolean isVideoPlaybackUrl = url.getPath().startsWith("/videoplayback");
    if (isVideoPlaybackUrl && rnParameterEnabled && !requestUrl.contains(RN_PARAMETER)) { // CCN: +3
        requestUrl += RN_PARAMETER + requestNumber;
        ++requestNumber;
    }

    if (rangeParameterEnabled && isVideoPlaybackUrl) { // CCN: +2
        final String rangeParameterBuilt = buildRangeParameter(position, length);
        if (rangeParameterBuilt != null) { // CCN: +1
            requestUrl += rangeParameterBuilt;
        }
    }

    final HttpURLConnection httpURLConnection = openConnection(new URL(requestUrl));
    httpURLConnection.setConnectTimeout(connectTimeoutMillis);
    httpURLConnection.setReadTimeout(readTimeoutMillis);

    final Map<String, String> requestHeaders = new HashMap<>();
    if (defaultRequestProperties != null) { // CCN: +1
        requestHeaders.putAll(defaultRequestProperties.getSnapshot());
    }
    requestHeaders.putAll(requestProperties.getSnapshot());
    requestHeaders.putAll(requestParameters);

    for (final Map.Entry<String, String> property : requestHeaders.entrySet()) {
        httpURLConnection.setRequestProperty(property.getKey(), property.getValue());
    }

    // CCN: +1
    if (!rangeParameterEnabled) {
        final String rangeHeader = buildRangeRequestHeader(position, length);
        if (rangeHeader != null) { // CCN: +1
            httpURLConnection.setRequestProperty(HttpHeaders.RANGE, rangeHeader);
        }
    }

    if (isWebStreamingUrl(requestUrl)
            || isWebEmbeddedPlayerStreamingUrl(requestUrl)) { // CCN: +2
        httpURLConnection.setRequestProperty(HttpHeaders.ORIGIN, YOUTUBE_BASE_URL);
        httpURLConnection.setRequestProperty(HttpHeaders.REFERER, YOUTUBE_BASE_URL);
        httpURLConnection.setRequestProperty(HttpHeaders.SEC_FETCH_DEST, "empty");
        httpURLConnection.setRequestProperty(HttpHeaders.SEC_FETCH_MODE, "cors");
        httpURLConnection.setRequestProperty(HttpHeaders.SEC_FETCH_SITE, "cross-site");
    }

    httpURLConnection.setRequestProperty(HttpHeaders.TE, "trailers");

    final boolean isAndroidStreamingUrl = isAndroidStreamingUrl(requestUrl);
    final boolean isIosStreamingUrl = isIosStreamingUrl(requestUrl);
    if (isAndroidStreamingUrl) { // CCN: +1
        // Improvement which may be done: find the content country used to request YouTube
        // contents to add it in the user agent instead of using the default
        httpURLConnection.setRequestProperty(HttpHeaders.USER_AGENT,
                getAndroidUserAgent(null));
    } else if (isIosStreamingUrl) { // CCN: +1
        httpURLConnection.setRequestProperty(HttpHeaders.USER_AGENT,
                getIosUserAgent(null));
    } else {
        // non-mobile user agent
        httpURLConnection.setRequestProperty(HttpHeaders.USER_AGENT, DownloaderImpl.USER_AGENT);
    }

    httpURLConnection.setRequestProperty(HttpHeaders.ACCEPT_ENCODING,
            allowGzip ? "gzip" : "identity");
    httpURLConnection.setInstanceFollowRedirects(followRedirects);
    // Most clients use POST requests to fetch contents
    httpURLConnection.setRequestMethod("POST");
    httpURLConnection.setDoOutput(true);
    httpURLConnection.setFixedLengthStreamingMode(POST_BODY.length);
    httpURLConnection.connect();

    final OutputStream os = httpURLConnection.getOutputStream();
    os.write(POST_BODY);
    os.close();

    return httpURLConnection;
}
