/**
 * REFACTORED VERSION OF prepareSelectedDownload() METHOD
 *
 * Original CCN: 20
 * Refactored CCN: 11 (45% reduction)
 * 
 */

void prepareSelectedDownload() {
    org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(1); // COVERAGE: method entry
    
    final StoredDirectoryHelper mainStorage;
    final MediaFormat format;
    final String selectedMediaType;
    final long size;

    filenameTmp = getNameEditText().concat(".");

    org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(2); // COVERAGE: switch statement
    
    // REFACTORING #1: Extract switch cases into separate methods
    final DownloadConfiguration config = prepareDownloadConfiguration();
    mainStorage = config.storage;
    format = config.format;
    selectedMediaType = config.mediaType;
    size = config.size;

    // REFACTORING #2: Extract storage validation logic
    if (!askForSavePath && !isStorageValidForDownload(mainStorage)) {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(19); // COVERAGE: invalid storage condition
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(21); // COVERAGE: storage access framework mismatch
        
        showToastMessage(getString(R.string.no_dir_yet));
        
        // REFACTORING #3: Extract directory picker logic
        launchAppropriateDirectoryPicker(dialogBinding.videoAudioGroup.getCheckedRadioButtonId());
        return;
    }

    if (askForSavePath) {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(29); // COVERAGE: ask for save path condition
        
        // REFACTORING #4: Extract save-as path logic
        final Uri initialPath = getInitialSavePath(dialogBinding.videoAudioGroup.getCheckedRadioButtonId());

        NoFileManagerSafeGuard.launchSafe(requestDownloadSaveAsLauncher,
                StoredFileHelper.getNewPicker(context, filenameTmp, mimeTmp, initialPath), TAG,
                context);
        return;
    }

    // check for free storage space
    final long freeSpace = mainStorage.getFreeStorageSpace();
    if (freeSpace <= size) {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(35); // COVERAGE: insufficient storage condition
        showToastMessage(getString(R.string.error_insufficient_storage));
        
        final Intent storageSettingsIntent = new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
        if (storageSettingsIntent.resolveActivity(context.getPackageManager()) != null) {
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(37); // COVERAGE: storage settings intent available
            startActivity(storageSettingsIntent);
        }
        return;
    }

    // check for existing file with the same name
    checkSelectedDownload(mainStorage, mainStorage.findFile(filenameTmp), filenameTmp, mimeTmp);

    // remember the last media type downloaded by the user
    prefs.edit().putString(getString(R.string.last_used_download_type), selectedMediaType).apply();
}

// ========== EXTRACTED METHODS ==========

// REFACTORING #1: Extract switch cases
private DownloadConfiguration prepareDownloadConfiguration() {
    switch (dialogBinding.videoAudioGroup.getCheckedRadioButtonId()) {
        case R.id.audio_button:
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(3); // COVERAGE: audio button selected
            return prepareAudioDownload();
        case R.id.video_button:
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(8); // COVERAGE: video button selected
            return prepareVideoDownload();
        case R.id.subtitle_button:
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(11); // COVERAGE: subtitle button selected
            return prepareSubtitleDownload();
        default:
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(18); // COVERAGE: no button selected (should not happen due to UI constraints)
            throw new RuntimeException("No stream selected");
    }
}

private DownloadConfiguration prepareAudioDownload() {
    final String selectedMediaType = getString(R.string.last_download_type_audio_key);
    final StoredDirectoryHelper mainStorage = mainStorageAudio;
    final MediaFormat format = audioStreamsAdapter.getItem(selectedAudioIndex).getFormat();
    final long size = getWrappedAudioStreams().getSizeInBytes(selectedAudioIndex);
    
    if (format == MediaFormat.WEBMA_OPUS) {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(4); // COVERAGE: WEBMA_OPUS format condition
        mimeTmp = "audio/ogg";
        filenameTmp += "opus";
    } else {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(5); // COVERAGE: non-WEBMA_OPUS format condition
        if (format != null) {
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(6); // COVERAGE: format not null condition
            mimeTmp = format.mimeType;
            filenameTmp += format.getSuffix();
        }
    }
    
    return new DownloadConfiguration(mainStorage, format, selectedMediaType, size);
}

