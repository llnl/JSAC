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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.arrayProcessing.Aligner;
import gov.llnl.gnem.jsac.arrayProcessing.ArrayData;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;

public class AlignSacCommand implements SacCommand {
	
    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add( new AttributeDescriptor( "BEARING",   ValuePossibilities.ONE_VALUE,   Double.class ) );
        descriptors.add( new AttributeDescriptor( "B",         ValuePossibilities.ONE_VALUE,   Double.class ) );
        descriptors.add( new AttributeDescriptor( "VELOCITY",  ValuePossibilities.ONE_VALUE,   Double.class ) );
        descriptors.add( new AttributeDescriptor( "V",         ValuePossibilities.ONE_VALUE,   Double.class ) );
        descriptors.add( new AttributeDescriptor( "EC",        ValuePossibilities.TWO_VALUES,  Double.class ) );
        descriptors.add( new AttributeDescriptor( "REFERENCE", ValuePossibilities.TWO_OR_MORE, Double.class ) );
        descriptors.add( new AttributeDescriptor( "REF",       ValuePossibilities.TWO_OR_MORE, Double.class ) );
        descriptors.add( new AttributeDescriptor( "REFSTA",    ValuePossibilities.ONE_VALUE,   String.class ) );
    }
    
    private boolean valid;
    private double  b;
    private double  v;
    private double  anginc;
    private double  survel;
    private double  reflat;
    private double  reflon;
    private double  refelev;
    private boolean useReferenceLocation;
    private boolean useReferenceStation;
    private boolean useElevationCorrections;
    private String  refStation;

    
    
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
			}
			Aligner.alignWaveformsInMemory( adata, b, v, useElevationCorrections, anginc, survel, reference );
			
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
		useReferenceLocation    =   false;
		useReferenceStation     =   false;
		useElevationCorrections =   false;
		refStation              =    null;
		
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
        
	}
	
	
	   @Override
	    public Collection<String> getCommandNames() {
	        String[] names = { "ALIGN" };
	        return new ArrayList<>( Arrays.asList(names) );
	    }

	   
	    @Override
	    public String getHelpString() {
	    	
	        return "SUMMARY\n"
	                + " Computes a beam using all data files in SAC memory.\n"
	                + "\n"
	                + "SYNTAX\n"
	                + "ALIGN {[B]EARING  b}\n"
	                + "      {[V]ELOCITY v}\n"
	                + "      {[REF]ERENCE lat lon {el}}\n"
	                + "      {EC         anginc survel}\n\n"
	                + "INPUT\n"
	                + " BEARING  b:               Set bearing to b, in degrees east from north.\n"
	                + " VELOCITY v:	              Set velocity to v, in kilometers/second.\n"
	                + " [REF]ERENCE lat lon {el}  Specifies the latitude, longitude and, optionally, the elevation of\n"
	                + "                             the beam reference point.\n"
	                + "  lat:                     Latitude of the beam reference point.\n"
	                + "  lon:                     Corresponding longitude.\n"
	                + "  el:                      Optional beam reference elevation in meters (positive is up).\n"
	                + " REFSTA station            Specifies a station's location as the beam reference point.\n"
	                + "  station:                 Name of the station.\n"
	                + " EC anginc survel:         Elevation correction:\n"
	                + "  anginc:                  Angle of incidence in degrees from the z axis.  Vertical incidence is 0.0.\n"
	                + "  survel:                  Surface medium velocity in kilometers/second.\n"
	                + "DEFAULT VALUES\n"
	                + " ALIGN B 0.0 V 99999.0 EC 33.0 6.0\n\n"
	                + "DESCRIPTION\n"
	                + " This command aligns the waveforms read into memory to a specified bearing and velocity.  The default behavior\n"
	                + " uses the centroid of the stations with data in memory as the reference point for alignment.  This behavior may be\n"
	                + " overridden in one of two ways:  by using the REFERENCE option to specify an alternative latitude, longitude and,\n"
	                + " optionally, elevation as the reference point, or by using the REFSTA option to specify the location of one station with\n"
	                + " data in memory as the reference point.  The following header values must be populated for all files in memmory: \n"
	                + "   stla\n"
	                + "   stlo\n"
	                + "   stel\n"
	                + "   kstnm\n"
	                + "   kcmpnm\n"
	                + " The default alignment uses an infinite velocity.\n"
	                + "EXAMPLES\n"
	                + " Specifying alignment to a Pn phase velocity (8.3) and 130 degrees backazimuth:\n"
	                + "\n"
	                + "     SAC> READ ARAO ARC1 ARC2 ARC3 ARC4 ARC5 ARC6 ARC7\n"
	                + "     SAC> READ MORE ARD1 ARD2 ARD3 ARD4 ARD5 ARD6 ARD7 ARD8 ARD9 \n"
	                + "     SAC> ALIGN B 130.0 V 8.3\n"
	                + "\n"
	                + " Aligning to an Sn type phase velocity at 130 degrees, referenced to the center element of the array:\n"
	                + "\n"
	                + "     SAC> ALIGN B 130.0 V 4.5 REFSTA ARA0\n"
	                + "\n"
	                + " This command is useful for visual assessment of phase coherence, for checking backazimuth and velocity of a \n"
	                + "   particular arrival and for checking phase moveout across an array.\n"
	                + "HEADER CHANGES\n"
	                + " none\n";

	    }


}
