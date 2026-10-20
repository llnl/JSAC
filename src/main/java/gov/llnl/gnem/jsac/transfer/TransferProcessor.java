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
package gov.llnl.gnem.jsac.transfer;

import static tec.units.ri.unit.Units.METRE;
import static tec.units.ri.unit.Units.METRE_PER_SECOND;
import static tec.units.ri.unit.Units.METRE_PER_SQUARE_SECOND;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.apache.commons.math3.complex.Complex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isti.jevalresp.ChannelMatchPolicy;
import com.isti.jevalresp.ResponseUnits;
import com.isti.jevalresp.UnitsStatus;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.dataAccess.ExternalWaveformServiceProvider;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.enums.DepVarType;
import gov.llnl.gnem.response.ChannelMatchPolicyHolder;
import gov.llnl.gnem.response.DCPFT;
import gov.llnl.gnem.response.FreqLimits;
import gov.llnl.gnem.response.InverseTransferFunction;
import gov.llnl.gnem.response.ResponseMetaData;
import gov.llnl.gnem.response.ResponseMetadataExtension;
import gov.llnl.gnem.response.ResponseTypeChecker;
import gov.llnl.gnem.response.ResponseType;
import gov.llnl.gnem.response.ResponseUnitsException;
import gov.llnl.gnem.response.TransferData;
import gov.llnl.gnem.response.TransferFunctionProcessor;
import gov.llnl.gnem.response.TransferFunctionUtils;
import llnl.gnem.core.dataObjects.DataAccessException;
import llnl.gnem.core.util.ApplicationLogger;
import llnl.gnem.core.util.Epoch;
import llnl.gnem.core.util.SeriesMath;
import llnl.gnem.core.util.StreamKey;
import llnl.gnem.core.util.TimeT;
import llnl.gnem.core.waveform.seismogram.CssSeismogram;
import llnl.gnem.core.responseProcessing.RESPFile;

/**
 *
 * @author dodge1
 */
public class TransferProcessor {

    private static final Logger log = LoggerFactory.getLogger(TransferProcessor.class);

    private final Map<StreamKey, File> keyFileMap;
    private final List<DatabaseTransferService> transferServices = new CopyOnWriteArrayList<>();

    private TransferProcessor() {
        keyFileMap = new HashMap<>();
        ServiceLoader<DatabaseTransferService> databaseTransferServices = ServiceLoader.load(DatabaseTransferService.class);
        for (DatabaseTransferService service : databaseTransferServices) {
            transferServices.add(service);
        }
    }

    public static TransferProcessor getInstance() {
        return TransferProcessorHolder.INSTANCE;
    }

    public void transfer(SacResponseType fromType, SacResponseType toType, FreqLimits freqLimits, ResponseOptions fromResponseOptions, Path fromFilePath, ResponseOptions toResponseOptions, Path toFilePath,
            StreamKey substituteKey, Double substituteTime) throws FileNotFoundException {
        if (fromType == SacResponseType.POLEZERO && fromResponseOptions != null && fromResponseOptions.getFilename() == null) {
            String pattern = "SAC_PZs_.+_.+_.+_.+_.+";
            int[] positions = {2, 3, 4, 5};
            populateKeyFileMap(fromResponseOptions.getSearchDir(), pattern, "_", positions);
        } else if (fromType == SacResponseType.EVALRESP && fromResponseOptions != null && fromResponseOptions.getFilename() == null) {
            String pattern = "RESP\\..+\\..+\\.*\\..+";
            int[] positions = {1, 2, 4, 3};
            populateKeyFileMap(fromResponseOptions.getSearchDir(), pattern, "\\.", positions);
        }
        long start = System.currentTimeMillis();

        SacDataModel.getInstance()
                .getData()
                .parallelStream()
                .forEach(std -> processOneTrace(std, fromType, toType, freqLimits, fromResponseOptions, fromFilePath, toResponseOptions, toFilePath, substituteKey, substituteTime));
        long end = System.currentTimeMillis();
        double elapsed = (end - start) / 1000.0;
        log.info(String.format("Transfer executed in %4.1f seconds.", elapsed));
    }

