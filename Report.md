
# Report for assignment 3

## Project

- Name: NewPipe
- URL: https://github.com/TeamNewPipe/NewPipe

NewPipe is a free, open-source Android app that allows users to watch and download YouTube videos without using the official YouTube app. It does not require a Google account and focuses on privacy by avoiding tracking and background data collection.

# Function 1: prepareSelectedDownload()

## Onboarding experience & Complexity

1. What are your results for five complex functions? 
* Did all methods (tools vs. manual count) get the same result? 

Yes, both Lizard and manual CCN count got the same: 20.

* Are the results clear? 

Yes, the results from Lizard were clear.

2. Are the functions just complex, or also long? 

This function is both complex, and very long, almost 100 LOC.

3. What is the purpose of the functions? 

The function prepares and validates a download request by:

- Determining download type (audio, video, or subtitle) based on user selection
- Building the filename with appropriate extension based on media format
- Selecting the correct storage location (audio or video directory)
- Validating storage availability
- Launching appropriate UI (folder picker or save-as dialog) when needed
- Persisting user preferences (last download type used)

4. Are exceptions taken into account in the given measurements? 

Lizard does not count exceptions, it only counts explicit control flow (if, else, switch, case, etc.). This function has no explicit try-catch blocks, so there are no exception handling branches to count.

5. Is the documentation clear w.r.t. all the possible outcomes? 

Yes, the function had clear documentation for its functionality.

## Refactoring

Plan for refactoring complex code: 

see [Refactoring-Plan.md](prepareSelectedDownload/Refactoring-Plan.md) for details

Estimated impact of refactoring (lower CC, but other drawbacks?).

New CCN for this function is 11 (45% reduction), all functionality is preserved. This refactoring improves testability and readability. A drawback from this could be that it becomes more fragmented, however all these switch cases and if-statements should have never been in one function to begin with.

Carried out refactoring (optional, P+):

see [RefactoredFunction.java](prepareSelectedDownload/RefactoredFunction.java) for details

## Coverage

### Tools

Document your experience in using a "new"/different coverage tool.
How well was the tool documented? Was it possible/easy/difficult to
integrate it with your build environment?

The tool we used for automated coverage was JaCoCo. There were some difficulties to run it, however this was mostly due to the project itself being an Android project (needed Android SDK) and also that there were some failing tests. However skipping the failures and forcing JaCoCo to generate a report anyway worked fine.

### Your own coverage tool

Show a patch (or link to a branch) that shows the instrumented code to
gather coverage measurements.

see [ManualCoverageInstrumentation.java](prepareSelectedDownload/ManualCoverageInstrumentation.java) for details

What kinds of constructs does your tool support, and how accurate is
its output? 

The tool supports normal control flow branches (if/else, switch/case, while loops) and boolean conditions (OR/AND operators), it tracks each as separate decision points. However, it does not automatically handle ternary operators (? :) or exception paths (try/catch). The output accuracy is identical to manual counting.

### Evaluation

1. How detailed is your coverage measurement? 

The coverage measurement tracks 38 distinct branches in prepareSelectedDownload(), including method entry, switch statement outcomes (4 cases), nested if/else branches (10 in switch cases), compound OR conditions (3 operators counted separately), and storage/path validation logic (15 branches). Each branch is labeled with its purpose ("if: format == MediaFormat.WEBMA_OPUS (true)") and reports hit count and coverage percentage.

2. What are the limitations of your own tool?

The tool requires manual source code modification (adding recordBranch() calls before each branch), does not automatically track ternary operators or exception handling paths. Also any code changes require manual re-instrumentation, so it is impractical for continuous development.

3. Are the results of your tool consistent with existing coverage tools?

Both the manual tool and JaCoCo reported 0% initial coverage and showed significant improvement after adding tests. However, there's a 2 branch difference (manual: 16/38, JaCoCo: 16/36), most likely because JaCoCo counts branches differently for compound conditions or combines some if/else pairs. The covered branch patterns match exactly (same switch cases and if-statements hit).

## Coverage improvement

