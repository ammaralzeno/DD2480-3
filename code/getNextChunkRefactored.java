public class getNextChunkRefactored {

    public Mp4DashChunk getNextChunk(final boolean infoOnly) throws IOException {
    final Mp4Track track = tracks[selectedTrack];

    while (stream.available()) {
        if (!advanceToNextBox()) {
            break;
        }

        final Mp4DashChunk chunk = processCurrentBox(track, infoOnly);
        if (chunk != null) {
            return chunk;
        }
    }

    return null;
}

    private boolean advanceToNextBox() throws IOException {
        if (!stream.available()) {
            return false;
        }

        if (chunkZero) {
            ensure(box);

            if (!stream.available()) {
                return false;
            }

            box = readBox();
        } else {
            chunkZero = true;
        }

        return true;
    }

    private Mp4DashChunk processCurrentBox(final Mp4Track track, final boolean infoOnly)
            throws IOException {
        if (box.type == ATOM_MOOF) {
            handleMoofBox(track);
            return null;
        }

        if (box.type == ATOM_MDAT) {
            return handleMdatBox(infoOnly);
        }

        // default: do nothing; the next loop iteration will ensure(box) and move on
        return null;
    }

    private void handleMoofBox(final Mp4Track track) throws IOException {
        if (moof != null) {
            throw new IOException("moof found without mdat");
        }

        moof = parseMoof(box, track.trak.tkhd.trackId);

        if (moof.traf != null) {
            normalizeTrafAfterParse(moof.traf, box.size);
        }
    }

    private void normalizeTrafAfterParse(final Traf traf, final long moofBoxSize)
            throws IOException {
        adjustDataOffsetIfPresent(traf.trun, moofBoxSize);
        computeChunkSizeIfMissing(traf.trun, traf.tfhd, moofBoxSize);
        computeChunkDurationIfMissing(traf.trun, traf.tfhd);
    }

    private void adjustDataOffsetIfPresent(final Trun trun, final long moofBoxSize)
            throws IOException {
        if (hasFlag(trun.bFlags, 0x0001)) {
            // keep same semantics as original (compound assignment with long RHS)
            trun.dataOffset -= moofBoxSize + 8;

            if (trun.dataOffset < 0) {
                throw new IOException(
                        "trun box has wrong data offset, points outside of concurrent mdat box");
            }
        }
    }

    private void computeChunkSizeIfMissing(final Trun trun, final Tfhd tfhd, final long moofBoxSize) {
        if (trun.chunkSize < 1) {
            if (hasFlag(tfhd.bFlags, 0x10)) {
                trun.chunkSize = tfhd.defaultSampleSize * trun.entryCount;
            } else {
                trun.chunkSize = (int) (moofBoxSize - 8);
            }
        }
    }

    private void computeChunkDurationIfMissing(final Trun trun, final Tfhd tfhd) {
        if (!hasFlag(trun.bFlags, 0x900) && trun.chunkDuration == 0) {
            if (hasFlag(tfhd.bFlags, 0x20)) {
                trun.chunkDuration = tfhd.defaultSampleDuration * trun.entryCount;
            }
        }
    }

    private Mp4DashChunk handleMdatBox(final boolean infoOnly) throws IOException {
        if (moof == null) {
            throw new IOException("mdat found without moof");
        }

        if (moof.traf == null) {
            moof = null;
            return null; // original code did: continue;
        }

        final Mp4DashChunk chunk = new Mp4DashChunk();
        chunk.moof = moof;

        if (!infoOnly) {
            chunk.data = stream.getView(moof.traf.trun.chunkSize);
        }

        moof = null;

        stream.skipBytes(chunk.moof.traf.trun.dataOffset);

        return chunk;
    }
}