    private void processOneTrace(SacTraceData std, SacResponseType fromType, SacResponseType toType, FreqLimits freqLimits, ResponseOptions fromResponseOptions, Path fromFilePath,
            ResponseOptions toResponseOptions, Path toFilePath, StreamKey substituteKey, Double substituteTime) {
        try {
            DepVarType dtype = DepVarType.IUNKN;
            CssSeismogram s = std.produceSeismogram(substituteKey, substituteTime);
            Unit<?> requestedUnits = ResponseUnits.NANOMETER;
            TransferData from = getFromTransferFunction(s, fromType, fromResponseOptions, fromFilePath);
            TransferData to = null;
            switch (toType) {
                case POLEZERO:
                case EVALRESP:
                case CSS:
                    to = getFromTransferFunction(s, toType, toResponseOptions, toFilePath);
                    break;
                case ACC:
                    break;
                case DBASE:
                    break;
                case DIS:
                    break;
                case FAPFILE:
                    break;
                case NONE:
                    break;
                case VEL:
                    break;
                default:
                    break;
            }
            if (from == null && to == null) {
                return;
            } else if ((from != null) && (requestedUnitsAreDerivable(from.getWorkingUnits().getInputUnits(), toType) && to == null)) {
                switch (toType) {
                    case NONE:
                    case DIS:
                        requestedUnits = ResponseUnits.NANOMETER;
                        dtype = DepVarType.IDISP;
                        break;
                    case VEL:
                        requestedUnits = ResponseUnits.NANOMETER_PER_SECOND;
                        dtype = DepVarType.IVEL;
                        break;
                    case ACC:
                        requestedUnits = ResponseUnits.NANOMETER_PER_SQUARE_SECOND;
                        dtype = DepVarType.IACC;
                        break;
                    case CSS:
                        break;
                    case DBASE:
                        break;
                    case EVALRESP:
                        break;
                    case FAPFILE:
                        break;
                    case POLEZERO:
                        break;
                    default:
                        break;
                }
                if (!requestedUnits.equals(from.getWorkingUnits().getInputUnits())) {
                    try {
                        from.maybeTransformData(requestedUnits);
                        ResponseUnits tmp = new ResponseUnits(requestedUnits, from.getOriginalUnits().getUnitObj(), UnitsStatus.FORCED_VALUE);
                        from.setForcedUnits(tmp);
                    } catch (ResponseUnitsException ex) {
                        log.warn("Failed transforming response to required units!");
                    }
                }
            }
            processSeismogram(s, from, freqLimits, to, std, dtype);

            log.info("Done.\n");
        } catch (FileNotFoundException ex) {
            log.warn("Failed to find specified response file!");
        } catch (DataAccessException ex) {
            log.warn("Data Access Exception while retrieving seismogram data!");
        }
    }

    private void processSeismogram(CssSeismogram s, TransferData from, FreqLimits freqLimits, TransferData to, SacTraceData std, DepVarType dtype) {
        try {
            int nfft = TransferFunctionUtils.next2(s.getNsamp());
            Complex[] transformedSeis = transformSeisToFreqDomain(s);
            int nfreq = transformedSeis.length / 2 + 1;
            Unit<?> originalInputUnits = ResponseUnits.COUNT;
            ResponseMetaData metadata = null;
            if (from != null) {
                originalInputUnits = from.getOriginalUnits().getInputUnits();
                metadata = from.getMetadata();
            }

            InverseTransferFunction func = TransferFunctionUtils.buildInverseTransferFunction(s.getNsamp(), s.getSamprate(), metadata, freqLimits, from, to, originalInputUnits);
            Complex[] deconvolvedSpectrum = TransferFunctionUtils.convolveWithTransferFunction(func, nfreq, transformedSeis, nfft);

            float[] data = transformToTimeDomain(s.getNsamp(), nfft, deconvolvedSpectrum, s.getDelta());
            std.setData(data);
            std.getSACHeader().setIdep(dtype);
            std.computeDataStats();
        } catch (IOException ex) {
            log.warn("Failed building inverse transfer function!");
        }
    }

