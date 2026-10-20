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

package gov.llnl.gnem.jsac.commands.dataFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.util.PartialDataWindow;

/**
 *
 * @author dodge1
 */
public class PDWSetterBase {
    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    private static final Logger log = LoggerFactory.getLogger(PDWSetterBase.class);

    static {
        String[] tmpList = { "B", "E", "O", "A", "F", "T0", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "SIGNAL" };
        for (String tmp : tmpList) {
            descriptors.add(new AttributeDescriptor(tmp, ValuePossibilities.ZERO_OR_MORE, String.class));
        }
        descriptors.add(new AttributeDescriptor("ON", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("OFF", ValuePossibilities.NO_VALUE, null));
    }

    private final PartialDataWindow pdw;
    private final String childName;

    public PDWSetterBase(String childName, PartialDataWindow pdw) {
        this.childName = childName;
        this.pdw = pdw;
    }

    public PartialDataWindow getPdw() {
        return pdw;
    }

    public void initialize(String[] tokens) {
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            log.warn(childName + " command without arguments has no effect!");
            return;
        }

        List<Object> tmp = parsedTokens.remove("ON");
        boolean enabled = tmp != null;
        tmp = parsedTokens.remove("OFF");
        if (tmp != null) {
            enabled = false;
        }
        pdw.setEnabled(enabled);

        tmp = parsedTokens.remove("SIGNAL");
        if (tmp != null) {
            pdw.setStartReference("A");
            pdw.setStartOffset(-1);
            pdw.setEndReference("F");
            pdw.setEndOffset(1);
            pdw.setEnabled(true);
            return;
        }
        tmp = parsedTokens.remove(TokenListParser.LEFT_OVER_TOKENS);// This handles PDWs with implicit reference to REFTIME like 12.5 56.2
        if (tmp != null) {
            processImplicitReftimePair(tmp, parsedTokens);
            return;
        }

        //Now there should be 0, 1 or 2 tuples
        int numTuples = parsedTokens.size();
        switch (numTuples) {
        case 0:
            return;
        case 1: {
            pdw.setDefaults();
            processSingleTokenSet(parsedTokens);
            break;
        }
        case 2: {//Start and end references supplied...
            pdw.setDefaults();
            Set<String> keys = parsedTokens.keySet();
            String key = keys.iterator().next();
            processStartTokens(parsedTokens, key);

            key = keys.iterator().next();
            processEndTokens(parsedTokens, key);
            break;
        }
        default:
            log.warn("Too many tuples!");
            pdw.setDefaults();
            pdw.setEnabled(false);
            return;
        }
        pdw.validate();

    }

    private void processSingleTokenSet(Map<String, List<Object>> parsedTokens) throws NumberFormatException, IllegalStateException {
        List<Object> tmp;
        //only the start reference is supplied...
        Set<String> keys = parsedTokens.keySet();
        String key = keys.iterator().next();
        tmp = parsedTokens.remove(key);
        pdw.setStartReference(key);
        pdw.setEnabled(true);
        if (tmp.isEmpty()) {//Assume zero offset.
            pdw.setStartOffset(0.0);
        } else if (tmp.size() == 1) {
            String value = (String) tmp.get(0);
            if (NumberUtils.isParsable(value)) {
                double dblValue = Double.parseDouble(value);
                pdw.setStartOffset(dblValue);
            } else { // non-numeric is a mistake here.
                String message = String.format("Expected a numeric value but got %s!", value);
                log.warn(message);
                throw new IllegalStateException("Failed parsing tokens to construct PDW!");
            }
            pdw.setEndReference("E"); // Since user entered just one limit this sets the PDW to run to the end of the file.
            pdw.setEndOffset(0.0);
        } else if (tmp.size() == 2) { // PDW end is also set relative to begin.
            String value1 = (String) tmp.get(0);
            String value2 = (String) tmp.get(1);
            if (value1.toUpperCase().equals("N") && NumberUtils.isParsable(value2)) {
                Integer npts = getIntegerOffsetValue(value2);
                if (npts != null) {
                    pdw.setStartIntegerOffset(npts);
                    pdw.setEndReference("E"); // Since user entered just one limit this sets the PDW to run to the end of the file.
                    pdw.setEndOffset(0.0);
                }
            } else if (NumberUtils.isParsable(value1) && NumberUtils.isParsable(value2)) {
                pdw.setEndReference(key);
                double dblValue = Double.parseDouble(value1);
                pdw.setStartOffset(dblValue);
                dblValue = Double.parseDouble(value2);
                pdw.setEndOffset(dblValue);
            }
        } else {
            String message = String.format("Too many tuples in token set!");
            log.warn(message);
            throw new IllegalStateException("Failed parsing tokens to construct PDW!");
        }
    }

