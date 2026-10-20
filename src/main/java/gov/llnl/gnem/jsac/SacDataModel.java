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
package gov.llnl.gnem.jsac;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oregondsp.signalProcessing.filter.iir.PassbandType;

import gov.llnl.gnem.jsac.commands.dataFile.CutErrSacCommand;
import gov.llnl.gnem.jsac.commands.dataFile.CutImSacCommand;
import gov.llnl.gnem.jsac.commands.dataFile.CutSacCommand;
import gov.llnl.gnem.jsac.commands.filtering.ContinuousCorrelator;
import gov.llnl.gnem.jsac.commands.filtering.Convolver;
import gov.llnl.gnem.jsac.commands.filtering.StackedCorrelator;
import gov.llnl.gnem.jsac.commands.filtering.Whitener;
import gov.llnl.gnem.jsac.commands.spectralAnalysis.HilbertTransformer;
import gov.llnl.gnem.jsac.commands.spectralAnalysis.HilbertTransformerType;
import gov.llnl.gnem.jsac.commands.unary.Differentiator;
import gov.llnl.gnem.jsac.commands.unary.DifferentiatorType;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData.CutErrorAction;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SpectralData;
import gov.llnl.gnem.jsac.io.SACHeader;
import gov.llnl.gnem.jsac.util.PartialDataWindow;
import gov.llnl.gnem.jsac.util.SacHeaderComparator;
import java.util.logging.Level;
import llnl.gnem.core.correlation.RealSequenceCorrelator;
import llnl.gnem.core.correlation.util.CorrelationMax;
import llnl.gnem.core.signalprocessing.filter.FilterDesign;
import llnl.gnem.core.util.ApplicationLogger;
import llnl.gnem.core.util.Epoch;
import llnl.gnem.core.util.PairT;
import llnl.gnem.core.util.TaperType;

/**
 *
 * @author dodge1
 */
public class SacDataModel {

    private static final Logger log = LoggerFactory.getLogger(SacDataModel.class);

    private final List<SacTraceData> traces;

    private SacDataModel() {
        traces = new ArrayList<>();
    }

    public int size() {
        return traces.size();
    }

    public boolean isEmpty() {
        return traces.isEmpty();
    }

    public void clear() {
        traces.clear();
    }

    public void setSingleSeismogram(SacTraceData data) {
        traces.clear();
        traces.add(data);
    }

    public void addAll(List<SacTraceData> data, Double elapsedSeconds) {
        int added = 0;
        PartialDataWindow pdw = CutSacCommand.getPartialDataWindow();
        if (pdw.isEnabled()) {
            CutErrorAction action = CutErrSacCommand.getErrorAction();

            for (SacTraceData std : data) {
                if (std.cut(pdw, action)) {
                    traces.add(std);
                    ++added;
                } else {
                    String msg = String.format("Cut failed for %s.", std.getFilename());
                    log.warn(msg);
                }
            }
        } else {
            added = data.size();
            traces.addAll(data);
        }
        String tmp = added == 1 ? "file" : "files";
        String msg = String.format("\n%d %s added in %4.1f seconds", added, tmp, elapsedSeconds);
        System.out.println(msg);
        System.out.println("SAC>");
    }

    public void cutFilesInMemory() {
        CutErrorAction action = CutErrSacCommand.getErrorAction();
        PartialDataWindow pdw = CutImSacCommand.getPartialDataWindow();
        traces.parallelStream().forEach(std -> {
            cutSingleTrace(std, pdw, action);
        });
    }

    private void cutSingleTrace(SacTraceData std, PartialDataWindow pdw, CutErrorAction action) {
        if (!std.cut(pdw, action)) {
            String msg = String.format("Cut failed for %s.", std.getFilename());
            log.warn(msg);
        }
    }

    public int getTraceCount() {
        return traces.size();
    }

    public List<SacTraceData> getData() {
        return new ArrayList<>(traces);
    }