Show the comments that describe the requirements for the coverage.
Report of old coverage: [manual_coverage(before)](prepareSelectedDownload/manual_coverage(before).txt)
Report of new coverage: [manual_coverage(after)](prepareSelectedDownload/manual_coverage(after).txt)

Test cases added:

see [DownloadDialogTest.java](prepareSelectedDownload/DownloadDialogTest.java) for details

# Function 2: getFilteredAudioStreams()

## Onboarding experience & Complexity

1. What are your results for the complex functions?
   * Did all methods (tools vs. manual count) get the same result?
   * Are the results clear?

Yes. For getFilteredAudioStreams both Lizard and the manual count got the same result, CCN 9.

2. Are the functions just complex, or also long?
    
    The function is mostly just complex its 35 LOC.

3. What is the purpose of the functions?

The purpose is to filter audio streams from a list of available streams, keeping the best-quality stream per audio track and filtering out unsupported formats and delivery methods.
    
4. Are exceptions taken into account in the given measurements?

No. For getFilteredAudioStreams there are no exceptions.

5. Is the documentation clear w.r.t. all the possible outcomes?

Partly. The doc describes overall behaviour (filter, best per track, sort) but not each branch (e.g. null → empty, skip TORRENT/HLS+OPUS, remove unknown when multiple tracks). 

## Refactoring

Plan for refactoring complex code: 

see [refactoringPlan.md](getFilteredAudioStreams/04-refactoring-plan/refactoringPlan.md) for details

Estimated impact of refactoring (lower CC, but other drawbacks?).

Main method CCN drops from 9 to 5 (~44 %). Behaviour is unchanged. The only drawback is a few extra small methods to maintain; each is short and has a single responsibility.

Carried out refactoring (optional, P+):

see [getFilteredAudioStreamsRefactor.java](getFilteredAudioStreams/05-refactor-function/getFilteredAudioStreamsRefactor.java) for details

### Tools

Document your experience in using a "new"/different coverage tool.
How well was the tool documented? Was it possible/easy/difficult to
integrate it with your build environment?

The tool I used for code coverage was JaCoCo. I initially had some trouble running it, but after installing the required Android and Java versions for the NewPipe project, it worked.

### Your own coverage tool

Show a patch (or link to a branch) that shows the instrumented code to
gather coverage measurements.

see [getFilteredAudioStreamsInstrumented.java](getFilteredAudioStreams/02-coverage-tool/getFilteredAudioStreamsInstrumented.java) and [manualCoverage.java](getFilteredAudioStreams/02-coverage-tool/manualCoverage.java) for details

What kinds of constructs does your tool support, and how accurate is
its output? 

It supports normal branches (if/else, for): each branch has a unique ID and we call hit(id) when it is taken. Output is accurate for instrumented branches. It does not track ternary operators or exceptions.

### Evaluation

1. How detailed is your coverage measurement? 

Branch-level: we report which of the 8 branch IDs were hit (e.g. 6/8). No line or instruction counts.

2. What are the limitations of your own tool?

Only branches we manually instrument are counted. No ternary or exception paths. If the code changes, instrumentation must be updated by hand.

3. Are the results of your tool consistent with existing coverage tools?

ManualCoverage reported 75% (6/8 branches) and JaCoCo 87%. They point in the same direction; JaCoCo counts more branch points (e.g. inside helpers) and uses a different denominator, so the percentages do not match exactly.

## Coverage improvement

Show the comments that describe the requirements for the coverage.
Report of old coverage: [coverageBefore.md](getFilteredAudioStreams/02-coverage-tool/coverageBefore.md)
Report of new coverage: [coverageAfter.md](getFilteredAudioStreams/03-improve-coverage/coverageAfter.md)

Test cases added:

see [getFilteredAudioStreamsTest.java](getFilteredAudioStreams/03-improve-coverage/getFilteredAudioStreamsTest.java) for details


# Function 3: getNextChunk(boolean infoOnly)

## Onboarding experience & Complexity