    private float[] transformToTimeDomain(int npts, int nfft, Complex[] transformedSeis, double delta) {
        /*
         * - Perform the inverse transform.
         */
        float[] result = new float[npts];
        double[] sre = new double[transformedSeis.length];
        double[] sim = new double[transformedSeis.length];
        for (int j = 0; j < sre.length; ++j) {
            Complex tmp = transformedSeis[j];
            sre[j] = tmp.getReal();
            sim[j] = tmp.getImaginary();
        }
        DCPFT dcpft = new DCPFT(nfft);
        dcpft.dcpft(sre, sim, nfft, 1);
        double scale = nfft * delta;
        /*
          * - Copy the transformed data back into the original data array.
         */
        for (int i = 0; i < result.length; ++i) {
            result[i] = (float) (sre[i] / scale);
        }
        return result;
    }

    private TransferData getFromTransferFunction(CssSeismogram s, SacResponseType fromType, ResponseOptions responseOptions, Path fromFilePath) throws DataAccessException, FileNotFoundException {
        StreamKey key = s.getStreamKey();
        ChannelMatchPolicy policy = ChannelMatchPolicyHolder.getInstance().getPolicy();

        switch (fromType) {
            case NONE:
                return null;
            case DATA_SERVICE: {
                return getFromTransferFunctionFromService(key, s, policy);
            }
            case EVALRESP:
                return EvalrespProcessor.getTransferFunction(responseOptions, key, s, policy, keyFileMap);
            case POLEZERO: {
                return PoleZeroProcessor.getTransferFunction(responseOptions, s, policy);
            }
            case FAPFILE:
                System.out.println("Not implemented yet");
                return null;
            case CSS:
                return getCssFromTransferFunction(fromFilePath, s, policy);
            case DBASE: {
                for (DatabaseTransferService service : transferServices) {
                    TransferData transferFunction = service.getFromTransfer(responseOptions, key, s, policy, keyFileMap);
                    if (transferFunction != null) {
                        return transferFunction;
                    }
                }
            }
            case ACC:
                break;
            case DIS:
                break;
            case VEL:
                break;
            default:
                break;
        }
        return null;
    }

    public TransferData getFromTransferFunctionFromService(StreamKey key, CssSeismogram s, ChannelMatchPolicy policy) throws DataAccessException, IllegalStateException {
        if (!ExternalWaveformServiceProvider.getInstance().getCurrentService().keyMeetsRequirements(key)) {
            return null;
        } else {
            try {
                ResponseOptions responseOptions = getResponseOptionsFromService(key, s.getEpoch());
                if (responseOptions != null) {
                    ResponseType aType = responseOptions.getResponseType();
                    if (aType != null) {
                        switch (aType) {
                            case EVRESP:
                                return EvalrespProcessor.getTransferFunction(responseOptions, key, s, policy, keyFileMap);
                            case CSS:
                            case FAP:
                            case FIRFAP:
                            case PAZ:
                            case PAZFAP:
                            case PAZFIR:
                                return getCssFromTransferFunction(Path.of(responseOptions.getFilename()), s, policy);
                            case SACPZF:
                                return PoleZeroProcessor.getTransferFunction(responseOptions, s, policy);
                            default:
                                ApplicationLogger.getInstance().log(Level.WARNING, "Unknown or unhandled response type " + aType);
                                return null;
                        }
                    }
                } else {
                    ApplicationLogger.getInstance().log(Level.WARNING, "Unable to retrieve response for: " + s);
                    return null;
                }
            } catch (IOException ex) {
                ApplicationLogger.getInstance().log(Level.SEVERE, "IO exception occurred retrieving response for" + s, ex);
                return null;
            }
        }
        return null;
    }

