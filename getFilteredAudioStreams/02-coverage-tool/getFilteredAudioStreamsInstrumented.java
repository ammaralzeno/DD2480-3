
    public static List<AudioStream> getFilteredAudioStreams(
            @NonNull Context context,
            @Nullable List<AudioStream> audioStreams) {
        if (audioStreams == null) {
            manualCoverage.hit(1);                      //manualCoverage
            return Collections.emptyList();
        }
        manualCoverage.hit(2);                          //manualCoverage

        HashMap<String, AudioStream> collectedStreams = new HashMap<>();
        Comparator<AudioStream> cmp = getAudioFormatComparator(context);

        for (AudioStream stream : audioStreams) {
            if (stream.getDeliveryMethod() == DeliveryMethod.TORRENT
                    || (stream.getDeliveryMethod() == DeliveryMethod.HLS
                    && stream.getFormat() == MediaFormat.OPUS)) {
                manualCoverage.hit(3);                  //manualCoverage
                continue;
            }
            manualCoverage.hit(4);                      //manualCoverage

            String trackId = Objects.toString(stream.getAudioTrackId(), "");
            AudioStream presentStream = collectedStreams.get(trackId);
            if (presentStream == null || cmp.compare(stream, presentStream) > 0) {
                manualCoverage.hit(5);                  //manualCoverage
                collectedStreams.put(trackId, stream);
            } else {
                manualCoverage.hit(6);                  //manualCoverage  
            }
        }

        if (collectedStreams.size() > 1) {
            manualCoverage.hit(7);                      //manualCoverage
            collectedStreams.remove("");
        } else {
            manualCoverage.hit(8);                      //manualCoverage
        }

        return collectedStreams.values().stream().sorted(getAudioTrackNameComparator())
                .collect(Collectors.toList());
    }
