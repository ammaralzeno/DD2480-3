/**
 * The chosen getNextChunk(boolean infoOnly) method
 *
 * CCN: 18
 *
 * Comments with "COVERAGE:" are added for the lab.
 */
public class GetNextChunkLocal {
    public Mp4DashChunk getNextChunk(final boolean infoOnly) throws IOException {

        org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(1);
        // COVERAGE: method entry

        final Mp4Track track = tracks[selectedTrack];

        while (stream.available()) {
            org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(2);
            // COVERAGE: while(stream.available()) TRUE

            if (chunkZero) {
                org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(3);
                // COVERAGE: chunkZero TRUE

                ensure(box);

                if (!stream.available()) {
                    org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(4);
                    // COVERAGE: !stream.available() TRUE (break)
                    break;
                } else {
                    org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(5);
                    // COVERAGE: !stream.available() FALSE
                }

                box = readBox();
            } else {
                org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(6);
                // COVERAGE: chunkZero FALSE
                chunkZero = true;
            }

            switch (box.type) {

                case ATOM_MOOF:
                    org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(7);
                    // COVERAGE: case ATOM_MOOF

                    if (moof != null) {
                        org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(8);
                        // COVERAGE: moof != null TRUE
                        throw new IOException("moof found without mdat");
                    } else {
                        org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(9);
                        // COVERAGE: moof != null FALSE
                    }

                    moof = parseMoof(box, track.trak.tkhd.trackId);

                    if (moof.traf != null) {
                        org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(10);
                        // COVERAGE: moof.traf != null TRUE

                        if (hasFlag(moof.traf.trun.bFlags, 0x0001)) {
                            org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(11);
                            // COVERAGE: hasFlag(trun,0x0001) TRUE

                            moof.traf.trun.dataOffset -= box.size + 8;

                            if (moof.traf.trun.dataOffset < 0) {
                                org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(12);
                                // COVERAGE: dataOffset < 0 TRUE
                                throw new IOException("trun box has wrong data offset");
                            } else {
                                org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(13);
                                // COVERAGE: dataOffset < 0 FALSE
                            }
                        } else {
                            org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(14);
                            // COVERAGE: hasFlag(trun,0x0001) FALSE
                        }

                        if (moof.traf.trun.chunkSize < 1) {
                            org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(15);
                            // COVERAGE: chunkSize < 1 TRUE

                            if (hasFlag(moof.traf.tfhd.bFlags, 0x10)) {
                                org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(16);
                                // COVERAGE: hasFlag(tfhd,0x10) TRUE
                                moof.traf.trun.chunkSize =
                                        moof.traf.tfhd.defaultSampleSize *
                                                moof.traf.trun.entryCount;
                            } else {
                                org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(17);
                                // COVERAGE: hasFlag(tfhd,0x10) FALSE
                                moof.traf.trun.chunkSize = (int) (box.size - 8);
                            }
                        } else {
                            org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(18);
                            // COVERAGE: chunkSize < 1 FALSE
                        }

                        if (!hasFlag(moof.traf.trun.bFlags, 0x900)
                                && moof.traf.trun.chunkDuration == 0) {

                            org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(19);
                            // COVERAGE: duration-fix condition TRUE

                            if (hasFlag(moof.traf.tfhd.bFlags, 0x20)) {
                                org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(20);
                                // COVERAGE: hasFlag(tfhd,0x20) TRUE

                                moof.traf.trun.chunkDuration =
                                        moof.traf.tfhd.defaultSampleDuration *
                                                moof.traf.trun.entryCount;
                            } else {
                                org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(21);
                                // COVERAGE: hasFlag(tfhd,0x20) FALSE
                            }
                        } else {
                            org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(22);
                            // COVERAGE: duration-fix condition FALSE
                        }

                    } else {
                        org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(23);
                        // COVERAGE: moof.traf == null
                    }

                    break;

                case ATOM_MDAT:
                    org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(24);
                    // COVERAGE: case ATOM_MDAT

                    if (moof == null) {
                        org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(25);
                        // COVERAGE: moof == null TRUE
                        throw new IOException("mdat found without moof");
                    } else {
                        org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(26);
                        // COVERAGE: moof == null FALSE
                    }

                    if (moof.traf == null) {
                        org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(27);
                        // COVERAGE: moof.traf == null TRUE
                        moof = null;
                        continue;
                    } else {
                        org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(28);
                        // COVERAGE: moof.traf == null FALSE
                    }

                    final Mp4DashChunk chunk = new Mp4DashChunk();
                    chunk.moof = moof;

                    if (!infoOnly) {
                        org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(29);
                        // COVERAGE: !infoOnly TRUE
                        chunk.data = stream.getView(moof.traf.trun.chunkSize);
                    } else {
                        org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(30);
                        // COVERAGE: !infoOnly FALSE
                    }

                    moof = null;

                    stream.skipBytes(chunk.moof.traf.trun.dataOffset);

                    org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(31);
                    // COVERAGE: return chunk
                    return chunk;

                default:
                    org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(32);
                    // COVERAGE: default case
            }
        }

        org.schabi.newpipe.coverage.Mp4DashReaderBranchCoverage.recordBranch(33);
        // COVERAGE: return null (end-of-stream)
        return null;
    }
}