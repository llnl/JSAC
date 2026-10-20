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
import java.text.DecimalFormat;

import org.ojalgo.matrix.Primitive64Matrix;


public class Block {
	
	double[][] B;
	int        N;
		
		
	public Block( double[][] B ) {
		this.B = B;
		N      = B.length;
	}
		
		
	public Block( int N ) {
		this.N = N;
		B = new double[N][N];
	}
		
		
	public Block( int N, double diagValue ) {
		this.N = N;
		B = new double[N][N];
		for ( int i = 0;  i < N;  i++ ) B[i][i] = diagValue;
	}
		
		
	public Block copy() {
		double[][] tmp = new double[N][];
		for ( int i = 0;  i < N;  i++ ) tmp[i] = B[i].clone();
		return new Block( tmp );
	}
		
		
	public Block transpose() {
	
		double[][] Bc = new double[N][N];
		for ( int i = 0;  i < N;  i++ ) {
			for ( int j = 0;  j < N;  j++ ) {
				Bc[i][j] = B[j][i];
			}
		}
			
		return new Block( Bc );
	}
	
	
	public Block[] symdec() {
		
		Block[] retval = new Block[2];
		retval[0] = new Block(N);  retval[1] = new Block(N);
		for ( int i = 0;  i < N;  i++ ) {
			for ( int j = 0;  j < N;  j++ ) {
				retval[0].B[i][j] = 0.5*( B[i][j] + B[j][i] );
				retval[1].B[i][j] = 0.5*( B[i][j] - B[j][i] );
			}
		}
		
		return retval;
	}
	
	
	public double trace() {
		
		double retval = 0.0;
		for ( int i = 0;  i < N;  i++ ) retval += B[i][i];
		
		return retval;
	}
		
		
	public void print( PrintStream ps ) {
		print( B,  ps );
	}
	
	
	public static Block minus( Block X, Block Y ) {
		
		if ( X.N != Y.N ) throw new IllegalStateException( "Attempt to subtract blocks of different size" );
		
		Block retval = new Block( X.N );
		for ( int i = 0;  i < X.N;  i++ ) {
			for (int j = 0;  j < X.N;  j++ ) {
				retval.B[i][j] = X.B[i][j] - Y.B[i][j];
			}
		}
		
		return retval;
	}
		
		
	public void plusProduct( Block X, Block Y ) {
			
		for ( int i = 0;  i < N;  i++ ) {
			for ( int j = 0;  j < N;  j++ ) {
				double tmp = 0.0;
				for ( int k = 0;  k < N;  k++ ) tmp += X.B[i][k]*Y.B[k][j];
				B[i][j] += tmp;
			}
		}

	}
		
		
	public void minusProduct( Block X, Block Y ) {
			
		for ( int i = 0;  i < N;  i++ ) {
			for ( int j = 0;  j < N;  j++ ) {
				double tmp = 0.0;
				for ( int k = 0;  k < N;  k++ ) tmp -= X.B[i][k]*Y.B[k][j];
				B[i][j] += tmp;
			}
		}
			
	}
	
	
	public void plusProduct( double s, Block other ) {
		for ( int i = 0;  i < N;  i++ ) {
			for ( int j = 0;  j < N;  j++ ) B[i][j] += s*other.B[i][j];
		}
	}
		
		
	public void plusTransposeProduct( Block X, Block Y ) {
			
		for ( int i = 0;  i < N;  i++ ) {
			for ( int j = 0;  j < N;  j++ ) {
				double tmp = 0.0;
				for ( int k = 0;  k < N;  k++ ) tmp += X.B[k][i]*Y.B[k][j];
				B[i][j] += tmp;
			}
		}
			
	}
		
		
	public void invert() {
		B = inverse( B );
	}
		
		
	public Block timesEquals( Block other ) {
		B = product( B, other.B );
		return this;
	}
		
		
	public Block minusEquals( Block other ) {
		minusEquals( B, other.B );
		return this;
	}
		
		
	public Block plusEquals( Block other ) {
		plusEquals( B, other.B );
		return this;
	}

	
	// Supporting static matrix methods
	
	
	// Cholesky factorization by outer product algorithm
	
	public static double[][] CholeskyFactorization( double[][] A ) {
		
		int N = A.length;
		
		double[][] L = new double[N][N];
		
		for ( int i = 0;  i < N;  i++ )  {
			for ( int j = i;  j < N;  j++ ) L[j][i] = A[j][i];
		}
		
		for ( int k = 0;  k < N;  k++ ) {
			
			L[k][k] = Math.sqrt( L[k][k] );
			
			for ( int i = k+1;  i < N;  i++ ) L[i][k] /= L[k][k];

			for ( int j = k+1;  j < N;  j++ ) {
				for ( int i = j; i < N;  i++ ) L[i][j] -= L[i][k]*L[j][k];
			}
			
		}
		
		return L;
	}
	
	
	// Inverse by Cholesky factorization followed by forward substitution
	