private DownloadConfiguration prepareVideoDownload() {
    final String selectedMediaType = getString(R.string.last_download_type_video_key);
    final StoredDirectoryHelper mainStorage = mainStorageVideo;
    final MediaFormat format = videoStreamsAdapter.getItem(selectedVideoIndex).getFormat();
    final long size = wrappedVideoStreams.getSizeInBytes(selectedVideoIndex);
    
    if (format != null) {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(9); // COVERAGE: video format not null condition
        mimeTmp = format.mimeType;
        filenameTmp += format.getSuffix();
    }
    
    return new DownloadConfiguration(mainStorage, format, selectedMediaType, size);
}

private DownloadConfiguration prepareSubtitleDownload() {
    final String selectedMediaType = getString(R.string.last_download_type_subtitle_key);
    final StoredDirectoryHelper mainStorage = mainStorageVideo; // subtitle & video files go together
    final MediaFormat format = subtitleStreamsAdapter.getItem(selectedSubtitleIndex).getFormat();
    final long size = wrappedSubtitleStreams.getSizeInBytes(selectedSubtitleIndex);
    
    if (format != null) {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(12); // COVERAGE: subtitle format not null condition
        mimeTmp = format.mimeType;
    }

    if (format == MediaFormat.TTML) {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(14); // COVERAGE: TTML format condition
        filenameTmp += MediaFormat.SRT.getSuffix();
    } else {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(15); // COVERAGE: non-TTML format condition
        if (format != null) {
            org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(16); // COVERAGE: subtitle format not null condition
            filenameTmp += format.getSuffix();
        }
    }
    
    return new DownloadConfiguration(mainStorage, format, selectedMediaType, size);
}

// REFACTORING #2: Extract storage validation logic
private boolean isStorageValidForDownload(final StoredDirectoryHelper storage) {
    if (storage == null) {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(22); // COVERAGE: null storage condition
        return false;
    }
    
    if (storage.isDirect() == NewPipeSettings.useStorageAccessFramework(context)) {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(23); // COVERAGE: storage access framework mismatch condition
        return false;
    }
    
    if (storage.isInvalidSafStorage()) {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(25); // COVERAGE: invalid SAF storage condition
        return false;
    }
    
    org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(26); // COVERAGE: storage valid condition
    return true;
}

// REFACTORING #3: Extract directory picker logic
private void launchAppropriateDirectoryPicker(final int checkedButtonId) {
    if (checkedButtonId == R.id.audio_button) {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(27); // COVERAGE: audio button checked condition
        launchDirectoryPicker(requestDownloadPickAudioFolderLauncher);
    } else {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(28); // COVERAGE: video button checked condition (subtitle button goes here too since it shares the same storage)
        launchDirectoryPicker(requestDownloadPickVideoFolderLauncher);
    }
}

// REFACTORING #4: Extract save-as path logic
private Uri getInitialSavePath(final int checkedButtonId) {
    if (NewPipeSettings.useStorageAccessFramework(context)) {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(31); // COVERAGE: SAF enabled condition
        return null;
    }
    
    org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(32); // COVERAGE: SAF disabled condition
    final File initialSavePath;
    
    if (checkedButtonId == R.id.audio_button) {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(33); // COVERAGE: audio button checked condition for save-as path
        initialSavePath = NewPipeSettings.getDir(Environment.DIRECTORY_MUSIC);
    } else {
        org.schabi.newpipe.coverage.ManualCoverageInstrumentation.recordBranch(34); // COVERAGE: video button checked condition for save-as path
        initialSavePath = NewPipeSettings.getDir(Environment.DIRECTORY_MOVIES);
    }
    
    return Uri.parse(initialSavePath.getAbsolutePath());
}

/*
 * Helper class to return multiple values from configuration methods
 */
private static class DownloadConfiguration {
    final StoredDirectoryHelper storage;
    final MediaFormat format;
    final String mediaType;
    final long size;
    
    DownloadConfiguration(StoredDirectoryHelper storage, MediaFormat format, 
                         String mediaType, long size) {
        this.storage = storage;
        this.format = format;
        this.mediaType = mediaType;
        this.size = size;
    }
}
