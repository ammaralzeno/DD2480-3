/**
     * Filter the list of audio streams and return a list with the preferred stream for
     * each audio track. Streams are sorted with the preferred language in the first position.
     *
     * @param context      the context to search for the track to give preference
     * @param audioStreams the list of audio streams
     * @return the sorted, filtered list
     */
    public static List<AudioStream> getFilteredAudioStreams(
            @NonNull final Context context,
            @Nullable final List<AudioStream> audioStreams) {
        if (audioStreams == null) {
            return Collections.emptyList();
        }

        final HashMap<String, AudioStream> collectedStreams = new HashMap<>();

        final Comparator<AudioStream> cmp = getAudioFormatComparator(context);

        for (final AudioStream stream : audioStreams) {
            if (stream.getDeliveryMethod() == DeliveryMethod.TORRENT
                    || (stream.getDeliveryMethod() == DeliveryMethod.HLS
                    && stream.getFormat() == MediaFormat.OPUS)) {
                continue;
            }

            final String trackId = Objects.toString(stream.getAudioTrackId(), "");

            final AudioStream presentStream = collectedStreams.get(trackId);
            if (presentStream == null || cmp.compare(stream, presentStream) > 0) {
                collectedStreams.put(trackId, stream);
            }
        }

        // Filter unknown audio tracks if there are multiple tracks
        if (collectedStreams.size() > 1) {
            collectedStreams.remove("");
        }

        // Sort collected streams by name
        return collectedStreams.values().stream().sorted(getAudioTrackNameComparator())
                .collect(Collectors.toList());
    }