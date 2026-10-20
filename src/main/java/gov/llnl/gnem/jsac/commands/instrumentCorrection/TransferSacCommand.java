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
package gov.llnl.gnem.jsac.commands.instrumentCorrection;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isti.jevalresp.ChannelMatchPolicy.Policy;
import gov.llnl.gnem.jsac.SacDataModel;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.Misc;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.ExternalWaveformServiceProvider;
import gov.llnl.gnem.jsac.transfer.ResponseOptions;
import gov.llnl.gnem.jsac.transfer.SacResponseType;
import gov.llnl.gnem.jsac.transfer.TransferProcessor;
import gov.llnl.gnem.jsac.transfer.TransferSubOptionParser;
import gov.llnl.gnem.jsac.util.PathManager;
import gov.llnl.gnem.response.ChannelMatchPolicyHolder;
import gov.llnl.gnem.response.FreqLimits;
import gov.llnl.gnem.response.ResponseType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Paths;
import llnl.gnem.core.util.StreamKey;

/**
 *
 * @author dodge1
 */
public class TransferSacCommand implements SacCommand {

    private static final Logger log = LoggerFactory.getLogger(TransferSacCommand.class);

    private static FreqLimits freqLimits = null;
    private static SacResponseType fromType = SacResponseType.NONE;
    private static SacResponseType toType = SacResponseType.NONE;
    private Policy policy = Policy.AGENCY_NET_STA_CHAN_LOCID_EPOCH_MATCH;
    private ResponseOptions fromResponseOptions = null;
    private ResponseOptions toResponseOptions = null;
    private Path fromResponseFilePath = null;
    private Path toResponseFilePath = null;
    private StreamKey substituteKey = null;
    private Double substituteTime = null;
    private boolean listServices = false;

