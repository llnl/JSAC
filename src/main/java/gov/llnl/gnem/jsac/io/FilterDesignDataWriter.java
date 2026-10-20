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
package gov.llnl.gnem.jsac.io;

import java.io.IOException;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.FilterDesignData;
import gov.llnl.gnem.jsac.io.enums.FileType;

public class FilterDesignDataWriter {
	
	private FilterDesignData fdd;
	
	public FilterDesignDataWriter( FilterDesignData fdd ) {
		this.fdd = fdd;
	}
	
	public void write( String path ) {
		
		try {
			
			// group delay
			
			SACFileWriter writer = SACFileWriter.fromStringPath( path + ".gd" );
			SACHeader header = writer.getHeader();
			loadHeader( header );
			header.setIftype( FileType.IGRP );
			header.setLeven( 1 );
			header.setDelta( fdd.getDeltaF() );
			float[] gd = fdd.getDigitalGroupDelay();
			header.setB( 0.0 );
			header.setE( (gd.length-1)*fdd.getDeltaF() );
			writer.write( gd, FileType.IGRP );
			double[] stats = getStats( gd );
			header.setDepmin( stats[0] );
			header.setDepmax( stats[1] );
			header.setDepmen( stats[2] );
			writer.close();
			
			// impulse response
			
			writer = SACFileWriter.fromStringPath( path + ".imp" );
			header = writer.getHeader();
			loadHeader( header );
			header.setIftype( FileType.ITIME );
			header.setLeven( 1 );
			header.setDelta( fdd.getDeltaT() );
			float[] imp = fdd.getDigitalImpulseResponse();
			header.setB( 0.0 );
			header.setE( (imp.length-1)*fdd.getDeltaT() );
			header.setNpts(0);
			writer.write( imp, FileType.ITIME );
			stats = getStats( imp );
			header.setDepmin( stats[0] );
			header.setDepmax( stats[1] );
			header.setDepmen( stats[2] );
			writer.close();
			
			// amplitude and phase
			
			writer = SACFileWriter.fromStringPath( path + ".spec" );
			header = writer.getHeader();
			loadHeader( header );
			header.setIftype( FileType.IAMPH );
			header.setLeven( 1 );
			header.setDelta( fdd.getDeltaF() );
			float[] xr = fdd.getDigitalSpectrumRealPart();
			float[] xi = fdd.getDigitalSpectrumImagPart();
			header.setB( 0.0 );
			header.setE( (xr.length-1)*fdd.getDeltaF() );
			float[] amp = new float[ xr.length ];
			float[] phs = new float[ xr.length ];
			for ( int i = 0;  i < xr.length;  i++ ) {
				amp[i] = (float) Math.sqrt( xr[i]*xr[i] + xi[i]*xi[i] );
				phs[i] = (float) Math.atan2( xi[i], xr[i] );
			}
			writer.write( amp, phs, FileType.IAMPH );
			header.setDepmin( null );
			header.setDepmax( null );
			header.setDepmen( null );
			writer.close();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	
	private void loadHeader( SACHeader header ) {
		
		Double[] user  = new Double[10];
		
		switch ( fdd.getPassbandType() ) {
		case LOWPASS:
			user[0] = 1.0;
			header.setKuser0( "lowpass " );
			break;
		case HIGHPASS:
			user[0] = 2.0;
			header.setKuser0( "highpass" );
			break;
		case BANDPASS:
			user[0] = 3.0;
			header.setKuser0( "bandpass" );
			break;
		case BANDREJECT:
			user[0] = 4.0;
			header.setKuser0( "bandrej " );
			break;
		}
		
		switch( fdd.getFilterDesign() ) {
		case Butterworth:
			user[1] = 1.0;
			header.setKuser1( "Butter  " );
			break;
		case Chebyshev1:
			user[1] = 3.0;
			header.setKuser1( "C1      " );
			break;
		case Chebyshev2:
			user[1] = 4.0;
			header.setKuser1( "C2      " );
			break;
		}
		
		user[2] = (double) fdd.getNPoles();
		user[3] = (double) fdd.getNPasses();
		user[4] = fdd.getTransitionBandwidth();
		user[5] = fdd.getAttenuation();
		user[6] = fdd.getDeltaT();
		user[7] = fdd.getCutoffs()[0];
		user[8] = fdd.getCutoffs()[1];
		user[9] = null;
		
		header.setUser( user );
		header.setKuser2( null );
		
	}
	
	
	
	private double[] getStats( float[] x ) {
		
		double[] retval = { Double.MAX_VALUE, Double.MIN_VALUE, 0.0 };
		
		for ( int i = 0;  i < x.length;  i++ ) {
			retval[0]  = Math.min( retval[0], x[i] );
			retval[1]  = Math.max( retval[1], x[i] );
			retval[2] += x[i];
		}
		retval[2] /= x.length;
		
		return retval;
	}
	
}
