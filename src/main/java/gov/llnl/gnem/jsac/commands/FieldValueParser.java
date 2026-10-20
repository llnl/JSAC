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

package gov.llnl.gnem.jsac.commands;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;

import gov.llnl.gnem.jsac.io.SACHeader;
import gov.llnl.gnem.jsac.io.SACHeader.UndefHeaderValue;
import gov.llnl.gnem.jsac.io.enums.Body;
import gov.llnl.gnem.jsac.io.enums.DataQuality;
import gov.llnl.gnem.jsac.io.enums.DepVarType;
import gov.llnl.gnem.jsac.io.enums.EventType;
import gov.llnl.gnem.jsac.io.enums.FileType;
import gov.llnl.gnem.jsac.io.enums.Iztype;
import gov.llnl.gnem.jsac.io.enums.MagSource;
import gov.llnl.gnem.jsac.io.enums.MagType;
import gov.llnl.gnem.jsac.io.enums.SyntheticsType;
import llnl.gnem.core.util.TimeT;

/**
 *
 * @author dodge1
 */
public class FieldValueParser {

    private static final Map<String, Type> NAME_TYPE_MAP = new HashMap<>();
    private static final Set<String> RELATIVE_TIME_FIELDS = new HashSet<>();

    static {
        Class<SACHeader> c = SACHeader.class;
        Field[] fields = c.getDeclaredFields();
        for (Field f : fields) {
            String fieldName = f.getName().toUpperCase();
            Type type = f.getType();
            NAME_TYPE_MAP.put(fieldName, type);
        }
        String[] tmpf = { "B", "E", "O", "A", "F" };
        RELATIVE_TIME_FIELDS.addAll(Arrays.asList(tmpf));

        for (int j = 0; j < 10; ++j) {
            String tmp = String.format("T%d", j).toUpperCase();
            NAME_TYPE_MAP.put(tmp, Double.class);
            RELATIVE_TIME_FIELDS.add(tmp);
            tmp = String.format("KT%d", j).toUpperCase();
            NAME_TYPE_MAP.put(tmp, String.class);
            tmp = String.format("RESP%d", j).toUpperCase();
            NAME_TYPE_MAP.put(tmp, Double.class);
            tmp = String.format("USER%d", j).toUpperCase();
            NAME_TYPE_MAP.put(tmp, Double.class);
        }

        // Some commands use 'BEGIN' as a substitute for 'B'
        NAME_TYPE_MAP.put("BEGIN", Double.class);
        
        // These are synthesized 'fields'
        NAME_TYPE_MAP.put("KZTIME", String.class);
        NAME_TYPE_MAP.put("KZDATE", String.class);
    }

    private static void parseGMTAssignment(List<String> mytokens, int j, Map<String, Object> parsedHeaderFields, String key) {
        String tmp = mytokens.get(j + 1).toUpperCase();
        if (tmp.equals("GMT")) {
            TimeT epochTime = parseEpochTime(mytokens, j + 2);
            if (epochTime != null) {
                parsedHeaderFields.put(key, epochTime);
            }
        }
    }

    private static void parseSingleValuedToken(List<String> mytokens, int jnext, Map<String, Object> parsedHeaderFields, String key) {
        // A single value was supplied.
        String value = mytokens.get(jnext - 1);
        if (value.equalsIgnoreCase("ASCEND") || value.equalsIgnoreCase("ASC")) {
            parsedHeaderFields.put(key, "ASCEND");
        } else if (value.equalsIgnoreCase("DESCEND") || value.equalsIgnoreCase("DESC")) {
            parsedHeaderFields.put(key, "DESCEND");
        } else {
            Object val = new UndefHeaderValue();
            if (!value.toUpperCase().equals(UndefHeaderValue.getFlagName())) {
                val = parseToken(key, value);
            }
            if (val != null) {
                parsedHeaderFields.put(key, val);
            }
        }
    }

