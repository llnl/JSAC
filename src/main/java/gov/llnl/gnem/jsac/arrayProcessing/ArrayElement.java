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

import java.util.Objects;
import java.util.StringTokenizer;

public class ArrayElement {

    private final String arrayName;
    private final String stationCode;
    private final double beginTime;
    private final double endTime;
    private final double dnorth;
    private final double deast;
    private final double lat;
    private final double lon;
    private final double elev;

    public ArrayElement(String arrayame, String stationCode, double beginTime, double endTime, double dnorth, double deast, double lat, double lon, double elev) {
        this.arrayName = arrayame;
        this.stationCode = stationCode;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.dnorth = dnorth;
        this.deast = deast;
        this.lat = lat;
        this.lon = lon;
        this.elev = elev;
    }

    public ArrayElement(String line) {
        StringTokenizer st = new StringTokenizer(line);
        if (st.countTokens() < 9) {
            throw new IllegalStateException("Not enough tokens: " + line);
        }
        arrayName = st.nextToken();
        stationCode = st.nextToken();
        beginTime = Double.parseDouble(st.nextToken());
        endTime = Double.parseDouble(st.nextToken());
        dnorth = Double.parseDouble(st.nextToken());
        deast = Double.parseDouble(st.nextToken());
        lat = Double.parseDouble(st.nextToken());
        lon = Double.parseDouble(st.nextToken());
        elev = Double.parseDouble(st.nextToken());
    }

    public String getArrayName() {
        return arrayName;
    }

    public String getStationCode() {
        return stationCode;
    }

    public double getBeginTime() {
        return beginTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public double getDnorth() {
        return dnorth;
    }

    public double getDeast() {
        return deast;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getElev() {
        return elev;
    }

    @Override
    public String toString() {
        return "ArrayElement{" + "arrayame=" + arrayName + ", stationCode=" + stationCode + ", beginTime=" + beginTime + ", endTime=" + endTime + ", dnorth=" + dnorth + ", deast=" + deast + ", lat=" + lat + ", lon=" + lon + ", elev=" + elev + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.arrayName);
        hash = 97 * hash + Objects.hashCode(this.stationCode);
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.beginTime) ^ (Double.doubleToLongBits(this.beginTime) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.endTime) ^ (Double.doubleToLongBits(this.endTime) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.dnorth) ^ (Double.doubleToLongBits(this.dnorth) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.deast) ^ (Double.doubleToLongBits(this.deast) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.lat) ^ (Double.doubleToLongBits(this.lat) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.lon) ^ (Double.doubleToLongBits(this.lon) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.elev) ^ (Double.doubleToLongBits(this.elev) >>> 32));
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
        final ArrayElement other = (ArrayElement) obj;
        if (Double.doubleToLongBits(this.beginTime) != Double.doubleToLongBits(other.beginTime)) {
            return false;
        }
        if (Double.doubleToLongBits(this.endTime) != Double.doubleToLongBits(other.endTime)) {
            return false;
        }
        if (Double.doubleToLongBits(this.dnorth) != Double.doubleToLongBits(other.dnorth)) {
            return false;
        }
        if (Double.doubleToLongBits(this.deast) != Double.doubleToLongBits(other.deast)) {
            return false;
        }
        if (Double.doubleToLongBits(this.lat) != Double.doubleToLongBits(other.lat)) {
            return false;
        }
        if (Double.doubleToLongBits(this.lon) != Double.doubleToLongBits(other.lon)) {
            return false;
        }
        if (Double.doubleToLongBits(this.elev) != Double.doubleToLongBits(other.elev)) {
            return false;
        }
        if (!Objects.equals(this.arrayName, other.arrayName)) {
            return false;
        }
        return Objects.equals(this.stationCode, other.stationCode);
    }

}
