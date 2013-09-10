/*
 * $RCSfile: PolyWarpSolver.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005-02-11 04:57:01 $
 * $State: Exp $
 */
package com.revolsys.swing.map.layer.raster.filter;

import java.util.Random;

import com.revolsys.gis.model.coordinates.list.CoordinatesList;

/**
 * A utility class to fit a polynomial to a set of corresponding
 * points in the source and destination images of a warp.  The core is
 * based on a public-domain Fortran utility routine for singular-value
 * decomposition.
 *
 * @since EA2
 *
 */
public class PolyWarpSolver {

  private static Random myRandom = new Random(0);

  private static double c0[] = new double[6];

  private static double c1[] = new double[6];

  private static double noise = 0.0F;

  /**
   *
   * @param sourcePixels a double array containing the source coordinates
   *        x_0, y_0, x_1, y_1, ...
   * @param targetPixels a double array containing the source coordinates
   *        x_0, y_0, x_1, y_1, ...
   * @return the best-fit coefficients for a bivariate polynomial of the
   *         given degree mapping the destination points into the source
   *         points.  The coefficients for the X polynomial are returned
   *         first, followed by those for the Y polynomial.
   */
  public static double[] getCoeffs(final CoordinatesList sourcePixels,
    final CoordinatesList targetPixels, final int degree) {

    final int pointCount = Math.min(sourcePixels.size(), targetPixels.size());
    final int equations = pointCount / 2;

    // Number of unknowns
    final int unknownCount = (degree + 1) * (degree + 2) / 2;
    final double[] out = new double[2 * unknownCount];

    // Special case for 3-point affine mapping
    if ((degree == 1) && (pointCount == 3)) {
      final double x0 = sourcePixels.getX(0);
      final double y0 = sourcePixels.getY(0);
      final double x1 = sourcePixels.getX(1);
      final double y1 = sourcePixels.getY(1);
      final double x2 = sourcePixels.getX(2);
      final double y2 = sourcePixels.getY(2);

      final double u0 = targetPixels.getX(0);
      final double v0 = targetPixels.getY(0);
      final double u1 = targetPixels.getX(1);
      final double v1 = targetPixels.getY(1);
      final double u2 = targetPixels.getX(2);
      final double v2 = targetPixels.getX(2);

      final double v0mv1 = v0 - v1;
      final double v1mv2 = v1 - v2;
      final double v2mv0 = v2 - v0;
      final double u1mu0 = u1 - u0;
      final double u2mu1 = u2 - u1;
      final double u0mu2 = u0 - u2;
      final double u1v2mu2v1 = u1 * v2 - u2 * v1;
      final double u2v0mu0v2 = u2 * v0 - u0 * v2;
      final double u0v1mu1v0 = u0 * v1 - u1 * v0;
      final double invdet = 1.0F / (u0 * (v1mv2) + v0 * (u2mu1) + (u1v2mu2v1));

      out[0] = (float)(((v1mv2) * x0 + (v2mv0) * x1 + (v0mv1) * x2) * invdet);
      out[1] = (float)(((u2mu1) * x0 + (u0mu2) * x1 + (u1mu0) * x2) * invdet);
      out[2] = (float)(((u1v2mu2v1) * x0 + (u2v0mu0v2) * x1 + (u0v1mu1v0) * x2) * invdet);
      out[3] = (float)(((v1mv2) * y0 + (v2mv0) * y1 + (v0mv1) * y2) * invdet);
      out[4] = (float)(((u2mu1) * y0 + (u0mu2) * y1 + (u1mu0) * y2) * invdet);
      out[5] = (float)(((u1v2mu2v1) * y0 + (u2v0mu0v2) * y1 + (u0v1mu1v0) * y2) * invdet);

      return out;
    }

    final double[][] A = new double[equations][unknownCount];

    /*
     * Fill in A with: 1 x_0 y_0 ... x_0*y_0^(n-1) y_0^n 1 x_1 y_1 ...
     * x_1*y_1^(n-1) y_1^n ... 1 x_(k-1) y_(k-1) ... x_(k-1)*y_(k-1)^(n-1)
     * y_(k-1)^n The height of the matrix is equal to the number of equations
     * The width of the matrix is equal to the number of unknowns
     */

    final double[] xpow = new double[degree + 1];
    final double[] ypow = new double[degree + 1];

    for (int i = 0; i < equations; i++) {
      final double[] Ai = A[i];
      final double x = targetPixels.getX(i);
      final double y = targetPixels.getY(i);

      double xtmp = 1.0F;
      double ytmp = 1.0F;
      for (int d = 0; d <= degree; d++) {
        xpow[d] = xtmp;
        ypow[d] = ytmp;
        xtmp *= x;
        ytmp *= y;
      }

      int index = 0;
      for (int deg = 0; deg <= degree; deg++) {
        for (int ydeg = 0; ydeg <= deg; ydeg++) {
          Ai[index++] = xpow[deg - ydeg] * ypow[ydeg];
        }
      }
    }

    final double[][] V = new double[unknownCount][unknownCount];
    final double[] W = new double[unknownCount];
    final double[][] U = new double[equations][unknownCount];
    SVD(A, W, U, V);

    // Multiply the columns of V by the inverted diagonal entries of W
    for (int j = 0; j < unknownCount; j++) {
      double winv = W[j];
      if (winv != 0.0) {
        winv = 1.0F / winv;
      }
      for (int i = 0; i < unknownCount; i++) {
        V[i][j] *= winv;
      }
    }

    // Multiply V by U^T
    final double[][] VWINVUT = matmul_t(V, U); // unknowns x equations

    // Multiply VWINVUT by source coords to yield output coefficients
    for (int i = 0; i < unknownCount; i++) {
      double tmp0 = 0;
      double tmp1 = 0;
      for (int j = 0; j < equations; j++) {
        final double val = VWINVUT[i][j];
        tmp0 += val * sourcePixels.getX(j);
        tmp1 += val * sourcePixels.getY(j);
      }
      out[i] = (float)tmp0;
      out[i + unknownCount] = (float)tmp1;
    }

    return out;
  }

