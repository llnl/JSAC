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

import llnl.gnem.core.signalprocessing.fft.OverlapAdd_dp;

public class IterativeAdaptiveBeamformer {

	private float[][] X;
	private int       M;
	private int       Nc;
	private int       K;
	private int       N;
	
	private ArrayList< double[][] > w;
	private double[][]              p;
	private double[][]              r;
	private ArrayList< float[] >    b;
	private float[]                 q;
	
	private float[][] Xc;
	
	private int             blockLength;
	private OverlapAdd_dp[] filters;
	
	public IterativeAdaptiveBeamformer( ArrayData data, double bearing, double velocity, boolean useElevationCorrections, 
			                            double anginc, double survel, double[] reference, int M ) {
		
		X = Aligner.align( data, bearing, velocity, useElevationCorrections, anginc, survel, reference );
		this.M = M;
		
		K = X.length;
		N = X[0].length;
		Nc = 2*M + 1;
		
		// initializations
		
		b = new ArrayList<>();
		
		Xc   = new float[K][N];
		float[] b0 = new float[N];
		for ( int i = 0;  i < N;  i++ ) {
			b0[i] = 0.0f;
			for ( int k = 0;  k < K;  k++ ) b0[i] += X[k][i];
			b0[i] /= K;
			for ( int k = 0;  k < K;  k++ ) Xc[k][i] = X[k][i] - b0[i];
		}
		b.add( b0 );
		
		double[][] wtmp = new double[K][Nc];
		for ( int k = 0;  k < K;  k++ ) wtmp[k][M] = 1.0/K;
		w = new ArrayList<>();
		w.add( wtmp );
		
		p = new double[K][Nc];
		r = new double[K][Nc];
		
		for ( int m = -M;  m <= M;  m++ ) {
			
			for ( int k = 0;  k < K;  k++ ) {
				double tmp = 0.0;
                for ( int n = Math.max(0,m);  n < Math.min(N,N+m);  n++ ) {
                	tmp += Xc[k][n-m]*b0[n];
                }
                p[k][m+M] = -tmp;
                r[k][m+M] = -tmp;
			}
			
		}

		q = new float[N];
		
		// Choose filtering block length, instantiate FFT
		// FFT size will be >= blockLength + 2*M
		
	
		if ( N + Nc - 1 > 8192 ) {
			blockLength = 8192-Nc+1;
		}
		else {
			blockLength = N;
		}
		
		filters = new OverlapAdd_dp[K];
	}
	
	// iterate
	