    public static void resetDefaults() {
        freqLimits = null;
        fromType = SacResponseType.NONE;
        toType = SacResponseType.NONE;
    }

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("SERVICE_NAME", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("LIST_SERVICES", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("FROM", ValuePossibilities.ONE_OR_MORE, String.class));
        descriptors.add(new AttributeDescriptor("TO", ValuePossibilities.ONE_OR_MORE, String.class));
        descriptors.add(new AttributeDescriptor("FREQLIMITS", ValuePossibilities.FOUR_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("FREQ", ValuePossibilities.FOUR_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("PREWHITENING", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("NETWORK", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("NET", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("STATION", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("STA", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("CHANNEL", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("CHAN", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("LOCID", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("TIME", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("HTIME", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("POLICY", ValuePossibilities.ONE_VALUE, String.class));
    }

    public TransferSacCommand() {
    }

    @Override
    public void initialize(String[] tokens) {
        ChannelMatchPolicyHolder.getInstance().setPolicy(policy);
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        policy = maybeGetPolicy(parsedTokens);
        substituteKey = maybeGetSubstituteKey(parsedTokens);
        substituteTime = maybeGetSubstituteTime(parsedTokens);
        freqLimits = TransferSubOptionParser.maybeGetFreqLimits(parsedTokens);
        listServices = false;
        List<Object> tmpList = parsedTokens.remove("LIST_SERVICES");
        if (tmpList != null) {
            listServices = true;
            return;
        }

        List<Object> values = parsedTokens.get("TO");
        if (values != null && values.size() >= 1) {
            try {
                String tmp = (String) values.get(0);
                toType = SacResponseType.valueOf(tmp.toUpperCase());
                if (toType != null) {
                    switch (toType) {
                        case POLEZERO:
                            try {
                                toResponseOptions = TransferSubOptionParser.getToOptions(values);
                            } catch (IllegalStateException e) {
                                System.out.println(e.getMessage());
                                throw (e);
                            }
                            break;
                        case CSS:
                            if (values.size() > 1) {
                                String tmpFileName = (String) values.get(1);
                                toResponseFilePath = PathManager.getInstance().resolveAndValidateFile(tmpFileName);
                            } else {
                                System.out.println("Option CSS must be followed by a file name!");
                                throw new IllegalStateException("File name was not specified!");
                            }
                            break;
                        case EVALRESP:
                            try {
                                toResponseOptions = TransferSubOptionParser.getToOptions(values);
                            } catch (IllegalStateException e) {
                                System.out.println(e.getMessage());
                                throw (e);
                            }
                            break;
                        case WA_NEW:
                        case WA_OLD: {
                            toResponseOptions = getBuiltInPZfilename(toType.toString());
                            toType = SacResponseType.POLEZERO;
                            break;
                        }
                    }
                }
            } catch (IllegalStateException ex) {
                throw new IllegalStateException("Invalid TO type: " + values.get(0));
            }
        }
        values = parsedTokens.get("FROM");
        if (values != null && values.size() >= 1) {
            try {
                String tmp = (String) values.get(0);
                fromType = SacResponseType.valueOf(tmp.toUpperCase());
                if (null != fromType) {
                    switch (fromType) {
                        case POLEZERO:
                            try {
                                fromResponseOptions = TransferSubOptionParser.getOptions(values);
                            } catch (IllegalStateException e) {
                                System.out.println(e.getMessage());
                                throw (e);
                            }
                            break;
                        case CSS:
                            if (values.size() > 1) {
                                String tmpFileName = (String) values.get(1);
                                fromResponseFilePath = PathManager.getInstance().resolveAndValidateFile(tmpFileName);
                            } else {
                                System.out.println("Option CSS must be followed by a file name!");
                                throw new IllegalStateException("File name was not specified!");
                            }
                            break;
                        case EVALRESP:
                            try {
                                fromResponseOptions = TransferSubOptionParser.getOptions(values);
                            } catch (IllegalStateException e) {
                                System.out.println(e.getMessage());
                                throw (e);
                            }
                            break;
                        case DATA_SERVICE: {
                            break;
                        }
                        case WA_NEW:
                        case WA_OLD: {
                            fromResponseOptions = getBuiltInPZfilename(fromType.toString());
                            fromType = SacResponseType.POLEZERO;
                            break;
                        }

                        default:
                            break;
                    }
                } else {
                    fromType = SacResponseType.NONE;
                }
            } catch (IllegalStateException ex) {
                throw ex;
            }
        }
        List<Object> serviceName = parsedTokens.remove("SERVICE_NAME");
        if (serviceName != null && serviceName.size() == 1) {
            for (Object obj : serviceName) {
                String service = (String) obj;
                ExternalWaveformServiceProvider.getInstance().setCurrentService(service.toUpperCase());
            }
        }

    }

    @Override
    public void execute() {
        if (listServices) {
            for (String string : ExternalWaveformServiceProvider.getInstance().getServiceDescriptions()) {
                System.out.println(string);
            }
            return;
        }
        if (SacDataModel.getInstance().isEmpty()) {
            log.warn("Cannot execute the transfer command because no data are in memory!");
            return;
        }
        ChannelMatchPolicyHolder.getInstance().setPolicy(policy);
        try {
            TransferProcessor.getInstance().transfer(fromType, toType, freqLimits, fromResponseOptions, fromResponseFilePath, toResponseOptions, toResponseFilePath, substituteKey, substituteTime);
        } catch (FileNotFoundException ex) {
            log.error("Failed executing transfer command! {}", ex);
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"TRANS", "TRANSFER"};
        return new ArrayList<>(Arrays.asList(names));
    }

    private StreamKey maybeGetSubstituteKey(Map<String, List<Object>> parsedTokens) {

        String net = getStringValue(parsedTokens, "NETWORK", "NET");
        String sta = getStringValue(parsedTokens, "STATION", "STA");
        String chan = getStringValue(parsedTokens, "CHANNEL", "CHAN");
        String locid = getStringValue(parsedTokens, "LOCID", "LOCID");
        if (net != null || sta != null || chan != null || locid != null) {
            return new StreamKey(net, sta, chan, locid);
        } else {
            return null;
        }
    }

    private String getStringValue(Map<String, List<Object>> parsedTokens, String longString, String shortString) {
        String value = null;
        List<Object> values = parsedTokens.remove(longString);
        if (values == null) {
            values = parsedTokens.remove(shortString);
        }
        if (values != null && !values.isEmpty()) {
            value = (String) values.get(0);
        }
        return value;
    }

    private Double maybeGetSubstituteTime(Map<String, List<Object>> parsedTokens) {
        List<Object> values = parsedTokens.remove("TIME");
        if (values != null && !values.isEmpty()) {
            return (Double) values.get(0);
        }
        values = parsedTokens.remove("HTIME");
        if (values != null && !values.isEmpty()) {
            String tmp = (String) values.get(0);
            return Misc.parseTimeString(tmp);
        }
        return null;
    }

    private Policy maybeGetPolicy(Map<String, List<Object>> parsedTokens) {
        String p = getStringValue(parsedTokens, "POLICY", "POLICY");
        if (p != null && !p.isEmpty()) {
            switch (p.toUpperCase()) {
                case "NET_STA_CHAN_LOCID_TIME":
                case "NSCLT":
                    return Policy.AGENCY_NET_STA_CHAN_LOCID_EPOCH_MATCH;
                case "AGENCY_NET_STA_CHAN_LOCID_TIME":
                case "ANSCLT":
                    return Policy.AGENCY_NET_STA_CHAN_LOCID_EPOCH_MATCH;
                case "STA_CHAN_TIME":
                case "SCT":
                    return Policy.STA_CHAN_EPOCH_MATCH;
            }
        }

        return Policy.AGENCY_NET_STA_CHAN_LOCID_EPOCH_MATCH;
    }

    private ResponseOptions getBuiltInPZfilename(String respName) {
        try {
            String dir = System.getProperty("java.io.tmpdir");
            String outfileName = respName + ".txt";
            Path path = Paths.get(dir, outfileName);
            List<String> lines = readPoleZeroFromResource(respName);
            try (PrintWriter pw = new PrintWriter(path.toString())) {
                for (String line : lines) {
                    pw.println(line);
                }
            }
            return new ResponseOptions(ResponseType.SACPZF, path.toString(), path.getParent());
        } catch (IOException ex) {
            System.getLogger(TransferSacCommand.class.getName()).log(System.Logger.Level.ERROR, (String) "Error retrieving response file.", ex);
            return null;
        }
    }

    public List<String> readPoleZeroFromResource(String respName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        List<String> lines = new ArrayList<>();
        try (InputStream in = classLoader.getResourceAsStream("gov/llnl/gnem/jsac/commands/instrumentCorrection/" + respName + ".txt")) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;

                // readLine() returns null when the end of the file is reached
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
        }
        return lines;
    }

}
