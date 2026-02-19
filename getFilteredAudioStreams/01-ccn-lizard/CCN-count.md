## Chosen function

getFilteredAudioStreams() in ListHelper.java (NewPipe) / src/main/java/ListHelper.java (DD2480-3 standalone)

## Lizard results for chosen function

NLOC=26    CCN=9   token=212  PARAM=2  length=35

## Manual CC count

| Line | Decision Point | Type | CCN +1 |
|------|---------------|------|--------|
| Base | Method entry | - | 1 |
| 296 | `if (audioStreams == null)` | if | +1 |
| 304 | `for (final AudioStream stream : audioStreams)` | for | +1 |
| 305-309 | `if (stream.getDeliveryMethod() == TORRENT \|\| (HLS && OPUS))` | if + \|\| + && | +3 |
| 314 | `if (presentStream == null \|\| cmp.compare(...) > 0)` | if + \|\| | +2 |
| 320 | `if (collectedStreams.size() > 1)` | if | +1 |

**Total CCN: 9** (matches lizard output)