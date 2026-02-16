## Chosen function

prepareSelectedDownload() in DownloadDialog.java

## Lizard results for chosen function

NLOC=94    CCN=20   token=628  PARAM=0  length=118  

location= DownloadDialog::prepareSelectedDownload@762-879@./app/src/main/java/org/schabi/newpipe/download/DownloadDialog.java

## Manual CC count

| Line | Decision Point | Type | CCN +1 |
|------|---------------|------|--------|
| Base | Method entry | - | 1 |
| 774 | `switch` statement | switch | +1 |
| 776 | `case R.id.audio_button` | case | +1 |
| 780 | `if (format == WEBMA_OPUS)` | if | +1 |
| 783 | `else if (format != null)` | else if | +1 |
| 788 | `case R.id.video_button` | case | +1 |
| 793 | `if (format != null)` | if | +1 |
| 798 | `case R.id.subtitle_button` | case | +1 |
| 805 | `if (format == TTML)` | if | +1 |
| 807 | `else if (format != null)` | else if | +1 |
| 812 | `default` | case | +1 |
| 816-818 | `if (!askForSavePath && (...))` with 2 ORs | if + \|\| + \|\| | +3 |
| 826 | `if (...== audio_button)` | if | +1 |
| 834 | `if (askForSavePath)` | if | +1 |
| 836 | `if (useStorageAccessFramework)` | if | +1 |
| 840 | `if (...== audio_button)` | if | +1 |
| 856 | `if (freeSpace <= size)` | if | +1 |
| 861 | `if (resolveActivity != null)` | if | +1 |

**Total CCN: 20** (matches lizard output)
