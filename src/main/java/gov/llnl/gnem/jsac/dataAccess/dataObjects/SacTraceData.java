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

package gov.llnl.gnem.jsac.dataAccess.dataObjects;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oregondsp.signalProcessing.filter.iir.Butterworth;
import com.oregondsp.signalProcessing.filter.iir.ChebyshevI;
import com.oregondsp.signalProcessing.filter.iir.ChebyshevII;
import com.oregondsp.signalProcessing.filter.iir.IIRFilter;
import com.oregondsp.signalProcessing.filter.iir.PassbandType;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.SpectralData.PresentationFormat;
import gov.llnl.gnem.jsac.io.GseSeismogram;
import gov.llnl.gnem.jsac.io.SACFile;
import gov.llnl.gnem.jsac.io.SACHeader;
import gov.llnl.gnem.jsac.io.enums.DepVarType;
import gov.llnl.gnem.jsac.io.enums.EventType;
import gov.llnl.gnem.jsac.io.enums.FileType;
import gov.llnl.gnem.jsac.io.enums.Iztype;
import gov.llnl.gnem.jsac.io.enums.MagType;
import gov.llnl.gnem.jsac.util.HeaderLister;
import gov.llnl.gnem.jsac.util.PartialDataWindow;
import gov.llnl.gnem.jsac.util.WaveformUtils;
import llnl.gnem.core.signalprocessing.Miscellaneous;
import llnl.gnem.core.signalprocessing.extended.SignalProcessingException;
import llnl.gnem.core.signalprocessing.filter.FilterDesign;
import llnl.gnem.core.util.Epoch;
import llnl.gnem.core.util.SeriesMath;
import llnl.gnem.core.util.StreamKey;
import llnl.gnem.core.util.TaperType;
import llnl.gnem.core.util.TimeT;
import llnl.gnem.core.util.Geometry.FlynnEngdahl;
import llnl.gnem.core.waveform.seismogram.CssSeismogram;

/**
 * @author dodge1
 */
public class SacTraceData {

    private static final Logger log = LoggerFactory.getLogger(SacTraceData.CutErrorAction.class);

    public enum CutErrorAction {
        FATAL, USEBE, FILLZ
    }

    private SpectralData spectralData;
    private final Path filename;
    private SACHeader header;
    private float[] xValues;
    private float[] yValues;
    private Epoch epoch;
    private String agency = null;

    public SacTraceData(Path filename, SACFile sac) {

        header = sac.getHeader();
        header.maybeUpdateDistAz();

        switch (header.getIftype()) {
        case ITIME:
            xValues = header.isEvenlySampled() ? null : sac.getDataBlock2();
            yValues = sac.getData();
            spectralData = null;
            epoch = header.getEpoch();
            break;
        case IGRP:
        case IREAL:
            xValues = null;
            yValues = sac.getData();
            spectralData = null;
            epoch = header.getEpoch();
            break;
        case IRLIM:
            xValues = null;
            yValues = null;
            epoch = header.getEpoch(); //min positive frequency to nyquist frequency
            float[] tmp1 = sac.getData();
            float[] tmp2 = sac.getDataBlock2();
            int npts = tmp1.length;
            Complex[] C = new Complex[npts];
            for (int i = 0; i < npts; i++) {
                C[i] = new Complex(tmp1[i], tmp2[i]);
            }
            spectralData = new SpectralData(C, header.getDelta(), PresentationFormat.RealImaginary, header.getNsnpts());
            break;
        case IAMPH:
            xValues = null;
            yValues = null;
            epoch = header.getEpoch(); //min positive frequency to nyquist frequency
            tmp1 = sac.getData();
            tmp2 = sac.getDataBlock2();
            npts = tmp1.length;
            C = new Complex[npts];
            for (int i = 0; i < npts; i++) {
                double R = tmp1[i] * Math.cos(tmp2[i]);
                double I = tmp1[i] * Math.sin(tmp2[i]);
                C[i] = new Complex(R, I);
            }
            spectralData = new SpectralData(C, header.getDelta(), PresentationFormat.AmplitudePhase, header.getNsnpts());
            break;
        case IXY:
            xValues = sac.getDataBlock2();
            yValues = sac.getData();
            epoch = header.getEpoch();

            break;
        default:
            System.out.println("Unimplemented file type: " + header.getIftype());
            break;
        }

        if (filename.getFileName().toString().equals(".")) {
            this.filename = Paths.get(sac.getHeader().createFileName());
        } else {
            this.filename = filename;
        }

    }