  private static final double hypot(final double x, final double y) {
    final double xabs = Math.abs(x);
    final double yabs = Math.abs(y);

    if (xabs > yabs) {
      return xabs * sqrt(square(yabs / xabs) + 1.0F);
    } else if (yabs != 0.0F) {
      return yabs * sqrt(square(xabs / yabs) + 1.0F);
    } else {
      return xabs;
    }
  }

  /* Multiply A * B^T */
  public static double[][] matmul_t(final double[][] A, final double[][] B) {
    final int rowsA = A.length;
    final int colsA = A[0].length;

    final int rowsB = B[0].length;
    final int colsB = B.length;

    // Must have colsA == rowsB

    final double[][] out = new double[rowsA][colsB];

    for (int i = 0; i < rowsA; i++) {
      final double[] outi = out[i];
      final double[] Ai = A[i];

      for (int j = 0; j < colsB; j++) {
        double tmp = 0.0F;
        for (int k = 0; k < colsA; k++) {
          tmp += Ai[k] * B[j][k];
        }
        outi[j] = tmp;
      }
    }

    return out;
  }

  private static double sign(double a, final double b) {
    a = Math.abs(a);
    if (b >= 0.0F) {
      return a;
    } else {
      return -a;
    }
  }

  private static final double sqrt(final double x) {
    return Math.sqrt(x);
  }

  private static final double square(final double x) {
    return x * x;
  }

