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

import static gov.llnl.gnem.jsac.io.SACHeaderIO.UNDEFINED_STRING16;
import static gov.llnl.gnem.jsac.io.SACHeaderIO.UNDEFINED_STRING8;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import gov.llnl.gnem.jsac.io.enums.FileType;

/**
 *
 * @author dodge1
 */
public class SACAlphaWriter {

    public static void writeFile(SACFile sac, Path file) throws IOException {
        try (PrintWriter out = new PrintWriter(file.toFile())) {
            writeHeader(sac, out);
            SACHeader h = sac.getHeader();
            int npts = h.getNpts();

            switch (h.getIftype()) {
            case ITIME: {
                if (h.isEvenlySampled()) {
                    writeTimeEvenSampled(sac, npts, out);
                    break;
                } else {
                    int expected = npts;
                    float[] dep = sac.getData();
                    if (dep == null || dep.length != expected) {
                        throw new IllegalStateException("Failed to get valid dependent array from SACFile!");
                    }
                    float[] indep = sac.getDataBlock2();
                    if (indep == null || indep.length != expected) {
                        throw new IllegalStateException("Failed to get valid independet array from SACFile!");
                    }
                    writeArray(out, dep);
                    writeArray(out, indep);
                    break;
                }
            }
            case IRLIM:
            case IAMPH: {
                int expected = npts;
                float[] amph = sac.getData();
                if (amph == null || amph.length != expected) {
                    throw new IllegalStateException("Failed to get valid amplitude array from SACFile!");
                }
                float[] phase = sac.getDataBlock2();
                if (phase == null || phase.length != expected) {
                    throw new IllegalStateException("Failed to get valid phase array from SACFile!");
                }
                writeArray(out, amph);
                writeArray(out, phase);
                break;
            }
            case IGRP:
            case IREAL:
            case IXY: {
                int expected = npts;
                float[] dep = sac.getData();
                if (dep == null || dep.length != expected) {
                    throw new IllegalStateException("Failed to get valid dependent array from SACFile!");
                }
                if (h.isEvenlySampled() && h.getIftype() == FileType.IXY) {
                    writeArray(out, dep);
                    break;
                }
                float[] indep = sac.getDataBlock2();
                if (indep == null || indep.length != expected) {
                    throw new IllegalStateException("Failed to get valid independent array from SACFile!");
                }
                writeArray(out, dep);
                writeArray(out, indep);
                break;
            }
            default:
                throw new IllegalStateException("Unsupported IFTYPE: " + h.getIftype());
            }
        }
    }

    private static void writeTimeEvenSampled(SACFile sac, int npts, final PrintWriter out) throws IllegalStateException {
        float[] data = sac.getData();
        if (data.length != npts) {
            throw new IllegalStateException("Binary data length does not match NPTS!");
        }
        writeArray(out, data);
    }

    private static void writeArray(final PrintWriter out, float[] data) {
        int npts = data.length;
        int nlines = npts / 5;
        int k = 0;
        for (int j = 0; j < nlines; ++j) {
            for (int m = 0; m < 5; ++m) {
                out.print(String.format("%15.7G", data[k++]));
                if (k >= npts) {
                    out.print("\n");
                    return;
                }
            }
            out.print("\n");
        }
        for (int j = k; j < npts; ++j) {
            out.print(String.format("%15.7G", data[j]));
        }
        out.print("\n");
    }

    private static void writeHeader(SACFile sac, final PrintWriter out) throws IllegalStateException {
        SACHeader h = sac.getHeader();
        List<Float> floats = h.getFloatsForWriting();
        if (floats.size() != 70) {
            throw new IllegalStateException("Expected " + 70 + " floats but got " + floats.size() + "!");
        }
        int nrows = 14;
        int k = 0;
        for (int j = 0; j < nrows; ++j) {
            for (int m = 0; m < 5; ++m) {
                out.print(String.format("%15.7G", floats.get(k++)));
            }
            out.print("\n");
        }
        List<Integer> ints = h.getIntsForWriting();
        if (ints.size() != 40) {
            throw new IllegalStateException("Expected " + 40 + " ints but got " + ints.size() + "!");
        }
        nrows = 8;
        k = 0;
        for (int j = 0; j < nrows; ++j) {
            for (int m = 0; m < 5; ++m) {
                out.print(String.format("%10d", ints.get(k++)));
            }
            out.print("\n");
        }
        out.println(String.format("%8s%16s", normalizeString(h.getKstnm(), 8), normalizeString(h.getKevnm(), 16)));
        List<String> strings = new ArrayList<>();
        strings.add(normalizeString(h.getKhole(), 8));
        strings.add(normalizeString(h.getKo(), 8));
        strings.add(normalizeString(h.getKa(), 8));

        String[] kt = h.getKt();
        for (String element : kt) {
            strings.add(normalizeString(element, 8));
        }

        strings.add(normalizeString(h.getKf(), 8));
        strings.add(normalizeString(h.getKuser0(), 8));
        strings.add(normalizeString(h.getKuser1(), 8));
        strings.add(normalizeString(h.getKuser2(), 8));
        strings.add(normalizeString(h.getKcmpnm(), 8));
        strings.add(normalizeString(h.getKnetwk(), 8));
        strings.add(normalizeString(h.getKdatrd(), 8));
        strings.add(normalizeString(h.getKinst(), 8));
        nrows = 7;
        k = 0;
        for (int j = 0; j < nrows; ++j) {
            for (int m = 0; m < 3; ++m) {
                out.print(String.format("%8s", strings.get(k++)));
            }
            out.print("\n");
        }
    }

    private static String normalizeString(String string, int width) {

        if (string != null) {
            if (string.length() < width) {
                StringBuilder sb = new StringBuilder(string);
                while (sb.length() < width) {
                    sb.append(' ');
                }
                string = sb.toString();
            } else if (string.length() > width) {
                string = string.substring(0, width);
            }
            return string;
        } else {
            return width == 8 ? UNDEFINED_STRING8 : UNDEFINED_STRING16;
        }

    }

}
