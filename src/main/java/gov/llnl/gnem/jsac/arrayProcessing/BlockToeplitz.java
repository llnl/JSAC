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


import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;

import gov.llnl.gnem.jsac.commands.arrayProcessing.enums.FKType;


public class BlockToeplitz {
	
	private int        N;           // number of channels
	private int        M;           // number of lags = M+1

	
	private ArrayList< Block > R;
	private Block[][]  T;
	private Block[][]  Tinv;
	private Block[][]  w;
	private Block[][]  y;
	private Block[]    QT;
	private Block[]    QTinv;
	
	public BlockToeplitz( float[][] X, double eps, int M ) {
		
		N      = X.length;
		this.M = M;
		
		R = new ArrayList<>();
		
		for ( int ioffset = -M;  ioffset <= M;  ioffset++ ) {
			
			double[][] Rtmp = new double[N][N];
			
			for ( int j = 0;  j < N;  j++ ) {
				float[] x = X[j];
				for ( int k = 0;  k < N;  k++ ) {
					float[] y = X[k];
					Rtmp[j][k] = Block.dotprod( x, y, ioffset );
				}
			}
			
			if ( ioffset == 0 ) {
				double trace = 0.0;
				for ( int j = 0;  j < N;  j++ ) trace += Rtmp[j][j];
				for ( int j = 0;  j < N;  j++ ) Rtmp[j][j] += trace/N*eps;
			}
			
			R.add( new Block( Rtmp ) );
		}
		
		T = new Block[M+1][M+1];
		
		for ( int i = 0;  i <= M;  i++ ) {
			for ( int j = 0;  j <= M;  j++ ) {
				T[i][j] = R.get(j-i+M).copy();
			}
		}
		
		QT    = null;
		QTinv = null;
		
	}
	
	
	public double[][] getCorrelation( int index ) {
		return R.get(index).B;
	}
	
	
	public Block getZeroLagCovariance() {
		return R.get( M );
	}
	
	
	public void printCorrelations( PrintStream ps ) {
		
		for ( int lag = -M;  lag <= M;  lag++ ) {
			ps.println( "lag: " + lag );
			R.get(lag+M).print( ps );
		}
		
	}
	
	
	public Block[][] getT() {
		return T;
	}
	
	
	public Block[][] getw() {
		return w;
	}
	
	
	public Block[][] gety() {
		return y;
	}
	
	
	public int getNumChannels() {
		return N;
	}


