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
package gov.llnl.gnem.jsac.commands.unary;

import com.oregondsp.signalProcessing.filter.fir.OverlapAdd;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACHeader;

public class Differentiator {
	
	private static final int    BLOCK_SIZE = 1500;
	private static final int    ORDER      = 40;
	
	private DifferentiatorType     type;
	
	private OverlapAdd             F     = null;
	
	private static final float[] impulseResponse = { -1.8179417E-6f,  6.183982E-6f,  -1.3746321E-5f,  2.7179718E-5f, -4.9315393E-5f, 
										        8.4131956E-5f, -1.3649464E-4f,  2.1313131E-4f, -3.222823E-4f,   4.7393143E-4f,
									           -6.803423E-4f,   9.5618516E-4f, -0.0013188422f,  0.0017885044f, -0.0023887008f,
									            0.0031460598f, -0.0040911324f,  0.005258199f,  -0.0066858605f, 	0.008417521f,
									           -0.010501634f,   0.012992911f,  -0.015953206f,   0.019453622f,  -0.023576543f,
									            0.02841971f,   -0.034101978f,   0.040771425f,  -0.04861851f,	0.05789651f,
									           -0.06895466f,    0.08229303f,   -0.09865981f,    0.11923167f,   -0.14597443f,
									            0.18243706f,   -0.23573616f,    0.3225088f,    -0.49272263f, 	0.9963428f,
									            0.0f,
									           -0.9963428f,     0.49272263f,   -0.3225088f, 	0.23573616f,   -0.18243706f,
									            0.1459744f,    -0.11923167f,    0.09865983f,   -0.08229302f, 	0.06895472f,
									           -0.05789654f,    0.048618443f,  -0.04077144f,    0.034102015f,  -0.02841968f,
									            0.02357649f,   -0.019453645f,   0.015953254f,  -0.012992889f, 	0.010501631f,
									           -0.008417521f,   0.0066858456f, -0.005258199f,   0.004091125f,  -0.00314603f,
									            0.002388738f,  -0.0017885417f,  0.0013188049f, -9.562373E-4f,   6.803572E-4f,
									           -4.7387183E-4f,  3.222823E-4f,  -2.1319091E-4f,  1.3650954E-4f, -8.40798E-5f,
									            4.9322844E-5f, -2.7157366E-5f,  1.3709068E-5f, -6.198883E-6f,   1.8328428E-6f };

	
	
	public Differentiator( DifferentiatorType type ) {
		
		this.type   = type;

		if ( type == DifferentiatorType.EQUIRIPPLE ) {
			F = new OverlapAdd( impulseResponse, BLOCK_SIZE );
		}
		
	}
	
	
	public void differentiate( SacTraceData std ) {
		
		switch (type) {
		case TWO_POINT_OPERATOR:
			differentiate_2( std );
			break;
		case THREE_POINT_OPERATOR:
			differentiate_3( std );
			break;
		case FIVE_POINT_OPERATOR:
			differentiate_5( std );
			break;
		case EQUIRIPPLE:
			differentiate_eq( std );
			break;
		default:
			differentiate_2( std );
			break;
		}
		
	}
	
	
	public void differentiate_2( SacTraceData std ) {
		
		float[] x = std.getData();
		float  dt = (float) std.getDelta();
		
		float[] y = new float[ x.length - 1 ];
		
		for ( int i = 0;  i < y.length;  i++ ) {
			y[i] = (x[i+1] - x[i]) / dt;
		}
		
		std.setData( y );
		
		SACHeader header = std.getSacFileHeader();
		header.setB( header.getB() + header.getDelta() / 2.0 );
		header.setE( header.getB() + (header.getNpts()-1) * header.getDelta() );
		
	}
	
	
	public void differentiate_3( SacTraceData std ) {
		
		float[] x = std.getData();
		float  dt = (float) std.getDelta();
		
		float[] y = new float[ x.length - 2 ];
		
		for ( int i = 0;  i < y.length;  i++ ) {
			y[i] = (x[i+2] - x[i])/(2*dt);
		}
		
		std.setData( y );
		
		SACHeader header = std.getSacFileHeader();
		header.setB( header.getB() + header.getDelta() );
		header.setE( header.getB() + (header.getNpts()-1) * header.getDelta() );

	}
	
	
	public void differentiate_5( SacTraceData std ) {
		
		float[] x = std.getData();
		float  dt = (float) std.getDelta();
		
		float[] y = new float[ x.length - 2 ];
		
		int n = y.length;
		
		// first point:  centered difference
		
		y[0] = x[2] - x[0];
		
		// last point:  centered difference
		
		y[n-1] = x[n+1] - x[n-1];
		
		// other points
		
		for ( int i = 1;  i < n-1;  i++ ) {
			int j = i+1;
			y[i] = ( 2.0f/3.0f * (x[j+1] - x[j-1])  -  1.0f/12.0f * (x[j+2] - x[j-2]) ) / dt;
		}
		
		std.setData( y );
		
		SACHeader header = std.getSacFileHeader();
		header.setB( header.getB() + header.getDelta() );
		header.setE( header.getB() + (header.getNpts()-1) * header.getDelta() );

	}
	
	
	public void differentiate_eq( SacTraceData std ) {
		
		float[] x = std.getData();
		float  dt = (float) std.getDelta();

		int N = x.length;
		int M = N + 2*ORDER;
		
		int nblocks = 1;
		while ( nblocks * BLOCK_SIZE < M ) {
			nblocks++;
		}
		
		float[] tmp = new float[ nblocks*BLOCK_SIZE ];
		System.arraycopy( x,  0,  tmp,  0, N );

		int ptr = 0;
		
		for ( int i = 0;  i < nblocks;  i++ ) {
			F.filter( tmp, ptr, tmp, ptr );
			ptr += BLOCK_SIZE;
		}
		
		for ( int i = 0;  i < tmp.length;  i++ ) tmp[i] /= dt;
		
		System.arraycopy( tmp, ORDER, x, 0, N );
		
		std.setData( x );
		std.resetStatistics();
		
	}

}
