## Chosen function
`getNextChunk(boolean infoOnly)` — `Mp4DashReader.java`  
`./app/src/main/java/org/schabi/newpipe/streams/Mp4DashReader.java`

---

## Part 1 — Complexity

### Lizard (before refactor)
- NLOC: 64
- CCN: 18
- token: 419
- PARAM: 1
- length: 76
- location: `@176–251`

### Manual CC (before refactor)
Counting rule: start at 1; +1 per `if/while`; +1 per `case` (excluding `default`); +1 per `&&/||`.

Result: **CC = 18**, same as Lizard.  
Different tools may disagree slightly depending on how they treat `switch` and short-circuit conditions.

### Notes
- The method isn’t huge in LOC, but it has lots of nested checks, validation logic, and early exits, which drives CC up.
- The high CC is mostly explained by what it does: it parses DASH MP4 fragments (`moof`/`mdat`), handles a bunch of flags, and needs to reject malformed sequences.
- Exceptions aren’t really “counted” in CC here (no `catch` blocks), but in practice each `throw` is another possible exit path, so the number of effective paths is higher than the CC number alone suggests.
- The outcomes are basically: return a chunk, return `null` at end-of-stream, or throw `IOException`. That isn’t super explicit in the Javadoc.

---

## Part 2 — Branch coverage (DIY)

### Tool
I instrumented `getNextChunk()` with `Mp4DashReaderBranchCoverage.recordBranch(ID)` at the start of each branch outcome.  
A shutdown hook prints the report after the unit tests finish.

Quality / limits:
- It covers the branches I explicitly instrumented (`if/else`, `switch/case`, `return`, `throw`).
- Ternary operators aren’t automatically handled (you’d have to instrument both outcomes manually).
- It’s manual and brittle: if the method changes, the IDs and markers must be updated.
- It won’t match bytecode tools perfectly (short-circuit compilation etc.), but it should be consistent for the source-level branches.

### Tests added
New unit tests exercise both normal and error paths using a minimal in-memory MP4 (`moof + mdat`) and reflection to set the private reader state.

Covered behavior:
- Valid `moof` then `mdat` returns a `Mp4DashChunk`
- `infoOnly=false` creates `chunk.data`, `infoOnly=true` keeps `chunk.data == null`
- Error cases:
  - `mdat` without `moof` → `IOException`
  - second `moof` before previous `mdat` → `IOException`
  - negative `trun.dataOffset` after adjustment → `IOException`
- Track mismatch: `moof.traf == null` makes `mdat` skipped and the function returns `null`

Coverage:
- Before tests: **0%** (function wasn’t executed)
- After tests: **81.82% (27/33)**  
  Uncovered IDs: **13, 17, 18, 21, 22, 32**

---

## Task 3 — Refactoring plan (≥35% CC reduction)

Goal: reduce CC of `getNextChunk()` from 18 to ≤11.

Approach: split the big method into small helpers so the main method becomes mostly orchestration:
- `advanceToNextBox()` — stream/box progression + `chunkZero`
- `processCurrentBox(track, infoOnly)` — dispatch
- `handleMoofBox(track)` / `handleMdatBox(infoOnly)` — box-specific logic
- `normalizeTrafAfterParse(...)` + small helpers for offset/size/duration fixes

### Lizard (after refactor)
`13  4  65  1  16  ...getNextChunk@177-192@app/src/main/java/org/schabi/newpipe/streams/Mp4DashReader.java`

So:
- NLOC: 13
- CCN: 4
- length: 16

Reduction: (18 - 4)/ 18 = 77.78%
