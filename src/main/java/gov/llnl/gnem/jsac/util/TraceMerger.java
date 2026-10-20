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
package gov.llnl.gnem.jsac.util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACFile;
import gov.llnl.gnem.jsac.io.SACHeader;
import llnl.gnem.core.util.SeriesMath;
import llnl.gnem.core.util.TimeT;

/**
 *
 * @author dodge1
 */
public class TraceMerger {

    private static double MAX_SAMPLE_INCONSISTENCY = 0.5;
    private static final int MAX_MISMATCH_COUNTS = 2;
    private static final int MAX_SHIFT_SAMPLES = 5;

    public static void setMAX_SAMPLE_INCONSISTENCY(double MAX_SAMPLE_INCONSISTENCY) {
        TraceMerger.MAX_SAMPLE_INCONSISTENCY = MAX_SAMPLE_INCONSISTENCY;
    }

    private static Double getMedianDifference(float[] data1, int data1Offset, float[] data2, int shift) {
        List<Double> values = new ArrayList<>();
        for (int j = data1Offset; j < data1.length; ++j) {
            int k = j + shift - data1Offset;
            if (k >= 0 && k < data2.length) {
                double diff = Math.abs(data1[j] - data2[k]);
                values.add(diff);
            }
        }
        return values.isEmpty() ? null : SeriesMath.getMedian(values);
    }

    private static List<SacTraceData> sortByTime(List<SacTraceData> files) {
        List<SacTraceData> result = new ArrayList<>();
        Map<Double, Collection<SacTraceData>> timeSeisMap = new TreeMap<>();
        for (SacTraceData std : files) {
            Collection<SacTraceData> cstd = timeSeisMap.get(std.getTime().getEpochTime());
            if (cstd == null) {
                cstd = new ArrayList<>();
                timeSeisMap.put(std.getTime().getEpochTime(), cstd);
            }
            cstd.add(std);
        }
        for (Double value : timeSeisMap.keySet()) {
            Collection<SacTraceData> cstd = timeSeisMap.get(value);
            result.addAll(cstd);
        }
        return result;
    }

    public static enum GapStrategy {
        ZERO, INTERP
    }

    public static SacTraceData merge(List<SacTraceData> files, GapStrategy gapStrategy) {
        if (files.isEmpty()) {
            return null;
        } else if (files.size() == 1) {
            return files.get(0);
        } else {
            files = sortByTime(files);
            SacTraceData result = merge(files.get(0), files.get(1), gapStrategy);
            for (int j = MAX_MISMATCH_COUNTS; j < files.size(); ++j) {
                result = merge(result, files.get(j), gapStrategy);
            }
            return result;
        }
    }

