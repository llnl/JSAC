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
package gov.llnl.gnem.jsac.commands.dataFile;

import llnl.gnem.core.dataObjects.ExternalEvent;
import gov.llnl.gnem.fdsndata.FDSNWaveformService;
import llnl.gnem.core.dataObjects.StaChanInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.Misc;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.ExternalWaveformServiceProvider;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACHeader;
import gov.llnl.gnem.jsac.io.enums.EventType;
import gov.llnl.gnem.jsac.io.enums.Iztype;
import gov.llnl.gnem.jsac.io.enums.MagType;
import gov.llnl.gnem.jsac.util.EventModel;
import gov.llnl.gnem.jsac.util.PathManager;
import gov.llnl.gnem.jsac.util.SeismogramMerger;
import java.nio.file.Path;
import java.util.logging.Level;
import llnl.gnem.core.dataObjects.DataAccessException;
import llnl.gnem.core.util.ApplicationLogger;
import llnl.gnem.core.util.Epoch;
import llnl.gnem.core.util.Geometry.EModel;
import llnl.gnem.core.util.StationKey;
import llnl.gnem.core.util.StreamKey;
import llnl.gnem.core.util.TimeT;
import llnl.gnem.core.waveform.merge.MergeException;
import llnl.gnem.core.waveform.seismogram.CssSeismogram;
import llnl.gnem.core.dataObjects.ExternalWaveformService;

public class ReadContinuousSeismogramSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("SERVICE", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("AGENCY", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("NETWORK", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("STATION", ValuePossibilities.ONE_OR_MORE, String.class));
        descriptors.add(new AttributeDescriptor("STA", ValuePossibilities.ONE_OR_MORE, String.class));
        descriptors.add(new AttributeDescriptor("CHANNEL", ValuePossibilities.ONE_OR_MORE, String.class));
        descriptors.add(new AttributeDescriptor("CHAN", ValuePossibilities.ONE_OR_MORE, String.class));
        descriptors.add(new AttributeDescriptor("LOCID", ValuePossibilities.ONE_OR_MORE, String.class));
        descriptors.add(new AttributeDescriptor("LOCATION_CODE", ValuePossibilities.ONE_OR_MORE, String.class));
        descriptors.add(new AttributeDescriptor("START", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("START_DATE", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("END", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("END_DATE", ValuePossibilities.ONE_VALUE, String.class));

        descriptors.add(new AttributeDescriptor("STATION_BOX", ValuePossibilities.FOUR_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("STATION_CIRCLE", ValuePossibilities.THREE_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("STATION_CIRCLE_KM", ValuePossibilities.THREE_VALUES, Double.class));

        descriptors.add(new AttributeDescriptor("MORE", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("LIST_SERVICES", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("LIST_AGENCIES", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("LIST_NETWORKS", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("LIST_STATIONS", ValuePossibilities.TWO_VALUES, String.class));
        descriptors.add(new AttributeDescriptor("LIST_TIME_FORMATS", ValuePossibilities.NO_VALUE, null));

        descriptors.add(new AttributeDescriptor("EVENT_ID", ValuePossibilities.ONE_OR_MORE, String.class));
        descriptors.add(new AttributeDescriptor("EVENT_AGENCY", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("SRC_STA_RAD_DEG", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("SRC_STA_RAD_KM", ValuePossibilities.ONE_VALUE, Double.class));

        descriptors.add(new AttributeDescriptor("MIN_DURATION_SECONDS", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("MAX_DURATION_SECONDS", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("PRE_EVENT_SECONDS", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("CUTOFF_VELOCITY", ValuePossibilities.ONE_VALUE, Double.class));

    }
    private static String agency = "IRISDMC";
    private static String networkCode = null;
    private ExternalWaveformService waveformService;
    private final List<String> stationCodes = new ArrayList<>();
    private final List<String> channels = new ArrayList<>();
    private final List<String> locationCodes = new ArrayList<>();

    private static final List<String> eventIds = new ArrayList<>();
    private static String eventAgency = "ISC";
    private boolean eventBasedRetrieval = false;

    private static Double startTime = null;
    private static Double endTime = null;

    private boolean useStationBox = false;
    private double minStaLat;
    private double maxStaLat;
    private double minStaLon;
    private double maxStaLon;

    private boolean useStationCircle = false;
    private double staCenterLat;
    private double staCenterLon;
    private double staCircleRadiusDeg;

    private boolean listServices = false;
    private boolean listAgencies = false;
    private boolean listNetworks = false;
    private boolean listStations = false;

    private String listingAgency = null;
    private String listingNetwork = null;
    private boolean more = false;

    private static double MIN_DURATION_SECONDS = 500.0;    //min value for calculated durations
    private static double MAX_DURATION_SECONDS = 25000.0;   //max value for calculated durations
    private static double PRE_EVENT_SECONDS = 300;
    private static double CUTOFF_VELOCITY = 1;
//    private Double cutoffSeconds = null;
    private static double srcStaRadiusDeg = 30.0;
    private static double srcStaRadiusKm = 1000.0;
    private boolean restrictStationsBySourceDistance;
    private boolean useSourceDistInDeg;
    private boolean listFormats;

    public ReadContinuousSeismogramSacCommand() {

    }

    @Override
    public void initialize(String[] tokens) {
        waveformService = ExternalWaveformServiceProvider.getInstance().getCurrentService();
        agency = "IRISDMC";
        eventAgency = "ISC";
        more = false;
        listServices = false;
        listAgencies = false;
        listNetworks = false;
        listStations = false;
        listFormats = false;
        useStationBox = false;
        useStationCircle = false;
        eventBasedRetrieval = false;
        restrictStationsBySourceDistance = false;
        useSourceDistInDeg = true;
        stationCodes.clear();
        channels.clear();
        locationCodes.clear();
        eventIds.clear();
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            return;
        }

        List<Object> serviceName = parsedTokens.remove("SERVICE");
        if (serviceName != null && serviceName.size() == 1) {
            for (Object obj : serviceName) {
                String service = (String) obj;
                ExternalWaveformServiceProvider.getInstance().setCurrentService(service.toUpperCase());
                waveformService = ExternalWaveformServiceProvider.getInstance().getCurrentService();
            }
        }

        List<Object> tmpList = parsedTokens.remove("LIST_SERVICES");
        if (tmpList != null) {
            listServices = true;
            return;
        }

        tmpList = parsedTokens.remove("LIST_AGENCIES");
        if (tmpList != null) {
            listAgencies = true;
            return;
        }

        tmpList = parsedTokens.remove("LIST_NETWORKS");
        if (tmpList != null) {
            if (tmpList.size() == 1) {
                for (Object obj : tmpList) {
                    listingAgency = (String) obj;
                }
                listNetworks = true;
                return;
            } else {
                throw new IllegalArgumentException("The LIST_NETWORKS option requires an AGENCY name!");
            }
        }

        tmpList = parsedTokens.remove("LIST_STATIONS");
        if (tmpList != null) {
            if (tmpList.size() == 2) {
                listingAgency = (String) tmpList.get(0);
                listingNetwork = (String) tmpList.get(1);
                listStations = true;
                return;
            } else {
                throw new IllegalArgumentException("The LIST_STATIONS option requires an AGENCY name and a NETWORK name in that order!");
            }
        }

        tmpList = parsedTokens.remove("LIST_TIME_FORMATS");
        if (tmpList != null) {
            listFormats = true;
        }
        tmpList = parsedTokens.remove("STATION_BOX");
        if (tmpList != null) {
            if (tmpList.size() == 4) {
                stationCodes.clear();
                minStaLat = (Double) tmpList.get(0);
                maxStaLat = (Double) tmpList.get(1);
                minStaLon = (Double) tmpList.get(2);
                maxStaLon = (Double) tmpList.get(3);
                useStationBox = true;
            } else {
                throw new IllegalArgumentException("The STATION_BOX option requires MIN_LAT MAX_LAT MIN_LON MAX_LON in that order!");
            }
        }

        tmpList = parsedTokens.remove("STATION_CIRCLE");
        if (tmpList != null) {
            if (tmpList.size() == 3) {
                stationCodes.clear();
                staCenterLat = (Double) tmpList.get(0);
                staCenterLon = (Double) tmpList.get(1);
                staCircleRadiusDeg = (Double) tmpList.get(2);
                useStationCircle = true;
            } else {
                throw new IllegalArgumentException("The STATION_CIRCLE option requires CENTER_LAT CENTER_LON RADIUS(Degrees) in that order!");
            }
        }

        tmpList = parsedTokens.remove("STATION_CIRCLE_KM");
        if (tmpList != null) {
            if (tmpList.size() == 3) {
                stationCodes.clear();
                staCenterLat = (Double) tmpList.get(0);
                staCenterLon = (Double) tmpList.get(1);
                double tmp = (Double) tmpList.get(2); // radius in km
                staCircleRadiusDeg = tmp * EModel.getDegreesPerKilometer(staCenterLat);
                useStationCircle = true;
            } else {
                throw new IllegalArgumentException("The STATION_CIRCLE_KM option requires CENTER_LAT CENTER_LON RADIUS(Kilometers) in that order!");
            }
        }

        List<Object> agencyName = parsedTokens.remove("AGENCY");
        if (agencyName != null && agencyName.size() == 1) {
            for (Object obj : agencyName) {
                agency = (String) obj;
            }
        }
        List<Object> network = parsedTokens.remove("NETWORK");
        if (network != null && network.size() == 1) {
            for (Object obj : network) {
                networkCode = (String) obj;
            }
        }
        List<Object> stations = parsedTokens.remove("STATION");
        if (stations == null) {
            stations = parsedTokens.remove("STA");
        }
        if (stations != null && stations.size() >= 1) {
            stationCodes.clear();
            for (Object obj : stations) {
                stationCodes.add((String) obj);
            }
        }
        List<Object> chans = parsedTokens.remove("CHANNEL");
        if (chans == null) {
            chans = parsedTokens.remove("CHAN");
        }
        if (chans != null && chans.size() >= 1) {
            channels.clear();
            for (Object obj : chans) {
                channels.add((String) obj);
            }
        }
        List<Object> locid = parsedTokens.remove("LOCID");
        if (locid == null) {
            locid = parsedTokens.remove("LOCATION_CODE");
        }
        if (locid != null && locid.size() >= 1) {
            locationCodes.clear();
            for (Object obj : locid) {
                locationCodes.add((String) obj);
            }
        }
        List<Object> start = parsedTokens.remove("START");
        if (start != null && start.size() == 1) {
            for (Object obj : start) {
                startTime = (Double) obj;
            }
        }
        start = parsedTokens.remove("START_DATE");
        if (start != null && start.size() == 1) {
            for (Object obj : start) {
                String dateString = (String) obj;
                startTime = Misc.parseTimeString(dateString);
            }
        }
        List<Object> end = parsedTokens.remove("END");
        if (end != null && end.size() == 1) {
            for (Object obj : end) {
                endTime = (Double) obj;
            }
        }
        end = parsedTokens.remove("END_DATE");
        if (end != null && end.size() == 1) {
            for (Object obj : end) {
                String dateString = (String) obj;
                endTime = Misc.parseTimeString(dateString);
            }
        }

        List<Object> eventAgencyName = parsedTokens.remove("EVENT_AGENCY");
        if (eventAgencyName != null && eventAgencyName.size() == 1) {
            for (Object obj : eventAgencyName) {
                eventAgency = (String) obj;
            }
        }

        List<Object> evids = parsedTokens.remove("EVENT_ID");
        if (evids != null && evids.size() >= 1) {
            eventIds.clear();
            for (Object obj : evids) {
                eventIds.add((String) obj);
            }
            eventBasedRetrieval = true;
        }

        List<Object> srcStaRad = parsedTokens.remove("SRC_STA_RAD_DEG");
        if (srcStaRad != null && srcStaRad.size() == 1) {
            for (Object obj : srcStaRad) {
                srcStaRadiusDeg = (Double) obj;
            }
            restrictStationsBySourceDistance = true;
            useSourceDistInDeg = true;

        }
        srcStaRad = parsedTokens.remove("SRC_STA_RAD_KM");
        if (srcStaRad != null && srcStaRad.size() == 1) {
            for (Object obj : srcStaRad) {
                srcStaRadiusKm = (Double) obj;
            }
            restrictStationsBySourceDistance = true;
            useSourceDistInDeg = false;

        }
        if (channels.isEmpty()) {
            channels.add("*");
        }
        if (locationCodes.isEmpty()) {
            locationCodes.add("*");
        }

        List<Object> tmp = parsedTokens.remove("MIN_DURATION_SECONDS");
        if (tmp != null && tmp.size() == 1) {
            for (Object obj : tmp) {
                MIN_DURATION_SECONDS = (Double) obj;
            }
        }

        tmp = parsedTokens.remove("MAX_DURATION_SECONDS");
        if (tmp != null && tmp.size() == 1) {
            for (Object obj : tmp) {
                MAX_DURATION_SECONDS = (Double) obj;
            }
        }

        tmp = parsedTokens.remove("PRE_EVENT_SECONDS");
        if (tmp != null && tmp.size() == 1) {
            for (Object obj : tmp) {
                PRE_EVENT_SECONDS = (Double) obj;
            }
        }

        tmp = parsedTokens.remove("CUTOFF_VELOCITY");
        if (tmp != null && tmp.size() == 1) {
            for (Object obj : tmp) {
                CUTOFF_VELOCITY = (Double) obj;
            }
        }

        List<Object> lo = parsedTokens.remove("MORE");
        if (lo != null) {
            more = true;
        }
        tmp = parsedTokens.remove(TokenListParser.LEFT_OVER_TOKENS);
        if (tmp != null && !tmp.isEmpty()) {
            for (Object obj : tmp) {
                System.out.println("Unrecognized token: " + obj);
            }
        }
    }

    private boolean validateArguments() {

        if (networkCode == null && waveformService.isRequiresNetworkCode()) {
            System.out.println("You must specify a network code!");
            return false;
        }
        if (stationCodes.isEmpty() && !useStationBox && !useStationCircle && !eventBasedRetrieval) {
            System.out.println("You must specify one or more station codes or else a station search strategy unless performing event-based retrieval!");
            return false;
        }
        if (startTime == null && !eventBasedRetrieval) {
            System.out.println("You must specify a start time unless performing event-based retrieval!");
            return false;
        }
        if (endTime == null && !eventBasedRetrieval) {
            System.out.println("You must specify an end time unless performing event-based retrieval!");
            return false;
        }
        if (eventBasedRetrieval) {
            startTime = null;
            endTime = null;
        }
        return true;
    }

    @Override
    public void execute() {
        if (listFormats) {
            System.out.println(Misc.listTimeFormats());
            return;
        }
        if (listServices) {
            for (String string : ExternalWaveformServiceProvider.getInstance().getServiceDescriptions()) {
                System.out.println(string);
            }
            return;
        }
        if (listAgencies) {
            try {
                for (String str : waveformService.getWaveformAgencies()) {
                    System.out.println(str);
                }
                return;
            } catch (DataAccessException ex) {
                ApplicationLogger.getInstance().log(Level.WARNING, "Failed retrieving agency list!");
                return;
            }
        } else if (listNetworks && listingAgency != null && !listingAgency.isEmpty()) {
            try {
                for (String str : waveformService.getNetworks(listingAgency)) {
                    System.out.println(str);
                }
                return;
            } catch (DataAccessException ex) {
                ApplicationLogger.getInstance().log(Level.WARNING, "Failed retrieving network list!");
                return;
            }
        } else if (listStations && listingAgency != null && !listingAgency.isEmpty() && listingNetwork != null && !listingNetwork.isEmpty()) {
            try {
                for (String str : waveformService.getStations(listingAgency, listingNetwork)) {
                    System.out.println(str);
                }
                return;
            } catch (DataAccessException ex) {
                ApplicationLogger.getInstance().log(Level.WARNING, "Failed retrieving station list!");
                return;
            }
        }
        if (!validateArguments()) {
            return;
        }

        if (!more) {
            SacDataModel.getInstance().clear();
        }
        List<SacTraceData> results = new ArrayList<>();
        if (waveformService.getName().equals("FDSN")) {
            System.out.println(String.format("\t(using service %s, agency %s, network %s, and event-agency %s)",
                    waveformService.getName(),
                    agency,
                    networkCode,
                    eventAgency));
        } else if (waveformService.getName().equals("GMPDB")) {
            System.out.println(String.format("\t(using service %s, agency %s, and  network %s EVENT AGENCY is LLNL)",
                    waveformService.getName(),
                    agency,
                    networkCode));
        } else {
            System.out.println(String.format("\t(using service %s for waveforms and event information. Agency and network code are ignored.)", waveformService.getName()));
        }
        Path path = PathManager.getInstance().getCurrentDir();
        long start = System.currentTimeMillis();

        try {
            if (useStationBox) {
                List<String> staCodes = waveformService.getStations(agency,
                        networkCode,
                        minStaLat,
                        maxStaLat,
                        minStaLon,
                        maxStaLon);
                if (!staCodes.isEmpty()) {
                    stationCodes.addAll(staCodes);
                } else {
                    throw new IllegalStateException("Specified station box has no stations!");
                }
            } else if (useStationCircle) {
                List<String> staCodes = waveformService.getStations(agency,
                        networkCode,
                        staCenterLat,
                        staCenterLon,
                        staCircleRadiusDeg);
                if (!staCodes.isEmpty()) {
                    stationCodes.addAll(staCodes);
                } else {
                    throw new IllegalStateException("Specified station circle has no stations!");
                }

            }
            if (eventBasedRetrieval) {
                for (String eventId : eventIds) {
                    ExternalEvent event = EventModel.getInstance().getEvent(eventAgency, eventId);
                    if (event != null) {
                        if (restrictStationsBySourceDistance) {
                            Double srcCircleRadiusDeg = useSourceDistInDeg ? srcStaRadiusDeg : srcStaRadiusKm * EModel.getDegreesPerKilometer(event.getLatitude());
                            List<String> staCodes = waveformService.getStations(agency,
                                    networkCode,
                                    event.getLatitude(),
                                    event.getLongitude(),
                                    srcCircleRadiusDeg);
                            stationCodes.clear();
                            stationCodes.addAll(staCodes);
                        }
                        Collection<SacTraceData> tmpResult = performEventBasedSeismogramRetrieval(path, event);
                        maybeFilterResults(tmpResult, results);
                    } else {
                        System.getLogger(FDSNWaveformService.class.getName()).log(System.Logger.Level.ERROR, () -> "Failed retrieving event information for " + eventId + "!");
                    }
                }

            } else {
                results.addAll(performNonEventBasedSeismogramRetrieval(path));
            }

        } catch (DataAccessException | MergeException ex) {
            System.getLogger(FDSNWaveformService.class.getName()).log(System.Logger.Level.ERROR, "Failed retrieving seismogram data!", ex);
        }

        long end = System.currentTimeMillis();
        double elapsed = (end - start) / 1000.0;
        SacDataModel.getInstance().addAll(results, elapsed);

    }

    public void maybeFilterResults(Collection<SacTraceData> tmpResult, List<SacTraceData> results) {
        if (restrictStationsBySourceDistance) {
            for (SacTraceData sd : tmpResult) {
                if (useSourceDistInDeg) {
                    if (sd.getSACHeader().getGcarc() <= srcStaRadiusDeg) {
                        results.add(sd);
                    }

                } else {
                    if (sd.getSACHeader().getDist() <= srcStaRadiusKm) {
                        results.add(sd);
                    }
                }
            }
        } else {
            results.addAll(tmpResult);
        }
    }

    public List<SacTraceData> performNonEventBasedSeismogramRetrieval(Path path) throws MergeException, DataAccessException {
        List<SacTraceData> results = new ArrayList<>();
        Epoch epoch = new Epoch(startTime, endTime);
        Map<StreamKey, StaChanInfo> staInfoMap = waveformService.getStaChanInfo(agency, networkCode, stationCodes, channels, locationCodes, epoch);
        Map<StreamKey, Collection<CssSeismogram>> seismograms = waveformService.getSeismograms(agency,
                networkCode,
                stationCodes, channels, locationCodes, epoch);
        for (StreamKey key : seismograms.keySet()) {
            Collection<CssSeismogram> seis = seismograms.get(key);
            if (seis.isEmpty()) {
                continue;
            }
            CssSeismogram merged = SeismogramMerger.mergeWaveforms(seis);
            SacTraceData std = new SacTraceData(merged, path);
            std.setAgency(agency);
            StaChanInfo sci = staInfoMap.get(key);
            if (sci != null) {
                SACHeader header = std.getSACHeader();
                header.setCmpaz(sci.getAzimuth());
                Double inc = sci.getDip() != null ? 90 - sci.getDip() : null;
                header.setCmpinc(inc);
                header.setStla(sci.getLat());
                header.setStlo(sci.getLon());
                header.setStel(sci.getElev());
                header.setStdp(sci.getDepth());
                header.setKhole(sci.getLocationCode());
                header.setKnetwk(networkCode);
                header.maybeUpdateDistAz();
            }
            results.add(std);
        }
        return results;
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"RCS", "READCONTINUOUSSEISMOGRAM"};
        return new ArrayList<>(Arrays.asList(names));
    }

    private Collection< SacTraceData> performEventBasedSeismogramRetrieval(Path path, ExternalEvent event) throws DataAccessException, MergeException {
        List<SacTraceData> results = new ArrayList<>();
        Epoch epoch = new Epoch(event.getTime().getEpochTime(), event.getTime().getEpochTime() + TimeT.SECPERDAY);
        Map<StreamKey, StaChanInfo> staInfoMap = waveformService.getStaChanInfo(agency, networkCode, stationCodes, channels, locationCodes, epoch);
        Map<StationKey, StaChanInfo> tmp = new HashMap<>();

        for (StreamKey key : staInfoMap.keySet()) {
            StaChanInfo sci = staInfoMap.get(key);
            tmp.put(key.getStationKey(), sci);
        }
        for (StationKey key : tmp.keySet()) {
            StaChanInfo sci = tmp.get(key);
            double distance = EModel.getDistanceWGS84(sci.getLat(), sci.getLon(), event.getLatitude(), event.getLongitude());
            epoch = getSegmentEpoch(event, distance);
            List<String> tmpStationCodes = new ArrayList<>();
            tmpStationCodes.add(sci.getStationCode());
            Map<StreamKey, Collection<CssSeismogram>> seismograms = waveformService.getSeismograms(agency,
                    networkCode,
                    tmpStationCodes, channels, locationCodes, epoch);
            for (StreamKey aKey : seismograms.keySet()) {
                Collection<CssSeismogram> seis = seismograms.get(aKey);
                if (seis.isEmpty()) {
                    continue;
                }
                CssSeismogram merged = SeismogramMerger.mergeWaveforms(seis);
                SacTraceData std = new SacTraceData(merged, path);
                std.setAgency(agency);
                StaChanInfo anSci = staInfoMap.get(aKey);
                if (anSci != null) {
                    SACHeader header = std.getSACHeader();
                    header.setCmpaz(anSci.getAzimuth());
                    Double inc = anSci.getDip() != null ? 90 - anSci.getDip() : null;
                    header.setCmpinc(inc);
                    header.setStla(anSci.getLat());
                    header.setStlo(anSci.getLon());
                    header.setStel(anSci.getElev());
                    header.setStdp(anSci.getDepth());
                    header.setKhole(anSci.getLocationCode());
                    header.setKnetwk(networkCode);
                    header.setEvla(event.getLatitude());
                    header.setEvlo(event.getLongitude());
                    header.setEvdp(event.getDepth());
                    header.setMag(event.getMagnitude());
                    header.setImagtyp(MagType.getMagTypeFromString(event.getMagType()));
                    header.setIevtyp(EventType.getEventTypeFromString(event.getEventType()));
                    double b = merged.getTimeAsDouble() - event.getTime().getEpochTime();
                    header.setB(b);
                    double referenceTime = merged.getTimeAsDouble() - b; // origin time
                    header.setReferenceTime(referenceTime);
                    header.setO(0.0);
                    header.setIztype(Iztype.IO);
                    header.setKdatrd();
                    header.setLovrok(1);
                    header.setNvhdr(6);
                    header.setScale(1.0);
                    header.setLcalda(1);
                    header.maybeUpdateDistAz();

                }
                results.add(std);
            }
        }

        return results;
    }

    private Epoch getSegmentEpoch(ExternalEvent event, double distance) {
        double eventWindowStart = event.getTime().getEpochTime() - PRE_EVENT_SECONDS;
        double duration;

        double computedDuration = Math.max(MIN_DURATION_SECONDS, distance / CUTOFF_VELOCITY);
        duration = Math.min(MAX_DURATION_SECONDS, computedDuration);
//        if (cutoffSeconds != null) {
//            duration = cutoffSeconds;
//        }
        double eventWindowEnd = event.getTime().getEpochTime() + duration;
        return new Epoch(eventWindowStart, eventWindowEnd);
    }

}
