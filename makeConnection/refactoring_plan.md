## Refactoring Plan for `makeConnection()`

## Current Metrics
- **CCN:** 16
- **Target CCN:** = 1
- **NLOC:** 71

By dividing the method up into one base call and three new functions, the CCN can be decreased dramatically.
The three new functions will be:
- String buildRequestUrl(final URL url, final long position, final long length)

Takes care of the building of request URL, breaking out three if-statements from the original function.

- Map<String, String> buildHeaders(final long position, final long length, final boolean allowGzip, final String requestUrl, final Map<String, String> requestParameters)

A cleaner way of handling the building of headers, which previously contributed significantly to the method's high CCN.

- String selectUserAgent(final String requestUrl)

Breaks out the final part of the method where the user agent is selected.