    public static Map<String, Object> parseHeaderVariablesFromTokenList(List<String> mytokens) {
        Map<String, Object> parsedHeaderFields = new LinkedHashMap<>();
        List<Integer> removedTokens = new ArrayList<>();
        List<Integer> hdrValPos = new ArrayList<>();
        for (int j = 0; j < mytokens.size(); ++j) {
            String tmp1 = mytokens.get(j);
            if (NAME_TYPE_MAP.get(tmp1.toUpperCase()) != null) {
                hdrValPos.add(j);
            }
        }
        if (hdrValPos.isEmpty()) {
            return parsedHeaderFields;
        }
        int k = 0;
        while (k < hdrValPos.size() - 1) {
            int j = hdrValPos.get(k);
            String key = mytokens.get(j).toUpperCase();

            int jnext = hdrValPos.get(k + 1);
            for (int m = j; m < jnext; ++m) {
                removedTokens.add(m);
            }
            if (jnext == j + 1) { //token with no value
                parsedHeaderFields.put(key, null);
            } else if (jnext == j + 2) {
                parseSingleValuedToken(mytokens, jnext, parsedHeaderFields, key);
            } else if (jnext == j + 8 && RELATIVE_TIME_FIELDS.contains(key)) {
                parseGMTAssignment(mytokens, j, parsedHeaderFields, key);
            }
            ++k;
        }
        int j = hdrValPos.get(k); // now process the last header field name
        removedTokens.add(j);
        String key = mytokens.get(j).toUpperCase();
        if (j == mytokens.size() - 1) { // No value was supplied...
            parsedHeaderFields.put(key, null);
        } else if (j == mytokens.size() - 2) { // A single value was supplied
            parseSingleValuedToken(mytokens, j + 2, parsedHeaderFields, key);
            removedTokens.add(j + 1);
        } else if (j == mytokens.size() - 8) {
            parseGMTAssignment(mytokens, j, parsedHeaderFields, key);
            for (int m = j; m < j + 7; ++m) {
                removedTokens.add(m);
            }
        }

        Collections.sort(removedTokens, Collections.reverseOrder());
        for (int val : removedTokens) {
            mytokens.remove(val);
        }
        return parsedHeaderFields;
    }

    public static Object parseToken(String fieldName, String token) {

        Type type = NAME_TYPE_MAP.get(fieldName.toUpperCase());
        if (type != null) {
            if (type.equals(String.class)) {
                return token;
            } else if (type.equals(Float.class) && NumberUtils.isParsable(token)) {
                return Float.valueOf(token);
            } else if (type.equals(Double.class) && NumberUtils.isParsable(token)) {
                return Double.valueOf(token);
            } else if (type.equals(Integer.class) && NumberUtils.isParsable(token)) {
                return Integer.valueOf(token);
            } else if (type.equals(EventType.class)) {
                return EventType.valueOf(token.toUpperCase());
            } else if (type.equals(MagType.class)) {
                return MagType.valueOf(token.toUpperCase());
            } else if (type.equals(DepVarType.class)) {
                return DepVarType.valueOf(token.toUpperCase());
            } else if (type.equals(DataQuality.class)) {
                return DataQuality.valueOf(token.toUpperCase());
            } else if (type.equals(SyntheticsType.class)) {
                return SyntheticsType.valueOf(token.toUpperCase());
            } else if (type.equals(FileType.class)) {
                return FileType.valueOf(token.toUpperCase());
            } else if (type.equals(Iztype.class)) {
                return Iztype.valueOf(token.toUpperCase());
            } else if (type.equals(Body.class)) {
                return Body.valueOf(token.toUpperCase());
            } else if (type.equals(MagSource.class)) {
                return MagSource.valueOf(token.toUpperCase());
            }

        }
        return null;
    }

    private static TimeT parseEpochTime(List<String> mytokens, int index) {
        String tmp = mytokens.get(index++);
        int year = Integer.parseInt(tmp);
        tmp = mytokens.get(index++);
        int jday = Integer.parseInt(tmp);
        tmp = mytokens.get(index++);
        int hour = Integer.parseInt(tmp);
        tmp = mytokens.get(index++);
        int min = Integer.parseInt(tmp);
        tmp = mytokens.get(index++);
        int sec = Integer.parseInt(tmp);
        tmp = mytokens.get(index++);
        int msec = Integer.parseInt(tmp);
        return new TimeT(year, jday, hour, min, sec, msec);
    }

}