  /**
   * Performs Singular-Value Decomposition on a given matrix.  The
   * number of rows of the matrix must be greater than or equal to
   * the number of columns.
   *
   * <p> When the routine completes, the product U*diag(W)*V^T
   * will be equal to the input matrix A.  U will be column-orthogonal
   * and V will be fully orthogonal.  The elements of W will be positive
   * or zero.
   *
   * <p> From the comments in the original Fortran version contained
   * in the Eispack library:
   *
   * <pre>
   * c     this subroutine is a translation of the algol procedure svd,
   * c     num. math. 14, 403-420(1970) by golub and reinsch.
   * c     handbook for auto. comp., vol ii-linear algebra, 134-151(1971).
   * c     Questions and comments should be directed to Alan K. Cline,
   * c     Pleasant Valley Software, 8603 Altus Cove, Austin, TX 78759.
   * c     Electronic mail to cline@cs.utexas.edu.
   * c
   * c     this version dated january 1989. (for the IBM 3090vf)
   * </pre>
   *
   * @param a the input matrix to be decomposed of size m x n.
   * @param w an empty vector of length n to be filled in.
   * @param u an empty matrix of size m x n to be filled in.
   * @param v an empty matrix of size n x n to be filled in.
   * @return true if convergence is acheived within 30 iterations.
   */
  private static boolean SVD(final double[][] a, final double[] w,
    final double[][] u, final double[][] v) {
    int i, j, k, l, m, n, i1, k1, l1, mn, its;
    double c, f, g, h, s, x, y, z, tst1, tst2, scale;
    final double fabs, gabs, habs;

    l = 0;
    l1 = 0;
    m = a.length;
    n = a[0].length;

    final double[] rv1 = new double[n];

    for (i = 0; i < m; i++) {
      for (j = 0; j < n; j++) {
        u[i][j] = a[i][j];
      }
    }

    g = 0.0F;
    scale = 0.0F;
    x = 0.0F;

    for (i = 0; i < n; i++) {
      l = i + 1;
      rv1[i] = scale * g;
      g = 0.0F;
      s = 0.0F;
      scale = 0.0F;

      if (i < m) {
        for (k = i; k < m; k++) {
          scale += Math.abs(u[k][i]);
        }

        if (scale != 0.0F) {
          for (k = i; k < m; k++) {
            u[k][i] /= scale;
            s += square(u[k][i]);
          }

          f = u[i][i];
          g = -sign(sqrt(s), f);
          h = f * g - s;
          u[i][i] = f - g;

          for (j = l; j < n; j++) {
            s = 0.0F;

            for (k = i; k < m; k++) {
              s += u[k][i] * u[k][j];
            }
            f = s / h;
            for (k = i; k < m; k++) {
              u[k][j] += f * u[k][i];
            }
          }

          for (k = i; k < m; k++) {
            u[k][i] *= scale;
          }
        }
      }

      w[i] = scale * g;
      g = 0.0F;
      s = 0.0F;
      scale = 0.0F;

      if ((i < m) && (i != n - 1)) {
        for (k = l; k < n; k++) {
          scale += Math.abs(u[i][k]);
        }

        if (scale != 0.0F) {
          for (k = l; k < n; k++) {
            u[i][k] /= scale;
            s += square(u[i][k]);
          }

          f = u[i][l];
          g = -sign(sqrt(s), f);
          h = f * g - s;
          u[i][l] = f - g;

          for (k = l; k < n; k++) {
            rv1[k] = u[i][k] / h;
          }

          for (j = l; j < m; j++) {
            s = 0.0F;

            for (k = l; k < n; k++) {
              s += u[j][k] * u[i][k];
            }

            for (k = l; k < n; k++) {
              u[j][k] += s * rv1[k];
            }
          }

          for (k = l; k < n; k++) {
            u[i][k] *= scale;
          }

        }
      }

      x = Math.max(x, Math.abs(w[i]) + Math.abs(rv1[i]));
    }

    for (i = n - 1; i >= 0; i--) {
      if (i != n - 1) {
        if (g != 0.0F) {
          for (j = l; j < n; j++) {
            v[j][i] = (u[i][j] / u[i][l]) / g;
          }

          for (j = l; j < n; j++) {
            s = 0.0F;
            for (k = l; k < n; k++) {
              s += u[i][k] * v[k][j];
            }
            for (k = l; k < n; k++) {
              v[k][j] += s * v[k][i];
            }
          }
        }

        for (j = l; j < n; j++) {
          v[i][j] = v[j][i] = 0.0F;
        }
      }

      v[i][i] = 1.0F;
      g = rv1[i];
      l = i;
    }

    mn = Math.min(m, n);

    for (i = mn - 1; i >= 0; i--) {
      l = i + 1;
      g = w[i];

      if (i != n - 1) {
        for (j = l; j < n; j++) {
          u[i][j] = 0.0F;
        }
      }

      if (g != 0.0F) {
        if (i != mn - 1) {
          for (j = l; j < n; j++) {
            s = 0.0F;

            for (k = l; k < m; k++) {
              s += u[k][i] * u[k][j];
            } // 440
            f = (s / u[i][i]) / g;
            for (k = i; k < m; k++) {
              u[k][j] += f * u[k][i];
            }
          }
        }

        for (j = i; j < m; j++) {
          u[j][i] /= g;
        }
      } else {
        for (j = i; j < m; j++) {
          u[j][i] = 0.0F;
        }
      }
      u[i][i] += 1.0F;
    }

    tst1 = x;

    for (k = n - 1; k >= 0; k--) {
      k1 = k - 1;
      its = 0;

      while (true) {
        boolean flag = true;

        for (l = k; l >= 0; l--) {
          l1 = l - 1;
          tst2 = tst1 + Math.abs(rv1[l]);
          if (tst2 == tst1) {
            flag = false;
            break;
          }

          tst2 = tst1 + Math.abs(w[l1]);
          if (tst2 == tst1) {
            flag = true;
            break;
          }
        }

        if (flag) {
          c = 0.0F;
          s = 1.0F;

          for (i = l; i < k + 1; i++) {
            f = s * rv1[i];
            rv1[i] *= c;

            tst2 = tst1 + Math.abs(f);
            if (tst2 != tst1) {
              g = w[i];

              h = hypot(f, g);
              w[i] = h;
              c = g / h;
              s = -f / h;

              for (j = 0; j < m; j++) {
                y = u[j][l1];
                z = u[j][i];
                u[j][l1] = y * c + z * s;
                u[j][i] = -y * s + z * c;
              }
            }
          }
        }

        z = w[k];

        if (l == k) {
          if (z < 0.0F) {
            w[k] = -z;
            for (j = 0; j < n; j++) {
              v[j][k] = -v[j][k];
            }
          }
          break;
        }

        if (its == 30) {
          return false;
        }

        ++its;

        x = w[l];
        y = w[k1];
        g = rv1[k1];
        h = rv1[k];
        f = 0.5F * (((g + z) / h) * ((g - z) / y) + y / h - h / y);

        g = hypot(f, 1.0F);
        f = x - (z / x) * z + (h / x) * (y / (f + sign(g, f)) - h);

        c = 1.0F;
        s = 1.0F;

        for (i1 = l; i1 <= k1; i1++) {
          i = i1 + 1;
          g = rv1[i];
          y = w[i];
          h = s * g;
          g = c * g;

          z = hypot(f, h);
          rv1[i1] = z;
          c = f / z;
          s = h / z;
          f = x * c + g * s;
          g = -x * s + g * c;
          h = y * s;
          y = y * c;

          for (j = 0; j < n; j++) {
            x = v[j][i1];
            z = v[j][i];
            v[j][i1] = x * c + z * s;
            v[j][i] = -x * s + z * c;
          }

          z = hypot(f, h);
          w[i1] = z;

          if (z != 0.0F) {
            c = f / z;
            s = h / z;
          }

          f = c * g + s * y;
          x = -s * g + c * y;

          for (j = 0; j < m; j++) {
            y = u[j][i1];
            z = u[j][i];
            u[j][i1] = y * c + z * s;
            u[j][i] = -y * s + z * c;
          }
        }

        rv1[l] = 0.0F;
        rv1[k] = f;
        w[k] = x;
      }
    }

    return true;
  }

  private static float xpoly(final float x, final float y) {
    return (float)(c0[0] + c0[1] * x + c0[2] * y + c0[3] * x * x + c0[4] * x
      * y + c0[5] * y * y + (myRandom.nextDouble() * noise));
  }

  private static float ypoly(final float x, final float y) {
    return (float)(c1[0] + c1[1] * x + c1[2] * y + c1[3] * x * x + c1[4] * x
      * y + c1[5] * y * y + (myRandom.nextDouble() * noise));
  }
}