**Chosen function**  
`getNextChunk(boolean infoOnly)` in `Mp4DashReader.java`  
Path: `./app/src/main/java/org/schabi/newpipe/streams/Mp4DashReader.java`

### Lizard results (before refactor)
- **NLOC = 64**
- **CCN = 18**
- token = 419, PARAM = 1, length = 76
- location: `@176-251`

### Manual CC count (before refactor)
Rule: start at 1; +1 per `if/while`; +1 per `case` (excluding `default`); +1 per `&&/||` in conditions.  
**Result:** **CC = 18** (matches Lizard)

1. **Results / agreement**  
   Manual and tool match (18). Some tools may count `switch`/short-circuit logic differently.

2. **High CC vs LOC**  
   Not huge LOC, but it’s branch-heavy: nested checks, many early exits, and flag-based cases.

3. **Purpose (why it’s branchy)**  
   Parses MP4-DASH fragment boxes (`moof` + `mdat`), enforces ordering, and derives missing fields based on flags (`dataOffset`, `chunkSize`, `chunkDuration`). Format variability drives branching.

4. **Exceptions and CC**  
   CC tools typically don’t treat `throw` as decision points (no `catch` blocks here). If you treat each `throw` as an extra possible exit path, the effective number of paths is higher than CC.

5. **Documentation clarity**  
   Outcomes are: return a chunk, return `null` (end/skip), or throw `IOException` for malformed sequences. This isn’t fully documented in Javadoc.

---

## Refactoring

### Plan
Split responsibilities into helpers:
- `advanceToNextBox()` — handles `chunkZero`, `ensure(box)`, and reading the next box
- `processCurrentBox(track, infoOnly)` — dispatch based on `box.type`
- `handleMoofBox(track)` / `handleMdatBox(infoOnly)` — box-specific logic
- `normalizeTrafAfterParse(...)` + helpers for offset/size/duration fixups

Goal: keep behavior identical, reduce nesting in the main loop, and improve testability.

### Carried out refactoring (P+)
Files:
- Refactored version: `code/getNextChunkRefactored.java`
- Original/instrumented snapshot: `code/getNextChunkLocal.java`

### Lizard results (after refactor)
From Lizard output:
- **NLOC = 13**
- **CCN = 4**
- token = 65, PARAM = 1, length = 16

**Complexity reduction:** 18 → 4 (**77.78%**, above the 35% target)

---

## Coverage

### Your own coverage tool (DIY)
Coverage helper:
- `code/Mp4DashReaderBranchCoverage.java`

Instrumentation approach:
- Insert `recordBranch(ID)` as the first statement of each branch outcome in `getNextChunk()` (`if/else` arms, box-type dispatch, `return`, `throw`).

#### Evaluation
1. **How detailed is the measurement?**  
   Branch-level for this single function: **33 branch IDs**, each reported as covered/not covered with hit counts.

2. **Limitations**  
   Manual and brittle: IDs + markers must be updated if code changes. No automatic mapping from ID → source line unless maintained separately. Doesn’t include compiler/bytecode-generated branches.

3. **Consistency vs automated tools**  
   Consistent for explicit `if/switch/return/throw` outcomes. Percentages may differ from JaCoCo due to different branch models/denominators.

---

## Coverage improvement

### Tests added
Test file:
- `code/Mp4DashReaderGetNextChunkTest.java`

What the tests cover (requirements):
- Valid `moof` then `mdat` returns a `Mp4DashChunk`
- `infoOnly=false` creates `chunk.data`, `infoOnly=true` keeps `chunk.data == null`
- Error paths:
   - `mdat` without `moof` → `IOException`
   - second `moof` before closing previous → `IOException`
   - negative `trun.dataOffset` after adjustment → `IOException`
- Track mismatch:
   - `moof.traf == null` causes `mdat` to be skipped and the function returns `null`

### Coverage before/after
- **Before tests:** 0% (function not executed)
- **After tests:** **81.82% (27/33)**  
  Log: `code/coverage(after).txt`

