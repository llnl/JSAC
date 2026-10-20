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
package gov.llnl.gnem.jsac.util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import gov.llnl.gnem.jsac.io.SACHeader;
import gov.llnl.gnem.jsac.io.enums.MagSource;
import gov.llnl.gnem.jsac.io.enums.MagType;
import llnl.gnem.core.util.TimeT;
import llnl.gnem.core.util.Geometry.FlynnEngdahl;

/**
 *
 * @author dodge1
 */
public class HeaderLister {

    public static enum ListType {
        DEFAULT, PICKS, SPECIAL
    }

    public static enum FileSelection {
        ALL, NONE, LIST
    }

    private static final List<String> picks;
    private static final List<String> defaults;
    private boolean inclusive;
    private ListType listType;
    private FileSelection fileSelection;
    private final List<String> specialList;

    static {
        String[] foo = {"NPTS", "B", "E", "O", "A", "T0", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "F", "KZDATE", "KZTIME"};
        picks = new ArrayList<>(Arrays.asList(foo));
        String[] foo2 = {"NPTS", "B", "E", "IFTYPE", "LEVEN", "DELTA", "ODELTA", "IDEP", "DEPMIN", "DEPMAX", "DEPMEN", "O", "A", "T0", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "F",
            "KZDATE", "KZTIME", "IZTYPE", "KINST", "RESP0", "RESP1", "RESP2", "RESP3", "RESP4", "RESP5", "RESP6", "RESP7", "RESP8", "RESP9", "KDATRD", "KSTNM", "CMPAZ", "CMPINC", "ISTREG", "STLA",
            "STLO", "STEL", "STDP", "KEVNM", "IEVREG", "EVLA", "EVLO", "EVEL", "EVDP", "IEVTYP", "KHOLE", "DIST", "AZ", "BAZ", "GCARC", "LOVROK", "IQUAL", "ISYNTH", "SCENARIO", "USER0", "USER1", "USER2",
            "USER3", "USER4", "USER5", "USER6", "USER7", "USER8", "USER9", "KUSER0", "KUSER1", "KUSER2", "NXSIZE", "XMINIMUM", "XMAXIMUM", "NYSIZE", "YMINIMUM", "YMAXIMUM", "NVHDR", "SCALE",
            "NORID", "NEVID", "NWFID", "IINST", "LPSPOL", "LCALDA", "KCMPNM", "AGENCY", "KNETWK", "MAG", "IMAGTYP", "IMAGSRC"};
        defaults = new ArrayList<>(Arrays.asList(foo2));
    }

    private HeaderLister() {
        inclusive = false;
        listType = ListType.DEFAULT;
        specialList = new ArrayList<>();
        fileSelection = FileSelection.ALL;
    }

    public void listHeader(SACHeader header, PrintStream out, String fileName, String agency) {
        out.println(String.format("FILE: %s", fileName));
        out.println("---------------------------------------------------");
        switch (listType) {
            case DEFAULT:
                listHeaderP(header, out, defaults, agency);
                break;
            case PICKS:
                listHeaderP(header, out, picks, agency);
                break;
            case SPECIAL:
                listHeaderP(header, out, specialList, agency);
        }
    }

    private void listHeaderP(SACHeader header, PrintStream out, List<String> aList, String agency) {
        for (String str : aList) {
            if (str.equals("AGENCY")) {
                String tmp = String.format("%8s =  ", str);
                String valStr = createValString(agency);
                out.println(tmp + valStr);
                continue;
            }
            Object obj = header.getValue(str);
            if (obj != null || inclusive) {
                String foo = getSpecialFormatting(str, obj, header);
                if (foo == null) {
                    String tmp = String.format("%8s =  ", str);
                    String valStr = createValString(obj);
                    out.println(tmp + valStr);
                } else {
                    out.println(foo);
                }
            }
        }
    }

    private String createValString(Object obj) {
        if (obj == null) {
            return "UNDEFINED";
        } else if (obj instanceof Double) {
            double tmp = (Double) obj;
            return String.format("%-10.6G", tmp);
        } else if (obj instanceof Integer) {
            return String.format("%d", (Integer) obj);
        } else if (obj instanceof String) {
            return obj.toString();
        }
        return "not implemented";
    }

