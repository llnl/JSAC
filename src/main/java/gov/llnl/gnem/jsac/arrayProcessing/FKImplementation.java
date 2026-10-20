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
package gov.llnl.gnem.jsac.arrayProcessing;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gov.llnl.gnem.jsac.commands.arrayProcessing.enums.FKType;
import llnl.gnem.core.signalprocessing.arrayProcessing.FKResult;
import llnl.gnem.core.signalprocessing.arrayProcessing.SlownessValue;

public class FKImplementation {
	
	private float[][]     waveforms;
	private double        dt;
	private float[]       xeast;
	private float[]       xnorth;
	private double        eps;
	private int           M;
	
	private SlownessValue slownessValue;
	private double        maxPower;
	private double        quality;
	private FKType        type;
	

	
	private BlockToeplitz T;
	
	public FKImplementation( float[][] waveforms, double dt, float[] xeast, float[] xnorth, double eps, int M, FKType type ) {
		
		this.waveforms = waveforms;
		this.dt        = dt;
		this.xeast     = xeast;
		this.xnorth    = xnorth;
		this.eps       = eps;
		this.M         = M;
		this.type      = type;
		
		T = new BlockToeplitz( waveforms, eps, M );
		if ( type == FKType.MLM ) T.LevinsonRecursion();

	}
	
	
	public FKResult evaluateBroadband( float[] band, int nbands, float smax, int nk ) {
		
		float f0 = band[0];
		float df = (band[1] - band[0])/nbands;
		
		ArrayList< FKEvaluator > evaluators = new ArrayList<>();

		for ( int ib = 0;  ib < nbands;  ib++ ) {
			
			float f = f0 + ib*df;
			double Omega = 2.0*Math.PI*f*dt;
			Block[] S = T.SpectralCovarianceMatrix( Omega, type );
			
			float kmax = smax*f;
			
			evaluators.add( new FKEvaluator( S, xeast, xnorth, kmax, nk, type ) );
		}
			
		List< float[][] > results = new ArrayList<>( evaluators.parallelStream().map( FKEvaluator::evaluate ).collect( Collectors.toList() ) );
			
		
		int N = 2*nk + 1;
		float[][] fks = new float[N][N];
			
		for( float[][] result : results ) {
			for ( int i = 0;  i < N;  i++ ) {
				for ( int j = 0;  j < N;  j++ ) fks[i][j] += result[i][j];
			}
		}
		
		boolean valid = makeMeasurements( fks, smax, nk );
		
		FKResult retval = null;
		
		float[] axisSampling = new float[N];
		float ds = smax / nk;
		int p = 0;
		for ( int i = -nk;  i <= nk;  i++ ) axisSampling[p++] = i*ds;
	
		float[][] fks_plot = new float[N][N];
		for ( int i = 0;  i < N;  i++ ) {
			for ( int j = 0;  j < N;  j++ ) fks_plot[i][j] = fks[N-1-j][N-1-i];
		}
			
		
		if ( valid )
			retval = new FKResult( axisSampling, axisSampling, fks_plot, slownessValue, maxPower, quality );
		else
			retval = new FKResult();
		
		return retval;
	}
	
	
	public FKResult evaluateNarrowband( float f, float smax, int nk ) {
		
		double Omega = 2.0*Math.PI*f*dt;
		Block[] S = T.SpectralCovarianceMatrix( Omega, type );
		
		float kmax = smax*f;
		
		int N = 2*nk + 1;
		float[][] fks = new float[N][N];
		
		fks = ( new FKEvaluator( S, xeast, xnorth, kmax, nk, type ) ).evaluate();
		
		boolean valid = makeMeasurements( fks, smax, nk );
		
		FKResult retval = null;
		
		float[] axisSampling = new float[N];
		float ds = smax / nk;
		int p = 0;
		for ( int i = -nk;  i <= nk;  i++ ) axisSampling[p++] = i*ds;
		
		float[][] fks_plot = new float[N][N];
		for ( int i = 0;  i < N;  i++ ) {
			for ( int j = 0;  j < N;  j++ ) fks_plot[i][j] = fks[N-1-j][N-1-i];
		}
		
		if ( valid )
			retval = new FKResult( axisSampling, axisSampling, fks_plot, slownessValue, maxPower, quality );
		else
			retval = new FKResult();
		
		return retval;
	}
	
	
	public boolean makeMeasurements( float[][] fks, float smax, int ns ) {
		
		boolean valid = true;
		
        float fkmax = 0.0f;
        float fkavg = 0.0f;
        int ixmax = -1;
        int iymax = -1;
        for (int ix = -ns; ix <= ns; ix++) {
            for (int iy = -ns; iy <= ns; iy++) {
                if (fks[ix+ns][iy+ns] > fkmax) {
                    fkmax = fks[ix+ns][iy+ns];
                    ixmax = ix;
                    iymax = iy;
                }
                fkavg += fks[ix+ns][iy+ns];
            }
        }
        fkavg /= ( (2*ns+1)*(2*ns+1) );
        if (ixmax < -ns || ixmax > ns || iymax < -ns || iymax > ns) { // something is wrong, so bail out.
            valid = false;
        }

        // quadratic refinement
        // Q = Ax^2 + 2Bxy + Cy^2 + Dx + Ey + F
        
        double[][] U  = { { 1,  2,  1, -1, -1,  1 }, 
        		          { 0,  0,  1,  0, -1,  1 }, 
        		          { 1, -2,  1,  1, -1,  1 }, 
        		          { 1,  0,  0, -1,  0,  1 }, 
        		          { 0,  0,  0,  0,  0,  1 }, 
        		          { 1,  0,  0,  1,  0,  1 }, 
        		          { 1, -2,  1, -1,  1,  1 },
        		          { 0,  0,  1,  0,  1,  1 }, 
        		          { 1,  2,  1,  1,  1,  1 }    };
        
//        double[][] UTUinv = { { 72.0,   0.0,   0.0,   0.0,   0.0, -48.0 },  
//        					  {  0.0,   9.0,   0.0,   0.0,   0.0,   0.0 },
//        					  {  0.0,   0.0,  72.0,   0.0,   0.0, -48.0 },
//        					  {  0.0,   0.0,   0.0,  24.0,   0.0,   0.0 },      / 144
//        					  {  0.0,   0.0,   0.0,   0.0,  24.0,   0.0 },
//        					  {-48.0,   0.0, -48.0,   0.0,   0.0,  80.0 } };
        
        double[] f = { fks[ixmax - 1 + ns][iymax - 1 + ns],
                	   fks[ixmax     + ns][iymax - 1 + ns],
                	   fks[ixmax + 1 + ns][iymax - 1 + ns],
                	   fks[ixmax - 1 + ns][iymax     + ns],
                	   fks[ixmax     + ns][iymax     + ns],
                	   fks[ixmax + 1 + ns][iymax     + ns],
                	   fks[ixmax - 1 + ns][iymax + 1 + ns],
                	   fks[ixmax     + ns][iymax + 1 + ns],
                	   fks[ixmax + 1 + ns][iymax + 1 + ns] };
        
        double[] UTf = new double[6];
        
        for ( int n = 0;  n < 6; n++ ) {
        	double tmp = 0.0;
        	for ( int m = 0;  m < 9;  m++ ) {
        		tmp += U[m][n]*f[m];
        	}
        	UTf[n] = tmp;
        }
        
        double A = ( 72.0*UTf[0] - 48.0*UTf[5] ) / 144.0;
        double B =    9.0*UTf[1] / 144.0;
        double C = ( 72.0*UTf[2] - 48.0*UTf[5] ) / 144.0;
        double D =   24.0*UTf[3] / 144.0;
        double E =   24.0*UTf[4] / 144.0;
        double F = (-48.0*UTf[0] - 48.0*UTf[2] + 80.0*UTf[5] ) / 144.0;
        
        // dQdx = 2Ax + 2By + D
        // dQdy = 2Bx + 2Cy + E
        
        double det = A*C - B*B;
        double dx = (C*D - B*E)/det*(-0.5);
        double dy = (-B*D + A*E)/det*(-0.5);
        
        maxPower = A*dx*dx + B*dx*dy + C*dy*dy + D*dx + E*dy + F;
        
        double ds = smax/ns;
        
        double seast  = -(ixmax + dx)*ds;
        double snorth = -(iymax + dy)*ds;
        
        slownessValue = new SlownessValue( snorth, seast );

		quality = maxPower / fkavg;
		
        return valid;
	}

}
