import java.io.File;
import java.io.IOException;

import org.schabi.newpipe.error.ErrorInfo;
import org.schabi.newpipe.error.ErrorUtil;
import org.schabi.newpipe.error.UserAction;
import org.schabi.newpipe.settings.NewPipeSettings;
import org.schabi.newpipe.streams.io.NoFileManagerSafeGuard;
import org.schabi.newpipe.streams.io.StoredDirectoryHelper;
import org.schabi.newpipe.streams.io.StoredFileHelper;

import us.shandian.giga.service.MissionState;


/**
 * The chosen checkSelectedDownload() method
 *
 * CCN: 25
 * 
 * Comments with "COVERAGE:" are the ones added for the lab.
 */
// COVERAGE: changed from private for testing purposes
void checkSelectedDownload(final StoredDirectoryHelper mainStorage,
                           final Uri targetFile,
                           final String filename,
                           final String mime) {
    org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(1); // COVERAGE: method entry

    StoredFileHelper storage;

    try {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(2); // COVERAGE: try reaching storage
        if (mainStorage == null) {
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(3); // COVERAGE:if: mainStorage == null
            // using SAF on older android version
            storage = new StoredFileHelper(context, null, targetFile, "");
        } else if (targetFile == null) {
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(4); // COVERAGE: else if: targetFile == null
            // the file does not exist, but it is probably used in a pending download
            storage = new StoredFileHelper(mainStorage.getUri(), filename, mime,
                    mainStorage.getTag());
        } else {
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(5); // COVERAGE: else if: targetFile == null
            // the target filename is already use, attempt to use it
            storage = new StoredFileHelper(context, mainStorage.getUri(), targetFile,
                    mainStorage.getTag());
        }
    } catch (final Exception e) {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(6); // COVERAGE: catch exception
        ErrorUtil.createNotification(requireContext(),
                new ErrorInfo(e, UserAction.DOWNLOAD_FAILED, "Getting storage"));
        return;
    }

    // get state of potential mission referring to the same file
    final MissionState state = downloadManager.checkForExistingMission(storage);
    @StringRes final int msgBtn;
    @StringRes final int msgBody;

    org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(7); // COVERAGE: switch: state
    // this switch checks if there is already a mission referring to the same file
    switch (state) {
        case Finished: // there is already a finished mission
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(8); // COVERAGE: case Finished
            msgBtn = R.string.overwrite;
            msgBody = R.string.overwrite_finished_warning;
            break;
        case Pending:
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(9); // COVERAGE: case Pending
            msgBtn = R.string.overwrite;
            msgBody = R.string.download_already_pending;
            break;
        case PendingRunning:
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(10); // COVERAGE: case PendingRunning
            msgBtn = R.string.generate_unique_name;
            msgBody = R.string.download_already_running;
            break;
        case None: // there is no mission referring to the same file
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(11); // COVERAGE: case None
            if (mainStorage == null) {
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(12); // COVERAGE: if: mainStorage == null
                // This part is called if:
                // * using SAF on older android version
                // * save path not defined
                // * if the file exists overwrite it, is not necessary ask
                if (!storage.existsAsFile() && !storage.create()) {
                    org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(13); // COVERAGE: if: !storage.existsAsFile() && !storage.create() (true)
                    showFailedDialog(R.string.error_file_creation);
                    return;
                }
                continueSelectedDownload(storage);
                return;
            } else if (targetFile == null) {
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(15); // COVERAGE: else if: targetFile == null (true)
                // This part is called if:
                // * the filename is not used in a pending/finished download
                // * the file does not exists, create

                if (!mainStorage.mkdirs()) {
                    org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(17); // COVERAGE: if: !mainStorage.mkdirs() (true) 
                    showFailedDialog(R.string.error_path_creation);
                    return;
                }

                storage = mainStorage.createFile(filename, mime);
                if (storage == null || !storage.canWrite()) {
                    org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(19); // COVERAGE: if: storage == null || !storage.canWrite() (true)
                    showFailedDialog(R.string.error_file_creation);
                    return;
                }

                continueSelectedDownload(storage);
                return;
            }
            msgBtn = R.string.overwrite;
            msgBody = R.string.overwrite_unrelated_warning;
            break;
        default:
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(21); // COVERAGE: default case
            return; // unreachable
    }

    final AlertDialog.Builder askDialog = new AlertDialog.Builder(context)
            .setTitle(R.string.download_dialog_title)
            .setMessage(msgBody)
            .setNegativeButton(R.string.cancel, null);
    final StoredFileHelper finalStorage = storage;


    if (mainStorage == null) {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(22); // COVERAGE: if: mainStorage == null (true)
        // This part is called if:
        // * using SAF on older android version
        // * save path not defined
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(24); // COVERAGE: switch: state
        switch (state) {
            case Pending:
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(25); // COVERAGE: case Pending
            case Finished:
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(26); // COVERAGE: case Finished
                askDialog.setPositiveButton(msgBtn, (dialog, which) -> {
                    dialog.dismiss();
                    downloadManager.forgetMission(finalStorage);
                    continueSelectedDownload(finalStorage);
                });
                break;
        }

        askDialog.show();
        return;
    }

    askDialog.setPositiveButton(msgBtn, (dialog, which) -> {
        dialog.dismiss();

        StoredFileHelper storageNew;
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(27); // COVERAGE: switch: state
        switch (state) {
            case Finished:
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(28); // COVERAGE: case Finished 
            case Pending:
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(29); // COVERAGE: case Pending
                downloadManager.forgetMission(finalStorage);
            case None:
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(30); // COVERAGE: case None
                if (targetFile == null) {
                    org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(31); // COVERAGE: if: targetFile == null (true)
                    storageNew = mainStorage.createFile(filename, mime);
                } else {
                    org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(32); // COVERAGE: if: targetFile == null (false)
                    try {
                        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(33); // COVERAGE: try: new storage
                        // try take (or steal) the file
                        storageNew = new StoredFileHelper(context, mainStorage.getUri(),
                                targetFile, mainStorage.getTag());
                    } catch (final IOException e) {
                        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(34); // COVERAGE: catch exceptions
                        Log.e(TAG, "Failed to take (or steal) the file in "
                                + targetFile.toString());
                        storageNew = null;
                    }
                }

                if (storageNew != null && storageNew.canWrite()) {
                    org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(35); // COVERAGE: if: storageNew != null && storageNew.canWrite() (true)
                    continueSelectedDownload(storageNew);
                } else {
                    org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(36); // COVERAGE: if: storageNew != null && storageNew.canWrite() (false)
                    showFailedDialog(R.string.error_file_creation);
                }
                break;
            case PendingRunning:
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(37); // COVERAGE: case PendingRunning
                storageNew = mainStorage.createUniqueFile(filename, mime);
                if (storageNew == null) {
                    org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(38); // COVERAGE: if: storageNew == null (true)
                    showFailedDialog(R.string.error_file_creation);
                } else {
                    org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(39); // COVERAGE: if: storageNew == null (false)
                    continueSelectedDownload(storageNew);
                }
                break;
        }
    });

    askDialog.show();
}
