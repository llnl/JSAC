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
package gov.llnl.gnem.jsac.util;

import llnl.gnem.core.dataObjects.ExternalEvent;
import gov.llnl.gnem.jsac.dataAccess.ExternalWaveformServiceProvider;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.logging.Level;
import llnl.gnem.core.dataObjects.ExternalWaveformService;
import llnl.gnem.core.util.ApplicationLogger;

public class EventModel {

    Map<String, Map<String, ExternalEvent>> agencyEventMap;

    private EventModel() {
        agencyEventMap = new HashMap<>();
    }

    public void addEvents(List<ExternalEvent> events) {
        for (ExternalEvent event : events) {
            addSingleEvent(event);
        }
    }

    public void addSingleEvent(ExternalEvent event) {
        String agency = event.getAgency();
        Map<String, ExternalEvent> tmpMap = agencyEventMap.get(agency);
        if (tmpMap == null) {
            tmpMap = new HashMap<>();
            agencyEventMap.put(agency, tmpMap);
        }
        String eventId = event.getEventID();
        tmpMap.put(eventId, event);
    }

    public ExternalEvent getEvent(String agency, String eventId) {
        Map<String, ExternalEvent> tmpMap = agencyEventMap.get(agency);
        if (tmpMap != null) {
            ExternalEvent event = tmpMap.get(eventId);
            if (event != null) {// Already have this one.
                return event;
            } else {
                return retrieveAndStore(agency, eventId);
            }
        } else { // Don't have anything for specified agency.
            return retrieveAndStore(agency, eventId);
        }
    }

    private ExternalEvent retrieveAndStore(String agency, String eventId) {
        try {
            ExternalWaveformService service = ExternalWaveformServiceProvider.getInstance().getCurrentService();
            ExternalEvent event = service.getSingleEvent(agency, eventId);
            if (event != null) {
                addSingleEvent(event);
                return event;
            } else {
                return null;
            }
        } catch (Exception ex) {
            ApplicationLogger.getInstance().log(Level.INFO, "Could not retrieve event data for agency=" + agency + " and eventId=" + eventId + "!");
            return null;
        }

    }

    public void listEvents() {
        Collection<String> agencies = agencyEventMap.keySet();
        if (agencies != null) {
            for (String agency : agencies) {
                listEvents(agency);
            }
        }
    }

    public void listEvents(String agency) {
        Map<String, ExternalEvent> tmpMap = agencyEventMap.get(agency);
        if (tmpMap != null) {
            for (ExternalEvent event : tmpMap.values()) {
                System.out.println(event);
            }
            System.out.println("");
        }
    }

    public void clear() {
        agencyEventMap.clear();
    }

    public static EventModel getInstance() {
        return EventModelHolder.INSTANCE;
    }

    public int size() {
        int size = 0;
        Collection<String> agencies = agencyEventMap.keySet();
        if (agencies != null) {
            for (String agency : agencies) {
                size += agencyEventMap.get(agency).size();
            }
        }
        return size;
    }

    private static class EventModelHolder {

        private static final EventModel INSTANCE = new EventModel();
    }
}
