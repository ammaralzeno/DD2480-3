/**
 * The chosen prepareSelectedDownload() method
 *
 * CCN: 20
 * 
 * Comments with "COVERAGE:" are the ones added for the lab.
 */

// COVERAGE: changed from private for testing purposes
void prepareSelectedDownload() {
    org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(1); // COVERAGE: method entry
    
    final StoredDirectoryHelper mainStorage;
    final MediaFormat format;
    final String selectedMediaType;
    final long size;

    // first, build the filename and get the output folder (if possible)
    // later, run a very very very large file checking logic

    filenameTmp = getNameEditText().concat(".");

    org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(2); // COVERAGE: switch statement
    switch (dialogBinding.videoAudioGroup.getCheckedRadioButtonId()) {
        case R.id.audio_button:
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(3); // COVERAGE: audio_button case
            selectedMediaType = getString(R.string.last_download_type_audio_key);
            mainStorage = mainStorageAudio;
            format = audioStreamsAdapter.getItem(selectedAudioIndex).getFormat();
            size = getWrappedAudioStreams().getSizeInBytes(selectedAudioIndex);
            if (format == MediaFormat.WEBMA_OPUS) {
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(4); // COVERAGE: WEBMA_OPUS true
                mimeTmp = "audio/ogg";
                filenameTmp += "opus";
            } else {
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(5); // COVERAGE: WEBMA_OPUS false
                if (format != null) {
                    org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(6); // COVERAGE: format != null true
                    mimeTmp = format.mimeType;
                    filenameTmp += format.getSuffix();
                } else {
                    org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(7); // COVERAGE: format != null false
                }
            }
            break;
        case R.id.video_button:
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(8); // COVERAGE: video_button case
            selectedMediaType = getString(R.string.last_download_type_video_key);
            mainStorage = mainStorageVideo;
            format = videoStreamsAdapter.getItem(selectedVideoIndex).getFormat();
            size = wrappedVideoStreams.getSizeInBytes(selectedVideoIndex);
            if (format != null) {
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(9); // COVERAGE: format != null true
                mimeTmp = format.mimeType;
                filenameTmp += format.getSuffix();
            } else {
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(10); // COVERAGE: format != null false
            }
            break;
        case R.id.subtitle_button:
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(11); // COVERAGE: subtitle_button case
            selectedMediaType = getString(R.string.last_download_type_subtitle_key);
            mainStorage = mainStorageVideo; // subtitle & video files go together
            format = subtitleStreamsAdapter.getItem(selectedSubtitleIndex).getFormat();
            size = wrappedSubtitleStreams.getSizeInBytes(selectedSubtitleIndex);
            if (format != null) {
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(12); // COVERAGE: format != null true (mime)
                mimeTmp = format.mimeType;
            } else {
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(13); // COVERAGE: format != null false (mime)
            }

            if (format == MediaFormat.TTML) {
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(14); // COVERAGE: TTML true
                filenameTmp += MediaFormat.SRT.getSuffix();
            } else {
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(15); // COVERAGE:     TTML false
                if (format != null) {
                    org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(16); // COVERAGE: format != null true (suffix)
                    filenameTmp += format.getSuffix();
                } else {
                    org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(17); // COVERAGE: format != null false (suffix)
                }
            }
            break;
        default:
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(18); // COVERAGE: default case
                throw new RuntimeException("No stream selected");
    }

    if (!askForSavePath) {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(19); // COVERAGE: !askForSavePath true
        if (mainStorage == null
                || mainStorage.isDirect() == NewPipeSettings.useStorageAccessFramework(context)
                || mainStorage.isInvalidSafStorage()) {
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(21); // COVERAGE: compound condition true
            // Track individual OR conditions
            if (mainStorage == null) {
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(22); // COVERAGE: mainStorage == null
            }
            if (mainStorage != null && mainStorage.isDirect() == NewPipeSettings.useStorageAccessFramework(context)) {
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(23); // COVERAGE: isDirect check
            }
            if (mainStorage != null && mainStorage.isInvalidSafStorage()) {
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(25); // COVERAGE: isInvalidSafStorage
            }
                
            // Pick new download folder if one of:
            // - Download folder is not set
            // - Download folder uses SAF while SAF is disabled
            // - Download folder doesn't use SAF while SAF is enabled
            // - Download folder uses SAF but the user manually revoked access to it
            Toast.makeText(context, getString(R.string.no_dir_yet),
                    Toast.LENGTH_LONG).show();

            if (dialogBinding.videoAudioGroup.getCheckedRadioButtonId() == R.id.audio_button) {
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(27); // COVERAGE: audio_button picker true
                launchDirectoryPicker(requestDownloadPickAudioFolderLauncher);
            } else {
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(28); // COVERAGE: audio_button picker false
                launchDirectoryPicker(requestDownloadPickVideoFolderLauncher);
            }
            return;
        } else {
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(26); // COVERAGE: compound condition false
        }
    } else {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(20); // COVERAGE: !askForSavePath false
    }

    if (askForSavePath) {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(29); // COVERAGE: askForSavePath true
        final Uri initialPath;
        if (NewPipeSettings.useStorageAccessFramework(context)) {
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(31); // COVERAGE: useStorageAccessFramework true
            initialPath = null;
        } else {
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(32); // COVERAGE: useStorageAccessFramework false
            final File initialSavePath;
            if (dialogBinding.videoAudioGroup.getCheckedRadioButtonId() == R.id.audio_button) {
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(33); // COVERAGE: audio_button path true
                initialSavePath = NewPipeSettings.getDir(Environment.DIRECTORY_MUSIC);
            } else {
                org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(34); // COVERAGE: audio_button path false
                initialSavePath = NewPipeSettings.getDir(Environment.DIRECTORY_MOVIES);
            }
            initialPath = Uri.parse(initialSavePath.getAbsolutePath());
        }

        NoFileManagerSafeGuard.launchSafe(requestDownloadSaveAsLauncher,
                StoredFileHelper.getNewPicker(context, filenameTmp, mimeTmp, initialPath), TAG,
                context);

        return;
    } else {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(30); // COVERAGE: askForSavePath false
    }

    // Check for free storage space
    final long freeSpace = mainStorage.getFreeStorageSpace();
    if (freeSpace <= size) {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(35); // COVERAGE: freeSpace <= size true
        Toast.makeText(context, getString(R.
                string.error_insufficient_storage), Toast.LENGTH_LONG).show();
        // move the user to storage setting tab
        final Intent storageSettingsIntent = new Intent(Settings.
                ACTION_INTERNAL_STORAGE_SETTINGS);
        if (storageSettingsIntent.resolveActivity(context.getPackageManager())
            != null) {
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(37); // COVERAGE: resolveActivity != null true
            startActivity(storageSettingsIntent);
        } else {
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(38); // COVERAGE: resolveActivity != null false
        }
        return;
    } else {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(36); // COVERAGE: freeSpace <= size false
    }
    // check for existing file with the same name
    checkSelectedDownload(mainStorage, mainStorage.findFile(filenameTmp), filenameTmp,
        mimeTmp);

    // remember the last media type downloaded by the user
    prefs.edit().putString(getString(R.string.last_used_download_type), selectedMediaType)
        .apply();
    }
