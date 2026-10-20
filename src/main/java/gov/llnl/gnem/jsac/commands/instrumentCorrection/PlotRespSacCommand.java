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

import com.isti.jevalresp.ChannelMatchPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.Misc;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.ExternalWaveformServiceProvider;
import gov.llnl.gnem.jsac.plots.plotresp.PlotRespFrame;
import gov.llnl.gnem.jsac.transfer.TransferProcessor;
import gov.llnl.gnem.response.ChannelMatchPolicyHolder;
import gov.llnl.gnem.response.TransferData;
import java.nio.file.Path;
import javax.swing.SwingUtilities;
import llnl.gnem.core.dataObjects.DataAccessException;
import llnl.gnem.core.util.StreamKey;
import llnl.gnem.core.util.TimeT;
import llnl.gnem.core.waveform.seismogram.CssSeismogram;

public class PlotRespSacCommand implements SacCommand {

    private static final Logger log = LoggerFactory.getLogger(PlotRespSacCommand.class);
    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();
    private StreamKey streamKey;
    private Double time;
    private String filename;
    private final ChannelMatchPolicy.Policy policy = ChannelMatchPolicy.Policy.AGENCY_NET_STA_CHAN_LOCID_EPOCH_MATCH;
    private int nsamp;
    private double samprate;
    private boolean listServices = false;
    private boolean unwrapPhase = false;

    static {
        descriptors.add(new AttributeDescriptor("SERVICE", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("LIST_SERVICES", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("FILE_NAME", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("AGENCY", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("NETWORK", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("NET", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("STATION", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("STA", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("CHANNEL", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("CHAN", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("LOCID", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("TIME", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("HTIME", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("NSAMP", ValuePossibilities.ONE_VALUE, Integer.class));
        descriptors.add(new AttributeDescriptor("SAMPRATE", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("UNWRAP", ValuePossibilities.NO_VALUE, null));
    }

    public PlotRespSacCommand() {
    }

    @Override
    public void initialize(String[] tokens) {
        ChannelMatchPolicyHolder.getInstance().setPolicy(policy);
        streamKey = null;
        time = new TimeT().getEpochTime();
        filename = null;
        listServices = false;
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        List<Object> tmpList = parsedTokens.remove("LIST_SERVICES");
        if (tmpList != null) {
            listServices = true;
            return;
        }

        maybeSetService(parsedTokens);
        setNsamp(parsedTokens);
        setSamprate(parsedTokens);
        setUnwrap(parsedTokens);
        streamKey = maybeGetStreamKey(parsedTokens);
        time = maybeGetTime(parsedTokens);
        filename = maybeGetFilename(parsedTokens);

        tmpList = parsedTokens.remove(TokenListParser.LEFT_OVER_TOKENS);
        if (tmpList != null && tmpList.size() >= 1) {
            for (Object obj : tmpList) {
                System.out.println("Unrecognized token: " + obj + "!");
            }
        }
    }

    private void setSamprate(Map<String, List<Object>> parsedTokens) {
        List<Object> values = parsedTokens.remove("SAMPRATE");
        samprate = 100.0;
        if (values != null && !values.isEmpty()) {
            samprate = (Double) values.get(0);
        }
    }

    private void setNsamp(Map<String, List<Object>> parsedTokens) {
        List<Object> values = parsedTokens.remove("NSAMP");
        nsamp = 10000;
        if (values != null && !values.isEmpty()) {
            nsamp = (Integer) values.get(0);
        }
    }

    private void setUnwrap(Map<String, List<Object>> parsedTokens) {
        List<Object> values = parsedTokens.remove("UNWRAP");
        unwrapPhase = false;
        if (values != null) {
            unwrapPhase = true;
        }
    }

    private void maybeSetService(Map<String, List<Object>> parsedTokens) {
        List<Object> serviceName = parsedTokens.remove("SERVICE");
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
        System.out.println("Using the following parameters:");
        System.out.println("\tChannel key: " + streamKey);
        System.out.println("\tFile name: " + filename);
        System.out.println("\tInstant: " + time + " (" + new TimeT(time).toString() + ")");
        System.out.println("\tNsamp: " + nsamp);
        System.out.println("\tSamprate: " + samprate);
        System.out.println("\tPhase unwrapping: " + (unwrapPhase ? "on" : "off"));

        if (streamKey == null && filename == null) {
            System.out.println("Invalid or incomplete specification supplied!");
        } else {
            try {
                ChannelMatchPolicy apolicy = ChannelMatchPolicyHolder.getInstance().getPolicy();
                float[] data = new float[nsamp];
                CssSeismogram s = new CssSeismogram(1L, streamKey, data, samprate, new TimeT(time), null, null);
                TransferData transferData = getTransferData(s, apolicy);
                if (transferData != null) {
                    SwingUtilities.invokeLater(() -> {
                        PlotRespFrame.getInstance().plotDataBlock(streamKey, new TimeT(time), transferData, unwrapPhase);
                        PlotRespFrame.getInstance().setVisible(true);
                    });
                }
            } catch (DataAccessException ex) {
                System.getLogger(PlotRespSacCommand.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }

    }

    private TransferData getTransferData(CssSeismogram s, ChannelMatchPolicy apolicy) throws DataAccessException {
        if (filename != null) {
            return TransferProcessor.getInstance().getFromTransferFunctionFromPath(Path.of(filename), s, apolicy);
        } else if (streamKey != null) {
            return TransferProcessor.getInstance().getFromTransferFunctionFromService(streamKey, s, apolicy);
        } else {
            throw new IllegalStateException("Neither file name nor channel description have been suplied!");
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"PLOTRESP", "PR"};
        return new ArrayList<>(Arrays.asList(names));
    }

    private StreamKey maybeGetStreamKey(Map<String, List<Object>> parsedTokens) {
        String agency = getStringValue(parsedTokens, "AGENCY", "AGENCY");
        String net = getStringValue(parsedTokens, "NETWORK", "NET");
        String sta = getStringValue(parsedTokens, "STATION", "STA");
        String chan = getStringValue(parsedTokens, "CHANNEL", "CHAN");
        String locid = getStringValue(parsedTokens, "LOCID", "LOCID");
        if (agency != null && net != null && sta != null && chan != null && locid != null) {
            return new StreamKey(agency, net, sta, chan, locid);
        } else if (sta != null && chan != null) {
            return new StreamKey(sta, chan);
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

    private Double maybeGetTime(Map<String, List<Object>> parsedTokens) {
        List<Object> values = parsedTokens.remove("TIME");
        if (values != null && !values.isEmpty()) {
            return (Double) values.get(0);
        }
        values = parsedTokens.remove("HTIME");
        if (values != null && !values.isEmpty()) {
            String tmp = (String) values.get(0);
            return Misc.parseTimeString(tmp);
        }
        return new TimeT().getEpochTime();
    }

    private String maybeGetFilename(Map<String, List<Object>> parsedTokens) {
        List<Object> values = parsedTokens.remove("FILE_NAME");
        if (values != null && !values.isEmpty()) {
            return (String) values.get(0);
        }
        return null;
    }

}
