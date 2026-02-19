package org.schabi.newpipe.download;

import android.content.Context;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.schabi.newpipe.streams.io.StoredDirectoryHelper;
import org.schabi.newpipe.streams.io.StoredFileHelper;
import org.schabi.newpipe.coverage.ManualCoverage;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

public class DownloadDialogTest {

    private DownloadDialog dialog;
    private StoredDirectoryHelper mockStorage;
    private StoredFileHelper mockFile;
    private Uri mockUri;

    @Before
    public void setUp() throws Exception {
        dialog = spy(new DownloadDialog());

        Context mockContext = mock(Context.class);
        dialog.context = mockContext;

        doNothing().when(dialog).showFailedDialog(anyInt());
        doNothing().when(dialog).continueSelectedDownload(any());

        mockStorage = mock(StoredDirectoryHelper.class);
        mockFile = mock(StoredFileHelper.class);
        mockUri = mock(Uri.class);

        when(mockFile.canWrite()).thenReturn(true);
        when(mockFile.existsAsFile()).thenReturn(false);

        when(mockStorage.mkdirs()).thenReturn(true);
        when(mockStorage.createFile(anyString(), anyString())).thenReturn(mockFile);
        when(mockStorage.getUri()).thenReturn(mockUri);
        when(mockStorage.getTag()).thenReturn("tag");
    }

    @AfterClass
    public static void printFinalReport() {
        ManualCoverage.printReport();
        double cov = ManualCoverage.getCoveragePercentage();

        // Write coverage report to file
        String projectDir = System.getProperty("user.dir");
        String reportPath = projectDir + "/manual_coverage.txt";
        ManualCoverage.writeReport(reportPath);
    }

    /** Test that the download succeeds normally when all arguments are provided correctly 
     * and environment is setup as expected */
    @Test
    public void testNormalDownload() {
        dialog.checkSelectedDownload(mockStorage, mockUri, "file.txt", "text/plain");

        verify(dialog, never()).showFailedDialog(anyInt());
        verify(dialog).continueSelectedDownload(any());
    }

    /** Test that a new file is created if TargetFile is null */
    @Test
    public void testTargetFileNull() {
        dialog.checkSelectedDownload(mockStorage, null, "file.txt", "text/plain");

        verify(dialog, org.mockito.Mockito.never()).showFailedDialog(any(int.class));
    }

}