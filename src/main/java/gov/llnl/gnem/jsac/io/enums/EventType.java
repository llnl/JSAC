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

/**
 *
 * @author dodge1
 */
public enum EventType {
    INUCL(37, " event type: nuclear shot  "),
    IPREN(38, " event type: nuke pre-shot "),
    IPOSTN(39, " event type: nuke post-shot"),
    IQUAKE(40, " event type: earthquake    "),
    IPREQ(41, " event type: foreshock     "),
    IPOSTQ(42, " event type: aftershock    "),
    ICHEM(43, " event type: chemical expl "),
    IOTHER(44, " event type: other source  "),
    IQB(72, " Quarry Blast or mine expl. confirmed by quarry "),
    IQB1(73, " Quarry or mine blast with designed shot information-ripple fired "),
    IQB2(74, " Quarry or mine blast with observed shot information-ripple fired "),
    IQBX(75, " Quarry or mine blast - single shot "),
    IQMT(76, " Quarry or mining-induced events: tremors and rockbursts "),
    IEQ(77, " Earthquake "),
    IEQ1(78, " Earthquakes in a swarm or aftershock sequence "),
    IEQ2(79, " Felt earthquake "),
    IME(80, " Marine explosion "),
    IEX(81, " Other explosion "),
    INU(82, " Nuclear explosion "),
    INC(83, " Nuclear cavity collapse "),
    IO_(84, " Other source of known origin "),
    IL(85, " Local event of unknown origin "),
    IR(86, " Regional event of unknown origin "),
    IT(87, " Teleseismic event of unknown origin "),
    IU(88, " Undetermined or conflicting information  "),
    IEQ3(89, " Damaging earthquake "),
    IEQ0(90, " Probable earthquake "),
    IEX0(91, " Probable explosion "),
    IQC(92, " Mine collapse "),
    IQB0(93, " Probable Mine Blast "),
    IGEY(94, " Geyser "),
    ILIT(95, " Light "),
    IMET(96, " Meteoric Event "),
    IODOR(97, " Odors "),
    IOS(103, " Other source: Known origin"),
    IUNKNOWN(71,"Unknown event type");

    private EventType(int code, String description) {
        this.code = code;
        this.description = description;
    }
    private final int code;
    private final String description;

    public static EventType getEventTypeFromCode( int code ) {
    	
    	if ( code == SACHeaderIO.UNDEFINED_INTEGER )
    		return null;
    	
        for ( EventType mt : EventType.values() ) {
            if (mt.code == code) {
                return mt;
            }
        }
        throw new IllegalArgumentException("Unrecognized event type code: " + code);
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static EventType getEventTypeFromString(String etype) {
        if (etype == null) {
            return EventType.IUNKNOWN;
        }
        switch (etype) {
            case "null":
                return EventType.IUNKNOWN;
            case "EARTHQUAKE":
                return EventType.IQUAKE;
            case "qt":
                return EventType.IEQ;
            case "uk":
                return EventType.IUNKNOWN;
            case "qp":
                return EventType.IEQ0;
            case "ec":
                return EventType.ICHEM;
            case "qf":
                return EventType.IEQ2;
            case "EXPLOSION":
                return EventType.IEX;
            case "mx":
                return EventType.IQB;
            case "ie":
                return EventType.IOS;
            case "ms":
                return EventType.IQB0;
            case "MINING_EXPLOSION":
                return EventType.IQBX;
            case "mp":
                return EventType.IQB0;
            case "INDUCED_OR_TRIGGERED_EVENT":
                return EventType.IQMT;
            case "QUARRY_BLAST":
                return EventType.IQBX;
            case "NOT_REPORTED":
                return EventType.IUNKNOWN;
            case "ROCK_BURST":
                return EventType.IQMT;
            case "mb":
                return EventType.IQMT;
            case "OTHER_EVENT":
                return EventType.IOTHER;
            case "me":
                return EventType.IQMT;
            case "ICE_QUAKE":
                return EventType.IOS;
            case "qd":
                return EventType.IEQ3;
            case "en":
                return EventType.INU;
            case "EXPERIMENTAL_EXPLOSION":
                return EventType.ICHEM;
            case "qh":
                return EventType.IOS;
            case "ex":
                return EventType.IEX;
            case "e3":
                return EventType.IEX;
            case "NUCLEAR_EXPLOSION":
                return EventType.INU;
            case "is":
                return EventType.IQMT;
            case "ep":
                return EventType.IEX0;
            case "CHEMICAL_EXPLOSION":
                return EventType.ICHEM;
            case "LANDSLIDE":
                return EventType.IOTHER;
            case "SONIC_BOOM":
                return EventType.IOTHER;
            case "mc":
                return EventType.IQC;
            case "ACOUSTIC_NOISE":
                return EventType.IOTHER;
            case "CONTROLLED_EXPLOSION":
                return EventType.IEX;
            case "ls":
                return EventType.IOS;
            case "MINE_COLLAPSE":
                return EventType.IQC;
            case "METEORITE":
                return EventType.IMET;
            case "COLLAPSE":
                return EventType.IQC;
            case "NOT_EXISTING":
                return EventType.IUNKNOWN;
            case "xm":
                return EventType.IMET;
            case "ROCKSLIDE":
                return EventType.IOS;
            case "e4":
                return EventType.IEX0;
            case "HYDROACOUSTIC_EVENT":
                return EventType.IOS;
            case "e2":
                return EventType.INU;
            default:
                return EventType.IUNKNOWN;
        }
    }
}
