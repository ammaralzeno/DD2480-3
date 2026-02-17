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

## Part 2: Coverage Measurement

### Task 1: Manual Branch Coverage Instrumentation

**Files Created:**
1. `ManualCoverageInstrumentation.java` - Coverage tracking infrastructure
2. Instrumented `DownloadDialog.java` - Modified the function with coverage calls

**Branch Tracking:**
-  1 method entry point
-  1 switch statement with 4 outcomes (3 cases + default)
-  10 nested if/else branches in switch cases
-  7 storage validation branches
-  8 path selection branches
-  3 storage space check branches

**Total: 38 branches** covering all decision points identified in manual CC count.

#### Quality of Manual Coverage Tool

**Strengths:**
-  Tracks all normal branches (if, while, switch/case)
-  Tracks compound boolean conditions with OR operators
-  Shows hit count for each branch

**Limitations:**
1. Ternary operators not automatically tracked (would require manual instrumentation of each ternary)
2. Exception paths are not automatically tracked without explicit try/catch instrumentation
3. OR/AND operators create implicit branches that are tracked at the condition level, not individual sub-expression level
4. The source code must be modified (temporary) to add recordBranch() calls
6. If the program (the function) changes, instrumentation must be updated manually

**Comparison to Automated Tools (JaCoCo):**
- JaCoCo automatically instruments bytecode, no source modifications needed
- JaCoCo tracks exceptions automatically
- JaCoCo provides whole-project coverage without per-function setup

**Baseline Coverage (Before Instrumentation):**
- JaCoCo Result: 0% branch coverage (0/36 branches)
- Manual Tool Result: 0% branch coverage (0/38 branches tracked)

### Task 2: Coverage Improvement

**Initial Coverage:** 0% (0/38 branches in manual tool)

**Strategy:** Create unit tests targeting different switch case branches. All tests use `askForSavePath = false` and `mainStorage = null` to short-circuit the compound OR condition without calling static Android methods (just pure JUnit environment).

**Tests Created:**

1. **testAudioWebmaOpus_nullStorage_launchesAudioPicker**
   - **Requirement:** WEBMA_OPUS audio streams must set mime to "audio/ogg" and suffix to "opus"
   - **Assertion:** Validates correct mime type and file suffix for WEBMA_OPUS format
   - **Branches covered:** 1, 2, 3, 4, 19, 21, 22, 27

2. **testVideoMpeg4_nullStorage_launchesVideoPicker**
   - **Requirement:** MPEG_4 video streams must set correct mime type and suffix
   - **Assertion:** Validates MPEG_4 mime type and suffix are correctly applied
   - **Branches covered:** 1, 2, 8, 9, 19, 21, 22, 28

3. **testSubtitleTtml_nullStorage_launchesVideoPicker**
   - **Requirement:** TTML subtitle format must be converted to SRT suffix
   - **Assertion:** Validates TTML→SRT conversion and correct mime type
   - **Branches covered:** 1, 2, 11, 12, 14, 19, 21, 22, 28

4. **testAudioM4a_nullStorage_launchesAudioPicker**
   - **Requirement:** M4A audio (non-WEBMA_OPUS) must use format's mime/suffix
   - **Assertion:** Validates M4A mime type and suffix handling in else branch
   - **Branches covered:** 1, 2, 3, 5, 6, 19, 21, 22, 27

**Final Coverage:** 42.11% (16/38 branches)

**Coverage Report Location:** `manual_coverage(after).txt`

### Task 3: Refactoring Plan

**Current Complexity:** CCN = 20
**Target:** ≤12 (40% reduction)

**Refactoring Strategy (see [Refactoring-Plan.md](Refactoriing-Plan.md) for details):**

1. **Extract Switch Cases** (CCN reduction: -9)
   - Create `prepareAudioDownload()`, `prepareVideoDownload()`, `prepareSubtitleDownload()`
   - Removes 3 case branches + nested if/else from main method

2. **Extract Storage Validation** (CCN reduction: -4)
   - Create `isStorageValidForDownload(StoredDirectoryHelper)`
   - Encapsulates compound OR condition into single method

3. **Extract Directory Picker Logic** (CCN reduction: -2)
   - Create `launchAppropriateDirectoryPicker(int checkedButtonId)`
   - Handles audio vs video picker selection

4. **Extract Save-As Path Logic** (CCN reduction: -2)
   - Create `getInitialSavePath(int checkedButtonId)`
   - Returns Uri based on media type and SAF settings

**Expected Result:** CCN ≈ 11 (45% reduction)

**Refactored Implementation:** See [RefactoredFunction.java](RefactoredFunction.java)

**Benefits:**
- Improved testability (smaller methods easier to test)
- Better readability (clear method names describe intent)
- Easier maintenance (changes localized to specific methods)
- No functionality changes (all logic preserved)

