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

import gov.llnl.gnem.jsac.io.SACHeaderIO;

public enum MagSource {
    INEIC(58, "INEIC  (National Earthquake Information Center)"),
    IPDEQ(59, "IPDE  (Preliminary Determination of Epicenter)"),
    IPDEW(60, "IPDE  (Preliminary Determination of Epicenter)"),
    IPDE(61, "IPDE   (Preliminary Determination of Epicenter)"),
    IISC(62, "IISC  (Internation Seismological Centre)"),
    IREB(63, "IREB   (Reviewed Event Bulletin)"),
    IUSGS(64, "IUSGS   (US Geological Survey)"),
    IBRK(65, "IBRK    (UC Berkeley)"),
    ICALTECH(66, "ICALTECH  (California Institute of Technology)"),
    ILLNL(67, "ILLNL  (Lawrence Livermore National Laboratory)"),
    IEVLOC(68, "IEVLOC  (Event Location (computer program) )"),
    IJSOP(69, "IJSOP   (Joint Seismic Observation Program)"),
    IUSER(70, "IUSER    (The individual using SAC)"),
    IUNKNOWN(71, "IUNKNOWN   (unknown)");

    private MagSource(int code, String description) {
        this.code = code;
        this.description = description;
    }
    private final int code;
    private final String description;
    
    
    public static MagSource getMagSourceFromCode( int code ) {
    	
    	if ( code == SACHeaderIO.UNDEFINED_INTEGER )
    		return null;
    	
        for (MagSource mt : MagSource.values()) {
            if (mt.code == code) {
                return mt;
            }
        }
        throw new IllegalArgumentException("Unrecognized mag source code: " + code);
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
