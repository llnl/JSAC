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
package gov.llnl.gnem.jsac.io;

import java.util.Objects;

import llnl.gnem.core.util.StreamKey;
import llnl.gnem.core.util.TimeT;
import llnl.gnem.core.waveform.seismogram.CssSeismogram;

/**
 *
 * @author dodge1
 */
public class GseSeismogram extends CssSeismogram {

    private static final long serialVersionUID = 1L;
    private final String instype;
    private final Double hang;
    private final Double vang;
    private final Double lat;
    private final Double lon;
    private final String coordSys;
    private final Double elev;
    private final Double edepth;

    public GseSeismogram(Long wfid, StreamKey key, float[] data, double samprate, TimeT time, Double calib, Double calper, String instype, Double hang, Double vang, Double lat, Double lon,
            String coordSys, Double elev, Double edepth) {
        super(wfid, key, data, samprate, time, calib, calper);
        this.instype = instype;
        this.hang = hang != null && hang != -1 ? hang : null;
        this.vang = vang != null && vang != -1 ? vang : null;
        this.lat = lat;
        this.lon = lon;
        this.coordSys = coordSys;
        this.elev = elev;
        this.edepth = edepth;
    }

    public String getInstype() {
        return instype;
    }

    public Double getHang() {
        return hang;
    }

    public Double getVang() {
        return vang;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    public String getCoordSys() {
        return coordSys;
    }

    public Double getElev() {
        return elev;
    }

    public Double getEdepth() {
        return edepth;
    }

    @Override
    public String toString() {
        return "GseSeismogram{"
                + "instype="
                + instype
                + ", hang="
                + hang
                + ", vang="
                + vang
                + ", lat="
                + lat
                + ", lon="
                + lon
                + ", coordSys="
                + coordSys
                + ", elev="
                + elev
                + ", edepth="
                + edepth
                + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.instype);
        hash = 29 * hash + Objects.hashCode(this.hang);
        hash = 29 * hash + Objects.hashCode(this.vang);
        hash = 29 * hash + Objects.hashCode(this.lat);
        hash = 29 * hash + Objects.hashCode(this.lon);
        hash = 29 * hash + Objects.hashCode(this.coordSys);
        hash = 29 * hash + Objects.hashCode(this.elev);
        hash = 29 * hash + Objects.hashCode(this.edepth);
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
        final GseSeismogram other = (GseSeismogram) obj;
        if (!Objects.equals(this.instype, other.instype)) {
            return false;
        }
        if (!Objects.equals(this.coordSys, other.coordSys)) {
            return false;
        }
        if (!Objects.equals(this.hang, other.hang)) {
            return false;
        }
        if (!Objects.equals(this.vang, other.vang)) {
            return false;
        }
        if (!Objects.equals(this.lat, other.lat)) {
            return false;
        }
        if (!Objects.equals(this.lon, other.lon)) {
            return false;
        }
        if (!Objects.equals(this.elev, other.elev)) {
            return false;
        }
        if (!Objects.equals(this.edepth, other.edepth)) {
            return false;
        }
        return true;
    }

}