	public void iterate( int niter ) {
	
		for ( int iter = 0;  iter < niter;  iter++ ) {

			// compute q
			
//			evaluateQ();	
			evaluate_q();
			
			// compute alpha
			
			double rTr = 0.0;
			for ( int m = -M;  m <= M;  m++ ) {
				for ( int k = 0;  k < K;  k++ ) rTr += r[k][m+M] * r[k][m+M];
			}
			double q2 = 0.0;
			for ( int n = 0;  n < N;  n++ ) q2 += q[n]*q[n];
			double alpha = rTr/q2;
			
			// update w
			
			double[][] wtmp = new double[K][Nc];
			double[][] wlast = w.get( iter );
			for ( int m = -M;  m <= M;  m++ ) {
				for ( int k = 0;  k < K;  k++ ) wtmp[k][m+M] = wlast[k][m+M] + alpha*p[k][m+M];
			}
			w.add( wtmp );
			
			// update r
			
			double rTrp = 0.0;
			for ( int m = -M;  m <= M;  m++ ) {
				
				for ( int k = 0;  k < K;  k++ ) {
					double tmp = 0.0;
	                for ( int n = Math.max(0,m);  n < Math.min(N,N+m);  n++ ) {
	                	tmp += Xc[k][n-m]*q[n];
	                }
	                r[k][m+M] -= alpha*tmp;
	                rTrp += r[k][m+M] * r[k][m+M];
				}
				
			}
			
			// compute beta
			
			double beta = rTrp / rTr;
			
			// update p
			
			for ( int m = -M;  m <= M;  m++ ) {
				for ( int k = 0;  k < K;  k++ ) p[k][m+M] = r[k][m+M] + beta*p[k][m+M];
			}
			
			// update beam
			
			float[] btmp = new float[N];
			float[] bptr = b.get(iter);
			for ( int n = 0;  n < N;  n++ ) btmp[n] = (float) (bptr[n] + alpha*q[n]);
			b.add( btmp );		
		
			System.out.println( "\nEnd of iteration: " + iter );
			System.out.println( "Residual energy:  " + E( X, w.get( w.size()-1 ), M ) );
			
		}
		
	}
	
	
	private void evaluateQ() {
		
		// q = sum m = -M to M (pT[m] X[n-m])
		
		// instantiate OverlapAdd implementations
		
		for ( int k = 0;  k < K;  k++ ) 
			filters[k] = new OverlapAdd_dp( p[k], blockLength );
		
		int      ptr        = 0;
		int      dptr       = 0;
		int      nRemaining = N;
		int      dstLength  = N + Nc - 1;
		float[]  dst        = new float[ dstLength ];
		double[] tmp        = new double[ blockLength ];
		double[] block      = new double[ blockLength ];
		
		// Iterate over blocks
		
		while ( nRemaining > 0 ) {
			
			int nToGet = Math.min( blockLength, nRemaining);
			int nToPut = Math.min( blockLength, dstLength-dptr-1 );

			for ( int k = 0;  k < K;  k++ ) {
				java.util.Arrays.fill( block, 0.0 );
				for ( int i = 0;  i < nToGet;  i++ ) block[i] = X[k][ptr+i];
				filters[k].filter( block, tmp, 0 );
				for ( int i = 0;  i < nToPut;  i++ ) dst[dptr+i] += (float) tmp[i];
			}
			
			ptr += nToGet;
			dptr += nToPut;
			nRemaining -= nToGet;
		}
		
		for ( int k = 0;  k < K;  k++ ) {
			filters[k].flush( tmp, 0 );
			int nToGet = dstLength - dptr;
			for ( int i = 0;  i < nToGet;  i++ ) dst[dptr+i] += (float) tmp[i];
		}
		
		for ( int i = 0;  i < N;  i++ ) {
			q[i] = dst[ i + M ];
		}
		
	}
	
	
	private void evaluate_q() {  // direct form convolution
		
        for ( int n = 0;  n < N;  n++ ) {
        	
			double tmp = 0.0;
        	for ( int m = -M;  m <= M;  m++ ) {
			
        		if ( n-m >= 0  &&  n-m < N ) {
        			for ( int k = 0;  k < K;  k++ ) {
        				tmp += X[k][n-m]*p[k][m+M];
        			}
        		}
        		
			}
			
        	q[n] = (float) tmp;
		}
		
	}
	
	
	public ArrayList< double[][] > getWeights() {
		return w;
	}
	
	
	public ArrayList< float[] > getBeams() {
		return b;
	}
	
	
	public float[] directBeam( float[][] X, double[][] w, int M ) {
		
		int K = X.length;
		int N = X[0].length;
		
		float[] retval = new float[ N ];
		
		for ( int n = 0;  n < N;  n++ ) {
			double tmp = 0.0;
			for ( int m = -M;  m <= M;  m++ ) {
				if ( n-m >= 0  &&  n-m < N ) {
					for ( int k = 0;  k < K;  k++ ) tmp += w[k][m+M]*X[k][n-m];
				}
			}
			retval[n] = (float) tmp;
		}
		
		return retval;
	}
	
	
	public double E( float[][] X, double[][] w, int M ) {
		
		double retval = 0.0;
		float[] beam = directBeam( X, w, M );
		for ( int i = 0;  i < beam.length;  i++ ) retval += beam[i]*beam[i];
		
		return retval;
	}
	
	
	public double[][] gradE( float[][] X, double[][] w, int M ) {
		
		double eps = 0.0000001;
		int Nc = 2*M+1;
		int K  = X.length;

		double[][] g = new double[K][Nc];
		
		double[][] wtmp = new double[K][Nc];
		
		for ( int k = 0;  k < K;  k++ ) {
			for ( int m = 0;  m < Nc;  m++ ) {
				
				for ( int k1 = 0;  k1 <K;  k1++ ) System.arraycopy( w[k1], 0, wtmp[k1], 0, Nc );
				
				wtmp[k][m] += eps;
				double Ep = E( X, wtmp, M );

				wtmp[k][m] -= 2*eps;
				double Em = E( X, wtmp, M );
				
				g[k][m] = (Ep-Em)/(2*eps);
				
			}
		}
		
		return g;
	}

}
