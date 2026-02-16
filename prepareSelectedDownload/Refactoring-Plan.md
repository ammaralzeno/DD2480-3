## Refactoring Plan for `prepareSelectedDownload()`

## Current Metrics
- **CCN:** 20
- **Target CCN:** = 12 (~40% reduction)
- **NLOC:** 94

## Strategies for improvement:

### 1. Extract Switch Cases (Estimated CCN reduction: -9)
**Extract each media type case into separate methods:**
- `prepareAudioDownload()`  handles audio_button case (CCN: 3)
- `prepareVideoDownload()`  handles video_button case (CCN: 2)
- `prepareSubtitleDownload()`  handles subtitle_button case (CCN: 3)

**Benefit:** Removes 3 case branches + nested if/else logic from main method. Each extracted method will have very low complexity on its own.

### 2. Extract Storage Validation Logic (Estimated CCN reduction: -4)
**Create method: `isStorageValidForDownload(StoredDirectoryHelper storage)`**
- Encapsulates the compound OR condition (lines 854-856)
- Returns boolean for storage validity check
- Reduces 3 OR operators + 1 if statement

**Benefit:** Simplifies the nested conditional logic and improves readability.

### 3. Extract Directory Picker Logic (Estimated CCN reduction: -2)
**Create method: `launchAppropriateDirectoryPicker(int checkedButtonId)`**
- Handles the if/else for audio vs video folder picker (lines 876-880)
- Encapsulates launcher selection logic

**Benefit:** Removes nested if/else within storage validation block.

### 4. Extract Save-As Path Logic (Estimated CCN reduction: -2)
**Create method: `getInitialSavePath(int checkedButtonId)`**
- Returns Uri for initial path based on media type (lines 883-892)
- Handles SAF vs direct path selection

**Benefit:** Removes nested if/else within askForSavePath block.

## Implementation Order
1. Extract switch cases first (biggest impact)
2. Extract storage validation logic
3. Extract picker and path helper methods

## Expected Result
- **New CCN:** ~11-12 (40-45% reduction)
- **Unchanged:** All functionality preserved
- **Improved:** Testability and readability

## Validation
- All existing tests must pass
- Manual coverage tool should show identical branch coverage patterns
- No changes to method inputs/outputs or any new side effects