    public TransferData getFromTransferFunctionFromPath(Path path, CssSeismogram s, ChannelMatchPolicy policy) throws DataAccessException, IllegalStateException {
        try {
            ResponseOptions responseOptions = getResponseOptionsFromResponseFile(path);
            if (responseOptions != null) {
                ResponseType aType = responseOptions.getResponseType();
                if (aType != null) {
                    switch (aType) {
                        case EVRESP:
                            StreamKey key = makeKeyFromPath(path);
                            CssSeismogram s2 = new CssSeismogram(s.getWaveformID(), key, s.getData(), s.getSamprate(), s.getTime(), null, null);
                            return EvalrespProcessor.getTransferFunction(responseOptions, key, s2, policy, keyFileMap);
                        case CSS:
                        case FAP:
                        case FIRFAP:
                        case PAZ:
                        case PAZFAP:
                        case PAZFIR:
                            return getCssFromTransferFunction(Path.of(responseOptions.getFilename()), s, policy);
                        case SACPZF:
                            return PoleZeroProcessor.getTransferFunction(responseOptions, s, policy);
                        default:
                            ApplicationLogger.getInstance().log(Level.WARNING, "Unknown or unhandled response type " + aType);
                            return null;
                    }
                }
            } else {
                ApplicationLogger.getInstance().log(Level.WARNING, "Unable to retrieve response for: " + s);
                return null;
            }
        } catch (Exception ex) {
            throw new DataAccessException(ex.getMessage());
        }
        return null;
    }

    private TransferData getCssFromTransferFunction(Path fromFilePath, CssSeismogram s, ChannelMatchPolicy policy) throws IllegalStateException {
        if (fromFilePath == null) {
            throw new IllegalStateException("CSS option requires specification of a file name!");
        } else {
            Unit<?> forcedInputUnits = ResponseUnits.NANOMETER;
            ResponseType responseType = ResponseType.CSS;

            String network = null;
            String sta = null;
            String chan = null;
            String locid = null;

            Double startTime = TimeT.MIN_EPOCH_TIME;
            Double endTime = TimeT.MAX_EPOCH_TIME;

            ResponseMetadataExtension rme = null;
            ResponseMetaData rmd = new ResponseMetaData(fromFilePath.toString(), responseType, null, null, null, null, null, null, startTime, endTime, rme, null);

            try {
                TransferData transferData = new TransferFunctionProcessor().getFromTransferFunction(s.getNsamp(), s.getSamprate(), s.getTimeAsDouble(), network, sta, chan, locid, rmd, policy);
                if (forcedInputUnits != null && !forcedInputUnits.equals(transferData.getOriginalUnits().getInputUnits())) {
                    ResponseUnits ru = new ResponseUnits(forcedInputUnits, transferData.getOriginalUnits().getUnitObj(), UnitsStatus.FORCED_VALUE);
                    transferData.setForcedUnits(ru);
                }
                try {
                    transferData.maybeTransformData(forcedInputUnits);
                } catch (ResponseUnitsException ex) {
                    LoggerFactory.getLogger(TransferFunctionProcessor.class).trace(ex.toString(), ex);
                }

                return transferData;

            } catch (IOException ex) {
                log.warn("Failed retrieving FROM transfer function!");
                return null;
            }
        }
    }

