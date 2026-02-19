package org.schabi.newpipe.streams;

import org.junit.Test;
import org.schabi.newpipe.streams.io.SharpStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Mp4DashReaderGetNextChunkTest {

    @Test
    public void returnsChunkInfoOnlyFalseComputesSizeAndDurationAndHasData() throws Exception {
        final int trackId = 1;

        final int tfhdFlags = 0x08 | 0x10 | 0x20;
        final int defaultSampleDuration = 5;
        final int defaultSampleSize = 4;
        final int defaultSampleFlags = 0;

        final int trunFlags = 0;
        final int entryCount = 3;

        final MoofMdatSpec spec = new MoofMdatSpec(
                trackId,
                tfhdFlags,
                defaultSampleDuration,
                defaultSampleSize,
                defaultSampleFlags,
                trunFlags,
                entryCount,
                false,
                0
        );

        final byte[] streamBytes = buildMoofThenMdat(spec);
        final Mp4DashReader reader = newReaderReadyForMoof(streamBytes, trackId);

        final Mp4DashReader.Mp4DashChunk chunk = reader.getNextChunk(false);

        assertNotNull(chunk);
        assertNotNull(chunk.moof);
        assertNotNull(chunk.moof.traf);
        assertNotNull(chunk.data);

        assertEquals(defaultSampleSize * entryCount, chunk.moof.traf.trun.chunkSize);
        assertEquals(defaultSampleDuration * entryCount, chunk.moof.traf.trun.chunkDuration);

        final byte[] read = readAll(chunk.data);
        assertEquals(defaultSampleSize * entryCount, read.length);

        final byte[] pattern = new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD};
        for (int i = 0; i < read.length; i++) {
            assertEquals(pattern[i % pattern.length], read[i]);
        }
    }

    @Test
    public void returnsChunkInfoOnlyTrueHasNullData() throws Exception {
        final int trackId = 1;

        final int tfhdFlags = 0x08 | 0x10 | 0x20;
        final int defaultSampleDuration = 7;
        final int defaultSampleSize = 2;
        final int defaultSampleFlags = 0;

        final int trunFlags = 0;
        final int entryCount = 4;

        final MoofMdatSpec spec = new MoofMdatSpec(
                trackId,
                tfhdFlags,
                defaultSampleDuration,
                defaultSampleSize,
                defaultSampleFlags,
                trunFlags,
                entryCount,
                false,
                0
        );

        final byte[] streamBytes = buildMoofThenMdat(spec);
        final Mp4DashReader reader = newReaderReadyForMoof(streamBytes, trackId);

        final Mp4DashReader.Mp4DashChunk chunk = reader.getNextChunk(true);

        assertNotNull(chunk);
        assertNotNull(chunk.moof);
        assertNotNull(chunk.moof.traf);
        assertNull(chunk.data);

        assertEquals(defaultSampleSize * entryCount, chunk.moof.traf.trun.chunkSize);
        assertEquals(defaultSampleDuration * entryCount, chunk.moof.traf.trun.chunkDuration);
    }

    @Test
    public void throwsIfMdatAppearsWithoutMoof() throws Exception {
        final int trackId = 1;
        final byte[] bytes = new byte[16];

        final Mp4DashReader reader = newReaderWithMinimalTrack(bytes, trackId);

        final Mp4DashReader.Box box = new Mp4DashReader.Box();
        box.type = atom("mdat");
        box.offset = 0;
        box.size = 16;

        setPrivate(reader, "box", box);
        setPrivate(reader, "moof", null);
        setPrivate(reader, "chunkZero", false);

        try {
            reader.getNextChunk(false);
            fail("Expected IOException");
        } catch (final IOException e) {
            assertTrue(e.getMessage().contains("mdat found without moof"));
        }
    }

    @Test
    public void throwsIfMoofAppearsButPreviousMoofNotClosedByMdat() throws Exception {
        final int trackId = 1;
        final byte[] bytes = new byte[16];

        final Mp4DashReader reader = newReaderWithMinimalTrack(bytes, trackId);

        final Mp4DashReader.Box box = new Mp4DashReader.Box();
        box.type = atom("moof");
        box.offset = 0;
        box.size = 16;

        setPrivate(reader, "box", box);
        setPrivate(reader, "chunkZero", false);
        setPrivate(reader, "moof", new Mp4DashReader.Moof());

        try {
            reader.getNextChunk(false);
            fail("Expected IOException");
        } catch (final IOException e) {
            assertTrue(e.getMessage().contains("moof found without mdat"));
        }
    }

    @Test
    public void returnsNullWhenMoofHasNoMatchingTrafAndMdatIsSkipped() throws Exception {
        final int selectedTrackId = 1;
        final int tfhdTrackIdInsideFile = 999;

        final MoofMdatSpec spec = new MoofMdatSpec(
                tfhdTrackIdInsideFile,
                0,
                0,
                0,
                0,
                0,
                1,
                false,
                0
        );

        final byte[] streamBytes = buildMoofThenMdat(spec);
        final Mp4DashReader reader = newReaderReadyForMoof(streamBytes, selectedTrackId);

        final Mp4DashReader.Mp4DashChunk chunk = reader.getNextChunk(false);
        assertNull(chunk);
    }

    @Test
    public void throwsIfTrunDataOffsetBecomesNegativeAfterAdjustment() throws Exception {
        final int trackId = 1;

        final MoofMdatSpec spec = new MoofMdatSpec(
                trackId,
                0,
                0,
                0,
                0,
                0x0001,
                1,
                true,
                0
        );

        final byte[] streamBytes = buildMoofThenMdat(spec);
        final Mp4DashReader reader = newReaderReadyForMoof(streamBytes, trackId);

        try {
            reader.getNextChunk(false);
            fail("Expected IOException");
        } catch (final IOException e) {
            assertTrue(e.getMessage().contains("trun box has wrong data offset"));
        }
    }

    private static Mp4DashReader newReaderReadyForMoof(final byte[] bytes,
                                                       final int selectedTrackId)
            throws Exception {
        final Mp4DashReader reader = newReaderWithMinimalTrack(bytes, selectedTrackId);

        final int moofSize = readUInt32(bytes, 0);
        final Mp4DashReader.Box box = new Mp4DashReader.Box();
        box.type = atom("moof");
        box.offset = 0;
        box.size = moofSize;

        setPrivate(reader, "box", box);
        setPrivate(reader, "chunkZero", false);
        setPrivate(reader, "moof", null);

        final DataReader dr = (DataReader) getPrivate(reader, "stream");
        dr.skipBytes(8);

        return reader;
    }

    private static Mp4DashReader newReaderWithMinimalTrack(final byte[] bytes,
                                                           final int trackId)
            throws Exception {
        final Mp4DashReader reader = new Mp4DashReader(new ByteArraySharpStream(bytes));

        final Mp4DashReader.Tkhd tkhd = new Mp4DashReader.Tkhd();
        tkhd.trackId = trackId;

        final Mp4DashReader.Trak trak = new Mp4DashReader.Trak();
        trak.tkhd = tkhd;

        final Mp4DashReader.Mp4Track t = new Mp4DashReader.Mp4Track();
        t.trak = trak;

        final Mp4DashReader.Mp4Track[] tracks = new Mp4DashReader.Mp4Track[]{t};
        setPrivate(reader, "tracks", tracks);

        reader.selectTrack(0);
        return reader;
    }

    private static byte[] buildMoofThenMdat(final MoofMdatSpec spec) throws IOException {
        final byte[] mfhd = box("mfhd", payloadMfhd(1));

        final byte[] tfhd = box(
                "tfhd",
                payloadTfhd(
                        spec.tfhdFlags,
                        spec.tfhdTrackId,
                        spec.defaultSampleDuration,
                        spec.defaultSampleSize,
                        spec.defaultSampleFlags
                )
        );

        final byte[] trun = box(
                "trun",
                payloadTrun(
                        spec.trunFlags,
                        spec.entryCount,
                        spec.dataOffsetPresent,
                        spec.dataOffset
                )
        );

        final byte[] traf = box("traf", concat(tfhd, trun));
        final byte[] moof = box("moof", concat(mfhd, traf));

        final int minLen = 16;
        final int desired = spec.defaultSampleSize * Math.max(spec.entryCount, 1);
        final int mdatPayloadLen = Math.max(minLen, desired);

        final byte[] mdatPayload = new byte[mdatPayloadLen];
        final byte[] pattern = new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD};
        for (int i = 0; i < mdatPayload.length; i++) {
            mdatPayload[i] = pattern[i % pattern.length];
        }

        final byte[] mdat = box("mdat", mdatPayload);
        return concat(moof, mdat);
    }

    private static byte[] payloadMfhd(final int sequence) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeInt(out, 0);
        writeInt(out, sequence);
        return out.toByteArray();
    }

    private static byte[] payloadTfhd(final int tfhdFlags,
                                      final int trackId,
                                      final int defaultSampleDuration,
                                      final int defaultSampleSize,
                                      final int defaultSampleFlags)
            throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeInt(out, tfhdFlags);
        writeInt(out, trackId);

        if ((tfhdFlags & 0x01) == 0x01) {
            writeLong(out, 0L);
        }
        if ((tfhdFlags & 0x02) == 0x02) {
            writeInt(out, 0);
        }
        if ((tfhdFlags & 0x08) == 0x08) {
            writeInt(out, defaultSampleDuration);
        }
        if ((tfhdFlags & 0x10) == 0x10) {
            writeInt(out, defaultSampleSize);
        }
        if ((tfhdFlags & 0x20) == 0x20) {
            writeInt(out, defaultSampleFlags);
        }

        return out.toByteArray();
    }

    private static byte[] payloadTrun(final int trunFlags,
                                      final int entryCount,
                                      final boolean dataOffsetPresent,
                                      final int dataOffset)
            throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeInt(out, trunFlags);
        writeInt(out, entryCount);

        if (dataOffsetPresent && (trunFlags & 0x0001) == 0x0001) {
            writeInt(out, dataOffset);
        }
        if ((trunFlags & 0x0004) == 0x0004) {
            writeInt(out, 0);
        }

        return out.toByteArray();
    }

    private static byte[] box(final String type, final byte[] payload) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeInt(out, 8 + payload.length);
        writeInt(out, atom(type));
        out.write(payload);
        return out.toByteArray();
    }

    private static byte[] concat(final byte[] a, final byte[] b) {
        final byte[] r = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }

    private static int atom(final String fourcc) {
        if (fourcc.length() != 4) {
            throw new IllegalArgumentException("fourcc must be 4 chars");
        }
        return (fourcc.charAt(0) << 24)
                | (fourcc.charAt(1) << 16)
                | (fourcc.charAt(2) << 8)
                | (fourcc.charAt(3));
    }

    private static int readUInt32(final byte[] bytes, final int off) {
        return ((bytes[off] & 0xFF) << 24)
                | ((bytes[off + 1] & 0xFF) << 16)
                | ((bytes[off + 2] & 0xFF) << 8)
                | (bytes[off + 3] & 0xFF);
    }

    private static void writeInt(final ByteArrayOutputStream out, final int v) {
        out.write((v >>> 24) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>> 8) & 0xFF);
        out.write(v & 0xFF);
    }

    private static void writeLong(final ByteArrayOutputStream out, final long v) {
        out.write((int) ((v >>> 56) & 0xFF));
        out.write((int) ((v >>> 48) & 0xFF));
        out.write((int) ((v >>> 40) & 0xFF));
        out.write((int) ((v >>> 32) & 0xFF));
        out.write((int) ((v >>> 24) & 0xFF));
        out.write((int) ((v >>> 16) & 0xFF));
        out.write((int) ((v >>> 8) & 0xFF));
        out.write((int) (v & 0xFF));
    }

    private static byte[] readAll(final InputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] buf = new byte[1024];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }

    private static Object getPrivate(final Object target, final String fieldName) throws Exception {
        final Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(target);
    }

    private static void setPrivate(final Object target,
                                   final String fieldName,
                                   final Object value)
            throws Exception {
        final Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static final class MoofMdatSpec {
        private final int tfhdTrackId;
        private final int tfhdFlags;
        private final int defaultSampleDuration;
        private final int defaultSampleSize;
        private final int defaultSampleFlags;
        private final int trunFlags;
        private final int entryCount;
        private final boolean dataOffsetPresent;
        private final int dataOffset;

        private MoofMdatSpec(final int tfhdTrackId,
                             final int tfhdFlags,
                             final int defaultSampleDuration,
                             final int defaultSampleSize,
                             final int defaultSampleFlags,
                             final int trunFlags,
                             final int entryCount,
                             final boolean dataOffsetPresent,
                             final int dataOffset) {
            this.tfhdTrackId = tfhdTrackId;
            this.tfhdFlags = tfhdFlags;
            this.defaultSampleDuration = defaultSampleDuration;
            this.defaultSampleSize = defaultSampleSize;
            this.defaultSampleFlags = defaultSampleFlags;
            this.trunFlags = trunFlags;
            this.entryCount = entryCount;
            this.dataOffsetPresent = dataOffsetPresent;
            this.dataOffset = dataOffset;
        }
    }

    private static final class ByteArraySharpStream extends SharpStream {
        private final byte[] data;
        private int pos;
        private boolean closed;

        private ByteArraySharpStream(final byte[] data) {
            this.data = data;
            this.pos = 0;
            this.closed = false;
        }

        @Override
        public int read() throws IOException {
            ensureOpen();
            if (pos >= data.length) {
                return -1;
            }
            return data[pos++] & 0xFF;
        }

        @Override
        public int read(final byte[] buffer) throws IOException {
            return read(buffer, 0, buffer.length);
        }

        @Override
        public int read(final byte[] buffer, final int offset, final int count) throws IOException {
            ensureOpen();
            if (count == 0) {
                return 0;
            }
            if (pos >= data.length) {
                return -1;
            }

            final int n = Math.min(count, data.length - pos);
            System.arraycopy(data, pos, buffer, offset, n);
            pos += n;
            return n;
        }

        @Override
        public long skip(final long amount) throws IOException {
            ensureOpen();
            if (amount <= 0) {
                return 0;
            }
            final int n = (int) Math.min(amount, (long) data.length - pos);
            pos += n;
            return n;
        }

        @Override
        public long available() {
            if (closed) {
                return 0;
            }
            return Math.max(0, data.length - pos);
        }

        @Override
        public void rewind() throws IOException {
            ensureOpen();
            pos = 0;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        @Override
        public void close() {
            closed = true;
        }

        @Override
        public boolean canRewind() {
            return true;
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public boolean canWrite() {
            return false;
        }

        @Override
        public void write(final byte value) throws IOException {
            throw new IOException("read-only stream");
        }

        @Override
        public void write(final byte[] buffer) throws IOException {
            throw new IOException("read-only stream");
        }

        @Override
        public void write(final byte[] buffer, final int offset, final int count)
                throws IOException {
            throw new IOException("read-only stream");
        }

        private void ensureOpen() throws IOException {
            if (closed) {
                throw new IOException("stream closed");
            }
        }
    }
}