    private void processEndTokens(Map<String, List<Object>> parsedTokens, String key) throws IllegalStateException, NumberFormatException {
        List<Object> tmp;
        tmp = parsedTokens.remove(key);
        pdw.setEndReference(key);
        if (tmp.isEmpty()) {//Assume zero offset.
            pdw.setEndOffset(0.0);
        } else if (tmp.size() == 1) {
            String value = (String) tmp.get(0);
            if (NumberUtils.isParsable(value)) {
                double dblValue = Double.parseDouble(value);
                pdw.setEndOffset(dblValue);
            } else { // non-numeric is a mistake here.
                String message = String.format("Expected a numeric value but got %s!", value);
                log.warn(message);
                throw new IllegalStateException("Failed parsing tokens to construct PDW!");
            }
        } else if (tmp.size() == 2) { //Specified number of points after end reference
            String value1 = (String) tmp.get(0);
            String value2 = (String) tmp.get(1);
            if (value1.toUpperCase().equals("N") && NumberUtils.isParsable(value2)) {
                Integer npts = getIntegerOffsetValue(value2);
                if (npts != null) {
                    pdw.setEndIntegerOffset(npts);
                }
            } else {
                String message = String.format("Invalid tokens supplied for end of PDW! Expected a number or N followed by a number.");
                log.warn(message);
                throw new IllegalStateException("Failed constructing PDW!");
            }
        } else {
            String message = String.format("Too many tokens supplied for end of PDW!");
            log.warn(message);
            throw new IllegalStateException("Failed constructing PDW!");
        }
    }

    private void processStartTokens(Map<String, List<Object>> parsedTokens, String key) throws IllegalStateException, NumberFormatException {
        List<Object> tmp;
        tmp = parsedTokens.remove(key); // Processing start reference
        pdw.setStartReference(key);
        pdw.setEnabled(true);
        if (tmp.isEmpty()) {//Assume zero offset.
            pdw.setStartOffset(0.0);
        } else if (tmp.size() == 1) {
            String value = (String) tmp.get(0);
            if (NumberUtils.isParsable(value)) {
                double dblValue = Double.parseDouble(value);
                pdw.setStartOffset(dblValue);
            } else { // non-numeric is a mistake here.
                String message = String.format("Expected a numeric value but got %s!", value);
                log.warn(message);
                throw new IllegalStateException("Failed parsing tokens to construct PDW!");
            }
        } else if (tmp.size() == 2) {
            String value1 = (String) tmp.get(0);
            String value2 = (String) tmp.get(1);
            if (value1.toUpperCase().equals("N") && NumberUtils.isParsable(value2)) {
                Integer npts = getIntegerOffsetValue(value2);
                if (npts != null) {
                    pdw.setStartIntegerOffset(npts);
                }
            } else {
                String message = String.format("Invalid tokens supplied for beginning of PDW! Expected a number or N followed by a number.");
                log.warn(message);
                throw new IllegalStateException("Failed constructing PDW!");

            }
        } else {
            String message = String.format("Too many tokens supplied for start of PDW!");
            log.warn(message);
            throw new IllegalStateException("Failed constructing PDW!");
        }
    }

    private void processImplicitReftimePair(List<Object> tmp, Map<String, List<Object>> parsedTokens) throws IllegalStateException {
        if (tmp.size() == 2 && parsedTokens.isEmpty()) {
            // User just supplied two numbers meant to be interpreted as relative to reftime
            pdw.setDefaults();
            pdw.setEnabled(true);
            pdw.setStartReference("reftime");
            pdw.setEndReference("reftime");
            String tmpValue = (String) tmp.get(0);
            Double doubleValue = getOffsetValue(tmpValue);
            if (doubleValue == null) {
                // Could not be parsed
                return;
            } else {
                pdw.setStartOffset(doubleValue);
            }
            tmpValue = (String) tmp.get(1);
            doubleValue = getOffsetValue(tmpValue);
            if (doubleValue != null) {
                pdw.setEndOffset(doubleValue);
            }
            pdw.validate();
            return;
        } else {
            String msg = "Unexpected combination of tokens. Unable to build PDW.";
            throw new IllegalStateException(msg);
        }
    }

    private Double getOffsetValue(String tmpValue) {
        if (!NumberUtils.isParsable(tmpValue)) { // Verify it is a number
            String msg = String.format("%s is not a valid number! %s is set to OFF.", tmpValue, childName);
            log.warn(msg);
            pdw.setEnabled(false);
            return null;
        } else {
            return Double.valueOf(tmpValue);
        }

    }

    private Integer getIntegerOffsetValue(String tmpValue) {
        if (!NumberUtils.isParsable(tmpValue)) { // Verify it is a number
            String msg = String.format("%s is not a valid number! %s is set to OFF.", tmpValue, childName);
            log.warn(msg);
            pdw.setEnabled(false);
            return null;
        } else {
            return Integer.valueOf(tmpValue);
        }

    }

}