	public Block[][] LevinsonRecursion() {

		// initializations
		
		w     = new Block[M+1][M+1];
		y     = new Block[M+1][M+1];
		
		for ( int i = 0;  i <=M;  i++ ) {
			for ( int j = 0;  j <= M;  j++ ) {
				w[i][j] = new Block(N);
				y[i][j] = new Block(N);
			}
		}
		
		w[0][0] = new Block( Block.CholeskyInverse( R.get(M).B ) );
		y[0][0] = w[0][0];
		
		LinkedList< Block > E = new LinkedList<>();
		LinkedList< Block > C = new LinkedList<>();
		
		E.add( R.get(M+1) );
		C.add( R.get(M+1).transpose() );
				
		for ( int p = 0;  p < M;  p++ ) {
			
			// calculate eps(p+1) and delta(p+1)

			Block eps   = new Block(N);
			Block delta = new Block(N);
			for ( int k = 0;  k < E.size();  k++ ) {
				eps.plusProduct( E.get(k), y[k][p] );
				delta.plusProduct( C.get(k), w[k][p] );
			}
			
			Block F = new Block( N, 1.0 );  F.minusProduct( eps, delta );
			Block B = new Block( N, 1.0 );  B.minusProduct( delta, eps );
			
			F.invert();
			B.invert();
			
			Block dF = delta.copy().timesEquals(F);
			Block eB = eps.copy().timesEquals(B);
			
			for ( int k = 0;  k <= p;  k++ ) {
				w[k][p+1].plusEquals( w[k][p].copy().timesEquals(F) );
				w[k+1][p+1].minusEquals( y[k][p].copy().timesEquals(dF) );
				y[k][p+1].minusEquals( w[k][p].copy().timesEquals(eB) );
				y[k+1][p+1] = y[k][p].copy().timesEquals(B);
			}
			
			if ( p == M-1 )
				break;
			
			// update E and C
			
			E.addLast( R.get( M+p+2 ));
			C.addFirst( R.get( M+p+2 ).transpose() );
			
		}
		
		// construct inverse matrix
		
		Tinv  = new Block[M+1][M+1];
		
		// first and last columns for free
		
		for ( int i = 0;  i <= M;  i++ ) {
			Tinv[i][0] = w[i][M].copy();
			Tinv[i][M] = y[i][M].copy();
		}
		
		// recursion for remaining columns
		
		Block[][] b = new Block[M+1][M+1];
		b[0][0] = new Block( N );
		Block I = new Block( N, 1.0 );
		Block Z = new Block( N );
		
		Block[][] f = new Block[M+1][M+1];
		for ( int i = 0;  i <= M;  i++ ) {
			for ( int j = 0;  j <= M;  j++ ) {
				if ( i == j ) f[i][j] = I;
				else f[i][j] = Z;
			}
		}
		
		for ( int icol = 1;  icol <= M-1;  icol++ ) {

			for ( int p = 0;  p < M;  p++ ) {
				
				// working on b[][p+1]
				
				Block Cb = new Block(N);
				for ( int k = 0;  k <= p;  k++ ) Cb.plusProduct( C.get(M-1-p+k), b[k][p] );

				for ( int k = 0;  k <= p;  k++ ) {
					Block tmp = b[k][p].copy();
					tmp.plusProduct( y[k][p+1], Block.minus( f[p+1][icol], Cb) );
					b[k][p+1] = tmp;
				}
				Block tmp = new Block(N);
				tmp.plusProduct( y[p+1][p+1], Block.minus(f[p+1][icol], Cb) );
				b[p+1][p+1] = tmp;
			}
			
			for ( int i = 0;  i <= M;  i++ ) {
				Tinv[i][icol] = b[i][M].copy();
			}

		}
		
		return Tinv;
	}
	
	
	// returns Block[2] with real and imaginary parts of spectral covariance matrix
	
	public Block[] SpectralCovarianceMatrix( double Omega, FKType type ) {
		
		Block[] Q = null;
		
		switch (type) {
		case PDS:
			if ( QT == null ) {                         //  Collapse T[][]
				QT = new Block[2*M+1];
				for ( int p = -M;  p <= M;  p++ ) {
					QT[p+M] = new Block(N);
				}
				for ( int i = 0;  i <= M;  i++ ) {
					for ( int j = 0;  j <= M;  j++ ) {
						int p = i-j;
						QT[p+M].plusEquals( T[i][j] );
					}
				}
			}
			Q = QT;
			break;
		case MLM:
			if ( QTinv == null ) {                       //  Collapse Tinv[][]
				QTinv = new Block[2*M+1];
				for ( int p = -M;  p <= M;  p++ ) {
					QTinv[p+M] = new Block(N);
				}
				for ( int i = 0;  i <= M;  i++ ) {
					for ( int j = 0;  j <= M;  j++ ) {
						int p = i-j;
						QTinv[p+M].plusEquals( Tinv[i][j] );
					}
				}
			}
			Q = QTinv;
			break;
		}
		
		Block[] S = new Block[2];
		S[0] = Q[M].copy();     S[1] = new Block(N);
		
		// transform kernel via coupled form oscillator
		
		double c  = 1.0;                 double s  = 0.0;
		double dc = Math.cos( Omega );   double ds = Math.sin( -Omega );
		for ( int i = 1;  i <= M;  i++ ) {
			double ctmp = c;
			c = ctmp*dc - s*ds;
			s = ctmp*ds + s*dc;
			S[0].plusProduct( c, Q[M+i] );
			S[1].plusProduct( s, Q[M+i] );
			S[0].plusProduct( c, Q[M-i] );
			S[1].plusProduct( -s, Q[M-i] );
		}
		
		return S;
	}
		
}
