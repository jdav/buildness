package com.davenport.buildness.matrix;

public class QRDecomposition {
	
	public static void main(String[] args) {
		gramSchmidt();
	}

	/**
	 * This implements the classic Schmidt algorithm as described in the following 
	 * paper: http://www.inf.ethz.ch/personal/gander/papers/qrneu.pdf
	 */
	static void schmidt() {

		final int m = 3;
		final int n = 3;

		double[][] a = { { 707.0000, 679.2000, 555.4000 },
				         { 207.0000, 46.6000,  919.2000 },
				         { 1.4000,   -0.7000,  1.2200   } };
		
		double[][] q = new double[m][n];
		double[][] r = new double[m][n];
		
		//First initialize q with a...
		for (int x=0; x<m; x++) {
			for (int y=0; y<n; y++) {
				q[x][y] = a[x][y];
			}
		}
		
		//Build Q and R...
		int s = 0;
		for (int k=0; k<n; k++) {
			
			for (int i=0; i<(k-1); i++) {
				s = 0;
				for (int j=0; j<m; j++) {
					s += (q[j][i] * q[j][k]);
				}
				r[i][k] = s;
			}
			
			for (int i=0; i<(k-1); i++) {

				for (int j=0; j<m; j++) {
					q[j][k] -= q[j][i] * r[i][k];
				}
			
				s = 0;
				for (int j=0; j<m; j++) {
					s += (q[j][k] * q[j][k]);
				}

				r[k][k] = Math.sqrt(s);
				
				for (int j=0; j<m; j++) {
					q[j][k] /= r[k][k];
				}
			}
		}
		
		//Print A, Q and R...
		printMatrix("A = ", a);
		printMatrix("Q = ", q);
		printMatrix("R = ", r);
	}
	
	/**
	 * This implements the modified Gram Schmidt algorithm as described in the 
	 * following paper: http://www.inf.ethz.ch/personal/gander/papers/qrneu.pdf
	 */
	static void gramSchmidt() {

		final int m = 3;
		final int n = 3;

		final double[][] a = { { 707.0000, 679.2000, 555.4000 },
				         	   { 207.0000, 46.6000,  919.2000 },
				               { 1.4000,   -0.7000,  1.2200   } };
		
		double[][] q = new double[m][n];
		double[][] r = new double[m][n];
		double[][] t = new double[m][n];
		
		//First initialize q and t with a...
		for (int x=0; x<m; x++) {
			for (int y=0; y<n; y++) {
				t[x][y] = q[x][y] = a[x][y];
			}
		}
		
		//Build Q and R...
		int s = 0;
		for (int k=0; k<n; k++) {
			
			s = 0;
			for (int j=0; j<m; j++) {
				s += (t[j][k] * t[j][k]);
			}
			
			r[k][k] = Math.sqrt(s);

			for (int j=0; j<m; j++) {
				q[j][k] /= r[k][k];
			}
			
			for (int i=k; i<n; i++) {
				s = 0;
				for (int j=0; j<m; j++) {
					s += (t[j][i] * q[j][k]);
				}
				
				r[k][i] = s;
				for (int j=0; j<m; j++) {
					t[j][i] -= (r[k][i] * q[j][k]);
				}
			}
		}
		
		//Print A, Q and R...
		printMatrix("A = ", a);
		printMatrix("Q = ", q);
		printMatrix("R = ", r);
	}

	static void printMatrix(String label, double[][] matrix) {
		System.out.println(label);
		for (int x=0; x<matrix.length; x++) {
			for (int y=0; y<matrix[0].length; y++) {
				System.out.print(matrix[x][y]);
				System.out.print("\t");
			}
			System.out.println();
		}
	}
}
