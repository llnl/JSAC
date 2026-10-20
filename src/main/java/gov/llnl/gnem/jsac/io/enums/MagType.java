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
package gov.llnl.gnem.jsac.io.enums;

/**
 *
 * @author dodge1
 */
public enum MagType {

    IMB(52, "Bodywave Magnitude"), IMS(53, "Surface Magnitude"), IML(54, "Local Magnitude "), IMW(55, "Moment Magnitude"), IMD(56, "Duration Magnitude"), IMX(57, "User Defined Magnitude");

    private MagType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    private final int code;
    private final String description;

    public static MagType getMagtypeFromCode(int code) {
        for (MagType mt : MagType.values()) {
            if (mt.code == code) {
                return mt;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static MagType getMagTypeFromString(String magtype) {
        if (magtype == null) {
            return MagType.IMX;
        }
        String tmp = magtype.toUpperCase();
        if (tmp.indexOf("MB") > 0) {
            return MagType.IMB;
        } else if (tmp.indexOf("MS") > 0) {
            return MagType.IMS;
        } else if (tmp.indexOf("ML") > 0) {
            return MagType.IML;
        } else if (tmp.indexOf("MW") > 0) {
            return MagType.IMW;
        } else if (tmp.indexOf("MD") > 0) {
            return MagType.IMD;
        }
        return MagType.IMX;
    }

}
