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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACHeader;
import llnl.gnem.core.util.PairT;

public class TraceRotator {

    private static final Logger log = LoggerFactory.getLogger(TraceRotator.class);

    public static enum RotationStyle {
        NORMAL, REVERSED
    }

    public static enum RotationType {
        TO_GCP, TO_VALUE, THROUGH_VALUE
    }

    public static void rotateTraces(List<SacTraceData> files, RotationStyle rotationStyle, RotationType rotationType, double rotationAngle) {
        List<SacTraceData> tmp = collectAllowableTraces(files, rotationType);
        Map<TraceGrouper, List<SacTraceData>> grpTraceMap = new HashMap<>();
        for (SacTraceData std : tmp) {
            SACHeader hdr = std.getSACHeader();
            TraceGrouper tg = new TraceGrouper(hdr.getKstnm(), hdr.getKhole(), hdr.getNpts(), hdr.getB(), hdr.getDelta());
            List<SacTraceData> tmp2 = grpTraceMap.get(tg);
            if (tmp2 == null) {
                tmp2 = new ArrayList<>();
                grpTraceMap.put(tg, tmp2);
            }
            tmp2.add(std);
        }
        for (List<SacTraceData> cstd : grpTraceMap.values()) {
            if (cstd.size() == 2) {
                rotatePair(cstd, rotationStyle, rotationType, rotationAngle);
            }
        }

    }

    private static List<SacTraceData> collectAllowableTraces(List<SacTraceData> files, RotationType rotationType) {
        List<SacTraceData> tmp = new ArrayList<>();
        for (SacTraceData std : files) {
            SACHeader hdr = std.getSACHeader();
            Double cmpinc = hdr.getCmpinc();
            Double cmpaz = hdr.getCmpaz();
            if (cmpinc == null) {
                String msg = String.format("CMPINC is not set for %s: Skipping...", std.getFilename().toString());
                log.debug(msg);
            } else if (cmpinc != 90) {
                String msg = String.format("CMPINC is not 90 degrees for %s: Skipping...", std.getFilename().toString());
                log.debug(msg);
            } else if (cmpaz == null) {
                String msg = String.format("CMPAZ is not set for %s: Skipping...", std.getFilename().toString());
                log.debug(msg);
            } else if (rotationType == RotationType.TO_GCP && !positionsAreSet(hdr)) {
                String msg = String.format(
                        "2004: Insufficient header information for rotation:\n" + "STLA, STLO, EVLA, EVLO must be defined for GCP option. For %s: Skipping...",
                            std.getFilename().toString());
                log.debug(msg);

            } else {
                tmp.add(std);
            }
        }
        return tmp;
    }

    private static boolean positionsAreSet(SACHeader hdr) {
        return hdr.getStla() != null && hdr.getStlo() != null && hdr.getEvla() != null && hdr.getEvlo() != null;
    }

    private static void rotatePair(List<SacTraceData> cstd, RotationStyle rotationStyle, RotationType rotationType, double rotationAngle) {
        SacTraceData std1 = cstd.get(0);
        SacTraceData std2 = cstd.get(1);

        // After normalizeTracePair std2 azimuth is std1 azimuth plus 90 degrees.
        PairT<SacTraceData, SacTraceData> tmp = normalizeTracePair(std1, std2);
        std1 = tmp.getFirst();
        std2 = tmp.getSecond();
        if (null != rotationType) {
            switch (rotationType) {
            case THROUGH_VALUE:
                //rotationAngle is the desired azimuth of std1
                rotateHorizontals(std1, std2, rotationAngle, rotationStyle, rotationType);
                break;
            case TO_VALUE:
                double currentAz = std1.getCmpaz();
                double desiredRotation = rotationAngle - currentAz;
                rotateHorizontals(std1, std2, desiredRotation, rotationStyle, rotationType);
                break;
            case TO_GCP:
                std1.getSACHeader().maybeUpdateDistAz();
                std2.getSACHeader().maybeUpdateDistAz();
                Double baz1 = std1.getBackAzimuth();
                Double baz2 = std2.getBackAzimuth();
                if (baz1 == null || baz2 == null) {
                    String msg = String.format("2004: Insufficient header information for rotation:\n" + "STLA, STLO, EVLA, EVLO must be defined for GCP option.");
                    log.warn(msg);
                } else {
                    double epsilon = 0.0001d;
                    if (!Precision.equals(baz1, baz2, epsilon)) {
                        String msg = String.format("Files %s and %s have differing BAZ values (%f, %f): No rotation.", std1.getFilename().toString(), std2.getFilename().toString(), baz1, baz2);
                        log.warn(msg);
                    } else {
                        rotateHorizontals(std1, std2, baz1 + 180 - std1.getCmpaz(), rotationStyle, rotationType);
                    }
                }
                break;
            default:
                break;
            }
        }
    }

