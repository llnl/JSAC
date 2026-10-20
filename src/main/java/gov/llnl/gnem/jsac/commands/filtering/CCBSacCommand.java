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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;

public class CCBSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();
    
    private boolean valid;
    private double  segmentLength;
    private double  maxDuration;

    static {
        descriptors.add( new AttributeDescriptor( "MAX_DURATION", ValuePossibilities.ONE_VALUE, Double.class ) );
        descriptors.add( new AttributeDescriptor( "MAX",          ValuePossibilities.ONE_VALUE, Double.class ) );
        descriptors.add( new AttributeDescriptor( "BLOCK",        ValuePossibilities.ONE_VALUE, String.class ) );
        descriptors.add( new AttributeDescriptor( "B",            ValuePossibilities.ONE_VALUE, String.class ) );    
    }
    
    public CCBSacCommand() {};

	
	@Override
	public void execute() {

		if ( valid ) {
			
			CCBlockBuilder builder = new CCBlockBuilder( segmentLength, maxDuration );
			builder.build();
			
		}
		
	}

	
	@Override
	public void initialize(String[] tokens) {

		// defaults
		
		valid         = true;
		maxDuration   = 86400 * 10;
		segmentLength = -1.0;
		
		// parsing

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens( descriptors, tokens );
        
        if ( parsedTokens.containsKey( "MAX_DURATION" )  ||  parsedTokens.containsKey( "MAX" ) ) {
        	
            List<Object> parameters = parsedTokens.get( "MAX_DURATION" );
            if ( parameters == null ) {
                parameters = parsedTokens.get( "MAX" );
            } 
            if ( parameters == null ) {
                valid = false;
            } 
            else if ( parameters.isEmpty() ) {
                valid = false;
            }
            else {
                maxDuration = ( (Double) parameters.get(0) )*3600.0;
            }
  
        }
        
        if ( parsedTokens.containsKey( "BLOCK" )  ||  parsedTokens.containsKey( "B" ) ) {
        	
            List<Object> parameters = parsedTokens.get( "BLOCK" );
            if ( parameters == null ) {
                parameters = parsedTokens.get( "B" );
            } 
            if ( parameters == null ) {
                valid = false;
            } 
            else if ( parameters.isEmpty() ) {
                valid = false;
            }
            else {

            	switch ( ( (String) parameters.get(0) ).toUpperCase() ) {
            	
            	case "MINUTE": case "MIN":
            		segmentLength = 60.0;
            		break;
            		
            	case "QUARTER":
            		segmentLength = 900.0;
            		break;
            		
            	case "HOUR":
            		segmentLength = 3600.0;
            		break;
            		
            	case "HALFDAY":
            		segmentLength = 43200.0;
            		break;
      
            	case "DAY":
            		segmentLength = 86400.0;
            		break;
            		
            	default:
            		segmentLength = -1.0;
            		break;
            		
            	}
            	
            }
  
        }
    	
	}
	
	
    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "CCB" };
        return new ArrayList<>(Arrays.asList(names));
    }

}