    public static SacDataModel getInstance() {
        return SacDataModelHolder.INSTANCE;
    }

    public void applyFilter(FilterDesign design, int order, PassbandType passband, double cutoff1, double cutoff2, double epsilon, double transitionBW, boolean twoPass) {
        traces.parallelStream().forEach(std -> {
            std.filter(design, order, passband, cutoff1, cutoff2, epsilon, transitionBW, twoPass);
        });
        notifyViewsDataChanged();
    }

    private void notifyViewsDataChanged() {

    }

    public void applyTaper(TaperType taperType, double taperPercent) {
        traces.parallelStream().forEach(std -> {
            std.applyTaper(taperType, taperPercent);
        });
        notifyViewsDataChanged();
    }

    public void removeMean() {
        traces.parallelStream().forEach(std -> {
            std.removeMean();
        });
        notifyViewsDataChanged();
    }

    public void addConstants(List<Float> constants) {
        float constant = 0;
        int numConstants = constants.size();
        for (int j = 0; j < traces.size(); ++j) {
            SacTraceData std = traces.get(j);
            if (j > numConstants - 1) {
                constant = constants.get(numConstants - 1);
            } else {
                constant = constants.get(j);
            }
            std.addConstant(constant);
        }
        notifyViewsDataChanged();
    }

    public void multiplyByConstant(List<Float> constants) {
        float constant = 1;
        int numConstants = constants.size();
        for (int j = 0; j < traces.size(); ++j) {
            SacTraceData std = traces.get(j);
            if (j > numConstants - 1) {
                constant = constants.get(numConstants - 1);
            } else {
                constant = constants.get(j);
            }
            std.multiplyByConstant(constant);
        }
        notifyViewsDataChanged();
    }

    public void setAgency(List<String> agencies) {

        String lastAgency = null;
        int numAgencies = agencies.size();
        for (int j = 0; j < traces.size(); ++j) {
            SacTraceData std = traces.get(j);
            if (numAgencies == 0) {
                std.setAgency(lastAgency);
                continue;
            } else if (j > numAgencies - 1) {
                lastAgency = agencies.get(numAgencies - 1);
            } else {
                lastAgency = agencies.get(j);
            }
            std.setAgency(lastAgency.equals("--") ? null : lastAgency);
        }
        notifyViewsDataChanged();

    }

    public void squareTraceData() {
        traces.parallelStream().forEach(std -> {
            std.squareData();
        });
        notifyViewsDataChanged();
    }

    public void removeTrend() {
        traces.parallelStream().forEach(std -> {
            std.removeTrend();
        });
        notifyViewsDataChanged();
    }

    public void applyHilbert(HilbertTransformerType type) {
        traces.parallelStream().forEach(std -> {
            parallelHilbert(type, std);
        });
        notifyViewsDataChanged();
    }

    public void parallelHilbert(HilbertTransformerType type, SacTraceData std) {
        HilbertTransformer H = new HilbertTransformer(type);
        H.transform(std);
    }

    public void convolve(float[] template, boolean centered) {

        traces.parallelStream().forEach(std -> {
            parallelConvolution(template, centered, std);
        });
        notifyViewsDataChanged();

    }

    public void parallelConvolution(float[] template, boolean centered, SacTraceData std) {
        Convolver convolver = new Convolver(template, centered);
        convolver.convolve(std);
    }

    public void whiten(boolean common, boolean filterDesign, int N) {
        List<double[]> correlationFunctions = new ArrayList<>();

        traces.parallelStream().forEach(std -> {
            correlationFunctions.add(parallelWhiten(N, std, common));
        });

        if (common) {
            double[] avgCorrelation = new double[N + 1];
            for (double[] correlationFunction : correlationFunctions) {
                for (int i = 0; i <= N; i++) {
                    avgCorrelation[i] += correlationFunction[i];
                }
            }

            double[] reflectionCoefficients = new double[N];

            double[] predictor = Whitener.generatePredictor(avgCorrelation, reflectionCoefficients);
            traces.parallelStream().forEach(std -> {
                Whitener.applyPredictor(std, predictor);
            });
        }

        //Offset the start times to account for the order N clipping off the
        // front of the trace
        traces.parallelStream().forEach(std -> {
            SACHeader header = std.getSACHeader();
            header.setB(header.getB() + N * header.getDelta());
        });

        notifyViewsDataChanged();
    }