    public SacTraceData(Path root, GseSeismogram seis) {
        yValues = seis.getData();
        filename = buildFilename(root, seis);
        header = buildHeader(seis);
        spectralData = null;
        epoch = seis.getEpoch();
    }

    public SacTraceData(CssSeismogram niw, Path parent) {
        yValues = niw.getData();
        String name = WaveformUtils.createName(niw);
        name = name.replaceAll("\\s", "_");
        name = name.replaceAll(":", "_");
        File file = new File(parent.toFile(), name);
        filename = file.toPath();
        header = buildHeader(niw);
        spectralData = null;
        epoch = niw.getEpoch();
    }

    @Override
    public String toString() {
        return "SacTraceData{" + "filename=" + filename + ", header=" + header + ", epoch=" + epoch + '}';
    }

    private SACHeader buildHeader(GseSeismogram seis) {
        SACHeader result = new SACHeader();
        result.setReferenceTime(seis.getTimeAsDouble());
        result.setB(0.0);
        result.setE(seis.getLengthInSeconds());
        result.setDelta(seis.getDelta());
        result.setNpts(seis.getLength());
        result.setStla(seis.getLat());
        result.setStlo(seis.getLon());
        result.setStel(seis.getElev());
        result.setStdp(seis.getEdepth());
        result.setCmpaz(seis.getHang());
        result.setCmpinc(90 - seis.getVang());
        result.setIftype(FileType.ITIME);
        result.setIdep(DepVarType.IUNKN);
        result.setIztype(Iztype.IB);
        result.setNvhdr(6);
        result.setKstnm(seis.getStreamKey().getSta());
        result.setKhole(seis.getStreamKey().getLocationCode());
        result.setKcmpnm(seis.getStreamKey().getChan());
        result.setKnetwk(seis.getStreamKey().getNet());
        result.setKinst(seis.getInstype());
        result.setLeven(1);

        return result;
    }

    private SACHeader buildHeader(CssSeismogram seis) {
        SACHeader result = new SACHeader();
        result.setReferenceTime(seis.getTimeAsDouble());
        result.setB(0.0);
        result.setE(getLengthInSeconds(seis));
        result.setDelta(1.0 / seis.getSamprate());
        result.setNpts(seis.getNsamp());
        result.setIftype(FileType.ITIME);
        result.setIdep(DepVarType.IUNKN);
        result.setIztype(Iztype.IB);
        result.setNvhdr(6);
        result.setKstnm(seis.getStreamKey().getSta());
        result.setKhole(seis.getStreamKey().getLocationCode());
        result.setKcmpnm(seis.getStreamKey().getChan());
        result.setKnetwk(seis.getStreamKey().getNet());
        result.setLeven(1);
        return result;
    }

    private Double getLengthInSeconds(CssSeismogram seis) {
        return (seis.getNsamp() - 1) / seis.getSamprate();
    }

    public CssSeismogram produceSeismogram(StreamKey substituteKey, Double substituteTime) {
        Double calib = null;
        Double calper = null;
        Integer nwfid = header.getNwfid();
        Long lnwfid = nwfid != null ? (long) nwfid : -1L;
        StreamKey key = makeStreamKey(substituteKey);
        Double tmp = substituteTime != null ? substituteTime : header.getBeginTime();
        if (tmp == null) {
            tmp = 0.0;
        }
        return new CssSeismogram(lnwfid, key, yValues, header.getSamprate(), new TimeT(tmp), calib, calper);
    }

    private StreamKey makeStreamKey(StreamKey substituteKey) {
        if (substituteKey != null) {
            return substituteKey;
        } else {
            StreamKey key = header.getStreamKey();
            if (this.agency != null) {
                return new StreamKey(agency, key.getNet(), key.getSta(), key.getChan(), key.getLocationCode());
            } else {
                return key;
            }
        }
    }

    public void maybeUpdateIdepForDif() {
        DepVarType dvt = header.getIdep();
        if (dvt != null) {
            switch (dvt) {
            case IDISP:
                header.setIdep(DepVarType.IVEL);
                return;
            case IVEL:
                header.setIdep(DepVarType.IACC);
            case IACC:
            case IUNKN:
            case IVOLTS:
            default:
                break;
            }
        }
    }

