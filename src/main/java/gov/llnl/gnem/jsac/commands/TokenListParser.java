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

import gov.llnl.gnem.jsac.util.OnOffFlag;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.math.NumberUtils;


public class TokenListParser {

    public static final String LEFT_OVER_TOKENS = "LEFT_OVER_TOKENS";

    public static List<Object> getObjects(Map<String, List<Object>> parsedTokens, List<String> paramNames) {
        List<Object> objects = null;
        for (String param : paramNames) {
            objects = parsedTokens.remove(param);
            if (objects != null) {
                return objects;
            }
        }
        return objects;
    }

    public static List<Object> getObjects(Map<String, List<Object>> parsedTokens,String paramName) {
        List<String> names = new ArrayList<>();
        names.add(paramName);
        return getObjects(parsedTokens, names);
    }

    public static List<Object> getObjects(Map<String, List<Object>> parsedTokens,String paramName1, String paramName2) {
        List<String> names = new ArrayList<>();
        names.add(paramName1);
        names.add(paramName2);
        return getObjects(parsedTokens, names);
    }

    public static Map<String, List<Object>> parseTokens(List<AttributeDescriptor> descriptors, String[] tokens) {
        return parseTokens(descriptors, tokens, false);
    }

    public static Map<String, List<Object>> parseTokens(List<AttributeDescriptor> descriptors, String[] tokens, boolean parseHeaderNames) {
        Map<String, List<Object>> result = new LinkedHashMap<>();
        List<String> mytokens = new ArrayList<>(Arrays.asList(tokens));
        mytokens.remove(0); // Don't need the command name anymore.
        if (mytokens.isEmpty()) {
            return result;
        }

        //parse-out any field-value token pairs where field is from the SAC Header.
        if (parseHeaderNames) {
            Map<String, Object> headerVariableAssignments = FieldValueParser.parseHeaderVariablesFromTokenList(mytokens);
            for (String str : headerVariableAssignments.keySet()) {
                List<Object> c = new ArrayList<>();
                c.add(headerVariableAssignments.get(str));
                result.put(str, c);
            }
        }

        List<Integer> removedTokens = new ArrayList<>();
        Map<String, AttributeDescriptor> attributeDescriptorMap = buildAttributeDescriptorMap(descriptors);
        Map<Integer, AttributeDescriptor> tokenPosDescriptorMap = new HashMap<>();
        for (int j = 0; j < mytokens.size(); ++j) {
            String str = mytokens.get(j);
            AttributeDescriptor ad = attributeDescriptorMap.get(str.toUpperCase());
            if (ad != null) {
                tokenPosDescriptorMap.put(j, ad);
                removedTokens.add(j);
            }
        }
        List<Integer> attributePositions = new ArrayList<>(tokenPosDescriptorMap.keySet());
        Collections.sort(attributePositions);

        int numAttributes = attributePositions.size();
        for (int j = 0; j < numAttributes; ++j) {
            int index = attributePositions.get(j);
            AttributeDescriptor descriptor = tokenPosDescriptorMap.get(index);
            Type type = descriptor.getAttributeType();
            int numTokensForAttribute = 0;
            if (descriptor.getNumExpectedValues().getNumValues() != 0) {
                numTokensForAttribute = getTokenCountForAttribute(mytokens, index, j, numAttributes, attributePositions);
            }
            if (numTokensForAttribute == 0) { // User supplied no tokens allowing default values to be used perhaps
                List<Object> c = new ArrayList<>();
                result.put(descriptor.getAttributeName(), c);
            } else if (numTokensForAttribute >= descriptor.getNumExpectedValues().getNumValues() || descriptor.getNumExpectedValues().getNumValues() < 0) {
                List<Object> c = new ArrayList<>();
                result.put(descriptor.getAttributeName(), c);

                for (int k = 0; k < numTokensForAttribute; ++k) {
                    int m = index + k + 1;
                    String token = mytokens.get(m);
                    if (descriptor.getNumExpectedValues().getNumValues() < 0 || k < descriptor.getNumExpectedValues().getNumValues()) {
                        Object obj = parseToken(token, type);
                        if (obj != null) {
                            c.add(obj);
                            removedTokens.add(m);
                        }
                    }
                }
            }
        }
        Collections.sort(removedTokens, Collections.reverseOrder());
        for (int val : removedTokens) {
            mytokens.remove(val);
        }
        if (!mytokens.isEmpty()) {
            result.put(LEFT_OVER_TOKENS, new ArrayList<>(mytokens));
        }
        return result;
    }

    private static int getTokenCountForAttribute(List<String> mytokens, int index, int j, int numAttributes, List<Integer> attributePositions) {
        int numTokens = mytokens.size() - index - 1;
        if (j < numAttributes - 1) {

            int nextIndex = attributePositions.get(j + 1);
            numTokens = nextIndex - index - 1;
        }
        return numTokens;
    }

    private static Map<String, AttributeDescriptor> buildAttributeDescriptorMap(List<AttributeDescriptor> descriptors) {
        Map<String, AttributeDescriptor> result = new HashMap<>();
        for (AttributeDescriptor des : descriptors) {
            result.put(des.getAttributeName().toUpperCase(), des);
        }
        return result;
    }

    private static Object parseToken(String token, Type type) {

        if (type.equals(String.class)) {
            return token;
        } else if (type.equals(OnOffFlag.class)) {
            return OnOffFlag.toBoolean(token);
        } else if (type.equals(Float.class)) {
            if (token.endsWith(".")) {
                token = token + "0";
            }
            if (NumberUtils.isParsable(token)) {
                return Float.valueOf(token);
            }

        } else if (type.equals(Double.class)) {
            if (token.endsWith(".")) {
                token = token + "0";
            }
            if (NumberUtils.isParsable(token)) {
                return Double.valueOf(token);
            }
        } else if (type.equals(Integer.class) && NumberUtils.isParsable(token)) {
            return Integer.valueOf(token);
        } else if (type.equals(Long.class) && NumberUtils.isParsable(token)) {
            return Long.valueOf(token);
        }

        return null;
    }

    public static String[] tokenizeString(String text) {
        if (!text.contains("\'") && !text.contains("\"")) {
            return text.split("(\\s+)|(\\s*,\\s*)");
        }
        ArrayList<String> tokens = new ArrayList<>();
        int singleQuotes = countSingleQuotes(text);
        if (singleQuotes % 2 != 0) {
            throw new IllegalStateException("Command string must have an even number of (\') characters!");
        }
        int doubleQuotes = countDoubleQuotes(text);
        if (doubleQuotes % 2 != 0) {
            throw new IllegalStateException("Command string must have an even number of (\") characters!");
        }
        if (singleQuotes > 0 && doubleQuotes > 0) {
            throw new IllegalStateException("Command string cannot contain a mixture of (\") and (\") characters!");
        }
        Pattern pattern = singleQuotes > 0 ? Pattern.compile("([^\']\\S*|\'.+?\')\\s*") : Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String tmp = matcher.group(0).trim();
            if (tmp.contains("\'") || tmp.contains("\"")) {
                tmp = tmp.substring(1);
                tmp = tmp.substring(0, tmp.length() - 1);
            }

            tokens.add(tmp);
        }
        return tokens.toArray(new String[0]);
    }

    private static int countSingleQuotes(String text) {
        return countChrs(text, '\'');
    }

    private static int countDoubleQuotes(String text) {
        return countChrs(text, '\"');
    }

    private static int countChrs(String text, char chr) {
        char[] chrs = text.toCharArray();
        int count = 0;
        for (char c : chrs) {
            if (c == chr) {
                ++count;
            }
        }
        return count;

    }
}