    public double[] parallelWhiten(int N, SacTraceData std, boolean common) {
        Whitener whitener = new Whitener(N);
        return whitener.whiten(std, common);
    }

    public void smooth(int halfWidth) {
        traces.parallelStream().forEach(std -> {
            std.smooth(halfWidth);
        });
        notifyViewsDataChanged();
    }

    public void interpolate(Double delta, Integer npts) {
        for (SacTraceData std : traces) {
            if (delta != null && delta > 0) {
                std.interpolate(1.0 / delta);
            } else if (npts != null && npts > 1) {
                double lengthInSeconds = std.getEpoch().getLengthInSeconds();
                double requestedDelta = lengthInSeconds / npts;
                std.interpolate(1.0 / requestedDelta);
            }
        }
        // Don't update view. The current view update code doesn't expect the data length to change.
    }

    public void computeEnvelope() {
        traces.parallelStream().forEach(std -> {
            std.computeEnvelope();
        });
        notifyViewsDataChanged();
    }

    public void squareRootTraceData() {
        traces.parallelStream().filter(std -> (std.isNonNegative())).forEachOrdered(std -> {
            std.squareRoot();
        });
        notifyViewsDataChanged();
    }

    public void absTraceData() {
        traces.parallelStream().forEach(std -> {
            std.abs();
        });
        notifyViewsDataChanged();
    }

    public void logTraceData() {
        traces.parallelStream().filter(std -> (std.isPositive())).forEachOrdered(std -> {
            std.log();
        });
        notifyViewsDataChanged();
    }

    public void log10TraceData() {
        traces.parallelStream().filter(std -> (std.isPositive())).forEachOrdered(std -> {
            std.log10();
        });
        notifyViewsDataChanged();
    }

    public void expTraceData() {
        traces.parallelStream().forEach(std -> {
            std.exp();
        });
        notifyViewsDataChanged();
    }

    public void exp10TraceData() {
        traces.parallelStream().forEach(std -> {
            std.exp10();
        });
        notifyViewsDataChanged();
    }

    public void differentiate(DifferentiatorType type) {
        traces.parallelStream().forEach(std -> {
            parallelDifferentiation(type, std);
        });
        notifyViewsDataChanged();
    }

    public void parallelDifferentiation(DifferentiatorType type, SacTraceData std) {
        Differentiator D = new Differentiator(type);
        D.differentiate(std);
        std.maybeUpdateIdepForDif();
    }

    public void reverseData() {
        traces.parallelStream().forEach(std -> {
            std.reverse();
        });
        notifyViewsDataChanged();
    }

    public void deleteChannels(boolean deleteAll, List<Integer> fileNumbersToDelete, List<String> filesToDelete) {

        if (deleteAll) {
            clear();
            notifyViewsDataChanged();
        } else {
            // Put indices in Set to avoid duplicates.
            Set<Integer> indices = new HashSet<>(fileNumbersToDelete);
            for (int j = 0; j < traces.size(); ++j) {
                SacTraceData std = traces.get(j);
                String name = std.getFilename().toString();
                if (filesToDelete.contains(name)) {
                    indices.add(j);
                }
            }
            //Sort into order high to low
            List<Integer> tmp = new ArrayList<>(indices);
            Collections.sort(tmp, Collections.reverseOrder());
            for (int jdx : tmp) {
                if (jdx >= 0 && jdx < traces.size()) {
                    traces.remove(jdx);
                }
            }
        }
    }

