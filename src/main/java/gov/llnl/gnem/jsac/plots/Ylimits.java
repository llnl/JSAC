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

package gov.llnl.gnem.jsac.plots;

import java.util.ArrayList;
import java.util.List;

import llnl.gnem.core.util.PairT;

/**
 *
 * @author dodge1
 */
public class Ylimits {

    private boolean limitsOn;
    private final List<PairT<Double, Double>> limitList;
    private int index;
    private PairT<Double, Double> dataMinMax;

    public Ylimits() {
        limitsOn = false;
        limitList = new ArrayList<>();
        index = 0;
        dataMinMax = null;
    }

    public void resetIndex() {
        index = 0;
    }

    public void addLimit(double min, double max) {
        limitList.add(new PairT<>(min, max));
    }

    public void addAllLimit() {
        limitList.add(new PairT<>(null, null));
    }

    public void reset() {
        limitsOn = false;
        limitList.clear();
        dataMinMax = null;
    }

    public boolean isLimitsOn() {
        return limitsOn;
    }

    public void setLimitsOn(boolean limitsOn) {
        this.limitsOn = limitsOn;
    }

    public PairT<Double, Double> nextLimit() {
        PairT<Double, Double> result = null;
        if (limitList.isEmpty()) {
            return null;
        } else if (index == limitList.size()) {
            result = limitList.get(index - 1);
        } else {
            result = limitList.get(index++);
        }
        if (result != null) {
            if (result.getFirst() != null) {
                return result;
            } else if (result.getFirst() == null && dataMinMax != null) {
                return dataMinMax;
            } else {
                return null;
            }
        }
        return result;
    }

    public void setDataRange(PairT<Double, Double> dataMinMax) {
        this.dataMinMax = dataMinMax;
    }
}
