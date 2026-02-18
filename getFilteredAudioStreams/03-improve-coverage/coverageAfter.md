getFilteredAudioStreams --- manualCoverage after new tests

manualCoverage: 6/8 (75%)
JaCoCo (getFilteredAudioStreams): 87% 

Branch IDs hit: 1, 2, 3, 4, 5, 8  
Missing: 6, 7 

1 = audioStreams == null, return
2 = audioStreams != null, continue
3 = skip (TORRENT or HLS+OPUS)
4 = process stream
5 = put stream
6 = do not put
7 = remove unknown track (size > 1)
8 = do not remove

Tests: nullReturnsEmpty, emptyListReturnsEmpty, torrentSkipped, normalStreamKept
