Refactoring plan: getFilteredAudioStreams

Current metrics
- CCN: 9
- Target CCN: 5 (~44 % reduction)
- NLOC: 26

Is the complexity needed?
The logic is simple (skip, compare, filter). The CCN is high because everything is inlined. We can move the decisions out into helpers.

Current state (CCN breakdown)
- Method entry: 1
- if (audioStreams == null): 1
- for loop: 1
- if (TORRENT || (HLS && OPUS)): 3
- if (presentStream == null || cmp.compare(...) > 0): 2
- if (collectedStreams.size() > 1): 1
- Total: 9

The plan

1. shouldSkipAudioStream(stream) (–3 CCN)
Pull out the TORRENT/HLS+OPUS check. Returns true if the stream should be skipped. In the loop: if (shouldSkipAudioStream(stream)) continue;
Benefit: Less clutter in the loop, skip logic in its own method.

2. isBetterStream(stream, presentStream, cmp) (–2 CCN)
Pull out the "replace or keep?" logic. Returns true if this stream should replace the current one (null or comparator prefers it). In the loop: if (isBetterStream(...)) put(...).
Benefit: Clearer intent, comparison logic in its own method.

3. removeUnknownTrackIfMultiple(collectedStreams) (–1 CCN)
Pull out the "remove unknown track when there are multiple" logic. If size > 1, remove the entry for key "". Call it after the loop.
Benefit: Single responsibility, cleanup in one place.

Result
- Main method CCN: before 9, after 5
- Reduction: 44 %
Main function becomes: null check → loop with the two helpers → remove unknown → sort and return.
