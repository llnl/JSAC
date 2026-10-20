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
package gov.llnl.gnem.jsac.commands.arrayProcessing;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.arrayProcessing.ArrayData;
import gov.llnl.gnem.jsac.arrayProcessing.Beamformer;
import gov.llnl.gnem.jsac.arrayProcessing.IterativeAdaptiveBeamformer;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.io.SACFileWriter;
import gov.llnl.gnem.jsac.io.SACHeader;
import gov.llnl.gnem.jsac.io.enums.FileType;
import gov.llnl.gnem.jsac.util.PathManager;

public class BeamSacCommand implements SacCommand {
	
    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add( new AttributeDescriptor( "BEARING",   ValuePossibilities.ONE_VALUE,    Double.class ) );
        descriptors.add( new AttributeDescriptor( "B",         ValuePossibilities.ONE_VALUE,    Double.class ) );
        descriptors.add( new AttributeDescriptor( "VELOCITY",  ValuePossibilities.ONE_VALUE,    Double.class ) );
        descriptors.add( new AttributeDescriptor( "V",         ValuePossibilities.ONE_VALUE,    Double.class ) );
        descriptors.add( new AttributeDescriptor( "EC",        ValuePossibilities.TWO_VALUES,   Double.class ) );
        descriptors.add( new AttributeDescriptor( "WRITE",     ValuePossibilities.ONE_VALUE,    String.class ) );
        descriptors.add( new AttributeDescriptor( "W",         ValuePossibilities.ONE_VALUE,    String.class ) );
        descriptors.add( new AttributeDescriptor( "REFERENCE", ValuePossibilities.TWO_OR_MORE,  Double.class ) );
        descriptors.add( new AttributeDescriptor( "REF",       ValuePossibilities.TWO_OR_MORE,  Double.class ) );
        descriptors.add( new AttributeDescriptor( "REFSTA",    ValuePossibilities.ONE_VALUE,    String.class ) );
        descriptors.add( new AttributeDescriptor( "ADAPTIVE",  ValuePossibilities.ZERO_OR_MORE, Integer.class ) );
        
        
    }

    private boolean valid;
    private double  b;
    private double  v;
    private double  anginc;
    private double  survel;
    private String  filename;
    private double  reflat;
    private double  reflon;
    private double  refelev;
    private boolean useReferenceLocation;
    private boolean useReferenceStation;
    private boolean useElevationCorrections;
    private String  refStation;
    private boolean adaptive;
    private int     M;
    private int     niter;
    
    
    public BeamSacCommand() {
    	
    }
    
    
	@Override
	public void execute() {

		if ( valid ) {
			
			ArrayData adata = new ArrayData( SacDataModel.getInstance().getData(), "CurrentArrayData" );
			
			double[] reference = new double[3];
			if ( useReferenceLocation ) {
				reference = adata.getLocalCoordinatesOf( reflat, reflon, refelev );
			}
			else if ( useReferenceStation ) {
				reference = adata.getLocalCoordinatesOf( refStation );
				double[] staCoords = adata.getGlobalCoordinatesOf( refStation );
				reflat  = staCoords[1];
				reflon  = staCoords[0];
				refelev = staCoords[2];
			}
			else {
				double[] centroidCoords = adata.getCentroidCoordinates();
				reflat  = centroidCoords[1];
				reflon  = centroidCoords[0];
				refelev = centroidCoords[2];
			}
			
			ArrayList< float[] > beams = new ArrayList<>();
			
			if ( adaptive ) {
				IterativeAdaptiveBeamformer IAB = new IterativeAdaptiveBeamformer( adata, b, v, useElevationCorrections, anginc, survel, reference, M );
				IAB.iterate( niter );
				beams = IAB.getBeams();
			}
			else {
				beams.add( Beamformer.computeBeam( adata, b, v, useElevationCorrections, anginc, survel, reference ) );
			}
			
			
			try {
				
	            String dir = PathManager.getInstance().getCurrentDir().toString();
	            
	            String suffix = "";
	            DecimalFormat F = new DecimalFormat( "00" );
	            
	            for ( int ib = 0;  ib < beams.size();  ib++ ) {
	            	
	            	float[] beam = beams.get( ib );
	            	
	            	if ( adaptive ) suffix = "_" + F.format(ib);
	            
	            	SACFileWriter writer = SACFileWriter.fromStringPath( dir + "/" + filename + suffix );
	            	SACHeader     header =  writer.getHeader();
	            	header.setNpts(0);
	            	header.setDelta( adata.getSamplingInterval() );
	            	header.setStla( reflat );                 // use station coordinates for beam reference
	            	header.setStlo( reflon );
	            	header.setStel( refelev );
	            	header.setIftype( FileType.ITIME );
	            	header.setLeven( 1 );
	            	header.setReferenceTime( adata.getStartTime() );
	            	header.setB( 0.0 );
	            	header.setE( (beam.length - 1)*adata.getSamplingInterval() );
	            	header.setKstnm( "beam" + suffix );
	            	Double[] userVariables = new Double[10];
	            	userVariables[0] = b;
	            	userVariables[1] = v;
	            	if ( useElevationCorrections ) {
	            		userVariables[2] = anginc;
	            		userVariables[3] = survel;
	            	}
	            	header.setUser( userVariables );
	            	writer.write( beam );
	            	writer.close();
	            }
				
			} catch ( IOException ioe ) {
				
			}

		}
		
	}

	
	@Override
	public void initialize( String[] tokens ) {
		
		// defaults
		
		valid                   =    true;
		b                       =     0.0;
		v                       = 99999.0;
		anginc                  =    33.0;
		survel                  =     6.0;
		filename                =  "beam";
		useReferenceLocation    =   false;
		useReferenceStation     =   false;
		useElevationCorrections =   false;
		refStation              =    null;
		adaptive                =   false;
		M                       =   50;
		niter                   =   M/10;
		
		// parse

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens( descriptors, tokens );
        if ( parsedTokens.isEmpty() ) {
            return;
        }
        
        if ( parsedTokens.containsKey( "BEARING" ) || parsedTokens.containsKey( "B" ) ) {
        	
            List<Object> parameters = parsedTokens.get( "BEARING" );
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
                b = (double) parameters.get(0);
            }
            
        }
		
        
        if ( parsedTokens.containsKey( "VELOCITY" ) || parsedTokens.containsKey( "V" ) ) {
        	
            List<Object> parameters = parsedTokens.get( "VELOCITY" );
            if ( parameters == null ) {
                parameters = parsedTokens.get( "V" );
            }
            if ( parameters == null ) {
                valid = false;
            } 
            else if ( parameters.isEmpty() ) {
                valid = false;
            }
            else {
                v = (double) parameters.get(0);
            }
            
        }
		
        
        if ( parsedTokens.containsKey( "EC" ) ) {
        	
            List<Object> parameters = parsedTokens.get( "EC" );
            if ( parameters == null ) {
                valid = false;
            } 
            else if ( parameters.isEmpty() ) {
                valid = false;
            }
            else {
            	useElevationCorrections = true;
                anginc = (double) parameters.get(0);
                survel = (double) parameters.get(1);
            }
            
        }
        
        
        if ( parsedTokens.containsKey( "WRITE" ) || parsedTokens.containsKey( "W" ) ) {
        	
            List<Object> parameters = parsedTokens.get( "WRITE" );
            if ( parameters == null ) {
                parameters = parsedTokens.get( "W" );
            }
            if ( parameters == null ) {
                valid = false;
            } 
            else if ( parameters.isEmpty() ) {
                valid = false;
            }
            else {
                filename = (String) parameters.get(0);
            }
            
        }
        
        
        if ( parsedTokens.containsKey( "REFERENCE" ) || parsedTokens.containsKey( "REF" ) ) {
        	
            List<Object> parameters = parsedTokens.get( "REFERENCE" );
            if ( parameters == null ) {
                parameters = parsedTokens.get( "REF" );
            }
            if ( parameters == null ) {
                valid = false;
            } 
            else if ( parameters.isEmpty() ) {
                valid = false;
            }
            else if ( parameters.size() < 2 ) {
            	valid = false;
            }
            else {
            	useReferenceLocation = true;
            	useReferenceStation  = false;
                reflat = (double) parameters.get(0);
                reflon = (double) parameters.get(1);
                if ( parameters.size() > 2 )
                	refelev = (double) parameters.get(2);
            }
            
        }
        
        
        if ( parsedTokens.containsKey( "REFSTA" ) ) {
        	
            List<Object> parameters = parsedTokens.get( "REFSTA" );
            if ( parameters == null ) {
                valid = false;
            } 
            else if ( parameters.isEmpty() ) {
                valid = false;
            }
            else {
                refStation = (String) parameters.get(0);
                useReferenceStation  = true;
                useReferenceLocation = false;
            }
            
        }
        
        
        if ( parsedTokens.containsKey( "ADAPTIVE" ) ) {
        	
            List<Object> parameters = parsedTokens.get( "ADAPTIVE" );
            adaptive = true;
            valid = true;
            if ( parameters == null ) {
            } 
            else if ( parameters.isEmpty() ) {
            }
            else {
                if ( parameters.size() == 1 ) {
                	M = (int) parameters.get(0);
                	niter = M/10;
                }
                else {
                	M     = (int) parameters.get(0);
                	niter = (int) parameters.get(1);
                }
            }
        	
        }
        
	}
	
	
	   @Override
	    public Collection<String> getCommandNames() {
	        String[] names = { "BEAM" };
	        return new ArrayList<>( Arrays.asList(names) );
	    }
}
