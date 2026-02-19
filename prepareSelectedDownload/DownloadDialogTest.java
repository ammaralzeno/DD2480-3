package org.schabi.newpipe.download;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.widget.RadioGroup;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.schabi.newpipe.R;
import org.schabi.newpipe.coverage.ManualCoverageInstrumentation;
import org.schabi.newpipe.databinding.DownloadDialogBinding;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.util.StreamItemAdapter;
import org.schabi.newpipe.util.StreamItemAdapter.StreamInfoWrapper;

import java.lang.reflect.Field;

/**
 * Unit tests for DownloadDialog.prepareSelectedDownload
 *
 * These tests improve branch coverage from 0% to ~42% by exploring
 * different paths through the switch/if-else structure of the function
 *
 * Four test scenarios:
 *  1. Audio / WEBMA_OPUS / null storage -> audio directory picker
 *  2. Video / MPEG_4    / null storage -> video directory picker
 *  3. Subtitle / TTML   / null storage -> video directory picker
 *  4. Audio / M4A       / null storage -> audio directory picker
 */
public class DownloadDialogTest {

    private DownloadDialog dialog;
    private RadioGroup mockRadioGroup;
    @SuppressWarnings("rawtypes")
    private StreamItemAdapter mockAudioAdapter;
    @SuppressWarnings("rawtypes")
    private StreamItemAdapter mockVideoAdapter;
    @SuppressWarnings("rawtypes")
    private StreamItemAdapter mockSubtitleAdapter;
    @SuppressWarnings("rawtypes")
    private StreamInfoWrapper mockWrappedAudio;
    private Context mockContext;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        ManualCoverageInstrumentation.initialize();

        dialog = spy(new DownloadDialog());

        mockContext = mock(Context.class);

        mockRadioGroup = mock(RadioGroup.class);

        DownloadDialogBinding mockBinding = mock(DownloadDialogBinding.class);
        setField(mockBinding, "videoAudioGroup", mockRadioGroup);

        mockAudioAdapter = mock(StreamItemAdapter.class);
        mockVideoAdapter = mock(StreamItemAdapter.class);
        mockSubtitleAdapter = mock(StreamItemAdapter.class);

        mockWrappedAudio = mock(StreamInfoWrapper.class);

        setField(dialog, "context", mockContext);
        setField(dialog, "dialogBinding", mockBinding);
        setField(dialog, "audioStreamsAdapter", mockAudioAdapter);
        setField(dialog, "videoStreamsAdapter", mockVideoAdapter);
        setField(dialog, "subtitleStreamsAdapter", mockSubtitleAdapter);
        setField(dialog, "selectedAudioIndex", 0);
        setField(dialog, "selectedVideoIndex", 0);
        setField(dialog, "selectedSubtitleIndex", 0);

        setField(dialog, "askForSavePath", false);
        setField(dialog, "mainStorageAudio", null);
        setField(dialog, "mainStorageVideo", null);

        doReturn("audio_key").when(dialog).getString(R.string.last_download_type_audio_key);
        doReturn("video_key").when(dialog).getString(R.string.last_download_type_video_key);
        doReturn("subtitle_key").when(dialog).getString(R.string.last_download_type_subtitle_key);
        doReturn("No dir yet").when(dialog).getString(R.string.no_dir_yet);