    private Complex[] transformSeisToFreqDomain(CssSeismogram seis) {
        int nfft = TransferFunctionUtils.next2(seis.getNsamp());

        double[] sre = new double[nfft];
        double[] sim = new double[nfft];
        Complex[] result = new Complex[nfft];

        // Round rate where possible to avoid cache misses
        double roundedRate = TransferFunctionUtils.standardizeSampleRate(seis.getSamprate());
        double delta = 1.0 / roundedRate;

        /*
         * - Fill a complex zero-padded vector and then transform it.
         */
        // Remove trend so possible integration is more stable.
        float[] data = seis.getData();
        SeriesMath.RemoveTrend(data);
        for (int i = 0; i < data.length; ++i) {
            sre[i] = data[i] * delta; // Why multiply by delta?
            sim[i] = 0.0;
        }

        for (int i = seis.getNsamp(); i < nfft; ++i) {
            sre[i] = 0.0;
            sim[i] = 0.0;
        }

        DCPFT dcpft = new DCPFT(nfft);
        dcpft.dcpft(sre, sim, nfft, -1);
        for (int i = 0; i < nfft; ++i) {
            result[i] = new Complex(sre[i], sim[i]);
        }
        return result;
    }

    private boolean requestedUnitsAreDerivable(Unit<? extends Quantity<?>> inUnit, SacResponseType toType) {
        if (inUnit == null) {
            return false;
        }
        switch (toType) {
            case NONE:
            case VEL:
            case ACC:
            case DIS: {
                return (inUnit.isCompatible(METRE) || inUnit.isCompatible(METRE_PER_SECOND) || inUnit.isCompatible(METRE_PER_SQUARE_SECOND));
            }
            case EVALRESP:
            case POLEZERO:
            case FAPFILE:
            case CSS:
            case DBASE:
                return false;
        }
        return false;
    }

    private void populateKeyFileMap(Path searchDir, String pattern, String splitRegex, int[] positions) {
        keyFileMap.clear();
        FilenameFilter filter = new FileFilter(pattern);
        File file = searchDir.toFile();
        File[] files = file.listFiles(filter);
        for (File afile : files) {
            if (afile.isFile()) {
                StreamKey key = createStreamKey(afile, splitRegex, positions);
                keyFileMap.put(key, afile);
            }
        }
    }

    private StreamKey createStreamKey(File afile, String splitRegex, int[] positions) {
        String name = afile.getName();
        String[] tokens = name.split(splitRegex);
        if (tokens.length < 5) {
            throw new IllegalStateException("Failed creating StreamKey from: " + name);
        }
        String net = tokens[positions[0]];
        String sta = tokens[positions[1]];
        String chan = tokens[positions[2]];
        String locid = tokens[positions[3]];
        return new StreamKey(net, sta, chan, locid);
    }

    private ResponseOptions getResponseOptionsFromService(StreamKey key, Epoch epoch) throws DataAccessException {
        Path path = ExternalWaveformServiceProvider.getInstance().getCurrentService().retrieveResponseFile(key.getAgency(),
                key.getNet(), key.getSta(), key.getChan(), key.getLocationCode(), epoch);
        if (path != null) {
            Path parent = path.getParent();
            ResponseType type = ResponseTypeChecker.determineResponseType(path.toString());
            return new ResponseOptions(type, path.toString(), parent);
        } else {
            return null;
        }
    }

    private ResponseOptions getResponseOptionsFromResponseFile(Path path) throws DataAccessException {
        if (path != null) {
            Path parent = path.getParent();
            ResponseType type = ResponseTypeChecker.determineResponseType(path.toString());
            return new ResponseOptions(type, path.toString(), parent);
        } else {
            return null;
        }
    }

    private StreamKey makeKeyFromPath(Path path) throws IOException {
        RESPFile rf = new RESPFile(path);
        String network = rf.getNet();
        String station = rf.getSta();
        String chan = rf.getChan();
        String locid = rf.getLoc();
        return new StreamKey(network, station, chan, locid);
    }

    private static class FileFilter implements FilenameFilter {

        private final String pattern;
        private final Pattern r;

        public FileFilter(String aPattern) {
            pattern = aPattern;
            r = Pattern.compile(pattern);
        }

        @Override
        public boolean accept(File file, String line) {
            Matcher m = r.matcher(line);
            return m.find();
        }

    }

    private static class TransferProcessorHolder {

        private static final TransferProcessor INSTANCE = new TransferProcessor();
    }
}
