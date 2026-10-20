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

import gov.llnl.gnem.jsac.util.PartialDataWindow;

/**
 *
 * @author dodge1
 */
public class Xlimits {

    boolean limitsOn;
    PartialDataWindow pdw;

    public Xlimits() {
        limitsOn = false;
        pdw = new PartialDataWindow();
    }

    public boolean isLimitsOn() {
        return limitsOn;
    }

    public PartialDataWindow getPdw() {
        return pdw;
    }

    public void setLimitsOn(boolean limitsOn) {
        this.limitsOn = limitsOn;
    }

    @Override
    public String toString() {
        return "Xlimits{" + "limitsOn=" + limitsOn + ", pdw=" + pdw + '}';
    }

}