    public void changeHeaderValues(List<Integer> fileNumbers, List<PairT<String, Object>> newValues, Double allTValue, String newAgency) {
        List<SacTraceData> tracesToProcess = new ArrayList<>();
        if (fileNumbers.isEmpty()) {
            tracesToProcess.addAll(traces);
        } else {
            for (int idx : fileNumbers) {
                if (idx > 0 && idx <= traces.size()) {
                    tracesToProcess.add(traces.get(idx - 1));
                }
            }
        }
        for (SacTraceData std : tracesToProcess) {
            if (newAgency != null) {
                std.setAgency(newAgency);
            }
            for (PairT<String, Object> pair : newValues) {
                std.getSACHeader().setHeaderValue(pair.getFirst(), pair.getSecond());
                if (pair.getFirst().toUpperCase().equals("KSTNM")) {
                    std.setStationCode((String) pair.getSecond());
                } else if (pair.getFirst().equalsIgnoreCase("KNETWK")) {
                    std.setNetworkCode((String) pair.getSecond());
                } else if (pair.getFirst().equalsIgnoreCase("KCMPNM")) {
                    std.setChannelCode((String) pair.getSecond());
                } else if (pair.getFirst().equalsIgnoreCase("KHOLE")) {
                    std.setLocationCode((String) pair.getSecond());
                }
            }
            if (allTValue != null) {
                std.getSACHeader().setAllT(allTValue);
            }
        }
    }

    public void integrate() {
        traces.parallelStream().forEach(std -> {
            std.integrate(false);
        });
        notifyViewsDataChanged();
    }

    public void computeFFT(boolean removeMean, SpectralData.PresentationFormat format) {
        traces.parallelStream().forEach(std -> {
            transformSingleTrace(removeMean, std, format);
        });
    }

    private void transformSingleTrace(boolean removeMean, SacTraceData std, SpectralData.PresentationFormat format) {
        if (removeMean) {
            std.removeMean();
        }
        std.FFT(format);
    }

    public void computeIFFT() {
        traces.parallelStream().forEach(std -> {
            inverseTransformSingleTrace(std);
        });
    }

    private void inverseTransformSingleTrace(SacTraceData std) {
        std.IFFT();
    }

    public void divOmega() {
        traces.parallelStream().forEach(std -> {
            std.divOmega();
        });
    }

    public void mulOmega() {
        traces.parallelStream().forEach(std -> {
            std.mulOmega();
        });
    }

    public void sort(SacHeaderComparator comparator) {
        Map<SACHeader, SacTraceData> tmpMap = new HashMap<>();
        for (SacTraceData std : traces) {
            tmpMap.put(std.getSACHeader(), std);
        }
        List<SACHeader> tmpArray = new ArrayList<>(tmpMap.keySet());
        Collections.sort(tmpArray, comparator);
        traces.clear();
        for (SACHeader sh : tmpArray) {
            SacTraceData std = tmpMap.get(sh);
            traces.add(std);
        }
    }

    public void add(SacTraceData std) {
        traces.add(std);
    }

    public List<SacTraceData> getPlottableData() {
        List<SacTraceData> result = new ArrayList<>();
        for (SacTraceData std : traces) {
            if (std.isPlottable()) {
                result.add(std);
            }
        }
        return result;
    }

    public void copyHeaderValues(String fromValue, List<String> headerValues) {
        SacTraceData std = null;
        if (NumberUtils.isParsable(fromValue)) {
            int fileNumber = Integer.parseInt(fromValue) - 1;// Command is 1-based
            if (fileNumber < 0 || fileNumber > traces.size() - 1) {
                String msg = String.format("Supplied file number (%d) is out of range of data in memory!", fileNumber + 1);
                log.warn(msg);
                return;
            }
            std = traces.get(fileNumber);
        } else {
            std = findTraceByFileName(fromValue);
            System.out.println(std);
        }
        if (std == null) {
            String msg = String.format("Failed to find SAC file corresponding to: %s!", fromValue);
            log.warn(msg);
            return;
        }
        for (String variable : headerValues) {
            Object obj = std.getSACHeader().getValue(variable);
            if (obj != null) {
                for (SacTraceData std2 : traces) {
                    if (std2 != std) {
                        std2.getSACHeader().setHeaderValue(variable, obj);
                    }
                }
            }
        }
    }

