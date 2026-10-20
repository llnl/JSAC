/*-
 * #%L
 * Java Seismic Analysis Code (JSAC)
 *  LLNL-CODE-855505
 *  This work was performed under the auspices of the U.S. Department of Energy
 *  by Lawrence Livermore National Laboratory under Contract DE-AC52-07NA27344.
 * %%
 * Copyright (C) 2022 - 2026 Lawrence Livermore National Laboratory
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
package gov.llnl.gnem.jsac.arrayProcessing;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACHeader;

import com.oregondsp.signalProcessing.filter.iir.ThiranAllpass;

import java.util.List;

public class ArrayData {

    private static final double SAMPLING_CONSISTENCY_THRESHOLD = 1.0e-05;
    private static final double DEGREES_TO_KILOMETERS = 111.132;

    private final String dataID;
    private float[][] waveforms;
    private int nch;
    private double delta;
    private double startTime;
    private double[] latitudes;
    private double[] longitudes;
    private double[] elevations;
    private String[] stations;
    private String[] channels;
    private double[] centroidCoordinates;
    private double[][] localCoordinates;
    private final ArrayDefinition arrayDefinition;

    public ArrayData(List<SacTraceData> traces, String ID) {
        arrayDefinition = maybeGetArrayDefinition(traces);
        dataID = ID;

        if (!checkSamplingConsistency(traces)) {
            throw new IllegalStateException("Sampling rate disparity among waveforms.  Correct by interpolating or decimating the offending waveforms.");
        }

        delta = traces.get(0).getDelta();
        nch = traces.size();
        waveforms = new float[nch][];
        latitudes = new double[nch];
        longitudes = new double[nch];
        elevations = new double[nch];
        stations = new String[nch];
        channels = new String[nch];

        // find earliest start time
        double minStart = Double.MAX_VALUE;
        double maxEnd = Double.MIN_VALUE;
        for (int ich = 0; ich < nch; ich++) {
            minStart = Math.min(minStart, traces.get(ich).getEpoch().getStart());
            maxEnd = Math.max(maxEnd, traces.get(ich).getEpoch().getEnd());
        }

        startTime = minStart;

        int N = (int) Math.ceil((maxEnd - minStart) / delta) + 1;  // This is the target waveform length

        // Align and save waveforms
        for (int ich = 0; ich < nch; ich++) {

            float[] x = traces.get(ich).getData().clone();
            double start = traces.get(ich).getEpoch().getStart();
            int npts = traces.get(ich).getNsamp();

            double delay = (start - minStart) / delta;

            if (delay > npts / 2) {
                throw new IllegalStateException("Alignment shift exceeds half the signal length.  Recut the data.");
            } else {

                float[] X = new float[N + 3];

                if (delay == 0.0) {
                    System.arraycopy(x, 0, X, 3, npts);
                } else {	// Calculate and apply the allpass shift
                    double D = 3.0 + delay - Math.floor(delay);
                    ThiranAllpass A = new ThiranAllpass(4, D);
                    System.arraycopy(x, 0, X, 0, npts);
                    A.filter(X);
                }

                x = new float[N];
                System.arraycopy(X, 3, x, 0, N);
                waveforms[ich] = x;

                SACHeader header = traces.get(ich).getSACHeader();
                if (header.getStla() != null) {
                    latitudes[ich] = header.getStla();
                } else {
                    throw new IllegalStateException("Undefined station latitude");
                }

                if (header.getStlo() != null) {
                    longitudes[ich] = header.getStlo();
                } else {
                    throw new IllegalStateException("Undefined station longitude");
                }

                if (header.getStel() != null) {
                    elevations[ich] = header.getStel();
                } else {
                    throw new IllegalStateException("Undefined station elevation");
                }

                if (header.getKstnm() != null) {
                    stations[ich] = header.getKstnm();
                } else {
                    throw new IllegalStateException("Undefined station name");
                }

                if (header.getKcmpnm() != null) {
                    channels[ich] = header.getKcmpnm();
                } else {
                    throw new IllegalStateException("Undefined station component");
                }
            }

        }

        populateLocalCoordinates();

        for (int ich = 0; ich < nch; ich++) {
            SACHeader header = traces.get(ich).getSACHeader();
            double deast = localCoordinates[ich][0];
            double dnorth = localCoordinates[ich][1];
            double delev = localCoordinates[ich][2];
            header.setUser(7, deast);
            header.setUser(8, dnorth);
            header.setUser(9, delev);
        }
    }

    public ArrayDefinition getArrayDefinition() {
        return arrayDefinition;
    }

    private boolean checkSamplingConsistency(List<SacTraceData> traces) {

        boolean retval = true;

        double localDelta = traces.get(0).getDelta();

        for (SacTraceData trace : traces) {
            double d = trace.getDelta();
            if (Math.abs((d - localDelta) / localDelta) > SAMPLING_CONSISTENCY_THRESHOLD) {
                retval = false;
            }
        }

        return retval;
    }

    public String getID() {
        return dataID;
    }

    public float[][] getWaveforms() {
        float[][] retval = new float[nch][];
        for (int ich = 0; ich < nch; ich++) {
            retval[ich] = waveforms[ich].clone();
        }
        return retval;
    }

    public float[][] getWindowedWaveforms(int[] windowLimits) {

        int N = waveforms[0].length;
        int n = windowLimits[1] - windowLimits[0] + 1;

        if (windowLimits[0] < 0 || windowLimits[1] < 0 || windowLimits[0] >= N || windowLimits[1] >= N || n <= 0) {
            throw new IllegalStateException("Window limits inconsistent or out of bounds");
        }

        float[][] retval = new float[nch][n];
        for (int ich = 0; ich < nch; ich++) {
            System.arraycopy(waveforms[ich], windowLimits[0], retval[ich], 0, n);
        }

        return retval;
    }

    public double getSamplingInterval() {
        return delta;
    }

    public double getStartTime() {
        return startTime;
    }

    public double[] getLatitudes() {
        return latitudes.clone();
    }

    public double[] getLongitudes() {
        return longitudes.clone();
    }

    public double[] getElevations() {
        return elevations.clone();
    }

    public String[] getStations() {
        return stations.clone();
    }

    public String[] getChannels() {
        return channels.clone();
    }

    private void populateLocalCoordinates() {

        if (arrayDefinition == null) {
            centroidCoordinates = new double[3];
            for (int ich = 0; ich < nch; ich++) {
                centroidCoordinates[0] += longitudes[ich];
                centroidCoordinates[1] += latitudes[ich];
                centroidCoordinates[2] += elevations[ich];
            }
            for (int i = 0; i < 3; i++) {
                centroidCoordinates[i] /= nch;
            }

            double lonToKm = DEGREES_TO_KILOMETERS * Math.cos(centroidCoordinates[1] / 180.0 * Math.PI);

            localCoordinates = new double[nch][3];
            for (int ich = 0; ich < nch; ich++) {
                localCoordinates[ich][0] = (longitudes[ich] - centroidCoordinates[0]) * lonToKm;
                localCoordinates[ich][1] = (latitudes[ich] - centroidCoordinates[1]) * DEGREES_TO_KILOMETERS;
                localCoordinates[ich][2] = (elevations[ich] - centroidCoordinates[2]) / 1000.0;
            }
        } else {
            String arrayName = arrayDefinition.getArrayName();
            ArrayElement refElement = arrayDefinition.getArrayElement(arrayName, startTime);
            if (refElement == null) {
                throw new IllegalStateException("Failed to get reference array element for array: " + arrayName);
            }
            centroidCoordinates = new double[3];
            centroidCoordinates[0] = refElement.getLon();
            centroidCoordinates[1] = refElement.getLat();
            centroidCoordinates[2] = refElement.getElev();
            localCoordinates = new double[nch][3];
            for (int ich = 0; ich < nch; ich++) {
                String stationCode = stations[ich];
                ArrayElement element = arrayDefinition.getArrayElement(stationCode, startTime);
                if (element == null) {
                    throw new IllegalStateException("Failed to get array element for station code: " + stationCode);
                }
                localCoordinates[ich][0] = element.getDeast();
                localCoordinates[ich][1] = element.getDnorth();
                localCoordinates[ich][2] = (element.getElev() - refElement.getElev()) / 1000.0;
            }
        }

    }

    public double[] getCentroidCoordinates() {
        return centroidCoordinates.clone();
    }

    public double[] getLocalCoordinatesOf(double latitude, double longitude, double elevation) {

        double[] retval = new double[3];

        retval[0] = (longitude - centroidCoordinates[0]) * DEGREES_TO_KILOMETERS * Math.cos(centroidCoordinates[1] / 180.0 * Math.PI);
        retval[1] = (latitude - centroidCoordinates[1]) * DEGREES_TO_KILOMETERS;
        retval[2] = (elevation - centroidCoordinates[2]) / 1000.0;

        return retval;
    }

    public double[] getLocalCoordinatesOf(String station) {

        int index = -1;
        for (int ich = 0; ich < nch; ich++) {
            if (stations[ich].equals(station)) {
                index = ich;
            }
        }

        if (index == -1) {
            throw new IllegalStateException("Unknown station: " + station);
        }

        return localCoordinates[index].clone();
    }

    public double[] getGlobalCoordinatesOf(String station) {

        int index = -1;
        for (int ich = 0; ich < nch; ich++) {
            if (stations[ich].equals(station)) {
                index = ich;
            }
        }

        if (index == -1) {
            throw new IllegalStateException("Unknown station: " + station);
        }

        double[] retval = new double[3];
        retval[0] = longitudes[index];
        retval[1] = latitudes[index];
        retval[2] = elevations[index];

        return retval;
    }

    public double[][] getArray() {
        return localCoordinates.clone();
    }

    public double[][] getCoarray() {

        int n = nch * nch;
        double[][] retval = new double[n][3];
        int p = 0;
        for (int i = 0; i < nch; i++) {
            for (int j = 0; j < nch; j++) {
                for (int k = 0; k < 3; k++) {
                    retval[p][k] = localCoordinates[i][k] - localCoordinates[j][k];
                }
                p++;
            }
        }

        return retval;
    }

    private ArrayDefinition maybeGetArrayDefinition(List<SacTraceData> traces) {
        ArrayDefinitions definitions = ArrayDefinitions.getInstance();
        if (definitions.isEmpty()) {
            return null;
        }
        ArrayDefinition result = null;
        for (SacTraceData std : traces) {
            String stationCode = std.getStreamKey().getSta();
            if (stationCode == null) { //element is unusable
                return null;
            }
            result = definitions.findArray(stationCode);
        }
        return result;
    }

}
