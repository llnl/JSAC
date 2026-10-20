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
package gov.llnl.gnem.jsac.arrayProcessing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ArrayDefinition {

    private final String arrayName;
    private final Map<String, Collection<ArrayElement>> stationElementMap;

    public ArrayDefinition(ArrayElement element) {
        arrayName = element.getArrayName();
        stationElementMap = new HashMap<>();
        ArrayList<ArrayElement> tmp = new ArrayList<>();
        stationElementMap.put(element.getStationCode(), tmp);
        tmp.add(element);
    }

    void addElement(ArrayElement element) {
        if (!element.getArrayName().equals(arrayName)) {
            throw new IllegalStateException("Attempt to add element from different array!");
        }
        Collection<ArrayElement> tmp = stationElementMap.get(element.getStationCode());
        if (tmp != null) {
            tmp.add(element);
        } else {
            tmp = new ArrayList<>();
            stationElementMap.put(element.getStationCode(), tmp);
            tmp.add(element);
        }
    }

    public String getArrayName() {
        return arrayName;
    }

    public boolean hasStation(String stationCode) {
        for (String sta : stationElementMap.keySet()) {
            if (sta.equals(stationCode)) {
                return true;
            }
        }
        return false;
    }

    public ArrayElement getArrayElement(String stationCode, double startTime) {
        Collection<ArrayElement> elements = stationElementMap.get(stationCode);
        if (elements == null) {
            return null;
        }
        for (ArrayElement ae : elements) {
            if (ae.getBeginTime() <= startTime && ae.getEndTime() >= startTime) {
                return ae;
            }
        }
        // Could not find a containing epoch so find the closest one.
        double minDist = Double.MAX_VALUE;
        ArrayElement best = null;
        for (ArrayElement ae : elements) {
            double dist = Math.min(Math.abs(ae.getBeginTime()-startTime), Math.abs(ae.getEndTime()-startTime));
            if(dist < minDist){
                minDist = dist;
                best = ae;
            }
        }
        return best;
    }

}
