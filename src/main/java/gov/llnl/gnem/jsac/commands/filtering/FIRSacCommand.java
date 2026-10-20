/*-
 * #%L
 * Java Seismic Analysis Code (JSAC)
 *  LLNL-CODE-855505
 *  This work was performed under the auspices of the U.S. Department of Energy
 *  by Lawrence Livermore National Laboratory under Contract DE-AC52-07NA27344.
 * %%
 * Copyright (C) 2022 - 2026 Lawrence Livermore National Laboratory
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
package gov.llnl.gnem.jsac.commands.filtering;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.util.PathManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FIRSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("CCB", ValuePossibilities.NO_VALUE, null));
    }

    private boolean valid;
    private boolean consecutiveFiles;
    private String filename;
    private float[] coefficients;

    public FIRSacCommand() {
    }

    @Override
    public void execute() {

        if (valid) {

            String dir = PathManager.getInstance().getCurrentDir().toString();
            File file = new File(dir, filename);

            ArrayList<String> lines = new ArrayList<>();
            try (Scanner reader = new Scanner(file)) {
                while (reader.hasNextLine()) {
                    lines.add(reader.nextLine());
                }

                coefficients = new float[lines.size()];
                for (int i = 0; i < lines.size(); i++) {
                    coefficients[i] = Float.parseFloat(lines.get(i));
                }

                FIRFilter implementation = new FIRFilter(coefficients, consecutiveFiles);
                implementation.apply();

            } catch (FileNotFoundException ex) {
                throw new IllegalStateException("Problem opening or reading coefficient file: " + file.toString());
            }

        }
    }

    @Override
    public void initialize(String[] tokens
    ) {

        // defaults
        valid = true;
        coefficients = null;
        consecutiveFiles = false;
        filename = "FIR.txt";

        // parsing
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);

        if (!parsedTokens.isEmpty()) {

            if (parsedTokens.containsKey("CCB")) {
                consecutiveFiles = true;
            }

            List< Object> ttmp = parsedTokens.remove(TokenListParser.LEFT_OVER_TOKENS);
            if (ttmp == null) {
                filename = "FIR.txt";
            } else if (ttmp.size() > 1) {
                throw new IllegalStateException("Ambiguous file name");
            } else {
                filename = (String) ttmp.get(0);
            }

        }

    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"FIR"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
