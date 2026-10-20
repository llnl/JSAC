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
package gov.llnl.gnem.jsac.io.gse;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.io.GseSeismogram;
import llnl.gnem.core.util.ApplicationLogger;
import llnl.gnem.core.util.StreamKey;
import llnl.gnem.core.util.TimeT;

/**
 *
 * @author dodge1
 */
public class Ims2WaveformReader {
    
    private static final Logger log = LoggerFactory.getLogger(Ims2WaveformReader.class);

    public static Collection<GseSeismogram> readWaveformFile(String filename) throws ParseException {
        Collection<GseSeismogram> result = new ArrayList<>();
        Map<StreamKey, Collection<GseSeismogram>> keySeismogramMap = new HashMap<>();
        List<List<String>> blockList = new ArrayList<>();
        List<String> block = null;
        try (Scanner sc = new Scanner(new File(filename))) {
            boolean inBlock = false;
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                block = new ArrayList<>();
                if (line.startsWith("WID2")) {
                    block.add(line);
                    blockList.add(block);
                    inBlock = true;
                    continue;
                }
                if (line.startsWith("CHK2") && inBlock && block != null) {
                    block.add(line);
                    inBlock = false;
                } else if (inBlock) {
                    block.add(line);
                }
            }
        } catch (FileNotFoundException ex) {
            log.error(ex.getLocalizedMessage(), ex);
        }
        for (List<String> ablock : blockList) {
            GseSeismogram seis = buildSeismogram(ablock);
            if (seis != null) {
                Collection<GseSeismogram> tmp = keySeismogramMap.get(seis.getStreamKey());
                if (tmp == null) {
                    tmp = new ArrayList<>();
                    keySeismogramMap.put(seis.getStreamKey(), tmp);
                }
                tmp.add(seis);
            }
        }
        for (StreamKey key : keySeismogramMap.keySet()) {
            Collection<GseSeismogram> tmp = keySeismogramMap.get(key);
            if (tmp.size() == 1) {
                result.addAll(tmp);
            } else if (tmp.size() > 1) {
                Collection<GseSeismogram> merged = maybeMergeSeismograms(tmp);
                result.addAll(merged);
            }
        }
        return result;
    }

    private static GseSeismogram buildSeismogram(List<String> block) throws ParseException {
        List<Integer> rawSamples = new ArrayList<>();
        Integer reportedChecksum = null;
        String wid2Line = block.remove(0);
        if (wid2Line.startsWith("WID2")) {

            String date = wid2Line.substring(5, 15);
            String timeString = wid2Line.substring(16, 28);
            TimeT time = getTime(date, timeString);
            String stationCode = wid2Line.substring(29, 34).trim();
            String chan = wid2Line.substring(35, 38).trim();
            String locid = wid2Line.substring(39, 43).trim();
            if (locid.isEmpty()) {
                locid = "--";
            }
            String subFormat = wid2Line.substring(44, 47).trim();
            WaveformFormat waveformFormat = WaveformFormat.valueOf(subFormat);
            int nSamples = Integer.parseInt(wid2Line.substring(48, 56).trim());
            double samprate = Double.parseDouble(wid2Line.substring(57, 68).trim());
            double calib = Double.parseDouble(wid2Line.substring(69, 79).trim());
            double calper = Double.parseDouble(wid2Line.substring(80, 87).trim());
            String instype = wid2Line.substring(88, 94).trim();
            double hang = Double.parseDouble(wid2Line.substring(95, 100).trim());
            double vang = Double.parseDouble(wid2Line.substring(101, 105).trim());

            String sta2Line = block.remove(0);
            if (sta2Line.startsWith("STA2")) {
                String network = sta2Line.substring(5, 14).trim();
                if (network.isEmpty()) {
                    network = null;
                }
                double lat = Double.parseDouble(sta2Line.substring(15, 24).trim());
                double lon = Double.parseDouble(sta2Line.substring(26, 35).trim());
                String coordSys = sta2Line.substring(36, 48).trim();
                double elev = Double.parseDouble(sta2Line.substring(48, 54).trim());
                double edepth = Double.parseDouble(sta2Line.substring(54, 59).trim());

                StreamKey key = new StreamKey(network, stationCode, chan, locid);
                String dat2Line = block.remove(0);
                if (dat2Line.startsWith("DAT2")) {
                    for (String line : block) {
                        if (line.startsWith("CHK2")) {
                            String tmp = line.substring(4).trim();
                            reportedChecksum = Integer.parseInt(tmp);

                        } else {
                            String[] tokens = line.split("\\s+");
                            for (String str : tokens) {
                                rawSamples.add(Integer.parseInt(str));
                            }
                        }
                    }
                    if (nSamples == rawSamples.size()) {
                        int checksum = CheckSumComputer.compute_checksum(rawSamples);
                        if (reportedChecksum != null && checksum == reportedChecksum) {
                            List<Integer> data = WaveformSampleDecoder.decodeValues(rawSamples, waveformFormat);
                            float[] floatData = convertToFloats(data);
                            return new GseSeismogram(null, key, floatData, samprate, time, calib, calper, instype, hang, vang, lat, lon, coordSys, elev, edepth);
                        } else {
                            log.warn("Checksum error!");
                        }
                    } else {
                        log.warn("Sample count != reported NumSamples!");
                    }
                } else {
                    log.warn("DAT2 line did not follow STA2 line!");
                }
            } else {
                log.warn("STA2 line did not follow WID2 line!");
            }
        } else {
            log.warn("Block did not begin with WID2!");
        }
        return null;
    }

    private static TimeT getTime(String date, String timeString) throws ParseException {
        return new TimeT(date + ":" + timeString, "yyyy/MM/dd:HH:mm:ss.SSS");
    }

    private static float[] convertToFloats(List<Integer> data) {
        float[] result = new float[data.size()];
        for (int j = 0; j < data.size(); ++j) {
            result[j] = data.get(j);
        }
        return result;
    }

    private static Collection<GseSeismogram> maybeMergeSeismograms(Collection<GseSeismogram> input) {
        Collection<GseSeismogram> result = new ArrayList<>();
        if (sampleRatesMatch(input)) {
            Map<Double, GseSeismogram> timeSeisMap = new TreeMap<>();
            for (GseSeismogram seis : input) {
                timeSeisMap.put(seis.getTimeAsDouble(), seis);
            }
            List<Double> times = new ArrayList<>(timeSeisMap.keySet());
            Double start = times.remove(0);
            GseSeismogram current = timeSeisMap.get(start);
            Double currentEnd = current.getEndtimeAsDouble();
            Double delta = current.getDelta();
            for (int j = 0; j < times.size(); ++j) {
                Double time = times.get(j);

                if (time <= currentEnd + 2 * delta) { // merge them
                    current = mergeSeismograms(current, timeSeisMap.get(time));
                    currentEnd = current.getEndtimeAsDouble();
                    if (j == times.size() - 1) { // No more seismograms to merge.
                        result.add(current);
                    }
                } else {
                    result.add(current);
                    current = timeSeisMap.get(time);
                    currentEnd = current.getEndtimeAsDouble();
                }
            }
        } else {
            return input;
        }

        return result;
    }

    private static boolean sampleRatesMatch(Collection<GseSeismogram> input) {
        Double rate = null;
        for (GseSeismogram seis : input) {
            if (rate == null) {
                rate = seis.getSamprate();
            } else if (rate != seis.getSamprate()) {
                return false;
            }
        }
        return true;
    }

    private static GseSeismogram mergeSeismograms(GseSeismogram current, GseSeismogram next) {
        float[] first = current.getData();
        float[] second = next.getData();
        double lengthInSeconds = next.getEndtimeAsDouble() - current.getTimeAsDouble();
        int nsamps = (int) Math.round(lengthInSeconds * current.getSamprate()) + 1;
        float[] target = new float[nsamps];
        System.arraycopy(first, 0, target, 0, first.length);

        int destPos = (int) Math.round((next.getTimeAsDouble() - current.getTimeAsDouble()) * current.getSamprate());
        System.arraycopy(second, 0, target, destPos, second.length);

        return new GseSeismogram(current.getWaveformID(),
                                 current.getStreamKey(),
                                 target,
                                 current.getSamprate(),
                                 current.getTime(),
                                 current.getCalib(),
                                 current.getCalper(),
                                 current.getInstype(),
                                 current.getHang(),
                                 current.getVang(),
                                 current.getLat(),
                                 current.getLon(),
                                 current.getCoordSys(),
                                 current.getElev(),
                                 current.getEdepth());
    }
}