    public void maybeUpdateIdepForInt() {
        DepVarType dvt = header.getIdep();
        if (dvt != null) {
            switch (dvt) {
            case IACC:
                header.setIdep(DepVarType.IVEL);
                return;
            case IVEL:
                header.setIdep(DepVarType.IDISP);
                return;
            case IDISP:
            case IUNKN:
            case IVOLTS:
            default:
                break;
            }
        }
    }

    public static enum BinOpType {
        ADD, SUB, MUL, DIV
    }

    public void applyBinOp(SacTraceData source, boolean replaceHeader, BinOpType opType) {
        SACHeader sourceHeader = source.getSACHeader();
        if (!sourceHeader.getNpts().equals(header.getNpts())) {
            System.out.println("Files " + this.filename + " and " + source.filename + " have different NPTS! Skipping");
            return;
        }
        if (!sampleIntervalsAgree(header.getDelta(), source.header.getDelta())) {
            System.out.println("Files " + this.filename + " and " + source.filename + " have different sample rates! Skipping");
            return;
        }
        for (int j = 0; j < yValues.length; ++j) {
            switch (opType) {
            case ADD:
                yValues[j] += source.yValues[j];
                break;
            case SUB:
                yValues[j] -= source.yValues[j];
                break;
            case MUL:
                yValues[j] *= source.yValues[j];
                break;
            case DIV:
                float tmp = source.yValues[j] != 0 ? source.yValues[j] : 1;
                yValues[j] /= tmp;
                break;
            }

        }
        if (replaceHeader) {
            header = source.header;
        }
        resetStatistics();
    }

    private boolean sampleIntervalsAgree(Double delta1, Double delta2) {
        double tmp1 = delta1;
        double tmp2 = delta2;
        if (tmp1 < 1 && tmp2 < 1) {
            tmp1 = 1 / tmp1;
            tmp2 = 1 / tmp2;
        }
        tmp1 = Math.round(tmp1);
        tmp2 = Math.round(tmp2);
        return tmp1 == tmp2;
    }

    public void abs() {
        yValues = SeriesMath.abs(yValues);
    }

    public void squareRoot() {
        yValues = SeriesMath.sqrt(yValues);
    }

    public void computeEnvelope() {
        if (!header.isEvenlySampled()) {
            throw new IllegalStateException("ERROR 1306: Illegal operation on unevenly spaced file");
        }
        yValues = Miscellaneous.envelope(yValues);
    }

    public boolean isNonNegative() {
        return SeriesMath.getMin(yValues) >= 0;
    }

    public void log() {
        yValues = log(yValues);
    }

    private float[] log(float[] data) {
        float[] result = new float[data.length];
        float value;
        for (int ii = 0; ii < data.length; ii++) {
            value = (float) Math.log(data[ii]);
            if (!Float.isFinite(value)) {
                if (Float.POSITIVE_INFINITY == value) {
                    value = 1.0e36f;
                } else {
                    value = -1.0e36f;
                }
            }
            result[ii] = value;
        }
        return result;
    }

    public boolean isPositive() {
        return SeriesMath.getMin(yValues) > 0;
    }

    public void log10() {
        yValues = SeriesMath.log10(yValues);
    }

    public void exp() {
        yValues = exp(yValues);
    }

    private float[] exp(float[] data) {
        float[] result = new float[data.length];
        for (int ii = 0; ii < data.length; ii++) {
            result[ii] = (float) Math.exp(data[ii]);
        }
        return result;
    }

    public void exp10() {
        yValues = exp10(yValues);
    }

    private float[] exp10(float[] data) {
        float[] result = new float[data.length];
        for (int ii = 0; ii < data.length; ii++) {
            result[ii] = (float) Math.pow(10.0, data[ii]);
        }
        return result;
    }

    public void reverse() {
        if (!header.isEvenlySampled()) {
            throw new IllegalStateException("ERROR 1306: Illegal operation on unevenly spaced file");
        }
        SeriesMath.ReverseArray(yValues);
    }

    public void setStationCode(String string) {
        header.setKstnm(string);
    }

    public void setNetworkCode(String string) {
        header.setKnetwk(string);
    }

    public void setChannelCode(String string) {
        header.setKcmpnm(string);
    }

    public void setLocationCode(String string) {
        header.setKhole(string);
    }