    private static PairT<SacTraceData, SacTraceData> normalizeTracePair(SacTraceData file1, SacTraceData file2) {
        SACHeader hdr1 = file1.getSACHeader();
        SACHeader hdr2 = file2.getSACHeader();
        Double cmpinc1 = hdr1.getCmpinc();
        Double cmpinc2 = hdr2.getCmpinc();
        if (cmpinc1 == null) {
            String msg = String.format("CMPINC is not set for %s!", file1.getFilename().toString());
            throw new IllegalStateException(msg);
        }
        if (cmpinc2 == null) {
            String msg = String.format("CMPINC is not set for %s!", file2.getFilename().toString());
            throw new IllegalStateException(msg);
        }
        if (cmpinc1 != 90) {
            String msg = String.format("CMPINC is not 90 degrees for %s!", file1.getFilename().toString());
            throw new IllegalStateException(msg);
        }
        if (cmpinc2 != 90) {
            String msg = String.format("CMPINC is not 90 degrees for %s!", file2.getFilename().toString());
            throw new IllegalStateException(msg);
        }

        Double cmpaz1 = hdr1.getCmpaz();
        Double cmpaz2 = hdr2.getCmpaz();
        if (cmpaz1 == null) {
            String msg = String.format("CMPAZ is not set for %s!", file1.getFilename().toString());
            throw new IllegalStateException(msg);
        }
        if (cmpaz2 == null) {
            String msg = String.format("CMPAZ is not set for %s!", file2.getFilename().toString());
            throw new IllegalStateException(msg);
        }

        // Y is (ideally) North and X is 90 degrees clockwise (ideally East) after following transformations.
        SacTraceData v1 = file1;
        SacTraceData v2 = file2;
        if (cmpaz1 > cmpaz2) {
            v1 = file2;
            v2 = file1;
            cmpaz1 = v1.getCmpaz();
            cmpaz2 = v2.getCmpaz();
        }
        float[] x = v2.getData();
        float[] y = v1.getData();
        if (x.length != y.length) {
            throw new IllegalStateException("ERROR 2010: Number of points in pair of files are not equal");
        }

        //Ensure orthogonality by projecting x onto y and a vector orthogonal to y.
        double desiredXAz = cmpaz1 + 90;
        double rotationAngleToDesired = desiredXAz - cmpaz2;
        double alpha = Math.toRadians(rotationAngleToDesired);
        float[] xprime = new float[x.length];
        float[] yprime = new float[x.length];
        double sina = Math.sin(alpha);
        double cosa = Math.cos(alpha);
        for (int j = 0; j < x.length; ++j) {
            yprime[j] = (float) (y[j] + sina * x[j]);
            xprime[j] = (float) (cosa * x[j]);
        }
        v1.setData(yprime);
        v2.setData(xprime);
        v2.getSACHeader().setCmpaz(desiredXAz);
        return new PairT<>(v1, v2);
    }

    private static void rotateHorizontals(SacTraceData file1, SacTraceData file2, double rotationAngleDegrees, RotationStyle style, RotationType rotationType) {

        SacTraceData v1 = file1;
        SacTraceData v2 = file2;
        // file2 is assumed to have azimuth equal to file1 azimuth + 90 degrees.
        SACHeader hdr1 = v1.getSACHeader();
        SACHeader hdr2 = v2.getSACHeader();
        Double cmpaz1 = hdr1.getCmpaz();
        Double cmpaz2 = hdr2.getCmpaz();
        float[] x = v1.getData();
        float[] y = v2.getData();
        double theta = Math.toRadians(rotationAngleDegrees);
        final double cosT = Math.cos(theta);
        final double sinT = Math.sin(theta);
        double con11 = cosT;
        double con12 = sinT;
        double con21 = -sinT;
        double con22 = cosT;
        double tmp = cmpaz1 + rotationAngleDegrees;
        tmp = normaliseAngle(tmp);
        hdr1.setCmpaz(tmp);
        tmp = cmpaz2 + rotationAngleDegrees;
        tmp = normaliseAngle(tmp);
        hdr2.setCmpaz(tmp);
        switch (style) {
        case NORMAL:
            break;
        case REVERSED:
            con21 = -con21;
            con22 = -con22;
            tmp = hdr2.getCmpaz() - 180;
            tmp = normaliseAngle(tmp);
            hdr2.setCmpaz(tmp);
            break;
        }
        for (int j = 0; j < x.length; ++j) {
            float tmpX = (float) (x[j] * con11 + y[j] * con12);
            float tmpY = (float) (x[j] * con21 + y[j] * con22);
            x[j] = tmpX;
            y[j] = tmpY;
        }
        v2.setData(y);
        v1.setData(x);
        hdr1.setKcmpnm(null);
        hdr2.setKcmpnm(null);
    }

    private static double normaliseAngle(double tmp) {
        if (tmp < 0) {
            tmp += 360;
        }
        if (tmp > 360) {
            tmp -= 360;
        }
        return tmp;
    }

    private static class TraceGrouper {

        private final String kstnm;
        private final String khole;
        private final int npts;
        private final double b;
        private final double delta;

        public TraceGrouper(String kstnm, String khole, int npts, double b, double delta) {
            this.kstnm = kstnm;
            this.khole = khole;
            this.npts = npts;
            this.b = b;
            this.delta = delta;
        }

        @Override
        public String toString() {
            return "TraceGrouper{" + "kstnm=" + kstnm + ", khole=" + khole + ", npts=" + npts + ", b=" + b + ", delta=" + delta + '}';
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + Objects.hashCode(this.kstnm);
            hash = 83 * hash + Objects.hashCode(this.khole);
            hash = 83 * hash + this.npts;
            hash = 83 * hash + (int) (Double.doubleToLongBits(this.b) ^ (Double.doubleToLongBits(this.b) >>> 32));
            hash = 83 * hash + (int) (Double.doubleToLongBits(this.delta) ^ (Double.doubleToLongBits(this.delta) >>> 32));
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TraceGrouper other = (TraceGrouper) obj;
            if (this.npts != other.npts) {
                return false;
            }
            if (Double.doubleToLongBits(this.b) != Double.doubleToLongBits(other.b)) {
                return false;
            }
            if (Double.doubleToLongBits(this.delta) != Double.doubleToLongBits(other.delta)) {
                return false;
            }
            if (!Objects.equals(this.kstnm, other.kstnm)) {
                return false;
            }
            return Objects.equals(this.khole, other.khole);
        }

    }
}
