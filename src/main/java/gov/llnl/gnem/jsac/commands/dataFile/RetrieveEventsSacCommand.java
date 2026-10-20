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

import llnl.gnem.core.dataObjects.EventSelectionCriteria;
import llnl.gnem.core.dataObjects.ExternalEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.Misc;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.ExternalWaveformServiceProvider;
import gov.llnl.gnem.jsac.util.EventModel;
import java.util.ServiceLoader;
import java.util.logging.Level;
import llnl.gnem.core.dataObjects.DataAccessException;
import llnl.gnem.core.dataObjects.MinMax;
import llnl.gnem.core.dataObjects.RestrictionCircle;
import llnl.gnem.core.dataObjects.RestrictionRectangle;
import llnl.gnem.core.util.ApplicationLogger;
import llnl.gnem.core.util.Geometry.EModel;
import llnl.gnem.core.util.TimeT;
import llnl.gnem.core.dataObjects.ExternalWaveformService;

public class RetrieveEventsSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();
    private static final ServiceLoader<ExternalWaveformService> waveformOpsList = ServiceLoader.load(ExternalWaveformService.class);

    static {
        descriptors.add(new AttributeDescriptor("SERVICE", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("AGENCY", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("LATEST", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("ALL_AGENCIES", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("LIST_SERVICES", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("LIST_AGENCIES", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("LIST_RETRIEVED", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("LIST_TIME_FORMATS", ValuePossibilities.NO_VALUE, null));

        descriptors.add(new AttributeDescriptor("TIME_LIMITS", ValuePossibilities.TWO_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("DATE_LIMITS", ValuePossibilities.TWO_VALUES, String.class));
        descriptors.add(new AttributeDescriptor("DEPTH_LIMITS", ValuePossibilities.TWO_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("MAG_LIMITS", ValuePossibilities.TWO_VALUES, Double.class));

        descriptors.add(new AttributeDescriptor("EVENT_BOX", ValuePossibilities.FOUR_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("EVENT_CIRCLE", ValuePossibilities.THREE_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("EVENT_CIRCLE_KM", ValuePossibilities.THREE_VALUES, Double.class));

        descriptors.add(new AttributeDescriptor("EVENT_ID", ValuePossibilities.ONE_OR_MORE, String.class));

        descriptors.add(new AttributeDescriptor("MORE", ValuePossibilities.NO_VALUE, null));

    }
    private static String agency = "ISC";
    private ExternalWaveformService waveformService;

    private MinMax timeRestriction = null;
    private MinMax magRestriction = null;
    private MinMax depthRestriction = null;
    private RestrictionRectangle srcBox = null;
    private RestrictionCircle evCircle;
    private boolean listServices = false;
    private boolean listAgencies;
    private boolean listRetrieved;
    private boolean retrieveAllAgencies;
    private static final List<String> eventIds = new ArrayList<>();
    private boolean eventBasedRetrieval = false;
    private boolean listFormats;

    public RetrieveEventsSacCommand() {

    }

    @Override
    public void initialize(String[] tokens) {
        srcBox = null;
        evCircle = null;
        timeRestriction = null;
        magRestriction = null;
        depthRestriction = null;
        listServices = false;
        listAgencies = false;
        listRetrieved = false;
        retrieveAllAgencies = false;
        eventIds.clear();
        eventBasedRetrieval = false;
        listFormats=false;
        waveformService = ExternalWaveformServiceProvider.getInstance().getCurrentService();
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);

        List<Object> serviceName = parsedTokens.remove("SERVICE");
        if (serviceName != null && serviceName.size() == 1) {
            for (Object obj : serviceName) {
                String service = (String) obj;
                ExternalWaveformServiceProvider.getInstance().setCurrentService(service.toUpperCase());
                waveformService = ExternalWaveformServiceProvider.getInstance().getCurrentService();
            }
        }

        if (parsedTokens.isEmpty()) {
            TimeT tmp = new TimeT();
            timeRestriction = new MinMax(tmp.getEpochTime() - TimeT.SECPERDAY, tmp.getEpochTime());
            return;
        }

        List<Object> tmpList = parsedTokens.remove("LIST_SERVICES");
        if (tmpList != null) {
            listServices = true;
            return;
        }

        List<Object> retrieveList = parsedTokens.remove("LIST_RETRIEVED");
        if (retrieveList != null) {
            listRetrieved = true;
            return;
        }
        tmpList = parsedTokens.remove("LIST_TIME_FORMATS");
        if (tmpList != null) {
            listFormats = true;
        }
        tmpList = parsedTokens.remove("EVENT_BOX");
        if (tmpList != null) {
            if (tmpList.size() == 4) {
                double minEventLat = (Double) tmpList.get(0);
                double maxEventLat = (Double) tmpList.get(1);
                double minEventLon = (Double) tmpList.get(2);
                double maxEventLon = (Double) tmpList.get(3);
                srcBox = new RestrictionRectangle(minEventLat, maxEventLat, minEventLon, maxEventLon);
            } else {
                throw new IllegalArgumentException("The EVENT_BOX option requires MIN_LAT MAX_LAT MIN_LON MAX_LON in that order!");
            }
        }

        tmpList = parsedTokens.remove("EVENT_CIRCLE");
        if (tmpList != null) {
            if (tmpList.size() == 3) {
                double eventCenterLat = (Double) tmpList.get(0);
                double eventCenterLon = (Double) tmpList.get(1);
                double eventCircleRadiusDeg = (Double) tmpList.get(2);
                evCircle = new RestrictionCircle(
                        eventCenterLat,
                        eventCenterLon,
                        eventCircleRadiusDeg);
            } else {
                throw new IllegalArgumentException("The EVENT_CIRCLE option requires CENTER_LAT CENTER_LON RADIUS(Degrees) in that order!");
            }
        }

        tmpList = parsedTokens.remove("EVENT_CIRCLE_KM");
        if (tmpList != null) {
            if (tmpList.size() == 3) {
                double eventCenterLat = (Double) tmpList.get(0);
                double eventCenterLon = (Double) tmpList.get(1);
                double tmp = (Double) tmpList.get(2); // radius in km
                double eventCircleRadiusDeg = tmp * EModel.getDegreesPerKilometer(eventCenterLat);
                evCircle = new RestrictionCircle(
                        eventCenterLat,
                        eventCenterLon,
                        eventCircleRadiusDeg);
            } else {
                throw new IllegalArgumentException("The EVENT_CIRCLE_KM option requires CENTER_LAT CENTER_LON RADIUS(Kilometers) in that order!");
            }
        }

        List<Object> agencyName = parsedTokens.remove("AGENCY");
        if (agencyName != null && agencyName.size() == 1) {
            for (Object obj : agencyName) {
                agency = (String) obj;
            }
        }
        List<Object> tokenArgs = parsedTokens.remove("TIME_LIMITS");
        if (tokenArgs != null && tokenArgs.size() == 2) {
            Double startTime = (Double) tokenArgs.get(0);
            Double endTime = (Double) tokenArgs.get(1);
            timeRestriction = new MinMax(startTime, endTime);
        }

        tokenArgs = parsedTokens.remove("DATE_LIMITS");
        if (tokenArgs != null && tokenArgs.size() == 2) {
            String dateString = (String) tokenArgs.get(0);
            Double startTime = Misc.parseTimeString(dateString);
            dateString = (String) tokenArgs.get(1);
            Double endTime = Misc.parseTimeString(dateString);
            timeRestriction = new MinMax(startTime, endTime);
        }

        tokenArgs = parsedTokens.remove("LATEST");
        if (tokenArgs != null) {
            TimeT tmp = new TimeT();
            timeRestriction = new MinMax(tmp.getEpochTime() - TimeT.SECPERDAY, tmp.getEpochTime());
        }
        tokenArgs = parsedTokens.remove("DEPTH_LIMITS");
        if (tokenArgs != null && tokenArgs.size() == 2) {
            Double minDepth = (Double) tokenArgs.get(0);
            Double maxDepth = (Double) tokenArgs.get(1);
            this.depthRestriction = new MinMax(minDepth, maxDepth);
        }

        tokenArgs = parsedTokens.remove("MAG_LIMITS");
        if (tokenArgs != null && tokenArgs.size() == 2) {
            Double minMag = (Double) tokenArgs.get(0);
            Double maxMag = (Double) tokenArgs.get(1);
            this.magRestriction = new MinMax(minMag, maxMag);
        }
        tokenArgs = parsedTokens.remove("LIST_AGENCIES");
        if (tokenArgs != null) {
            listAgencies = true;
        }
        tokenArgs = parsedTokens.remove("ALL_AGENCIES");
        if (tokenArgs != null) {
            retrieveAllAgencies = true;
        }

        List<Object> evids = parsedTokens.remove("EVENT_ID");
        if (evids != null && evids.size() >= 1) {
            eventIds.clear();
            for (Object obj : evids) {
                eventIds.add((String) obj);
            }
            eventBasedRetrieval = true;
        }
    }

    @Override
    public void execute() {
        if(listFormats){
            System.out.println(Misc.listTimeFormats());
            return;
        }
        if (listServices) {
            for (String string : ExternalWaveformServiceProvider.getInstance().getServiceDescriptions()) {
                System.out.println(string);
            }
            return;
        }
        if (listRetrieved) {
            EventModel.getInstance().listEvents();
            return;
        }
        if (listAgencies) {
            try {
                List<String> agencies = waveformService.getEventAgencies();
                for (String anAgency : agencies) {
                    System.out.println(anAgency);
                }
                System.out.println("");
                return;
            } catch (DataAccessException ex) {
                ApplicationLogger.getInstance().log(Level.SEVERE, "Failed retrieving agency names!");
                return;
            }

        }
        long start = System.currentTimeMillis();

        if (eventBasedRetrieval) {
            for (String eventId : eventIds) {
                ExternalEvent event = EventModel.getInstance().getEvent(agency, eventId);
                if (event != null) {
                    System.out.println(event);
                }
            }
            printEndMessage(start, 1);
            return;
        }

        EventSelectionCriteria criteria;
        List<String> agencies = getAgenciesToProcess();
        for (String anAgency : agencies) {
            if (srcBox != null) {
                criteria = new EventSelectionCriteria(anAgency, srcBox, timeRestriction, magRestriction, depthRestriction);
            } else if (evCircle != null) {
                criteria = new EventSelectionCriteria(anAgency, evCircle, timeRestriction, magRestriction, depthRestriction);
            } else {
                criteria = new EventSelectionCriteria(anAgency, timeRestriction, magRestriction, depthRestriction);

            }
            try {
                List<ExternalEvent> events = waveformService.getEvents(criteria);
                EventModel.getInstance().addEvents(events);
                EventModel.getInstance().listEvents();
            } catch (Exception ex) {
                System.getLogger(RetrieveEventsSacCommand.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }
        printEndMessage(start, agencies.size());

    }

    public void printEndMessage(long start, int numAgencies) {
        long end = System.currentTimeMillis();
        double elapsed = (end - start) / 1000.0;
        int nevents = EventModel.getInstance().size();
        String msg = String.format("\n%d events retrieved from %d agencies in %4.1f seconds", nevents, numAgencies, elapsed);
        System.out.println(msg);
    }

    List<String> getAgenciesToProcess() {
        List<String> result = new ArrayList<>();
        if (this.retrieveAllAgencies) {
            try {
                result.addAll(waveformService.getEventAgencies());
            } catch (DataAccessException ex) {
                ApplicationLogger.getInstance().log(Level.WARNING, "Exception occurred retrieving agency list!");
                System.getLogger(RetrieveEventsSacCommand.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        } else {
            result.add(agency);
        }
        return result;
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"REV", "RetrieveEvents"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