    public StreamKey getStreamKey() {
        return new StreamKey(header.getKnetwk(), header.getKstnm(), header.getKcmpnm(), header.getKhole());
    }

    public void integrate(boolean b) {
        double samprate = 1.0 / header.getDelta();
        SeriesMath.Integrate(yValues, samprate);
        maybeUpdateIdepForInt();
    }

    public void removeMean() {
        SeriesMath.RemoveMean(yValues);
    }

    public void divOmega() {
        if (this.spectralData == null) {
            log.warn("No spectral data exists for trace!");
        } else {
            spectralData.divOmega();
        }
    }

    public void mulOmega() {
        if (this.spectralData == null) {
            log.warn("No spectral data exists for trace!");
        } else {
            spectralData.mulOmega();
        }
    }

    public void interpolate(double newsamprate) {
        double samprate = header.getSamprate();
        if ((newsamprate > 0.)) {
            yValues = SeriesMath.interpolate(0., 1. / samprate, yValues, 1. / newsamprate);
            header.setDelta(1.0 / newsamprate);
            header.setNpts(yValues.length);
        }
    }

    public int getNsamp() {
        return yValues.length;
    }

    public Epoch getEpoch() {
        return epoch;
    }

    public void removeTrend() {
        SeriesMath.RemoveTrend(yValues);
    }

    public void applyHilbert() {
        if (!header.isEvenlySampled()) {
            throw new IllegalStateException("ERROR 1306: Illegal operation on unevenly spaced file");
        }
        float[] tmp = Miscellaneous.hilbert(yValues);
        System.arraycopy(tmp, 0, yValues, 0, yValues.length);
    }

    public void smooth(int halfWidth) {
        if (!header.isEvenlySampled()) {
            throw new IllegalStateException("ERROR 1306: Illegal operation on unevenly spaced file");
        }
        yValues = SeriesMath.MeanSmooth(yValues, halfWidth);
    }

    public void squareData() {
        yValues = SeriesMath.square(yValues);
    }

    public void multiplyByConstant(float constant) {
        SeriesMath.MultiplyScalar(yValues, constant);
    }

    public void addConstant(float constant) {
        SeriesMath.AddScalar(yValues, constant);
    }

    public void applyTaper(TaperType taperType, double taperPercent) {
        if (!header.isEvenlySampled()) {
            throw new IllegalStateException("ERROR 1306: Illegal operation on unevenly spaced file");
        }
        SeriesMath.Taper(yValues, taperPercent);
    }

    public SpectralData getSpectralData() {
        return spectralData;
    }

    public SACHeader getSACHeader() {
        return header;
    }

    private Path buildFilename(Path root, GseSeismogram seis) {
        String name = WaveformUtils.createName(seis);
        File file = new File(root.toFile(), name);
        return file.toPath();
    }

    public float[] getData() {
        if (yValues == null) {
            return null;
        }
        return yValues.clone();
    }

    public Double getBeginTimeMarker() {
        return header.getB();
    }

    public Long getEvid() {
        Integer nevid = header.getNevid();
        if (nevid != null) {
            return (long) nevid;
        } else {
            return null;
        }
    }

    public Double getBackAzimuth() {
        return header.getBackAzimuth();
    }

    public Double getDistanceKm() {
        return header.getDistanceKm();
    }

    public Double getDistanceDeg() {
        return header.getDistanceDeg();
    }

    public Double getEvla() {
        return header.getEvla();
    }

    public Double getEvlo() {
        return header.getEvlo();
    }

    public Double getEvdp() {
        return header.getEvdp();
    }

    public Double getMag() {
        return header.getMag();
    }

    public Double getStla() {
        return header.getStla();
    }

    public Double getStlo() {
        return header.getStlo();
    }

    public Double getStel() {
        return header.getStel();
    }

    public Double getStdp() {
        return header.getStdp();
    }

    public Double getCmpaz() {
        return header.getCmpaz();
    }

    public Double getCmpinc() {
        return header.getCmpinc();
    }

    public boolean hasEventInfo() {
        return header.hasEventInfo();
    }

    public boolean hasStationInfo() {
        return header.hasStationInfo();
    }

    public Long getWaveformId() {
        Integer tmp = header.getNwfid();
        return tmp != null ? (long) tmp : null;
    }

    public String getKevnm() {
        return header.getKevnm();
    }

    public Integer getIevreg() {
        return header.getIevreg();
    }

    public EventType getIevtyp() {
        return header.getIevtyp();
    }

