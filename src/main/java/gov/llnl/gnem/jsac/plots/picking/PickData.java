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
package gov.llnl.gnem.jsac.plots.picking;

import java.util.Objects;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.JSubplot;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.VPickLine;

/**
 *
 * @author dodge1
 */
public class PickData {

    private final VPickLine vpl;
    private final SacTraceData std;
    private final String headerVariable;
    private final JSubplot sp;

    public PickData(JSubplot sp, VPickLine vpl, SacTraceData std, String headerVariable) {
        this.sp = sp;
        this.vpl = vpl;
        this.std = std;
        this.headerVariable = headerVariable;
    }

    public JSubplot getSp() {
        return sp;
    }

    public VPickLine getVpl() {
        return vpl;
    }

    public SacTraceData getStd() {
        return std;
    }

    public String getHeaderVariable() {
        return headerVariable;
    }

    @Override
    public String toString() {
        return "PickData{" + "vpl=" + vpl + ", std=" + std + ", headerVariable=" + headerVariable + ", sp=" + sp + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.vpl);
        hash = 23 * hash + Objects.hashCode(this.std);
        hash = 23 * hash + Objects.hashCode(this.headerVariable);
        hash = 23 * hash + Objects.hashCode(this.sp);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PickData other = (PickData) obj;
        if (!Objects.equals(this.headerVariable, other.headerVariable)) {
            return false;
        }
        if (!Objects.equals(this.vpl, other.vpl)) {
            return false;
        }
        if (!Objects.equals(this.std, other.std)) {
            return false;
        }
        return Objects.equals(this.sp, other.sp);
    }

}
