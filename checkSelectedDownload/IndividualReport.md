## Onboarding experience

The onboarding experience for NewPipe was mediocre. There is some documentation to build the project but it seems to be both slightly outdated and a little unclear to begin with. A few tools had to be installed in order to run the software, some were named but there was no information about where to get them from. The tools in and of themselves seemed quite well documented though.

Most components were added automatically once the tools were installed, but the build still did not conclude automatically as some tests in the project fail which causes errors.

## Chosen function

checkSelectedDownload() in DownloadDialog.java

This method decides whether a download can proceed (given a number of checks), needs user confirmation, or fails.

The documentation of the method is somewhat clear, some branches' outcomes are described while others are not.

## Lizard results for chosen function

NLOC=120    CCN=27   token=681  PARAM=4  length=150  

location= DownloadDialog::checkSelectedDownload@881-1030@.\download\DownloadDialog.java

## Manual CC count

| Line | Decision Point | Type | CCN +1 |
|------|---------------|------|--------|
| Base | Method entry | - | 1 |
| 888 | `if (mainStorage == null)` | if | +1 |
| 891 | `else if (targetFile == null)` | if | +1 |
| 913 | `case Finished` | case | +1 |
| 917 | `case Pending` | case | +1 |
| 921 | `case PendingRunning` | case | +1 |
| 925 | `case None` | case | +1 |
| 926 | `if (mainStorage == null)` | if | +1 |
| 931 | `if (!storage.existsAsFile() && !storage.create())` | if + && | +2 |
| 937 | `else if (targetFile == null)` | else if | +1 |
| 942 | `if (!mainStorage.mkdirs())` | if | +1 |
| 948 | `if (storage == null \|\| !storage.canWrite())` | if + \|\| | +2 |
| 970 | `if (mainStorage == null)` | if | +1 |
| 975 | `case Pending` | case | +1 |
| 976 | `case Finished` | case | +1 |
| 994 | `case Finished` | case | +1 |
| 995 | `case Pending` | case | +1 |
| 997 | `case None` | case | +1 |
| 998 | `if (targetFile == null)` | if | +1 |
| 1012 | `if (storageNew != null && storageNew.canWrite())` | if + && | +2 |
| 1018 | `case PendingRunning` | case | +1 |
| 1020 | `if (storageNew == null)` | if | +1 |

**Total CCN: 25** 
lizard seems to take exceptions into account when counting, since there are two `try {...} catch {...}` blocks in the method.

