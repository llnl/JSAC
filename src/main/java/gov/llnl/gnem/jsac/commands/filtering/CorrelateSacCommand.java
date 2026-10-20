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
package gov.llnl.gnem.jsac.commands.filtering;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACFileReader;
import gov.llnl.gnem.jsac.io.SACHeader;
import gov.llnl.gnem.jsac.util.PathManager;

public class CorrelateSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add( new AttributeDescriptor( "MASTER",    ValuePossibilities.ONE_VALUE, String.class ) );
        descriptors.add( new AttributeDescriptor( "FILE",      ValuePossibilities.ONE_VALUE, String.class ) );
        descriptors.add( new AttributeDescriptor( "NORMALIZE", ValuePossibilities.ONE_VALUE, String.class ) );
        
        // abbreviations
        
        descriptors.add( new AttributeDescriptor( "M",         ValuePossibilities.ONE_VALUE, String.class ) );
        descriptors.add( new AttributeDescriptor( "F",         ValuePossibilities.ONE_VALUE, String.class ) );
        descriptors.add( new AttributeDescriptor( "N",         ValuePossibilities.ONE_VALUE, String.class ) );
    }

    private static final float EPS = 1.0e-5f;

    private static boolean normalize = false;
    private static float[] master;
    private final List<SacTraceData> traceData;

    private double delta;

    public CorrelateSacCommand() {

        traceData = new ArrayList<>();
    }

    private void populateTraceData() throws IllegalStateException {
    	
    	List< SacTraceData > tmp = SacDataModel.getInstance().getData();
    	
    	if ( tmp.isEmpty() ) 
    		throw new IllegalStateException( "No SAC files read in" );
    	else {
            traceData.clear();
            traceData.addAll( tmp );
    	}

        // check consistency of sampling intervals
        
        boolean first = true;

        delta = -1.0;

        for (SacTraceData std : traceData) {
            if (first) {
                delta = std.getDelta();
                first = false;
            } else if ((std.getDelta() - delta) > EPS * delta) {
                throw new IllegalStateException( "A data file has an inconsistent sampling interval, delta: " + std.getDelta());
            }
        }
        
    }
    

    public static boolean isIntegerString(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        // Check every single character in the string
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    
    @Override
    public void initialize( String[] tokens ) {
    	
        populateTraceData();
    	
    	master = traceData.get(0).getData();

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens( descriptors, tokens );

        List<Object> parameters = parsedTokens.remove( "MASTER" );
        if ( parameters == null ) {
        	parameters = parsedTokens.remove( "M" );
        }
        if ( parameters != null ) {
            String token = (String) parameters.get(0);
            if ( isIntegerString(token) ) {
                int tmp = Integer.parseInt(token) - 1; // command is 1-based but List is 0-based
                if ( tmp < 0  ||  tmp > traceData.size() - 1 ) {
                    throw new IllegalStateException( "Specified file number (" + (tmp + 1) + " is out of range!" );
                } else {
                    master = traceData.get(tmp).getData();
                }
            } else {
                for ( SacTraceData std : traceData ) {
                    if ( std.getFilename().endsWith( token ) ) {
                        master = std.getData();
                    }
                }
            }
        }
        
        normalize = true;
        parameters = parsedTokens.remove( "NORMALIZE" );
        if ( parameters == null ) {
            parameters = parsedTokens.remove( "N" );
        }
        if ( parameters != null ) {
        	
            String token = (String) parameters.get(0);
            if ( token.equalsIgnoreCase( "OFF" ) ) {
                normalize = false;
            } 
            else if ( token.equalsIgnoreCase( "ON" ) ) {
            	normalize = true;
            }
            else {
            	throw new IllegalStateException ( "Option " + token + " not implemented" );
            }
            
        }
        
        parameters = parsedTokens.remove( "FILE" );
        if ( parameters == null ) {
            parameters = parsedTokens.remove( "F" );
        }
        if ( parameters != null ) {
            Path filePath = PathManager.getInstance().resolvePath( (String) parameters.get(0) );
            try {
                SACFileReader reader = new SACFileReader( filePath );
                SACHeader header = reader.getHeader();

                if (Math.abs(header.getDelta() - delta) > EPS * delta) {
                    throw new IllegalStateException("File sampling interval does not match data sampling interval: " + header.getDelta());
                }

                master = new float[ header.getNpts() ];
                reader.read(master);
                reader.close();

            } catch (IOException e) {
                throw new IllegalStateException( "Failed reading master SAC file!" );
            }
        }
        
    }

    
    @Override
    public void execute() {

    	SimpleCorrelator.correlateAll( traceData, master, normalize );

    }

    
    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "CORRELATE" };
        return new ArrayList<>(Arrays.asList(names));
    }

}
