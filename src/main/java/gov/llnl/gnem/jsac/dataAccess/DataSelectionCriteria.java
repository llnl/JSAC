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

package gov.llnl.gnem.jsac.dataAccess;

import java.util.ArrayList;
import java.util.List;

import llnl.gnem.core.dataObjects.MinMax;
import llnl.gnem.core.dataObjects.RestrictionCircle;

/**
 *
 * @author dodge1
 */
public class DataSelectionCriteria {

    private final List<Long> evids;
    private final List<Long> wfids;
    private final List<Integer> scenarioIdList;
    private final List<String> agencies;
    private final List<String> networks;
    private final List<String> stations;
    private final List<String> channels;
    private final List<String> locids;
    private final List<String> bands;
    private final List<String> instrumentCodes;
    private final List<String> orientationCodes;
    private MinMax staLat = null;
    private MinMax staLon = null;
    private MinMax mag = null;
    private MinMax srcLat = null;
    private MinMax srcLon = null;
    private MinMax srcDep = null;
    private MinMax time = null;
    private MinMax degDist = null;
    private RestrictionCircle srcCircle = null;
    private int maxRows;
    private boolean addArrivals = false;

    private DataSelectionCriteria() {
        evids = new ArrayList<>();
        wfids = new ArrayList<>();
        scenarioIdList = new ArrayList<>();
        agencies = new ArrayList<>();
        networks = new ArrayList<>();
        stations = new ArrayList<>();
        channels = new ArrayList<>();
        locids = new ArrayList<>();
        bands = new ArrayList<>();
        instrumentCodes = new ArrayList<>();
        orientationCodes = new ArrayList<>();
        maxRows = 500;
        addArrivals = false;
    }

    public void clear() {
        evids.clear();
        wfids.clear();
        scenarioIdList.clear();
        agencies.clear();
        networks.clear();
        stations.clear();
        channels.clear();
        locids.clear();
        bands.clear();
        instrumentCodes.clear();
        orientationCodes.clear();
        staLat = null;
        staLon = null;
        mag = null;
        srcLat = null;
        srcLon = null;
        srcDep = null;
        time = null;
        degDist = null;
        srcCircle = null;

    }

    public static DataSelectionCriteria getInstance() {
        return DBSelectionCriteriaHolder.INSTANCE;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    public void addEvidList(List<Long> evids) {
        this.evids.addAll(evids);
    }

    public void addWfidList(List<Long> wfids) {
        this.wfids.addAll(wfids);
    }

    public void addAgencyList(List<String> values) {
        agencies.addAll(values);
    }

    public void addNetworkList(List<String> values) {
        networks.addAll(values);
    }

    public void addStationList(List<String> values) {
        for(String sta : values){
            stations.add(maybeMapWildcards(sta));
        }
    }

    public void addChannelList(List<String> values) {
        for (String chan : values) {
            channels.add(maybeMapWildcards(chan));
        }
    }

    public void addLocidList(List<String> values) {
        locids.addAll(values);
    }

    public void addBandList(List<String> values) {
        bands.addAll(values);
    }

    public void addInstrumentCodeList(List<String> values) {
        instrumentCodes.addAll(values);
    }

    public void addOrientationCodeList(List<String> values) {
        orientationCodes.addAll(values);
    }

    public void setStaLat(MinMax value) {
        staLat = value;
    }

    public void setStaLon(MinMax staLon) {
        this.staLon = staLon;
    }

    public void setMag(MinMax mag) {
        this.mag = mag;
    }

    public void setSrcLat(MinMax srcLat) {
        this.srcLat = srcLat;
    }

    public void setSrcLon(MinMax srcLon) {
        this.srcLon = srcLon;
    }

    public void setSrcDep(MinMax srcDep) {
        this.srcDep = srcDep;
    }

    public void setTime(MinMax time) {
        this.time = time;
    }

    public void setDegDist(MinMax value) {
        degDist = value;
    }

    public void setSrcCircle(RestrictionCircle value) {
        srcCircle = value;
    }

    public boolean hasEvidList() {
        return !evids.isEmpty();
    }

    public boolean hasStationList() {
        return !stations.isEmpty();
    }

    public List<Long> getEvids() {
        return evids;
    }

    public List<Long> getWfids() {
        return wfids;
    }

    public List<String> getAgencies() {
        return agencies;
    }

    public List<String> getNetworks() {
        return networks;
    }

    public List<String> getStations() {
        return stations;
    }

    public List<String> getChannels() {
        return channels;
    }

    public List<String> getLocids() {
        return locids;
    }

    public List<String> getBands() {
        return bands;
    }

    public List<String> getInstrumentCodes() {
        return instrumentCodes;
    }

    public List<String> getOrientationCodes() {
        return orientationCodes;
    }

    public MinMax getStaLat() {
        return staLat;
    }

    public MinMax getStaLon() {
        return staLon;
    }

    public MinMax getMag() {
        return mag;
    }

    public MinMax getSrcLat() {
        return srcLat;
    }

    public MinMax getSrcLon() {
        return srcLon;
    }

    public MinMax getSrcDep() {
        return srcDep;
    }

    public MinMax getTime() {
        return time;
    }

    public MinMax getDegDist() {
        return degDist;
    }

    public RestrictionCircle getSrcCircle() {
        return srcCircle;
    }

    public boolean hasWfidList() {
        return !wfids.isEmpty();
    }

    public boolean isAddArrivals() {
        return addArrivals;
    }

    public void setAddArrivals(boolean addArrivals) {
        this.addArrivals = addArrivals;
    }

    public boolean isStationDriving() {
        return evids.isEmpty() && wfids.isEmpty() && scenarioIdList.isEmpty() && channels.isEmpty() && locids.isEmpty() && (!stations.isEmpty() || !networks.isEmpty() || (staLat != null && staLon != null));
    }

    public boolean isChannelDriving() {
        return evids.isEmpty() && wfids.isEmpty() && scenarioIdList.isEmpty() && !(channels.isEmpty() && locids.isEmpty());
    }

    public boolean hasAnyCriteria() {
        return !(evids.isEmpty()
                && wfids.isEmpty()
                && scenarioIdList.isEmpty()
                && agencies.isEmpty()
                && networks.isEmpty()
                && stations.isEmpty()
                && channels.isEmpty()
                && locids.isEmpty()
                && bands.isEmpty()
                && instrumentCodes.isEmpty()
                && orientationCodes.isEmpty()
                && staLat == null
                && staLon == null
                && mag == null
                && srcLat == null
                && srcLon == null
                && srcDep == null
                && time == null
                && degDist == null
                && srcCircle == null);
    }

    private String maybeMapWildcards(String value) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < value.length(); ++j) {
            char c = value.charAt(j);
            switch (c) {
            case '*':
                sb.append('%');
                break;
            case '?':
                sb.append('_');
                break;
            default:
                sb.append(c);
                break;
            }
        }
        return sb.toString();
    }

    public void addScenarioIdList(List<Integer> scids) {
        scenarioIdList.addAll(scids);
     }

    public List<Integer> getScenarioIdList() {
        return new ArrayList<>(scenarioIdList);
    }

    private static class DBSelectionCriteriaHolder {
        private static final DataSelectionCriteria INSTANCE = new DataSelectionCriteria();
    }
}
