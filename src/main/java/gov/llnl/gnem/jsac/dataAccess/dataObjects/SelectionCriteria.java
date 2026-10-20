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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectionCriteria {

    private static final Logger log = LoggerFactory.getLogger(SelectionCriteria.class);

    private static final String[] keyList = new String[] { "WFID", "EVID", "STA", "CHAN", "LOCID", "NET", "AGENCY", "MAG", "DEGDIST", "TIME" };
    private static final List<String> wordList = Arrays.asList(keyList);
    private final Collection<Long> waveformIds;
    private final Collection<Long> eventIds;
    private final Collection<String> stations;
    private final Collection<String> channels;
    private final Collection<String> locids;
    private final Collection<String> networks;
    private final Collection<String> agencies;
    private Double minMagnitude = null;
    private Double maxMagnitude = null;
    private Double minRadius = null;
    private Double maxRadius = null;
    private Double minTime = null;
    private Double maxTime = null;

    public SelectionCriteria(List<String> tokens) {
        waveformIds = new ArrayList<>();
        eventIds = new ArrayList<>();
        stations = new ArrayList<>();
        channels = new ArrayList<>();
        locids = new ArrayList<>();

        networks = new ArrayList<>();
        agencies = new ArrayList<>();
        Map<String, List<String>> cmdTokenMap = groupTokens(tokens);
        List<String> wfidStrings = cmdTokenMap.get("WFID");
        if (wfidStrings != null) {
            for (String tmp : wfidStrings) {
                waveformIds.add(Long.parseLong(tmp));
            }
        }

        List<String> evidStrings = cmdTokenMap.get("EVID");
        if (evidStrings != null) {
            for (String tmp : evidStrings) {
                eventIds.add(Long.parseLong(tmp));
            }
        }

        List<String> tmp = cmdTokenMap.get("STA");
        if (tmp != null && tmp.size() > 0) {
            stations.addAll(tmp);
        }
        tmp = cmdTokenMap.get("CHAN");
        if (tmp != null && tmp.size() > 0) {
            channels.addAll(tmp);
        }

        tmp = cmdTokenMap.get("LOCID");
        if (tmp != null && tmp.size() > 0) {
            locids.addAll(tmp);
        }

        tmp = cmdTokenMap.get("NET");
        if (tmp != null && tmp.size() > 0) {
            networks.addAll(tmp);
        }

        tmp = cmdTokenMap.get("AGENCY");
        if (tmp != null && tmp.size() > 0) {
            agencies.addAll(tmp);
        }

        tmp = cmdTokenMap.get("MAG");
        if (tmp != null) {
            if (tmp.size() != 2) {
                log.warn("MAG Option ignored! The MAG option requires 2 values (min and max) to be specified.");
            } else {
                minMagnitude = Double.parseDouble(tmp.get(0));
                maxMagnitude = Double.parseDouble(tmp.get(1));
            }
        }
        tmp = cmdTokenMap.get("DEGDIST");
        if (tmp != null) {
            if (tmp.size() != 2) {
                log.warn("DEGDIST Option ignored! The DEGDIST option requires 2 values (min and max) to be specified.");
            } else {
                minRadius = Double.parseDouble(tmp.get(0));
                maxRadius = Double.parseDouble(tmp.get(1));
            }
        }

        tmp = cmdTokenMap.get("TIME");
        if (tmp != null) {
            if (tmp.size() != 2) {
                log.warn("TIME Option ignored! The TIME option requires 2 values (min and max) to be specified.");
            } else {
                minTime = Double.parseDouble(tmp.get(0));
                maxTime = Double.parseDouble(tmp.get(1));
            }
        }
    }

    public Collection<Long> getWfidList() {
        return new ArrayList<>(waveformIds);
    }

    public Collection<Long> getEvidList() {
        return new ArrayList<>(eventIds);
    }

    public boolean hasChannelCriteria() {
        return !getStations().isEmpty() || !getChannels().isEmpty() || !getLocids().isEmpty() || !getNetworks().isEmpty() || !getAgencies().isEmpty();
    }

    public Collection<String> getStations() {
        return new ArrayList<>(stations);
    }

    public Collection<String> getChannels() {
        return new ArrayList<>(channels);
    }

    public Collection<String> getLocids() {
        return new ArrayList<>(locids);
    }

    public Collection<String> getNetworks() {
        return new ArrayList<>(networks);
    }

    public Collection<String> getAgencies() {
        return new ArrayList<>(agencies);
    }

    public boolean hasDistanceRestriction() {
        return getMinRadius() != null && getMaxRadius() != null;
    }

    public Double getMinRadius() {
        return minRadius;
    }

    public Double getMaxRadius() {
        return maxRadius;
    }

    public Double getMinTime() {
        return minTime;
    }

    public Double getMaxTime() {
        return maxTime;
    }

    private Map<String, List<String>> groupTokens(List<String> tokens) {
        Map<String, List<String>> result = new HashMap<>();
        String cmd = tokens.remove(0).toUpperCase();
        if (!wordList.contains(cmd)) {
            throw new IllegalArgumentException("First token must be a parameter name!");
        }
        List<String> values = new ArrayList<>();
        result.put(cmd, values);
        while (!tokens.isEmpty()) {
            String value = tokens.remove(0).toUpperCase();
            if (!wordList.contains(value)) {
                values.add(value);
            } else {
                cmd = value;
                values = new ArrayList<>();
                result.put(cmd, values);
            }
        }
        return result;
    }

    public boolean hasWfidList() {
        return !waveformIds.isEmpty();
    }

    public boolean hasEvidList() {
        return !eventIds.isEmpty();
    }

    public boolean hasMagnitudeRestriction() {
        return getMinMagnitude() != null && getMaxMagnitude() != null;
    }

    public Double getMinMagnitude() {
        return minMagnitude;
    }

    public Double getMaxMagnitude() {
        return maxMagnitude;
    }

    public boolean hasTimeRestriction() {
        return getMinTime() != null && getMaxTime() != null;
    }

}