Uncovered IDs: **13, 17, 18, 21, 22, 32**  
(e.g., dataOffset non-negative path, size fallback branch, duration-fix false branch, default/other box type)

---



# Function 4: xxx()

## Onboarding experience & Complexity

1. What are your results for five complex functions?
   * Did all methods (tools vs. manual count) get the same result?
   * Are the results clear?
2. Are the functions just complex, or also long?
3. What is the purpose of the functions?
4. Are exceptions taken into account in the given measurements?
5. Is the documentation clear w.r.t. all the possible outcomes?

## Refactoring

Plan for refactoring complex code:

Estimated impact of refactoring (lower CC, but other drawbacks?).

Carried out refactoring (optional, P+):

git diff ...

## Coverage

### Tools

Document your experience in using a "new"/different coverage tool.

How well was the tool documented? Was it possible/easy/difficult to
integrate it with your build environment?

### Your own coverage tool

Show a patch (or link to a branch) that shows the instrumented code to
gather coverage measurements.

The patch is probably too long to be copied here, so please add
the git command that is used to obtain the patch instead:

git diff ...

What kinds of constructs does your tool support, and how accurate is
its output?

### Evaluation

1. How detailed is your coverage measurement?

2. What are the limitations of your own tool?

3. Are the results of your tool consistent with existing coverage tools?

## Coverage improvement

Show the comments that describe the requirements for the coverage.

Report of old coverage: [link]

Report of new coverage: [link]

Test cases added:

git diff ...

Number of test cases added: two per team member (P) or at least four (P+).

# Function 5: xxx()

## Onboarding experience & Complexity

1. What are your results for five complex functions?
   * Did all methods (tools vs. manual count) get the same result?
   * Are the results clear?
2. Are the functions just complex, or also long?
3. What is the purpose of the functions?
4. Are exceptions taken into account in the given measurements?
5. Is the documentation clear w.r.t. all the possible outcomes?

## Refactoring

Plan for refactoring complex code:

Estimated impact of refactoring (lower CC, but other drawbacks?).

Carried out refactoring (optional, P+):

git diff ...

## Coverage

### Tools

Document your experience in using a "new"/different coverage tool.

How well was the tool documented? Was it possible/easy/difficult to
integrate it with your build environment?

### Your own coverage tool

Show a patch (or link to a branch) that shows the instrumented code to
gather coverage measurements.

The patch is probably too long to be copied here, so please add
the git command that is used to obtain the patch instead:

git diff ...

What kinds of constructs does your tool support, and how accurate is
its output?

### Evaluation

1. How detailed is your coverage measurement?

2. What are the limitations of your own tool?

3. Are the results of your tool consistent with existing coverage tools?

## Coverage improvement

Show the comments that describe the requirements for the coverage.

Report of old coverage: [link]

Report of new coverage: [link]

Test cases added:

git diff ...

Number of test cases added: two per team member (P) or at least four (P+).


# Self-assessment: Way of working

Current state according to the Essence standard: "In Place"

Was the self-assessment unanimous? Any doubts about certain items?

    Yes, the self-assessment was unanimous. There were no major doubts, although some items required brief discussion to ensure a shared understanding.

How have you improved so far?

    During the course, we have improved as a group, especially in working together more effectively and collaboratively.

Where is potential for improvement?

    We need to reach a stage where the team naturally applies the practices without having to consciously think about them.

# Overall experience

What are your main take-aways from this project?

It is harder than expected to jump into an unknown complex project, which is not very structured and has only 2% test coverage. The onboarding experience was not very smooth. It is important to do a little research before deciding to work on a specific open source project, since it can be time draining if one chooses a badly structured project. 

What did you learn?

During this project we learnd a lot about the complexity of working with a larger project, due to NewPipes large amount of contributors, it was a lot to understand and it was quite messy.
We also learnd more about using the branch covering tool to be able easier see how well the functions are tested.

Is there something special you want to mention here?

The assignment was unnecessarily complex. In order to get P+ we needed to work in two different repos, and copy files in between. Assignment 1 & 2 were much better structured and we felt that we learned more from those.
