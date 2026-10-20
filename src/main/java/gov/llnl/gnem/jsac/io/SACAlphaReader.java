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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SACAlphaReader {

    public static SACFile readAlpha(InputStream stream) throws IOException {
        List<String> allLines = new ArrayList<>();
        try ( InputStreamReader isr = new InputStreamReader(stream)) {
            try ( BufferedReader br = new BufferedReader(isr)) {
                while (br.ready()) {
                    allLines.add(br.readLine());
                }
            }
        }
        if (allLines.size() < SAC_HEADER_LINE_COUNT) {
            throw new IllegalStateException("SAC Alpha file does not have a complete header!");
        }
        return processAllLines(allLines);
    }

    public static SACFile processAllLines(List<String> allLines) {
        SACHeader h = buildHeaderFromTextLines(allLines);

        int npts = h.getNpts();

        switch (h.getIftype()) {
            case ITIME: {
                if (h.isEvenlySampled()) {
                    return buildFromEvenlySampledYdata(allLines, npts, h);
                } else {
                    return buildFromXYData(allLines, h, true);
                }
            }
            case IGRP:
            case IREAL:
                return buildFromEvenlySampledYdata(allLines, npts, h);
            case IRLIM:
            case IAMPH:
                return buildFromXYData(allLines, h, false);
            case IXY:
                return buildFromXYData(allLines, h, true);
            default:
                throw new IllegalStateException("Unsupported IFTYPE: " + h.getIftype());
        }

    }

    private static SACFile buildFromEvenlySampledYdata(List<String> allLines, int npts, SACHeader h) throws NumberFormatException, IllegalStateException {
        List<Float> data = new ArrayList<>();
        for (int j = SAC_HEADER_LINE_COUNT; j < allLines.size(); ++j) {
            String line = allLines.get(j);
            String[] tokens = line.trim().split("\\s+");
            for (String str : tokens) {
                data.add(Float.parseFloat(str));
            }
        }
        if (npts != data.size()) {
            throw new IllegalStateException("Header NPTS does not match data size!");
        }
        float[] v = new float[data.size()];
        for (int j = 0; j < data.size(); ++j) {
            v[j] = data.get(j);
        }
        return new SACFile(h, v);
    }
    private static final int SAC_HEADER_LINE_COUNT = 30;

    private static SACFile buildFromXYData(List<String> allLines, SACHeader h, boolean isXY) {
        int npts = h.getNpts();
        List<Float> data1 = new ArrayList<>();
        List<Float> data2 = new ArrayList<>();
        int linesRemaining = allLines.size() - SAC_HEADER_LINE_COUNT;
        int linesPerBlock = linesRemaining / 2;
        if (2 * linesPerBlock != linesRemaining) {
            throw new IllegalStateException("For XY data te number of lines must be even!");
        }
        for (int j = 0; j < linesPerBlock; ++j) {
            String line = allLines.get(j + SAC_HEADER_LINE_COUNT);
            String[] tokens = line.trim().split("\\s+");
            for (String str : tokens) {
                data1.add(Float.parseFloat(str));
            }
        }
        if (data1.size() != npts) {
            throw new IllegalStateException("Expected first data block to contain " + npts + " points but actually got " + data1.size() + "!");
        }
        for (int j = 0; j < linesPerBlock; ++j) {
            String line = allLines.get(j + SAC_HEADER_LINE_COUNT + linesPerBlock);
            String[] tokens = line.trim().split("\\s+");
            for (String str : tokens) {
                data2.add(Float.parseFloat(str));
            }
        }
        if (data2.size() != npts) {
            throw new IllegalStateException("Expected second data block to contain " + npts + " points but actually got " + data2.size() + "!");
        }
        float[] v1 = new float[npts];
        float[] v2 = new float[npts];
        for (int j = 0; j < npts; ++j) {
            v1[j] = data1.get(j);
            v2[j] = data2.get(j);
        }
        if (isXY) {
            if (h.getDelta() != null) {
                boolean fixedDelta = SACFileReader.isFixedDelta(v2);
                if (!fixedDelta) {
                    h.setDelta(null);
                }
            }
        }
        return new SACFile(h, v1, v2);

    }

    private static SACHeader buildHeaderFromTextLines(List<String> allLines) {
        List<String> headerLines = new ArrayList<>();
        for (int j = 0; j < SAC_HEADER_LINE_COUNT; ++j) {
            headerLines.add(allLines.get(j));
        }
        SACHeader h = SACHeaderIO.produceHeaderFromStrings(headerLines);
        return h;
    }

    public static SACFile readAlpha(Path path) throws IOException {
        List<String> allLines = new ArrayList<>();
        try ( Scanner s = new Scanner(path)) {
            while (s.hasNextLine()) {
                String line = s.nextLine();
                allLines.add(line);
            }
        }
        if (allLines.size() < SAC_HEADER_LINE_COUNT) {
            throw new IllegalStateException("SAC Alpha file does not have a complete header!");
        }
        return processAllLines(allLines);
    }

    public static SACFile readFile(Path file) throws IOException {
        try (final InputStream is = Files.newInputStream(file)) {
            return readAlpha(is);
        }
    }

}