    private SacTraceData findTraceByFileName(String fromValue) {
        for (SacTraceData std : traces) {
            Path path = std.getFilename();
            String fileNameString = path.toString();
            if (fileNameString.contains(fromValue)) {
                return std;
            }
        }
        return null;
    }

    public void synchronize(boolean begin, boolean round) {
        if (traces.size() < 2) {
            ApplicationLogger.getInstance().log(Level.FINE, "There must be at least two files in memory for the synchronize command.");
            return;
        }
        if (!begin) {
            Double latestStart = getLatestStartTime();
            for (SacTraceData std : traces) {
                Double myRefTime = std.getReferenceTime();
                Double diff = myRefTime - latestStart;
                std.getSACHeader().setReferenceTime(latestStart);
                std.getSACHeader().shiftAllT(diff);
            }
        } else {
            Double earliestRefTime = getEarliestRefTime();
            for (SacTraceData std : traces) {
                std.getSACHeader().setReferenceTime(earliestRefTime);
                Double beginTime = std.getBeginTimeMarker();
                if (beginTime != null && beginTime != 0) {
                    std.getSACHeader().shiftAllT(-beginTime);
                }
            }
        }
        if (round) {
            for (SacTraceData std : traces) {
                Double tmp = std.getBeginTimeMarker();
                double rate = std.getSamprate();
                if (rate >= 1.0) {
                    Long rounded = Math.round(tmp * rate);
                    double roundedBegin = (double) rounded / Math.round(rate);
                    double difference = tmp - roundedBegin;
                    std.getSACHeader().shiftAllT(-difference);
                }
            }
        }
    }

    private Double getLatestStartTime() {
        Double latest = Double.NEGATIVE_INFINITY;
        for (SacTraceData std : traces) {
            Epoch epoch = std.getEpoch();
            if (epoch.getStart() > latest) {
                latest = epoch.getStart();
            }
        }
        return latest;
    }

    private Double getEarliestRefTime() {
        Double earliest = Double.MAX_VALUE;
        for (SacTraceData std : traces) {
            if (std.getReferenceTime() < earliest) {
                earliest = std.getReferenceTime();
            }
        }
        return earliest;
    }

    private static class SacDataModelHolder {

        private static final SacDataModel INSTANCE = new SacDataModel();
    }

    public void correlate(float[] master,
            boolean normalize,
            boolean useSubwindows,
            int windowLength,
            int numberOfWindows,
            boolean statisticsOnly) {

        if (statisticsOnly) {
            for (SacTraceData std : traces) {
                float[] v = std.getData();
                CorrelationMax cm = RealSequenceCorrelator.correlate(master, v);
                System.out.println(std.getFilename());
                System.out.println(cm);
                System.out.println("------------------------------------------------------------\n");
            }
            return;
        }
        if (!useSubwindows) {
            traces.parallelStream().forEach(std -> {
                parallelContinuousCorrelate(master, normalize, std);
            });
        } else {
            traces.parallelStream().forEach(std -> {
                parallelStackedCorrelate(master, normalize, windowLength, numberOfWindows, std);
            });
        }
        notifyViewsDataChanged();

    }

    public void parallelContinuousCorrelate(float[] master, boolean normalize, SacTraceData std) {
        ContinuousCorrelator correlator = new ContinuousCorrelator(master, normalize);
        correlator.correlate(std);
    }

    public void parallelStackedCorrelate(float[] master, boolean normalize, int windowLength, int numberOfWindows, SacTraceData std) {
        StackedCorrelator correlator = new StackedCorrelator(master, normalize, windowLength, numberOfWindows, std.getNsamp());
        correlator.correlate(std);
    }

}
