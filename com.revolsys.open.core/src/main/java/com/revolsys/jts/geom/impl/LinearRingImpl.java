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
package com.revolsys.jts.geom.impl;

import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Dimension;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;

/**
 * Models an OGC SFS <code>LinearRing</code>.
 * A <code>LinearRing</code> is a {@link LineString} which is both closed and simple.
 * In other words,
 * the first and last coordinate in the ring must be equal,
 * and the interior of the ring must not self-intersect.
 * Either orientation of the ring is allowed.
 * <p>
 * A ring must have either 0 or 4 or more points.  
 * The first and last points must be equal (in 2D).
 * If these conditions are not met, the constructors throw 
 * an {@link IllegalArgumentException}
 *
 * @version 1.7
 */
public class LinearRingImpl extends LineStringImpl implements LinearRing {

  private static final long serialVersionUID = -4261142084085851829L;

  /**
   * Constructs a <code>LinearRing</code> with the vertices
   * specifed by the given {@link CoordinatesList}.
   *
   *@param  points  a sequence points forming a closed and simple linestring, or
   *      <code>null</code> to create the empty geometry.
   *      
   * @throws IllegalArgumentException if the ring is not closed, or has too few points
   *
   */
  public LinearRingImpl(final CoordinatesList points,
    final GeometryFactory factory) {
    super(points, factory);
    if (isClosed()) {
      final int vertexCount = getVertexCount();
      if (vertexCount >= 1 && vertexCount < MINIMUM_VALID_SIZE) {
        throw new IllegalArgumentException(
          "Invalid number of points in LinearRing (found " + vertexCount
            + " - must be 0 or >= 4)");
      }
    } else {
      throw new IllegalArgumentException(
        "Points of LinearRing do not form a closed linestring");
    }
  }

  @Override
  public LinearRingImpl clone() {
    return (LinearRingImpl)super.clone();
  }

  /**
   * Returns <code>Dimension.FALSE</code>, since by definition LinearRings do
   * not have a boundary.
   *
   * @return Dimension.FALSE
   */
  @Override
  public int getBoundaryDimension() {
    return Dimension.FALSE;
  }

  @Override
  public String getGeometryType() {
    return "LinearRing";
  }

  /**
   * Tests whether this ring is closed.
   * Empty rings are closed by definition.
   * 
   * @return true if this ring is closed
   */
  @Override
  public boolean isClosed() {
    if (isEmpty()) {
      // empty LinearRings are closed by definition
      return true;
    } else {
      return super.isClosed();
    }
  }

  @Override
  public LinearRing reverse() {
    final CoordinatesList points = getCoordinatesList();
    final CoordinatesList reversePoints = points.reverse();
    final GeometryFactory geometryFactory = getGeometryFactory();
    final LinearRing reverseLine = geometryFactory.createLinearRing(reversePoints);
    return reverseLine;
  }
}