    private String getSpecialFormatting(String str, Object obj, SACHeader header) {
        switch (str) {
            case ("O"): {

                if (obj != null) {
                    Double tmp = (Double) obj;
                    String tmpStr = header.getKo();
                    if (tmpStr == null) {
                        tmpStr = "O";
                    }
                    return String.format(" OMARKER =  %-7.2f          (%s)", tmp, tmpStr.trim());
                } else {
                    return String.format(" OMARKER =  UNDEFINED");
                }
            }
            case ("A"): {
                if (obj != null) {
                    Double tmp = (Double) obj;
                    String tmpStr = header.getKa();
                    if (tmpStr == null) {
                        return String.format(" AMARKER =  %-7.2f", tmp);
                    } else {
                        return String.format(" AMARKER =  %-7.2f          (%s)", tmp, tmpStr.trim());
                    }
                } else {
                    return String.format(" AMARKER =  UNDEFINED");
                }
            }
            case ("F"): {
                if (obj != null) {
                    Double tmp = (Double) obj;
                    String tmpStr = header.getKf();
                    if (tmpStr == null) {
                        return String.format(" FMARKER =  %-7.2f ", tmp);
                    } else {
                        return String.format(" FMARKER =  %-7.2f     (%s)", tmp, tmpStr.trim());
                    }
                } else {
                    return String.format(" FMARKER =  UNDEFINED");
                }
            }
            case "T0":
            case "T1":
            case "T2":
            case "T3":
            case "T4":
            case "T5":
            case "T6":
            case "T7":
            case "T8":
            case "T9": {
                int idx = Integer.parseInt(str.substring(1));
                return header.getTString(idx);
            }
            case "KZDATE": {
                Double refTime = header.getReferenceTime();
                if (refTime == null) {
                    return "    KZDATE = UNDEFINED";
                } else {
                    TimeT time = new TimeT(refTime);
                    String kzdate = time.toString("MMM dd (DDD), yyyy").toUpperCase();
                    return String.format("  KZDATE =  %s", kzdate);
                }
            }
            case "KZTIME": {
                Double refTime = header.getReferenceTime();
                if (refTime == null) {
                    return "    KZTIME = UNDEFINED";
                } else {
                    TimeT time = new TimeT(refTime);
                    String kztime = time.toString("HH:mm:ss.SSS");
                    return String.format("  KZTIME =  %s", kztime);
                }
            }
            case "IFTYPE": {
                String value = obj == null ? "UNDEFINED" : header.getIftype().getDescription().toUpperCase();
                return String.format("%8s =  %s", str, value);
            }
            case "LEVEN": {
                Integer leven = (Integer) obj;
                String value = obj == null ? "UNDEFINED" : (leven > 0 ? "TRUE" : "FALSE");
                return String.format("%8s =  %s", str, value);
            }
            case "IZTYPE": {
                String value = obj == null ? "UNDEFINED" : obj.toString().toUpperCase();
                return String.format("%8s =  %s", str, value);
            }
            case "IEVREG": {
                Integer ievreg = (Integer) obj;
                String value = ievreg == null ? "UNDEFINED" : FlynnEngdahl.getInstance().getRegionName(ievreg);
                return String.format("%8s =  %s", str, value);
            }
            case "IEVTYP": {
                String value = obj == null ? "UNDEFINED" : obj.toString();
                return String.format("%8s  = %s", str, value);
            }
            case "IMAGTYP": {
                if (obj != null) {
                    MagType magtype = header.getImagtyp();
                    return String.format("%8s =  %s", str, magtype.getDescription());
                } else {
                    return String.format("%8s = %s", str, "UNDEFINED");
                }
            }
            case "IMAGSRC": {

                if (obj != null) {
                    MagSource imagsrc = header.getImagsrc();
                    if (imagsrc != null) {
                        return String.format("%8s = %s", str, imagsrc.getDescription());
                    } else {
                        return String.format("%8s = %s", str, "UNDEFINED");
                    }
                }
            }

            case "LOVROK": {
                Integer lovrok = (Integer) obj;
                String value = lovrok == null ? "UNDEFINED" : (lovrok > 0 ? "TRUE" : "FALSE");
                return String.format("%8s =  %s", str, value);
            }
            case "IQUAL": {
                String value = obj == null ? "UNDEFINED" : obj.toString();
                return String.format("%8s =  %s", str, value);
            }
            case "IDEP": {
                String value = "UNDEFINED";
                if (obj != null) {
                    value = header.getIdep().getDescription().toUpperCase();
                }
                return String.format("%8s =  %s", str, value);

            }
            case "LPSPOL": {
                String value = "UNDEFINED";
                if (obj != null) {
                    int tmp = (Integer) obj;
                    value = tmp > 0 ? "TRUE" : "FALSE";
                }
                return String.format("%8s =  %s", str, value);
            }
            case "LCALDA": {
                String value = "UNDEFINED";
                if (obj != null) {
                    int tmp = (Integer) obj;
                    value = tmp > 0 ? "TRUE" : "FALSE";
                }
                return String.format("%8s =  %s", str, value);
            }
        }
        return null;
    }

    public boolean isInclusive() {
        return inclusive;
    }

    public void setInclusive(boolean inclusive) {
        this.inclusive = inclusive;
    }

    public ListType getListType() {
        return listType;
    }

    public void setListType(ListType listType) {
        this.listType = listType;
    }

    public void setHeaderList(Collection<String> hdrlist) {
        specialList.clear();
        specialList.addAll(hdrlist);
    }

    public FileSelection getFileSelection() {
        return fileSelection;
    }

    public void setFileSelection(FileSelection fileSelection) {
        this.fileSelection = fileSelection;
    }

    public static HeaderLister getInstance() {
        return ListHeaderOptionsHolder.INSTANCE;
    }

    private static class ListHeaderOptionsHolder {

        private static final HeaderLister INSTANCE = new HeaderLister();
    }
}
