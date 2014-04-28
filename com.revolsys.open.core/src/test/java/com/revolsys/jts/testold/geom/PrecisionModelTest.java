/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

package com.revolsys.jts.testold.geom;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.PrecisionModel;

/**
 * @version 1.7
 */
public class PrecisionModelTest extends TestCase {

  public static Test suite() {
    return new TestSuite(PrecisionModelTest.class);
  }

  public PrecisionModelTest(final String name) {
    super(name);
  }

  private void preciseCoordinateTester(final PrecisionModel pm,
    final double x1, final double y1, final double x2, final double y2) {
    final Coordinates p = new Coordinate((double)x1, y1, Coordinates.NULL_ORDINATE);

    pm.makePrecise(p);

    final Coordinates pPrecise = new Coordinate((double)x2, y2, Coordinates.NULL_ORDINATE);
    assertTrue(p.equals2d(pPrecise));
  }

  public void testGetMaximumSignificantDigits() {
    assertEquals(16,
      new PrecisionModel(PrecisionModel.FLOATING).getMaximumSignificantDigits());
    assertEquals(
      6,
      new PrecisionModel(PrecisionModel.FLOATING_SINGLE).getMaximumSignificantDigits());
    assertEquals(1,
      new PrecisionModel(PrecisionModel.FIXED).getMaximumSignificantDigits());
    assertEquals(4, new PrecisionModel(1000).getMaximumSignificantDigits());
  }

  public void testMakePrecise() {
    final PrecisionModel pm_10 = new PrecisionModel(0.1);

    preciseCoordinateTester(pm_10, 1200.4, 1240.4, 1200, 1240);
    preciseCoordinateTester(pm_10, 1209.4, 1240.4, 1210, 1240);
  }

  public void testParameterlessConstructor() {
    final PrecisionModel p = new PrecisionModel();
    // Implicit precision model has scale 0
    assertEquals(0, p.getScale(), 1E-10);
  }

}
