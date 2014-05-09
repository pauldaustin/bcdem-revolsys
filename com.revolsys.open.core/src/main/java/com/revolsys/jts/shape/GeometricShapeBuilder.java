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

package com.revolsys.jts.shape;

import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LineSegmentImpl;

public abstract class GeometricShapeBuilder {
  protected BoundingBox extent = new Envelope(2, 0, 0, 1, 1);

  protected int numPts = 0;

  protected GeometryFactory geometryFactory;

  public GeometricShapeBuilder(final GeometryFactory geomFactory) {
    this.geometryFactory = geomFactory;
  }

  protected Coordinates createCoord(final double x, final double y) {
    return new DoubleCoordinates(geometryFactory.makePrecise(0, x),
      geometryFactory.makePrecise(1, y));
  }

  public Coordinates getCentre() {
    return extent.getCentre();
  }

  public double getDiameter() {
    return Math.min(extent.getHeight(), extent.getWidth());
  }

  public BoundingBox getExtent() {
    return extent;
  }

  public abstract Geometry getGeometry();

  public double getRadius() {
    return getDiameter() / 2;
  }

  public LineSegment getSquareBaseLine() {
    final double radius = getRadius();

    final Coordinates centre = getCentre();
    final Coordinate p0 = new Coordinate(centre.getX() - radius, centre.getY()
      - radius, Coordinates.NULL_ORDINATE);
    final Coordinate p1 = new Coordinate(centre.getX() + radius, centre.getY()
      - radius, Coordinates.NULL_ORDINATE);
    return new LineSegmentImpl(p0, p1);
  }

  public BoundingBox getSquareExtent() {
    final double radius = getRadius();

    final Coordinates centre = getCentre();
    return new Envelope(2, centre.getX() - radius, centre.getY() - radius,
      centre.getX() + radius, centre.getY() + radius);
  }

  public void setExtent(final BoundingBox extent) {
    this.extent = extent;
  }

  /**
   * Sets the total number of points in the created {@link Geometry}.
   * The created geometry will have no more than this number of points,
   * unless more are needed to create a valid geometry.
   */
  public void setNumPoints(final int numPts) {
    this.numPts = numPts;
  }

}