    public MagType getImagtyp() {
        return header.getImagtyp();
    }

    public Path getFilename() {
        return filename;
    }

    public Iztype getIztype() {
        return header.getIztype();
    }

    public Double getOmarkerTime() {
        return header.getO();
    }

    public Double getReferenceTime() {
        return header.getReferenceTime();
    }

    public SACHeader getSacFileHeader() {
        return header;
    }

    public TimeT getTime() {
        return epoch.getTime();
    }

    public double getDelta() {
        return header.getDelta();
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public String getAgency() {
        return agency;
    }

    public void listHeader(PrintStream out) {
        computeDataStats();
        HeaderLister.getInstance().listHeader(header, out, getFilename().toString(), agency);
    }

    public void computeDataStats() {

        if (yValues != null) {

            DescriptiveStatistics stats = new DescriptiveStatistics();
            for (float v : yValues) {
                stats.addValue(v);
            }

            header.setDepmin(stats.getMin());
            header.setDepmax(stats.getMax());
            header.setDepmen(stats.getMean());
        } else {
            header.setDepmin(null);
            header.setDepmax(null);
            header.setDepmen(null);
        }

        Double evla = header.getEvla();
        Double evlo = header.getEvlo();
        if (evla != null && evlo != null && FlynnEngdahl.getInstance().isValid()) {
            Integer ievreg = FlynnEngdahl.getInstance().getGeoRegion(evla, evlo);
            header.setIevreg(ievreg);
        }

    }

    public TimeT getEndtime() {
        return epoch.getEndtime();
    }

    public Double getTimeFor(String refString) {
        switch (refString) {
        case "B":
            return header.getB();
        case "E":
            return header.getE();
        case "O":
            return header.getO();
        case "A":
            return header.getA();
        case "F":
            return header.getF();
        case "T0":
            return header.getT()[0];
        case "T1":
            return header.getT()[1];
        case "T2":
            return header.getT()[2];
        case "T3":
            return header.getT()[3];
        case "T4":
            return header.getT()[4];
        case "T5":
            return header.getT()[5];
        case "T6":
            return header.getT()[6];
        case "T7":
            return header.getT()[7];
        case "T8":
            return header.getT()[8];
        case "T9":
            return header.getT()[9];
        case "REFTIME":
            return 0.0;
        }
        return null;
    }

    public SacPlotData getPlotData() {
        Double referenceTime = header.getReferenceTime();
        if (referenceTime == null) {
            referenceTime = 0.0;
        }
        switch (header.getIftype()) {
        case ITIME:
            if (header.isEvenlySampled()) {
                return new SacPlotData(header.getB(), header.getDelta(), yValues, referenceTime);
            } else {
                return new SacPlotData(xValues, yValues, referenceTime);
            }
        case IGRP:
            return new SacPlotData(header.getB(), header.getDelta(), yValues, referenceTime);
        case IREAL:
            throw new IllegalStateException("not implemented.");
        case IRLIM:
        case IAMPH:
            return new SacPlotData(spectralData);
        case IXY:
            return new SacPlotData(xValues, yValues, referenceTime);
        default:
            throw new IllegalStateException("not implemented.");
        }
    }

    private Double getCutStart(PartialDataWindow pdw) {
        Double cutStart = null;
        Double refTime = this.getTimeFor(pdw.getStartReference());
        if (refTime != null) {
            cutStart = refTime + pdw.getStartOffset(getDelta());
        } else {
            String msg = String.format("Cannot set cut start for %s because %s is not set! Skipping file.", getFilename().toString(), pdw.getStartReference());
            log.warn(msg);
        }
        return cutStart;
    }

    private Double getCutEnd(PartialDataWindow pdw) {
        Double cutEnd = null;
        Double refTime = getTimeFor(pdw.getEndReference());
        if (refTime != null) {
            cutEnd = refTime + pdw.getEndOffset(getDelta());
        } else {
            String msg = String.format("Cannot set cut end for %s because %s is not set! Skipping file.", getFilename().toString(), pdw.getEndReference());
            log.warn(msg);
        }
        return cutEnd;
    }

    public boolean cut(PartialDataWindow pdw, CutErrorAction action) {
        Double cutStart = getCutStart(pdw);
        Double cutEnd = getCutEnd(pdw);
        try {
            return cut(cutStart, cutEnd, action);
        } catch (IllegalStateException ex) {
            log.warn(ex.getMessage());
            return false;
        }
    }

    public void markptp(PartialDataWindow pdw, double slidingWindowLength, int tIndex) {
        Double pdwStartSeconds = getCutStart(pdw);
        Double pdwEndseconds = getCutEnd(pdw);
        if (pdwStartSeconds != null && pdwEndseconds != null) {
            double delta = header.getDelta();
            int startOffset = (int) Math.round((pdwStartSeconds - header.getB()) / delta);
            int endOffset = (int) Math.round((pdwEndseconds - header.getB()) / delta);
            int npts = endOffset - startOffset + 1;
            int length = (int) Math.round(slidingWindowLength / delta) + 1;
            if (length > npts) {
                length = npts;
            } else if (length <= 0) {
                length = npts / 2;
            }
            double ptpval = 0.;
            int start = 0;
            int stop = length;
            int imax = 0;
            int indexOfMin = 0;
            int indexOfMax = 0;
            int ipmin = 0;
            while (stop <= npts) {
                double localMax = -Float.MAX_VALUE;
                double localMin = -localMax;
                for (int i = start; i < stop; i++) {
                    if (yValues[i] > localMax) {
                        localMax = yValues[i];
                        imax = i;
                    }
                    if (yValues[i] < localMin) {
                        localMin = yValues[i];
                        indexOfMin = i;
                    }
                }

                //    Check p-p in window against accumulating p-p
                if (localMax - localMin > ptpval) {
                    ptpval = localMax - localMin;
                    indexOfMax = imax;
                    ipmin = indexOfMin;
                }
                //    Slide window
                start++;
                stop++;
            }
            double minTime = header.getB() + ipmin * delta;
            double maxTime = header.getB() + indexOfMax * delta;

            header.setT(tIndex, minTime);
            header.setKt(tIndex, "PTPMIN");
            header.setT(tIndex + 1, maxTime);
            header.setKt(tIndex + 1, "PTPMAX");
            header.setUser(0, ptpval);
            header.setKuser0("PTPAMP"); //setKUser(tIndex, ptpval);
        } else {
            log.warn("Unable to create measurement window!");
        }
    }

    private boolean cut(Double cutStart, Double cutEnd, CutErrorAction action) {
        if (cutStart == null || cutEnd == null) {
            String msg = "Cut limits not properly set! Start = " + cutStart + ", End = " + cutEnd + " Skipping cut for this trace.";
            log.warn(msg);
            return true;
        }
        double startEpochTime = getTime().getEpochTime();
        double offset1 = cutStart - header.getB();
        double offset2 = cutEnd - header.getB();
        double newStart = startEpochTime + offset1;
        double newEnd = startEpochTime + offset2;
        Epoch trimEpoch = new Epoch(newStart, newEnd);
        if (cutStart >= header.getB() && cutEnd <= header.getE()) {
            trimTo(trimEpoch);
            header.setB(cutStart);
            header.setE(cutEnd);
            header.setNpts(getNsamp());
            return true;
        } else {
            switch (action) {
            case FATAL:
                return false;
            case USEBE:
                return true;
            case FILLZ: {
                if (cutStart < header.getB()) {
                    padFront(newStart, 0); // fillZ
                }
                if (cutEnd > header.getE()) {
                    padBack(newEnd, 0);// fillZ
                }
                trimTo(trimEpoch);
                header.setB(cutStart);
                header.setE(cutEnd);
                return true;
            }
            }
            return false;
        }
    }

    public void trimTo(Epoch epoch) {
        if (!(getTime().equals(epoch.getTime()) && getEndtime().equals(epoch.getEndtime()))) {
            cut(epoch.getTime(), epoch.getEndtime());
        }
    }

    public void FFT(SpectralData.PresentationFormat format) {
        if (!header.isEvenlySampled()) {
            throw new IllegalStateException("ERROR 1306: Illegal operation on unevenly spaced file");
        }
        Complex[] result = Miscellaneous.fft(yValues);

        int nfreqs = result.length;
        double dt = header.getDelta();
        double nyquist = 1.0 / (2.0 * dt);
        double deltaF = 2.0 * nyquist / nfreqs;

        // Apply scaling factor
        for (int j = 0; j < nfreqs; ++j) {
            result[j] = result[j].multiply(dt);
        }
        spectralData = new SpectralData(result, deltaF, format, yValues.length);
        header.updateForFFT(spectralData);
        yValues = null;
        xValues = null;
    }

    public boolean IFFT() {
        if (spectralData == null) {
            log.warn("No spectral data exists for trace!");
            return false;
        } else {
            Complex[] spectralData = this.spectralData.getFrequencyDomainData();
            try {
                double u = header.getSdelta();
                Complex[] tmp = Miscellaneous.ifft(spectralData);
                float[] v = new float[this.spectralData.getOriginalNsamps()];
                for (int j = 0; j < v.length; ++j) {
                    v[j] = (float) tmp[j].divide(u).getReal();
                }
                yValues = v;
                header.updateForIFFT();
                this.spectralData = null;
                return true;
            } catch (SignalProcessingException ex) {
                log.error("Failed performing IFFT", ex);
                return false;
            }
        }

    }

    public void cut(TimeT start, TimeT end) {
        if (start.ge(end)) {
            throw new IllegalArgumentException("Start time of cut is >= end time of cut.");
        }
        if (start.ge(getEndtime())) {
            throw new IllegalArgumentException("Start time of cut is >= end time of Seismogram.");
        }
        if (end.le(getTime())) {
            throw new IllegalArgumentException("End time of cut is <= start time of Seismogram.");
        }
        TimeT S = new TimeT(start);
        if (S.lt(getTime())) {
            S = getTime();
        }
        TimeT E = new TimeT(end);
        if (E.gt(this.getEndtime())) {
            E = getEndtime();
        }
        double duration = E.getEpochTime() - S.getEpochTime();
        yValues = getSubSection(S, duration);

        double adjustmentToB = start.getEpochTime() - epoch.getbeginning().getEpochTime();
        double adjustmentToE = end.getEpochTime() - epoch.getEnd();
        header.setB(header.getB() + adjustmentToB);
        header.setE(header.getE() + adjustmentToE);
        epoch = new Epoch(start, end);
    }

    private float[] getSubSection(TimeT start, double duration) {
        return getSubSection(start.getEpochTime(), duration);
    }

    private float[] getSubSection(double startEpoch, double requesteduration) {
        return getSubSection(startEpoch, requesteduration, null);
    }

    private float[] getSubSection(double startEpoch, double requestedDuration, float[] result) {
        int Nsamps = yValues.length;
        if (Nsamps >= 1) {
            double duration = Math.abs(requestedDuration);
            if (duration < getDelta()) {
                String msg = String.format("Requested duration of %f is less than the sample interval of %f", duration, getDelta());
                throw new IllegalStateException(msg);
            }
            int startIndex = getIndexForTime(startEpoch);
            int endIndex = getIndexForTime(startEpoch + duration);

            if (startIndex < 0) {
                startIndex = 0;
            }

            if (endIndex >= yValues.length) {
                endIndex = yValues.length - 1;
            }

            int sampsRequired = endIndex - startIndex + 1;
            if (result == null) {
                result = new float[sampsRequired];
            }
            try {
                System.arraycopy(yValues, startIndex, result, 0, sampsRequired);
            } catch (ArrayIndexOutOfBoundsException ex) {
                String msg = String.format("Requested %d samples from seismogram of length %d from index %d into buffer of length %d", sampsRequired, yValues.length, startIndex, result.length);
                throw new IllegalStateException(msg, ex);
            }
            return result;
        } else {
            return null;
        }
    }

    private int getIndexForTime(double epochtime) {
        double dataStart = this.getEpoch().getStart();
        return (int) Math.round((epochtime - dataStart) * getSamprate());
    }

    public double getSamprate() {
        return 1.0 / header.getDelta();
    }

    public void padFront(double newStart, float padValue) {
        if (newStart >= getTime().getEpochTime()) {
            return; // Nothing to do
        }
        double offset = getTime().getEpochTime() - newStart;
        int sampsToAdd = (int) Math.round(offset * getSamprate());
        int totalSamps = sampsToAdd + yValues.length;
        float[] tmp = new float[totalSamps];
        Arrays.fill(tmp, padValue);
        System.arraycopy(yValues, 0, tmp, sampsToAdd, yValues.length);
        epoch = new Epoch(newStart, epoch.getEnd());
        header.setB(header.getB() - newStart);
        yValues = tmp;
        header.setNpts(totalSamps);
    }

    public void padBack(double newEnd, float padValue) {
        double currentEnd = getEndtime().getEpochTime();
        if (newEnd <= currentEnd) {
            return; // Nothing to do
        }
        double offset = newEnd - currentEnd;
        int sampsToAdd = (int) Math.round(offset * getSamprate());
        int totalSamps = sampsToAdd + yValues.length;
        float[] tmp = new float[totalSamps];
        Arrays.fill(tmp, padValue);
        System.arraycopy(yValues, 0, tmp, 0, yValues.length);
        epoch = new Epoch(epoch.getStart(), newEnd);
        header.setE(header.getE() + offset);
        yValues = tmp;
        header.setNpts(totalSamps);
    }

    private void apply2(IIRFilter filt, boolean two_pass) {

        filt.initialize();
        filt.filter(yValues);
        if (two_pass) {
            SeriesMath.ReverseArray(yValues);
            filt.initialize();
            filt.filter(yValues);
            SeriesMath.ReverseArray(yValues);
        }
    }

    public void filter(FilterDesign design, int order, PassbandType passband, double cutoff1, double cutoff2, double epsilon, double transitionBW, boolean twoPass) {

        if (!header.isEvenlySampled()) {
            throw new IllegalStateException("ERROR 1306: Illegal operation on unevenly spaced file");
        }
        com.oregondsp.signalProcessing.filter.iir.IIRFilter filt = null;

        double dt = header.getDelta();

        switch (design) {
        case Butterworth:
            filt = new Butterworth(order, passband, cutoff1, cutoff2, dt);
            break;
        case Chebyshev1:
            filt = new ChebyshevI(order, epsilon, passband, cutoff1, cutoff2, dt);
            break;
        case Chebyshev2:
            filt = new ChebyshevII(order, epsilon, 1.0 + transitionBW, passband, cutoff1, cutoff2, dt);
            break;
        default:
            throw new IllegalArgumentException("Unsupported design: " + design);
        }

        if (filt != null) {
            apply2(filt, twoPass);
        }

    }

    public void setData(float[] x) {
        yValues = x;
        header.setNpts(x.length);
        resetStatistics();
    }

    public void resetStatistics() {
        float dmin = Float.MAX_VALUE;
        float dmax = Float.MIN_VALUE;
        float dmean = 0.0f;
        for (float yValue : yValues) {
            dmin = Math.min(dmin, yValue);
            dmax = Math.max(dmax, yValue);
            dmean += yValue;
        }
        header.setDepmin((double) dmin);
        header.setDepmax((double) dmax);
        header.setDepmen(((double) dmean) / yValues.length);
    }

    public SACFile getSacFile() {
        if (yValues != null) {
            if (xValues == null) {
                return new SACFile(header, yValues);
            } else {
                return new SACFile(header, yValues, xValues);
            }
        } else if (spectralData != null) {
            switch (header.getIftype()) {
            case IRLIM: {
                float[] data1 = spectralData.getRealArray();
                float[] data2 = spectralData.getImagArray();
                return new SACFile(header, data1, data2);
            }
            case IAMPH: {
                float[] data1 = spectralData.getAmplitudeArray();
                float[] data2 = spectralData.getPhaseArray();
                return new SACFile(header, data1, data2);
            }
            default: {
                throw new IllegalStateException("Spectral data with IFTYPE = " + header.getIftype() + " not allowed!");
            }
            }
        }
        throw new IllegalStateException("Failed creating SAC file from trace data!");
    }

    public boolean isPlottable() {
        return (yValues != null && yValues.length > 1) || (spectralData != null && spectralData.isPlottable());
    }

    public boolean isSpectral() {
        return spectralData != null;
    }

    public float[] getPartialDataWindow(PartialDataWindow pdw) {
        if (yValues == null || yValues.length == 0) {
            log.warn("No y-values available!");
            return null;
        }
        Double pdwStartSeconds = getCutStart(pdw);
        Double pdwEndseconds = getCutEnd(pdw);
        if (pdwStartSeconds != null && pdwEndseconds != null) {
            double delta = header.getDelta();
            int startOffset = (int) Math.round((pdwStartSeconds - header.getB()) / delta);
            int endOffset = (int) Math.round((pdwEndseconds - header.getB()) / delta);
            int npts = endOffset - startOffset + 1;
            float[] result = new float[npts];
            System.arraycopy(yValues, startOffset, result, 0, npts);
            return result;
        }
        return null;
    }

}