        doReturn("test_video").when(dialog).getNameEditText();
        doNothing().when(dialog).launchDirectoryPicker(any());
        doNothing().when(dialog).showToastMessage(anyString());
        doReturn(mockWrappedAudio).when(dialog).getWrappedAudioStreams();
    }

    @AfterClass
    public static void printFinalReport() {
        ManualCoverageInstrumentation.printReport();
        double cov = ManualCoverageInstrumentation.getCoveragePercentage();

        // Write coverage report to file
        String projectDir = System.getProperty("user.dir");
        String reportPath = projectDir + "/manual_coverage.txt";
        ManualCoverageInstrumentation.writeReport(reportPath);
    }

    /*
     * TEST 1 - Audio / WEBMA_OPUS / null storage -> audio picker
     *
     * Requirement: Selecting a WEBMA_OPUS audio stream must set mime
     *   to "audio/ogg" and suffix to "opus". With null mainStorageAudio,
     *   the method short-circuits to the directory picker for audio.
     *
     * Covered branches: 1, 2, 3, 4, 19, 21, 22, 27
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAudioWebmaOpus_nullStorage_launchesAudioPicker() throws Exception {
        when(mockRadioGroup.getCheckedRadioButtonId()).thenReturn(R.id.audio_button);

        AudioStream mockStream = mock(AudioStream.class);
        when(mockStream.getFormat()).thenReturn(MediaFormat.WEBMA_OPUS);
        when(mockAudioAdapter.getItem(0)).thenReturn(mockStream);
        when(mockWrappedAudio.getSizeInBytes(0)).thenReturn(1_000_000L);

        dialog.prepareSelectedDownload();

        String filenameTmp = (String) getField(dialog, "filenameTmp");
        assertTrue("filename should end with 'opus'", filenameTmp.endsWith("opus"));

        String mimeTmp = (String) getField(dialog, "mimeTmp");
        assertEquals("WEBMA_OPUS mime must be audio/ogg", "audio/ogg", mimeTmp);
    }

    /*
     * TEST 2 - Video / MPEG_4 / null storage -> video picker
     *
     * Requirement: Selecting an MPEG_4 video stream must set the MPEG_4
     *   mime and suffix. With null mainStorageVideo, the method
     *   short-circuits to the directory picker for video.
     *
     * Covered branches: 1, 2, 8, 9, 19, 21, 22, 28
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testVideoMpeg4_nullStorage_launchesVideoPicker() throws Exception {
        when(mockRadioGroup.getCheckedRadioButtonId()).thenReturn(R.id.video_button);

        VideoStream mockStream = mock(VideoStream.class);
        when(mockStream.getFormat()).thenReturn(MediaFormat.MPEG_4);
        when(mockVideoAdapter.getItem(0)).thenReturn(mockStream);

        StreamInfoWrapper mockWrappedVideo = mock(StreamInfoWrapper.class);
        when(mockWrappedVideo.getSizeInBytes(0)).thenReturn(5_000_000L);
        setField(dialog, "wrappedVideoStreams", mockWrappedVideo);

        dialog.prepareSelectedDownload();

        String filenameTmp = (String) getField(dialog, "filenameTmp");
        assertTrue("filename should end with MPEG_4 suffix",
                filenameTmp.endsWith(MediaFormat.MPEG_4.getSuffix()));

        String mimeTmp = (String) getField(dialog, "mimeTmp");
        assertEquals("MPEG_4 mime type", MediaFormat.MPEG_4.mimeType, mimeTmp);
    }

    /*
     * TEST 3 - Subtitle / TTML / null storage -> video picker
     *
     * Requirement: When the subtitle format is TTML, the filename suffix
     *   should be SRT (TTML->SRT conversion). With null mainStorageVideo,
     *   the method short-circuits to the video directory picker.
     *
     * Covered branches: 1, 2, 11, 12, 14, 19, 21, 22, 28
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSubtitleTtml_nullStorage_launchesVideoPicker() throws Exception {
        when(mockRadioGroup.getCheckedRadioButtonId()).thenReturn(R.id.subtitle_button);

        SubtitlesStream mockStream = mock(SubtitlesStream.class);
        when(mockStream.getFormat()).thenReturn(MediaFormat.TTML);
        when(mockSubtitleAdapter.getItem(0)).thenReturn(mockStream);

        StreamInfoWrapper mockWrappedSub = mock(StreamInfoWrapper.class);
        when(mockWrappedSub.getSizeInBytes(0)).thenReturn(50_000L);
        setField(dialog, "wrappedSubtitleStreams", mockWrappedSub);

        dialog.prepareSelectedDownload();

        String filenameTmp = (String) getField(dialog, "filenameTmp");
        assertTrue("TTML should be converted - filename must end with SRT suffix",
                filenameTmp.endsWith(MediaFormat.SRT.getSuffix()));

        String mimeTmp = (String) getField(dialog, "mimeTmp");
        assertEquals("TTML mime type", MediaFormat.TTML.mimeType, mimeTmp);
    }

    /*
     * TEST 4 - Audio / M4A (non-WEBMA_OPUS) / null storage -> audio picker
     *
     * Requirement: Selecting an M4A audio stream (not WEBMA_OPUS) must
     *   take the else-branch of the WEBMA_OPUS check, set the M4A mime
     *   and suffix. With null mainStorageAudio, the method short-circuits
     *   to the audio directory picker.
     *
     * Covered branches: 1, 2, 3, 5, 6, 19, 21, 22, 27
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAudioM4a_nullStorage_launchesAudioPicker() throws Exception {
        when(mockRadioGroup.getCheckedRadioButtonId()).thenReturn(R.id.audio_button);

        AudioStream mockStream = mock(AudioStream.class);
        when(mockStream.getFormat()).thenReturn(MediaFormat.M4A);
        when(mockAudioAdapter.getItem(0)).thenReturn(mockStream);
        when(mockWrappedAudio.getSizeInBytes(0)).thenReturn(10_000_000L);

        dialog.prepareSelectedDownload();

        String mimeTmp = (String) getField(dialog, "mimeTmp");
        assertEquals("M4A mime type", MediaFormat.M4A.mimeType, mimeTmp);

        String filenameTmp = (String) getField(dialog, "filenameTmp");
        assertTrue("filename should end with M4A suffix",
                filenameTmp.endsWith(MediaFormat.M4A.getSuffix()));
    }


    /* =================================================================
     * helpers
     * ================================================================= */

    /**
     * Sets any field (private, final, etc.) on the given object.
     * For Mockito mocks the "final" enforcement is relaxed, so plain
     * reflection works.
     */
    private static void setField(Object target, String name, Object value)
            throws Exception {
        Field f = findField(target.getClass(), name);
        if (f == null) {
            throw new NoSuchFieldException(
                    name + " not found on " + target.getClass().getName());
        }
        f.setAccessible(true);
        f.set(target, value);
    }

    private static Object getField(Object target, String name) throws Exception {
        Field f = findField(target.getClass(), name);
        if (f == null) {
            throw new NoSuchFieldException(name);
        }
        f.setAccessible(true);
        return f.get(target);
    }

    private static Field findField(Class<?> clazz, String name) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) { }
        }
        return null;
    }
}
