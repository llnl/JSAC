/*-
 * #%L
 * Java Seismic Analysis Code (JSAC)
 *  LLNL-CODE-855505
 *  This work was performed under the auspices of the U.S. Department of Energy
 *  by Lawrence Livermore National Laboratory under Contract DE-AC52-07NA27344.
 * %%
 * Copyright (C) 2022 - 2023 Lawrence Livermore National Laboratory
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package gov.llnl.gnem.jsac.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import gov.llnl.gnem.jsac.io.enums.FileType;

public class SACFileReader {

    private final SACHeaderIO headerIO;
    private final SACHeader header;
    private final FileChannel fileChannel;

    public SACFileReader(Path path) throws IOException {
        fileChannel = FileChannel.open(path, StandardOpenOption.READ);
        headerIO = new SACHeaderIO(fileChannel);
        header = headerIO.read();
    }

    public static SACFile readFile(Path path) throws IOException {

        SACFile retval = null;

        try ( FileChannel fc = FileChannel.open(path, StandardOpenOption.READ)) {

            SACHeaderIO io = new SACHeaderIO(fc);
            SACHeader h = io.read();

            int npts = h.getNpts();

            switch (h.getIftype()) {
                case ITIME: {
                    if (h.isEvenlySampled()) {
                        float[] v = read(h, fc, io.getByteOrder(), npts);
                        return new SACFile(h, v);
                    } else {
                        return buildFromXYData(h, fc, io, npts, true);
                    }
                }
                case IGRP:
                case IREAL:
                    float[] v = read(h, fc, io.getByteOrder(), npts);
                    retval = new SACFile(h, v);
                    break;
                case IRLIM:
                case IAMPH:
                    return buildFromXYData(h, fc, io, npts, false);
                case IXY:
                    return buildFromXYData(h, fc, io, npts, true);
                default:
                    throw new IllegalStateException("Unsupported IFTYPE: " + h.getIftype());
            }

        }

        return retval;
    }

    private static SACFile buildFromXYData(SACHeader h, final FileChannel fc, SACHeaderIO io, int npts, boolean isXY) throws IOException {
        float[] tmp = read(h, fc, io.getByteOrder(), 2 * npts);
        if (h.isEvenlySampled() && tmp.length == npts && h.getIftype() == FileType.IXY) {
            return new SACFile(h, tmp);
        } else if (tmp.length == 2 * npts) {
            float[] v1 = new float[npts];
            float[] v2 = new float[npts];
            System.arraycopy(tmp, 0, v1, 0, npts);
            System.arraycopy(tmp, npts, v2, 0, npts);
            if (isXY) {
                if (h.getDelta() != null) {
                    boolean fixedDelta = isFixedDelta(v2);
                    if (!fixedDelta) {
                        h.setDelta(null);
                    }
                }
            }
            return new SACFile(h, v1, v2);
        } else {
            throw new IllegalStateException("Error reading XY-data: Retrieved data is not equal to NPTS in length.");
        }
    }

    public static SACFileReader fromStringPath(String filePath) throws IOException {
        return new SACFileReader(Paths.get(filePath));
    }

    public SACHeader getHeader() {
        return header;
    }

    public long remainingSamples() throws IOException {
        return remainingSamples(fileChannel, header);
    }

    private static long remainingSamples(FileChannel fc, SACHeader h) throws IOException {
        long retval = -1;
        if (fc.isOpen()) {
            long d = fc.size() - fc.position();
            if (h.getNvhdr() == 7) {
                retval = (d - SACHeaderIO.SAC_FOOTER_BYTE_SIZE) / 4;
            } else {
                retval = d / 4;
            }
        }

        return retval;
    }

    public int skipSamples(int nskip) throws IOException {

        int nToSkip = Math.min(nskip, (int) remainingSamples());
        fileChannel.position(fileChannel.position() + 4 * nToSkip);

        return nToSkip;
    }

    public int read(float[] waveform, int offset, int nsamples) throws IOException {

        int n = Math.min((int) remainingSamples(), Math.min(waveform.length - offset, nsamples));
        ByteBuffer buffer = ByteBuffer.allocate(4 * n);
        int nread = fileChannel.read(buffer) / 4;
        buffer.flip();
        buffer.order(headerIO.getByteOrder());
        float[] tmp = new float[nread];
        FloatBuffer fbuffer = buffer.asFloatBuffer();
        fbuffer.get(tmp);
        System.arraycopy(tmp, 0, waveform, offset, nread);

        return nread;
    }

    private static float[] read(SACHeader h, FileChannel fc, ByteOrder b, int nsamples) throws IOException {
        int n = Math.min((int) remainingSamples(fc, h), nsamples);
        ByteBuffer buffer = ByteBuffer.allocate(4 * n);
        int nread = fc.read(buffer) / 4;
        buffer.flip();
        buffer.order(b);
        float[] tmp = new float[nread];
        FloatBuffer fbuffer = buffer.asFloatBuffer();
        fbuffer.get(tmp);
        return tmp;
    }

    public int read(float[] waveform) throws IOException {
        return read(waveform, 0, waveform.length);
    }

    public float[][] readTwoComponent() throws IOException {

        FileType ftype = header.getIftype();
        float[][] retval = null;

        if (ftype == FileType.IAMPH || ftype == FileType.IRLIM || ftype == FileType.IXY) {

            int n = header.getNpts();
            retval = new float[2][n];

            ByteBuffer buffer = ByteBuffer.allocate(2 * 4 * n);
            buffer.flip();
            buffer.order(headerIO.getByteOrder());
            FloatBuffer fbuffer = buffer.asFloatBuffer();
            fbuffer.get(retval[0]);
            fbuffer.get(retval[1]);
        } else {
            System.out.println("Not a two component (spectral or xy) file");
        }

        return retval;

    }

    public void close() throws IOException {
        if (fileChannel.isOpen()) {
            fileChannel.close();
        }
    }

     static boolean isFixedDelta(float[] v2) {
        double MAX_FRACTION_DIFFERENCE = 0.1;
        Double myDelta = null;
        for (int j = 1; j < v2.length; ++j) {
            double v = v2[j] - v2[j - 1];
            if (myDelta == null) {
                myDelta = v;
                if (myDelta <= 0) {
                    return false;
                }
            } else {
                double difference = Math.abs(v - myDelta);
                double fractionalDiff = difference / myDelta;
                if (fractionalDiff > MAX_FRACTION_DIFFERENCE) {
                    return false;
                }
            }
        }
        return true;
    }

}