    public static SacTraceData merge(SacTraceData trace1, SacTraceData trace2, GapStrategy gapStrategy) {
        if (!trace1.getStreamKey().equals(trace2.getStreamKey())) {
            throw new IllegalStateException("Attempt to merge incompatible channels!");
        }
        // Make sure trace 1 is the earliest...
        TimeT t1 = trace1.getTime();
        TimeT t2 = trace2.getTime();
        if (t2.lt(t1)) {
            SacTraceData tmp = trace1;
            trace1 = trace2;
            trace2 = tmp;
        }

        double delta1 = trace1.getDelta();
        double delta2 = trace2.getDelta();
        double averageDelta = (delta1 + delta2) / MAX_MISMATCH_COUNTS;

        int nsamps1 = trace1.getNsamp();
        int nsamps2 = trace2.getNsamp();
        double b1 = trace1.getBeginTimeMarker();
        double b2 = trace2.getBeginTimeMarker();
        double e1 = trace1.getSACHeader().getE();
        double e2 = trace2.getSACHeader().getE();
        // Trace1: Make sure that the delta and NPTS are consistent with the endtime - begintime
        double nsampsInconsistency = computeInternalRateTimeNsampsConsistency(b1, e1, delta1, nsamps1);
        if (nsampsInconsistency > MAX_SAMPLE_INCONSISTENCY) {
            throw new IllegalStateException("File1 sample rate, b, e, npts are inconsistent!");
        }
        // Trace2: Make sure that the delta and NPTS are consistent with the endtime - begintime
        nsampsInconsistency = computeInternalRateTimeNsampsConsistency(b2, e2, delta2, nsamps2);
        if (nsampsInconsistency > MAX_SAMPLE_INCONSISTENCY) {
            throw new IllegalStateException("File2 sample rate, b, e, npts are inconsistent!");
        }

        // Case 0: trace2 is a subset of trace1
        if (trace2.getEpoch().isSubset(trace1.getEpoch())) {
            return trace1;
        }

        double trace1End = trace1.getEndtime().getEpochTime();
        double trace2Start = trace2.getTime().getEpochTime();
        long overlappedSamples = Math.round((trace1End - trace2Start) / averageDelta) + 1;
        // Case 1: trace2.begin <= trace1.end and trace2.end > trace1.end
        if (overlappedSamples >= 1) {
            return mergeOverlappedTraces(trace1, trace2, trace2Start, averageDelta);
        }
        //Case 2: trace2 starts at trace1.end + 1 sample
        double gap = trace2Start - trace1End;
        if (gap > averageDelta / MAX_MISMATCH_COUNTS && gap < 1.5 * averageDelta) {
            return mergeExactlySequentialTraces(trace1, trace2, averageDelta);
        } else if (gap >= MAX_MISMATCH_COUNTS * averageDelta) {
            //Case3: trace2 starts more than 1 sample after end of trace 1
            switch (gapStrategy) {
            case ZERO:
                float[] data1 = trace1.getData();
                float[] data2 = trace2.getData();
                int gapSamples = (int) Math.round((trace2Start - trace1End) / averageDelta) - 1;
                int totalSamples = data1.length + data2.length + gapSamples;
                float[] newData = new float[totalSamples];
                System.arraycopy(data1, 0, newData, 0, data1.length);
                System.arraycopy(data2, 0, newData, data1.length + gapSamples, data2.length);
                SACHeader header = trace1.getSACHeader(); // need to clone this so original file is unaltered.
                header.setDelta(averageDelta);
                header.setNpts(newData.length);
                double newEnd = header.getB() + (newData.length - 1) * averageDelta;
                // Maybe compare with the latest end in the input files?
                header.setE(newEnd);
                Path path = trace1.getFilename();
                SACFile sac = new SACFile(header, newData);
                SacTraceData result = new SacTraceData(path, sac);
                result.resetStatistics();
                return result;
            case INTERP:
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }

        System.out.println("");

        return null;
    }

    private static SacTraceData mergeExactlySequentialTraces(SacTraceData trace1, SacTraceData trace2, double averageDelta) {
        float[] data1 = trace1.getData();
        float[] data2 = trace2.getData();
        int totalSamples = data1.length + data2.length;
        float[] newData = new float[totalSamples];
        System.arraycopy(data1, 0, newData, 0, data1.length);
        System.arraycopy(data2, 0, newData, data1.length, data2.length);
        SACHeader header = trace1.getSACHeader(); // need to clone this so original file is unaltered.
        header.setDelta(averageDelta);
        header.setNpts(newData.length);
        double newEnd = header.getB() + (newData.length - 1) * averageDelta;
        // Maybe compare with the latest end in the input files?
        header.setE(newEnd);
        Path path = trace1.getFilename();
        SACFile sac = new SACFile(header, newData);
        SacTraceData result = new SacTraceData(path, sac);
        result.resetStatistics();
        return result;
    }

    private static SacTraceData mergeOverlappedTraces(SacTraceData trace1, SacTraceData trace2, double trace2Start, double averageDelta) throws IllegalStateException {
        float[] data1 = trace1.getData();
        float[] data2 = trace2.getData();
        int offset = (int) Math.round((trace2Start - trace1.getTime().getEpochTime()) / averageDelta);

        double minDiff = Double.MAX_VALUE;
        int idx = 0;
        for (int j = -5; j <= MAX_SHIFT_SAMPLES; ++j) {
            Double medDiff = getMedianDifference(data1, offset, data2, j);
            if (medDiff != null && medDiff < minDiff) {
                minDiff = medDiff;
                idx = j;
            }
        }
        if (minDiff > MAX_MISMATCH_COUNTS) {
            throw new IllegalStateException("Traces do not match at predicted offset!");
        }

        offset -= (idx);
        int samplesToSkip = data1.length - offset;
        int totalSamples = data1.length + data2.length - samplesToSkip;
        float[] newData = new float[totalSamples];
        System.arraycopy(data1, 0, newData, 0, data1.length);
        System.arraycopy(data2, samplesToSkip - 1, newData, data1.length, data2.length - samplesToSkip);
        SACHeader header = trace1.getSACHeader(); // need to clone this so original file is unaltered.
        header.setDelta(averageDelta);
        header.setNpts(newData.length);
        double newEnd = header.getB() + (newData.length - 1) * averageDelta;
        // Maybe compare with the latest end in the input files?
        header.setE(newEnd);
        Path path = trace1.getFilename();
        SACFile sac = new SACFile(header, newData);
        SacTraceData result = new SacTraceData(path, sac);
        result.resetStatistics();
        return result;
    }

    private static double computeInternalRateTimeNsampsConsistency(double b, double e, double delta, int nsamps) {
        return Math.abs((e - b + delta) / delta - nsamps);
    }

    public static boolean sampleIntervalsAgree(double delta1, double delta2, double maxPercentError) {
        double tmp1 = delta1;
        double tmp2 = delta2;
        if (tmp1 == 0 || tmp2 == 0) {
            throw new IllegalStateException("Encountered sample interval of 0!");
        }
        if (tmp1 < 1 && tmp2 < 1) {
            tmp1 = 1 / tmp1;
            tmp2 = 1 / tmp2;
        }

        double percentError = 200 * (tmp1 - tmp2) / (tmp1 + tmp2);
        return percentError <= maxPercentError;
    }

}
