## Chosen function

makeConnection(url, httpMethod, httpBody, position, length, allowGzip, followRedirects, requestParameters)
in YoutubeHttpDataSource.java

## Lizard results for chosen function

NLOC=71    CCN=16    token=518    PARAM=8    length=91

location=getSimpleName::makeConnection@612-702@./app/src/main/java/org/schabi/newpipe/player/datasource/YoutubeHttpDataSource.java

## Manual CC count

| Line | Decision Point | Type | CCN |
|------|----------------|------|-----|
| 620 | Method entry | - | +1 |
| 628 | `if (isVideoPlaybackUrl && (...) && !(...))` | if + && + && | +3 |
| 633 | `if (rangeParameterEnabled && isVideoPlaybackUrl)` | if + && | +2 |
| 635 | `if (rangeParameterBuilt != null)` | if | +1 |
| 645 | `if (defaultRequestProperties != null)` | if | +1 |
| 655 | `if (!rangeParameterEnabled)` | if | +1 |
| 657 | `if (rangeHeader != null)` | if | +1 |
| 662 | `if (isWebStreamingUrl(requestUrl) \|\| (...))` | if | +2 + \|\| |
| 675 | `if (isAndroidStreamingUrl)` | if | +1 |
| 680 | `else if (isIosStreamingUrl)` | else if | +1 |

**Total CCN: 14** (two less than lizard)

## Part 2: Coverage Measurement

### Task 1: Manual Branch Coverage Instrumentation
before added tests: 0/26 branches
after added tests: 17/26 branches (~65%)

### Task 2: Coverage Improvement

JaCoCo before: 0%
JaCoCo after: 
| Method | Branch coverage |
|--------|----------------|
| makeConnection() | 100% |
| selectUserAgent() | 50% |
| buildRequestUrl() | 25% |
| buildHeaders() | 66% |

Added unit tests for:
- making a (fake) connection
- gzip/identity logic
- logic for building and adding range header
- followRedirect flag

### Task 3: Refactoring Plan

- break out building of url
- break out building of headers
- break out selection of user agent
