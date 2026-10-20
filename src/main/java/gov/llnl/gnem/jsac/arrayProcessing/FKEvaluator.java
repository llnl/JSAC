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

import gov.llnl.gnem.jsac.commands.arrayProcessing.enums.FKType;

public class FKEvaluator {
	
	private final Block[]   S;
	private final int       N;
	private final float[]   xeast;
	private final float[]   xnorth;
	private final float     kmax;
	private final int       nk;
	private final FKType    type;
	
	
	public FKEvaluator( Block[] S, float[] xeast, float[] xnorth, float kmax, int nk, FKType type ) {
		
		this.S      = new Block[2];
		this.S[0]   = S[0].copy();
		this.S[1]   = S[1].copy();
		this.xeast  = xeast;
		this.xnorth = xnorth;
		this.kmax   = kmax;
		this.nk     = nk;
		this.type   = type;
		
		N = S[0].N;
		
		if ( xeast.length != N  ||  xnorth.length != N )
			throw new IllegalStateException( "Mismatch between spectral covariance matrix dimension and xeast or xnorth length" );
		
	}
	
	// S      Hermitian symmetric spectral covariance matrix
	// x      array of station local east coordinates (km)
	// y      array of station local north coordinates (km)
	// kmax   maximum wavenumber (cycle/km)
	// nk     number of wavenumber samples (one-sided)  kmax = dk * nk;  grid is 2*nk+1 x 2*nk+1
	
	
	public float[][] evaluate() {
		
		double dk = kmax/nk;            // wavenumber sampling increment
		                                // grid range -nk:nk in both directions
		
		// rearrange upper triangular part of S into real and imaginary vectors
		// steering vectors will be defined on the coarray
		
		double[][] Sr = S[0].B;
		double[][] Si = S[1].B;
		
		// Complex exponential factors
		
		double[] Er0  = new double[N];
		double[] Ei0  = new double[N];
		double[] Er   = new double[N];
		double[] Ei   = new double[N];
		double[] dErx = new double[N];
		double[] dEry = new double[N];
		double[] dEix = new double[N];
		double[] dEiy = new double[N];
		
		// initialize steering vector and coupled form oscillator increment vectors
		
		double kx = -dk*nk;
		double ky = -dk*nk;
		
		for ( int i = 0;  i < N;  i++ ) {
			double arg = 2.0*Math.PI*( kx*xeast[i] + ky*xnorth[i] );
			Er0[i]     = Math.cos( arg );
			Ei0[i]     = Math.sin( arg );
			arg        = 2.0*Math.PI*dk*xeast[i];
			dErx[i]    = Math.cos(arg);
			dEix[i]    = Math.sin(arg);
			arg        = 2.0*Math.PI*dk*xnorth[i];
			dEry[i]    = Math.cos(arg);
			dEiy[i]    = Math.sin(arg);
		}
		
		// Evaluate the FK spectrum from -kymax to kymax and from -kxmax to kxmax for each value of ky
		// starting in lower left hand corner (-kxmax, -kymax)
		// The array is dimensioned 2*nk+1 x 2*nk+1
		
		int kdim = 2*nk+1;
		float[][] FKS = new float[kdim][kdim];
		
		double constant = 0.0;
		for ( int m = 0;  m < N;  m++ ) constant += Sr[m][m];
		
		for ( int iy = -nk;  iy <= nk;  iy++ ) {
			
			for ( int m = 0;  m < N;  m++ ) {
				Er[m] = Er0[m];
				Ei[m] = Ei0[m];
			}
			
			for ( int ix = -nk;  ix <= nk;  ix++ ) {
				
				double tmpr = 0.0;
				double tmpi = 0.0;
				for ( int m = 0;  m < N;  m++ ) {
					for ( int n = m+1;  n < N;  n++ ) {
						tmpr += Sr[m][n] * ( Er[m]*Er[n] + Ei[m]*Ei[n] );
						tmpi += Si[m][n] * ( Er[m]*Ei[n] - Er[n]*Ei[m] );
					}
				}
				tmpr = 2.0*tmpr + constant;
				FKS[ ix+nk][ iy+nk] = (float) ( tmpr - 2.0*tmpi );
				FKS[-ix+nk][-iy+nk] = (float) ( tmpr + 2.0*tmpi );

				if ( ix == 0  &&  iy == 0 ) break;
				
				// coupled form oscillator update - x direction
				
				for ( int m = 0;  m < N;  m++ ) {
					double ctmp = Er[m];
					Er[m] = ctmp*dErx[m] - Ei[m]*dEix[m];
					Ei[m] = ctmp*dEix[m] + Ei[m]*dErx[m];
				}
				
			}

			// coupled form oscillator update - y direction
			
			for ( int m = 0;  m < N;  m++ ) {
				double ctmp = Er0[m];
				Er0[m] = ctmp*dEry[m] - Ei0[m]*dEiy[m];
				Ei0[m] = ctmp*dEiy[m] + Ei0[m]*dEry[m];
			}
			
		}
		
		if ( type == FKType.MLM ) {
			for ( int ix = 0;  ix < 2*nk+1;  ix++ ) {
				for ( int iy = 0;  iy < 2*nk+1;  iy++ ) FKS[ix][iy] = 1.0f/FKS[ix][iy];
			}
		}
		
		return FKS;
	}
	
	
	public float[][] directEvaluate() {
		
		float[][] FKS = new float[ 2*nk+1 ][ 2*nk+1 ];
		
		double dk = kmax/nk;            // wavenumber sampling increment
		                                // grid range -nk:nk in both directions
		
		double[] Er  = new double[N];
		double[] Ei  = new double[N];
		double[] SEr = new double[N];
		double[] SEi = new double[N];
		double[][] Sr = S[0].B;
		double[][] Si = S[1].B;
		
		for ( int iy = -nk;  iy <= nk;  iy++ ) {
			
			double ky = iy*dk;
			
			for ( int ix = -nk;  ix <= nk;  ix++ ) {
				
				double kx = ix*dk;
				for ( int i = 0;  i < N;  i++ ) {
					double arg = 2.0*Math.PI*(kx*xeast[i] + ky*xnorth[i] );
					Er[i] = Math.cos( arg );
					Ei[i] = Math.sin( arg );
				}
				
				for ( int m = 0;  m < N;  m++ ) {
					
					double tmpr = 0.0;
					double tmpi = 0.0;
					for ( int n = 0;  n < N;  n++ ) {
						tmpr += Sr[m][n]*Er[n] - Si[m][n]*Ei[n];
						tmpi += Si[m][n]*Er[n] + Sr[m][n]*Ei[n];
					}
					
					SEr[m] = tmpr;
					SEi[m] = tmpi;
				}
				
				double tmp = 0.0;
				for ( int m = 0;  m < N;  m++ ) {
					tmp += Er[m]*SEr[m] + Ei[m]*SEi[m];
				}
				
				if ( type == FKType.MLM )
					FKS[ix+nk][iy+nk] = (float) (1.0/tmp);
				else
					FKS[ix+nk][iy+nk] = (float) tmp;
			}
		}
		
		return FKS;
	}
	
}

