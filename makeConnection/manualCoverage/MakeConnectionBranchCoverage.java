private String buildRequestUrl(final URL url, final long position, final long length) {
    String requestUrl = url.toString();
    final boolean isVideoPlaybackUrl = url.getPath().startsWith("/videoplayback");

    ManualCoverage.hit(1); // branch: evaluated isVideoPlaybackUrl && rnParameterEnabled && !contains RN

    if (isVideoPlaybackUrl && rnParameterEnabled && !requestUrl.contains(RN_PARAMETER)) {
        ManualCoverage.hit(2); // branch: true path
        requestUrl += RN_PARAMETER + requestNumber++;
    } else {
        ManualCoverage.hit(3); // branch: false path
    }

    ManualCoverage.hit(4); // branch: evaluated rangeParameterEnabled && isVideoPlaybackUrl

    if (rangeParameterEnabled && isVideoPlaybackUrl) {
        final String rangeParam = buildRangeParameter(position, length);

        ManualCoverage.hit(5); // branch: evaluated rangeParam != null

        if (rangeParam != null) {
            ManualCoverage.hit(6); // true path
            requestUrl += rangeParam;
        } else {
            ManualCoverage.hit(7); // false path
        }
    } else {
        ManualCoverage.hit(8); // branch: range not enabled or not video playback
    }

    return requestUrl;
}

private Map<String, String> buildHeaders(
        final long position,
        final long length,
        final boolean allowGzip,
        final String requestUrl,
        final Map<String, String> requestParameters) {

    final Map<String, String> headers = new HashMap<>();

    ManualCoverage.hit(10); // evaluated defaultRequestProperties != null
    if (defaultRequestProperties != null) {
        ManualCoverage.hit(11); // true
        headers.putAll(defaultRequestProperties.getSnapshot());
    } else {
        ManualCoverage.hit(12); // false
    }

    headers.putAll(requestProperties.getSnapshot());
    headers.putAll(requestParameters);

    ManualCoverage.hit(13); // evaluated !rangeParameterEnabled
    if (!rangeParameterEnabled) {
        final String rangeHeader = buildRangeRequestHeader(position, length);

        ManualCoverage.hit(14); // evaluated rangeHeader != null
        if (rangeHeader != null) {
            ManualCoverage.hit(15); // true
            headers.put(HttpHeaders.RANGE, rangeHeader);
        } else {
            ManualCoverage.hit(16); // false
        }
    } else {
        ManualCoverage.hit(17); // rangeParameterEnabled == true
    }

    ManualCoverage.hit(18); // evaluated streaming URL conditions
    if (isWebStreamingUrl(requestUrl) || isWebEmbeddedPlayerStreamingUrl(requestUrl)) {
        ManualCoverage.hit(19); // true
        headers.put(HttpHeaders.ORIGIN, YOUTUBE_BASE_URL);
        headers.put(HttpHeaders.REFERER, YOUTUBE_BASE_URL);
        headers.put(HttpHeaders.SEC_FETCH_DEST, "empty");
        headers.put(HttpHeaders.SEC_FETCH_MODE, "cors");
        headers.put(HttpHeaders.SEC_FETCH_SITE, "cross-site");
    } else {
        ManualCoverage.hit(20); // false
    }

    headers.put(HttpHeaders.TE, "trailers");
    headers.put(HttpHeaders.ACCEPT_ENCODING, allowGzip ? "gzip" : "identity");

    return headers;
}

private String selectUserAgent(final String requestUrl) {

    ManualCoverage.hit(30); // evaluated isAndroidStreamingUrl
    if (isAndroidStreamingUrl(requestUrl)) {
        ManualCoverage.hit(31); // true
        return getAndroidUserAgent(null);
    }

    ManualCoverage.hit(32); // evaluated isIosStreamingUrl
    if (isIosStreamingUrl(requestUrl)) {
        ManualCoverage.hit(33); // true
        return getIosUserAgent(null);
    }

    ManualCoverage.hit(34); // default branch
    return DownloaderImpl.USER_AGENT;
}


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
    ManualCoverage.hit(100); // method entered

    final String requestUrl = buildRequestUrl(url, position, length);

    final HttpURLConnection httpURLConnection = openConnection(new URL(requestUrl));
    httpURLConnection.setConnectTimeout(connectTimeoutMillis);
    httpURLConnection.setReadTimeout(readTimeoutMillis);

    final Map<String, String> requestHeaders =
        buildHeaders(position, length, allowGzip, requestUrl, requestParameters);

    for (final Map.Entry<String, String> property : requestHeaders.entrySet()) {
        httpURLConnection.setRequestProperty(property.getKey(), property.getValue());
    }

    httpURLConnection.setRequestProperty(HttpHeaders.USER_AGENT, selectUserAgent(requestUrl));

    httpURLConnection.setInstanceFollowRedirects(followRedirects);
    // Most clients use POST requests to fetch contents
    httpURLConnection.setRequestMethod("POST");
    httpURLConnection.setDoOutput(true);
    httpURLConnection.setFixedLengthStreamingMode(POST_BODY.length);
    httpURLConnection.connect();

    final OutputStream os = httpURLConnection.getOutputStream();
    os.write(POST_BODY);
    os.close();

    ManualCoverage.hit(101); // about to return

    return httpURLConnection;
}
