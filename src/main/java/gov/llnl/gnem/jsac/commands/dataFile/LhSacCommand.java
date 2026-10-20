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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.math.NumberUtils;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.util.HeaderLister;
import gov.llnl.gnem.jsac.util.HeaderLister.FileSelection;
import gov.llnl.gnem.jsac.util.HeaderLister.ListType;

/**
 *
 * @author dodge1
 */
public class LhSacCommand implements SacCommand {

    private final Scanner sc;

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("FILES", ValuePossibilities.ONE_OR_MORE, String.class));
        descriptors.add(new AttributeDescriptor("DEFAULT", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("DEF", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("PICKS", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("SPECIAL", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("INCLUSIVE", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("AGENCY", ValuePossibilities.ONE_VALUE, String.class));
    }

    private final List<Integer> fileNumbers;

    public LhSacCommand() {
        sc = new Scanner(System.in);
        fileNumbers = new ArrayList<>();
    }

    @Override
    public void initialize(String[] tokens) {
        fileNumbers.clear();
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens, true);
        if (parsedTokens.isEmpty()) {
            return;
        }
        List<Object> tmp = parsedTokens.remove("INCLUSIVE");
        if (tmp != null && tmp.size() == 1) {
            String value = (String) tmp.get(0);
            if (value.equalsIgnoreCase("ON")) {
                HeaderLister.getInstance().setInclusive(true);
            } else {
                HeaderLister.getInstance().setInclusive(false);
            }
        }
        tmp = parsedTokens.remove("DEFAULT");
        if (tmp != null) {
            HeaderLister.getInstance().setListType(ListType.DEFAULT);
        }
        tmp = parsedTokens.remove("DEF");
        if (tmp != null) {
            HeaderLister.getInstance().setListType(ListType.DEFAULT);
        }
        tmp = parsedTokens.remove("PICKS");
        if (tmp != null) {
            HeaderLister.getInstance().setListType(ListType.PICKS);
        }
        tmp = parsedTokens.remove("SPECIAL");
        if (tmp != null) {
            HeaderLister.getInstance().setListType(ListType.SPECIAL);
        }

        List<Object> files = parsedTokens.remove("FILES");
        if (files != null) {
            if (files.size() == 1) {
                String tmp2 = (String) files.get(0);
                if (tmp2.equalsIgnoreCase("ALL")) {
                    HeaderLister.getInstance().setFileSelection(FileSelection.ALL);
                } else if (tmp2.equalsIgnoreCase("NONE")) {
                    HeaderLister.getInstance().setFileSelection(FileSelection.NONE);
                } else if (NumberUtils.isParsable(tmp2)) {
                    fileNumbers.add(Integer.valueOf(tmp2));
                    HeaderLister.getInstance().setFileSelection(FileSelection.LIST);
                }
            } else if (files.size() > 1) {
                for (Object obj : files) {
                    String tmp2 = (String) obj;
                    if (NumberUtils.isParsable(tmp2)) {
                        fileNumbers.add(Integer.valueOf(tmp2));
                        HeaderLister.getInstance().setFileSelection(FileSelection.LIST);
                    }
                }
            }
        }

        // Gather explicitly-specified header fields to list
        Collection<String> hdrlist = parsedTokens.keySet();
        if (!hdrlist.isEmpty()) {
            HeaderLister.getInstance().setHeaderList(hdrlist);
            HeaderLister.getInstance().setListType(ListType.SPECIAL);
        }
    }

    @Override
    public void execute() {
        if (HeaderLister.getInstance().getFileSelection() == FileSelection.NONE) {
            return; // don't list anything.
        }
        List<SacTraceData> files = SacDataModel.getInstance().getData();

        List<SacTraceData> filesToProcess = new ArrayList<>();
        if (HeaderLister.getInstance().getFileSelection() == FileSelection.ALL) {
            filesToProcess.addAll(files);
        } else if (HeaderLister.getInstance().getFileSelection() == FileSelection.LIST) {
            for (int j = 0; j < fileNumbers.size(); ++j) {
                int idx = fileNumbers.get(j) - 1;  //SAC is one-based
                if (idx >= 0 && idx < files.size()) {
                    filesToProcess.add(files.get(idx));
                }
            }
        }
        for (int j = 0; j < filesToProcess.size(); ++j) {
            SacTraceData std = filesToProcess.get(j);

            std.listHeader(System.out);
            if (j < filesToProcess.size() - 1) {
                displayPrompt();
                String text = sc.nextLine().trim().toUpperCase();
                if (text.equals("Q") || text.equals("QUIT")) {
                    return;
                }
            }

        }
    }

    private void displayPrompt() {
        System.out.println("Waiting (Q to quit listing)>");
        System.out.flush();
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"LH", "LISTHEADER"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
