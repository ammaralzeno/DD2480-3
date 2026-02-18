/** Refactored (CCN ~5). Branch IDs: see manualCoverage.md. */

    /** Skip TORRENT or HLS+OPUS. */
    private static boolean shouldSkipAudioStream(AudioStream stream) {
        return stream.getDeliveryMethod() == DeliveryMethod.TORRENT
                || (stream.getDeliveryMethod() == DeliveryMethod.HLS
                && stream.getFormat() == MediaFormat.OPUS);
    }

    /** True if stream should replace presentStream. */
    private static boolean isBetterStream(AudioStream stream, AudioStream presentStream,
            Comparator<AudioStream> cmp) {
        return presentStream == null || cmp.compare(stream, presentStream) > 0;
    }

    /** Remove unknown track (key "") if multiple. */
    private static void removeUnknownTrackIfMultiple(HashMap<String, AudioStream> map) {
        if (map.size() > 1) {
            map.remove("");
        }
    }

    /** Filter streams; best per track. */
    public static List<AudioStream> getFilteredAudioStreams(
            @NonNull Context context,
            @Nullable List<AudioStream> audioStreams) {
        if (audioStreams == null) {
            return Collections.emptyList();
        }

        HashMap<String, AudioStream> collectedStreams = new HashMap<>();
        Comparator<AudioStream> cmp = getAudioFormatComparator(context);

        for (AudioStream stream : audioStreams) {
            if (shouldSkipAudioStream(stream)) {
                continue;
            }

            String trackId = Objects.toString(stream.getAudioTrackId(), "");
            AudioStream presentStream = collectedStreams.get(trackId);
            if (isBetterStream(stream, presentStream, cmp)) {
                collectedStreams.put(trackId, stream);
            }
        }

        removeUnknownTrackIfMultiple(collectedStreams);

        return collectedStreams.values().stream().sorted(getAudioTrackNameComparator())
                .collect(Collectors.toList());
    }