	public static double[][] CholeskyInverse( double[][] A ) {
		
		int N = A.length;
		
		double[][] L = CholeskyFactorization( A );
		double[][] LI = new double[N][N];
		
		for ( int i = 0;  i < N;  i++ ) {
			
			LI[0][i] = delta(0,i)/L[0][0];
			
			for ( int j = 1; j < N; j++ ) {
				
				double tmp = 0.0;
				for ( int k = 0;  k < j;  k++ ) {
					tmp += L[j][k]*LI[k][i];
				}
				LI[j][i] = (delta(j,i) - tmp)/L[j][j];
			}
			 
		}
		
		double[][] retval = product( transpose(LI), LI );
		
		return retval;
	}
	
	
	
	// Kronecker delta
	
	public static double delta( int i, int j ) {
		if ( i == j ) return 1;
		else return 0;
	}
	

	
	
	// Matrix multiply
	
	public static double[][] product( double[][] A, double[][] B ) {
		
		int mA = A.length;  int nA = A[0].length;
		int mB = B.length;  int nB = B[0].length;
		
		if ( nA != mB ) throw new IllegalStateException( "Matrix dimensions don't match" );
		
		double[][] retval = new double[ mA ][ nB ];
		
		for ( int i = 0;  i < mA;  i++ ) {
			for ( int j = 0;  j < nB;  j++ ) {
				double tmp = 0.0;
				for ( int k = 0;  k < nA;  k++ ) tmp += A[i][k]*B[k][j];
				retval[i][j] = tmp;
			}
		}
		
		return retval;
	}
	
	
	// Matrix addition
	
	public static void plusEquals( double[][] A, double[][] B ) {
		
		int mA = A.length;  int nA = A[0].length;
		int mB = B.length;  int nB = B[0].length;
		
		if ( mA != mB  ||  nA != nB ) throw new IllegalStateException( "Matrix dimensions don't match" );

		for ( int i = 0;  i < mA;  i++ ) {
			for ( int j = 0;  j < nA;  j++ ) A[i][j] += B[i][j];
		}

	}
	
	
	// Matrix subtraction
	
	public static void minusEquals( double[][] A, double[][] B ) {
		
		int mA = A.length;  int nA = A[0].length;
		int mB = B.length;  int nB = B[0].length;
		
		if ( mA != mB  ||  nA != nB ) throw new IllegalStateException( "Matrix dimensions don't match" );
		
		for ( int i = 0;  i < mA;  i++ ) {
			for ( int j = 0;  j < nA;  j++ ) A[i][j] -= B[i][j];
		}
		
	}
	
	
	// Matrix transpose
	
	public static double[][] transpose( double[][] A ) {
		
		int mA = A.length;  int nA = A[0].length;
		double[][] retval = new double[ nA ][ mA ];
		for ( int i = 0;  i < mA;  i++ ) {
			for ( int j = 0;  j < nA;  j++ ) {
				retval[j][i] = A[i][j];
			}
		}
		
		return retval;
	}
	
	
	// basic dot product
	
	public static double dotprod( float[] x, float[] y, int offset ) {
		
		double retval = 0.0;
		int    n      = x.length;
		if ( offset >= 0 ) {
		  for ( int i = offset;  i < n;  i++ ) {
			  retval += x[i]*y[i-offset];
		  }
		}
		else {
			for ( int i = 0;  i < n+offset;  i++ ) {
				retval += x[i]*y[i-offset];
			}
		}
		
		return retval;
	}
	
	
	// matrix inverse with Ojalgo
	
	public static double[][] inverse( double[][] A ) {
		
		int N = A.length;
		
		Primitive64Matrix Amat = Primitive64Matrix.FACTORY.rows( A );
		Primitive64Matrix Ainv = Amat.invert();
		
		double[][] retval = new double[N][N];
		for ( int i = 0;  i < N;  i++ ) {
			for ( int j = 0;  j < N;  j++ ) retval[i][j] = Ainv.doubleValue(i,j);
		}
		
		return retval;
	}
	
	
	public static void print( double[][] A, PrintStream ps ) {
		
		double Amax = Double.MIN_VALUE;
		int    N    = A.length;
		for ( int i = 0;  i < N;  i++ ) {
			for ( int j = 0;  j < N;  j++ ) Amax = Math.max( Amax, A[i][j] );
		}
		double scale = 1.e-20;
		int    exp   = -20;
		while ( scale <= 0.1*Amax ) {
			scale *= 10;  exp++;
		}
		
		ps.println( "\nx 10^" + exp );
		DecimalFormat F = new DecimalFormat( "0.000000" );
		for ( int i = 0;  i < N;  i++ )  {
			String line = "";
			for ( int j = 0;  j < N;  j++ ) {
				double value = A[i][j];
				String s = F.format( value/scale );
				if ( value < 0.0 ) line = line + " " + s;
				else               line = line + "  " + s;
			}
			ps.println( line );
		}
		ps.println();
		
	}
	
}
