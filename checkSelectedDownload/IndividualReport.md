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

## Part 2: Coverage Measurement

### Task 1: Manual Branch Coverage Instrumentation

**Files Created:**
1. `ManualCoverage.java` - Coverage tracking infrastructure
2. Instrumented `DownloadDialog.java` - Modified method with coverage calls

**Total: 39 branches** covering all decision points identified in manual CC count.

#### Quality of Manual Coverage Tool

**Strengths:**
-  Tracks all normal branches (if, while, switch/case)
-  Tracks compound boolean conditions with OR operators
-  Tracks try/catch exception paths
-  Shows hit count for each branch

**Limitations:**
1. Ternary operators are not automatically tracked
2. Exception paths without explicit try/catch instrumentation are not automatically tracked 
3. OR/AND operators create implicit branches that are tracked at the condition level, not individual sub-expression level
4. The source code must be modified to add recordBranch() calls
6. Instrumentation must be updated manually if the method is modified

**Comparison to Automated Tools (JaCoCo):**
- JaCoCo automatically instruments bytecode, no source modifications needed
- JaCoCo tracks exceptions automatically
- JaCoCo provides whole-project coverage without per-function setup

**Baseline Coverage (Before Instrumentation):**
- JaCoCo Result: 0% branch coverage
- Manual Tool Result: 0% branch coverage

### Task 2: Coverage Improvement

**Initial Coverage:** 0% (0/39 branches in manual tool)

**Strategy:** Create unit tests targeting different switch case branches. Avoid setting parameters calling static Android methods (just pure JUnit environment).

**Tests Created:**

1. **testNormalDownload**
   - **Requirement:** Dialog can proceed without errors ocurring
   - **Assertion:** Verifies that showFailedDialog() is never called, in other words that the download succeeds

2. **testTargetFileNull**
   - **Requirement:** The case when the target file is null gets handled by creating a new file
   - **Assertion:** Verifies that showFailedDialog() is never called, meaning a new file is created successfully in mainStorage

**Coverage Report Location:** `manual_coverage(after).txt`

### Task 3: Refactoring Plan

checkSelectedDownload can (and probably should) be split up into smaller methods. This can be done as follows:

1. Extract Storage Resolution Logic 
   - Create method such as resolveStorage to determine which StoredFileHelper to use and handle exceptions and calls to showFailedDialog
   - Would replace the first try block including all nested if/else logic

2. Extract Mission State Logic
   - Create method such as getStorageMissionState
   - Would replace the switch case for what file missions refer to

3. Extract Dialog Message Selection
   - Create method such as determineDialogMessages
   - Would be called inside the None case in the mission state switch (see 2.) instead of having a large if/else

4. Extract Positive-Button Action Logic 
   - Create method such as handlePositiveButtonAction to handle switch case inside setPositiveButton
   - Would handle creation of new storage, continueSelectedDownload(storage), and failure handling

**Benefits:**
- Improved testability thanks to smaller methods with lower CC each
- Better readability because of less nested conditional statements
- Easier maintenance 