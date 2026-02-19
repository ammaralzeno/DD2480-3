import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for getFilteredAudioStreams. */
class GetFilteredAudioStreamsTest {

    @BeforeAll
    static void init() {
        manualCoverage.init();
    }

    @AfterAll
    static void report() {
        manualCoverage.report();
    }

    /** Null input returns empty list. */
    @Test
    void nullReturnsEmpty() {
        Comparator<AudioStream> cmp = ListHelper.getAudioFormatComparator(MediaFormat.M4A, false);
        List<AudioStream> r = ListHelper.getFilteredAudioStreams(cmp, null);
        assertTrue(r.isEmpty());
    }

    /** Empty list in returns empty list out. */
    @Test
    void emptyListReturnsEmpty() {
        Comparator<AudioStream> cmp = ListHelper.getAudioFormatComparator(MediaFormat.M4A, false);
        List<AudioStream> r = ListHelper.getFilteredAudioStreams(cmp, Collections.emptyList());
        assertTrue(r.isEmpty());
    }

    /** TORRENT streams are filtered out. */
    @Test
    void torrentSkipped() {
        Comparator<AudioStream> cmp = ListHelper.getAudioFormatComparator(MediaFormat.M4A, false);
        List<AudioStream> in = List.of(
                generateAudioStreamWithDelivery("t1", MediaFormat.M4A, 128,
                        DeliveryMethod.TORRENT, "tr"));
        List<AudioStream> r = ListHelper.getFilteredAudioStreams(cmp, in);
        assertTrue(r.isEmpty());
    }

    /** Normal stream is kept. */
    @Test
    void normalStreamKept() {
        Comparator<AudioStream> cmp = ListHelper.getAudioFormatComparator(MediaFormat.M4A, false);
        AudioStream a = generateAudioStreamWithDelivery("a1", MediaFormat.M4A, 128,
                DeliveryMethod.PROGRESSIVE_HTTP, "tk");
        List<AudioStream> r = ListHelper.getFilteredAudioStreams(cmp, List.of(a));
        assertEquals(1, r.size());
        assertEquals("a1", r.get(0).getId());
    }

    /** Build AudioStream for tests. */
    private static AudioStream generateAudioStreamWithDelivery(String id, MediaFormat format,
            int bitrate, DeliveryMethod delivery, String trackId) {
        return new AudioStream.Builder()
                .setId(id)
                .setContent("", true)
                .setMediaFormat(format)
                .setAverageBitrate(bitrate)
                .setDeliveryMethod(delivery)
                .setAudioTrackId(trackId)
                .build();
    }
}
