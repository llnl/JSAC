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

import com.oregondsp.signalProcessing.filter.fir.equiripple.EquirippleLowpass;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACHeader;

public class DecimateSacCommand implements SacCommand {
	
    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add( new AttributeDescriptor( "FILTER", ValuePossibilities.ONE_VALUE, String.class ) );
    }
	
	
	private int     decrate;
	private boolean antiAlias;
	private boolean valid;
	private float[] coefficients;
	
	
	public DecimateSacCommand() {}

	
	@Override
	public void execute() {

		if ( valid ) {
			
			if ( antiAlias ) {
			
				// design filter;  can be applied to files with any sampling interval
			
				double Omega_s = 1.0/decrate;
				double Omega_p = 0.8/decrate;
				double Ws      = 2.0;
				double Wp      = 1.0;
				int    N       = 15*decrate;
			
				coefficients = ( new EquirippleLowpass( N, Omega_p, Wp, Omega_s, Ws ) ).getCoefficients();
			
				FIRFilter implementation = new FIRFilter( coefficients, false );
				implementation.apply();
				
			}

			List<SacTraceData> traceData = SacDataModel.getInstance().getData();
			
			for ( SacTraceData trace : traceData ) {
				float[] waveform  = trace.getData();
				int     npts      = waveform.length;
				int     n         = npts / decrate;
				float[] decimated = new float[ n ];
				for ( int i = 0;  i < n;  i++ ) {
					decimated[i] = waveform[i*decrate ];
				}
				trace.setData( decimated );
				SACHeader header = trace.getSacFileHeader();
				header.setDelta( header.getDelta()*decrate );
			}
			
		}
		
	}

	
	@Override
	public void initialize( String[] tokens ) {
		
		// defaults
		
		decrate   = 2;
		antiAlias = true;
		
		valid     = true;

		// parsing
		
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens( descriptors, tokens );
        
        if ( !parsedTokens.isEmpty() ) {
        
        	List< Object > ttmp = parsedTokens.remove( TokenListParser.LEFT_OVER_TOKENS );
        	if ( !(ttmp == null) ) {
        		
        		if ( !(ttmp.isEmpty() ) ) {
        			
        			if ( ttmp.size() > 1 ) {
        				throw new IllegalStateException( "Ambiguous decimation rate" );
        			}
        			else {
        				decrate = Integer.parseInt( (String) ttmp.get(0) );
        			}
        			
        		}
        		
        	}
            
            if ( parsedTokens.containsKey( "FILTER" ) ) {
            	
                List<Object> parameters = parsedTokens.get( "FILTER" );
                if ( parameters == null ) {
                    valid = false;
                } 
                else if ( parameters.isEmpty() ) {
                    valid = false;
                }
                else if ( parameters.size() == 1 ){
                	if ( ( (String) parameters.get(0) ).equals( "OFF" ) ) {
                		antiAlias = false;
                	}
                	else if ( ( (String) parameters.get(0) ).equals( "ON" ) ) {
                		antiAlias = true;
                	}
                }
                else {
                	throw new IllegalStateException( "Incorrect number of arguments for FILTER." );
                }
                
            }
        	
        }
        
	}

	
    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "DECIMATE", "DEC" };
        return new ArrayList<>(Arrays.asList(names));
    }
    
}
