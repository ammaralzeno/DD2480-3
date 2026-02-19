
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

        return httpURLConnection;
    }

    /**
     * Builds request url for connection.
     *
     * @param url       The url to connect to.
     * @param position  The byte offset of the requested data.
     * @param length    The length of the requested data, or {@link C#LENGTH_UNSET}.
     * @return request url as String
     */
    private String buildRequestUrl(final URL url, final long position, final long length) {
        String requestUrl = url.toString();
        final boolean isVideoPlaybackUrl = url.getPath().startsWith("/videoplayback");

        // Don't add the request number parameter if it has been already added (for instance in
        // DASH manifests) or if that's not a videoplayback URL
        if (isVideoPlaybackUrl && rnParameterEnabled && !requestUrl.contains(RN_PARAMETER)) {
            requestUrl += RN_PARAMETER + requestNumber++;
        }

        if (rangeParameterEnabled && isVideoPlaybackUrl) {
            final String rangeParam = buildRangeParameter(position, length);
            if (rangeParam != null) {
                requestUrl += rangeParam;
            }
        }

        return requestUrl;
    }

    /**
     * Builds headers for connection.
     *
     * @param position              The byte offset of the requested data.
     * @param length                The length of the requested data, or {@link C#LENGTH_UNSET}.
     * @param allowGzip             Whether to allow the use of gzip.
     * @param requestUrl            Full request url.
     * @param requestParameters     Parameters (HTTP headers) to include in request.
     * @return map with headers
     */
    private Map<String, String> buildHeaders(
        final long position,
        final long length,
        final boolean allowGzip,
        final String requestUrl,
        final Map<String, String> requestParameters) {

        final Map<String, String> headers = new HashMap<>();

        if (defaultRequestProperties != null) {
            headers.putAll(defaultRequestProperties.getSnapshot());
        }
        headers.putAll(requestProperties.getSnapshot());
        headers.putAll(requestParameters);

        if (!rangeParameterEnabled) {
            final String rangeHeader = buildRangeRequestHeader(position, length);
            if (rangeHeader != null) {
                headers.put(HttpHeaders.RANGE, rangeHeader);
            }
        }

        if (isWebStreamingUrl(requestUrl) || isWebEmbeddedPlayerStreamingUrl(requestUrl)) {
            headers.put(HttpHeaders.ORIGIN, YOUTUBE_BASE_URL);
            headers.put(HttpHeaders.REFERER, YOUTUBE_BASE_URL);
            headers.put(HttpHeaders.SEC_FETCH_DEST, "empty");
            headers.put(HttpHeaders.SEC_FETCH_MODE, "cors");
            headers.put(HttpHeaders.SEC_FETCH_SITE, "cross-site");
        }

        headers.put(HttpHeaders.TE, "trailers");
        headers.put(HttpHeaders.ACCEPT_ENCODING, allowGzip ? "gzip" : "identity");

        return headers;
    }

    /**
     * Selects user agent for connection.
     *
     * @param requestUrl    Full request url.
     * @return user agent as String
     */
    private String selectUserAgent(final String requestUrl) {

        if (isAndroidStreamingUrl(requestUrl)) {
            // Improvement which may be done: find the content country used to request YouTube
            // contents to add it in the user agent instead of using the default
            return getAndroidUserAgent(null);
        }
        if (isIosStreamingUrl(requestUrl)) {
            return getIosUserAgent(null);
        }
        // non-mobile user agent
        return DownloaderImpl.USER_AGENT;
    }

