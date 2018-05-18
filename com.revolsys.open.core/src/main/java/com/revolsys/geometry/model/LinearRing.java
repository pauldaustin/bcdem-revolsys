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
package com.revolsys.geometry.model;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;

import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.model.coordinates.CoordinatesUtil;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.editor.LineStringEditor;
import com.revolsys.geometry.model.editor.LinearRingEditor;
import com.revolsys.geometry.model.prep.PreparedLinearRing;
import com.revolsys.util.QuantityType;

import tec.uom.se.quantity.Quantities;

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
public interface LinearRing extends LineString {
  /**
  * The minimum number of vertices allowed in a valid non-empty ring (= 4).
  * Empty rings with 0 vertices are also valid.
  */
  int MINIMUM_VALID_SIZE = 4;

  /**
   *  Returns the minimum coordinate, using the usual lexicographic comparison.
   *
   *@param  coordinates  the array to search
   *@return              the minimum coordinate in the array, found using <code>compareTo</code>
   *@see Point#compareTo(Object)
   */
  static int minCoordinateIndex(final LinearRing ring) {
    double minX = ring.getX(0);
    double minY = ring.getY(0);
    int minIndex = 0;
    final int vertexCount = ring.getVertexCount();
    for (int vertexIndex = 1; vertexIndex < vertexCount; vertexIndex++) {
      final double x = ring.getX(vertexIndex);
      final double y = ring.getY(vertexIndex);
      if (CoordinatesUtil.compare(minX, minY, x, y) > 0) {
        minIndex = vertexIndex;
        minX = x;
        minY = y;
      }
    }
    return minIndex;
  }

  @SuppressWarnings("unchecked")
  static <G extends LinearRing> G newLinearRing(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof LinearRing) {
      return (G)value;
    } else if (value instanceof Geometry) {
      throw new IllegalArgumentException(
        ((Geometry)value).getGeometryType() + " cannot be converted to a LinearRing");
    } else {
      final String string = DataTypes.toString(value);
      return (G)GeometryFactory.DEFAULT_3D.geometry(string, false);
    }
  }

  /**
   *  Shifts the positions of the coordinates until <code>firstCoordinate</code>
   *  is first.
   *
   *@param  coordinates      the array to rearrange
   *@param  firstCoordinate  the coordinate to make first
   */
  static LinearRing scroll(final LinearRing ring, final int index) {
    final int vertexCount = ring.getVertexCount();
    final int axisCount = ring.getAxisCount();
    final double[] coordinates = new double[vertexCount * axisCount];
    int newVertexIndex = 0;
    for (int vertexIndex = index; vertexIndex < vertexCount - 1; vertexIndex++) {
      CoordinatesListUtil.setCoordinates(coordinates, axisCount, newVertexIndex++, ring,
        vertexIndex);
    }
    for (int vertexIndex = 0; vertexIndex < index; vertexIndex++) {
      CoordinatesListUtil.setCoordinates(coordinates, axisCount, newVertexIndex++, ring,
        vertexIndex);
    }
    CoordinatesListUtil.setCoordinates(coordinates, axisCount, vertexCount - 1, ring, index);
    final GeometryFactory geometryFactory = ring.getGeometryFactory();
    return geometryFactory.linearRing(axisCount, coordinates);
  }

  default LinearRing clipRectangle(double minX, double minY, double maxX, double maxY) {
    if (minX > maxX) {
      final double t = minX;
      minX = maxX;
      maxX = t;
    }
    if (minY > maxY) {
      final double t = minY;
      minY = maxY;
      maxY = t;
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (getBoundingBox().intersects(minX, minY, maxX, maxY)) {
      final int axisCount = getAxisCount();
      LineStringEditor newLine = new LineStringEditor(geometryFactory);

      // Clip top
      {
        final LineString line = this;
        final int vertexCount = line.getVertexCount();
        newLine = new LineStringEditor(geometryFactory);
        double x1 = line.getX(0);
        double y1 = line.getY(0);
        boolean inside1 = y1 < maxY;
        if (inside1) {
          newLine.appendVertex(x1, y1);
          for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
            final double coordinate = line.getCoordinate(0, axisIndex);
            newLine.setCoordinate(0, axisIndex, coordinate);
          }
        }
        for (int vertexIndex = 1; vertexIndex < vertexCount; vertexIndex++) {
          final double x2 = line.getX(vertexIndex);
          final double y2 = line.getY(vertexIndex);
          final boolean inside2 = y2 <= maxY;
          if (inside1 != inside2) {
            if (y1 != maxY && y2 != maxY) {
              final double newX = x1 + (x2 - x1) * (maxY - y1) / (y2 - y1);
              newLine.appendVertex(newX, maxY);
            }
          }
          if (inside2) {
            newLine.appendVertex(x2, y2);
            for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
              final double coordinate = line.getCoordinate(vertexIndex, axisIndex);
              newLine.setCoordinate(vertexIndex, axisIndex, coordinate);
            }
          }
          inside1 = inside2;
          x1 = x2;
          y1 = y2;
        }
        newLine.closeRing();
      }

      // Clip bottom
      {
        final LineString line = newLine;
        final int vertexCount = line.getVertexCount();
        newLine = new LineStringEditor(geometryFactory);
        double x1 = line.getX(0);
        double y1 = line.getY(0);
        boolean inside1 = y1 >= minY;
        if (inside1) {
          newLine.appendVertex(x1, y1);
          for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
            final double coordinate = line.getCoordinate(0, axisIndex);
            newLine.setCoordinate(0, axisIndex, coordinate);
          }
        }
        for (int vertexIndex = 1; vertexIndex < vertexCount; vertexIndex++) {
          final double x2 = line.getX(vertexIndex);
          final double y2 = line.getY(vertexIndex);
          final boolean inside2 = y2 >= minY;
          if (inside1 != inside2) {
            if (y1 != minY && y2 != minY) {
              final double newX = x1 + (x2 - x1) * (minY - y1) / (y2 - y1);
              newLine.appendVertex(newX, minY);
            }
          }
          if (inside2) {
            newLine.appendVertex(x2, y2);
            for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
              final double coordinate = line.getCoordinate(vertexIndex, axisIndex);
              newLine.setCoordinate(vertexIndex, axisIndex, coordinate);
            }
          }

          inside1 = inside2;
          x1 = x2;
          y1 = y2;
        }
        newLine.closeRing();
      }

      // Clip left
      {
        final LineString line = newLine;
        final int vertexCount = line.getVertexCount();
        newLine = new LineStringEditor(geometryFactory);
        double x1 = line.getX(0);
        double y1 = line.getY(0);
        boolean inside1 = x1 >= minX;
        if (inside1) {
          newLine.appendVertex(x1, y1);
          for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
            final double coordinate = line.getCoordinate(0, axisIndex);
            newLine.setCoordinate(0, axisIndex, coordinate);
          }
        }
        for (int vertexIndex = 1; vertexIndex < vertexCount; vertexIndex++) {
          final double x2 = line.getX(vertexIndex);
          final double y2 = line.getY(vertexIndex);
          final boolean inside2 = x2 >= minX;
          if (inside1 != inside2) {
            if (x1 != minX && x2 != minX) {
              final double newY = y1 + (y2 - y1) * (minX - x1) / (x2 - x1);
              newLine.appendVertex(minX, newY);
            }
          }
          if (inside2) {
            newLine.appendVertex(x2, y2);
            for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
              final double coordinate = line.getCoordinate(vertexIndex, axisIndex);
              newLine.setCoordinate(vertexIndex, axisIndex, coordinate);
            }
          }

          inside1 = inside2;
          x1 = x2;
          y1 = y2;
        }
        newLine.closeRing();
      }

      // Clip right
      {
        final LineString line = newLine;
        final int vertexCount = line.getVertexCount();
        newLine = new LineStringEditor(geometryFactory);
        double x1 = line.getX(0);
        double y1 = line.getY(0);
        boolean inside1 = x1 <= maxX;
        if (inside1) {
          newLine.appendVertex(x1, y1);
          for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
            final double coordinate = line.getCoordinate(0, axisIndex);
            newLine.setCoordinate(0, axisIndex, coordinate);
          }
        }
        for (int vertexIndex = 1; vertexIndex < vertexCount; vertexIndex++) {
          final double x2 = line.getX(vertexIndex);
          final double y2 = line.getY(vertexIndex);
          final boolean inside2 = x2 <= maxX;
          if (inside1 != inside2) {
            if (x1 != maxX && x2 != maxX) {
              final double newY = y1 + (y2 - y1) * (maxX - x1) / (x2 - x1);
              newLine.appendVertex(maxX, newY);
            }
          }
          if (inside2) {
            newLine.appendVertex(x2, y2);
            for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
              final double coordinate = line.getCoordinate(vertexIndex, axisIndex);
              newLine.setCoordinate(vertexIndex, axisIndex, coordinate);
            }
          }

          inside1 = inside2;
          x1 = x2;
          y1 = y2;
        }
        newLine.closeRing();
      }
      return newLine.newLinearRing();
    } else {
      return geometryFactory.linearRing();
    }
  }

  @Override
  LinearRing clone();

  /**
   * Returns <code>Dimension.FALSE</code>, since by definition LinearRings do
   * not have a boundary.
   *
   * @return Dimension.FALSE
   */
  @Override
  default int getBoundaryDimension() {
    return Dimension.FALSE;
  }

  /**
   * Get the area of the polygon using the http://en.wikipedia.org/wiki/Shoelace_formula
   *
   * @return The area of the polygon.
   */
  default double getPolygonArea() {
    final int vertexCount = getVertexCount();
    double area;
    if (vertexCount < 3) {
      area = 0;
    } else {
      /**
       * Based on the Shoelace formula.
       * http://en.wikipedia.org/wiki/Shoelace_formula
       */
      double p1x = getX(0);
      double p1y = getY(0);

      final double x0 = p1x;
      double p2x = getX(1) - x0;
      double p2y = getY(1);
      double sum = 0;
      for (int i = 1; i < vertexCount - 1; i++) {
        final double p0y = p1y;
        p1x = p2x;
        p1y = p2y;
        p2x = getX(i + 1) - x0;
        p2y = getY(i + 1);
        sum += p1x * (p0y - p2y);
      }
      area = Math.abs(sum / 2.0);
    }
    return area;
  }

  default double getPolygonArea(final Unit<Area> unit) {
    double area = 0;
    final CoordinateSystem coordinateSystem = getHorizontalCoordinateSystem();
    if (coordinateSystem instanceof GeographicCoordinateSystem) {
      // TODO better algorithm than converting to world mercator
      final GeometryFactory geometryFactory = GeometryFactory.worldMercator();
      final LinearRing ring = as2d(geometryFactory);
      return ring.getPolygonArea(unit);
    } else if (coordinateSystem instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem projectedCoordinateSystem = (ProjectedCoordinateSystem)coordinateSystem;
      final Unit<Length> lengthUnit = projectedCoordinateSystem.getLengthUnit();
      @SuppressWarnings("unchecked")
      final Unit<Area> areaUnit = (Unit<Area>)lengthUnit.multiply(lengthUnit);
      area = getPolygonArea();
      final Quantity<Area> areaMeasure = Quantities.getQuantity(area, areaUnit);
      area = QuantityType.doubleValue(areaMeasure, unit);
    } else {
      area = getPolygonArea();
    }
    return area;
  }

  @Override
  default boolean isValid() {
    return LineString.super.isValid();
  }

  @Override
  default LinearRing newGeometry(final GeometryFactory geometryFactory) {
    return geometryFactory.linearRing(this);
  }

  @Override
  default LinearRingEditor newGeometryEditor() {
    return new LinearRingEditor(this);
  }

  @Override
  default LinearRingEditor newGeometryEditor(final int axisCount) {
    final LinearRingEditor editor = new LinearRingEditor(this);
    editor.setAxisCount(axisCount);
    return editor;
  }

  @Override
  default LinearRing newLineString() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.linearRing(this);
  }

  @Override
  default LinearRing newLineString(final double... coordinates) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final int axisCount = getAxisCount();
    return geometryFactory.linearRing(axisCount, coordinates);
  }

  @Override
  default LineString newLineString(final GeometryFactory geometryFactory, final int axisCount,
    final int vertexCount, final double... coordinates) {
    final GeometryFactory geometryFactoryAxisCount = geometryFactory.convertAxisCount(axisCount);
    return geometryFactoryAxisCount.linearRing(axisCount, vertexCount, coordinates);
  }

  @Override
  default LinearRing newLineStringEmpty() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return newLineStringEmpty(geometryFactory);
  }

  @Override
  default LinearRing newLineStringEmpty(final GeometryFactory geometryFactory) {
    return geometryFactory.linearRing();
  }

  default Polygon newPolygon() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.polygon(this);
  }

  @SuppressWarnings("unchecked")
  @Override
  default <G> G newUsingGeometryFactory(final GeometryFactory factory) {
    if (factory == getGeometryFactory()) {
      return (G)this;
    } else if (isEmpty()) {
      return (G)factory.linearRing();
    } else {
      final double[] coordinates = getCoordinates();
      final int axisCount = getAxisCount();
      return (G)factory.linearRing(axisCount, coordinates);
    }
  }

  default LinearRing normalize(final boolean clockwise) {
    if (isEmpty()) {
      return this;
    } else {
      LinearRing ring = this;
      final int index = minCoordinateIndex(ring);
      if (index > 0) {
        ring = scroll(ring, index);
      }
      if (ring.isCounterClockwise() == clockwise) {
        return ring.reverse();
      } else {
        return ring;
      }
    }
  }

  @Override
  default LinearRing prepare() {
    return new PreparedLinearRing(this);
  }

  @Override
  default LinearRing removeDuplicatePoints() {
    if (isEmpty()) {
      return this;
    } else {
      final LineStringEditor editor = newGeometryEditor();
      final int axisCount = getAxisCount();
      double previousX = getX(0);
      double previousY = getY(0);
      int newVertexIndex = 1;

      final int vertexCount = getVertexCount();

      for (int vertexIndex = 1; vertexIndex < vertexCount; vertexIndex++) {
        final double x = getX(vertexIndex);
        final double y = getY(vertexIndex);
        if (x != previousX || y != previousY) {
          if (newVertexIndex != vertexIndex) {
            editor.setX(newVertexIndex, x);
            editor.setY(newVertexIndex, y);
            for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
              final double coordinate = getCoordinate(newVertexIndex, axisIndex);
              editor.setCoordinate(newVertexIndex, axisIndex, coordinate);
            }
          }
          newVertexIndex++;
        }

        previousX = x;
        previousY = y;
      }
      editor.setVertexCount(newVertexIndex);
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (newVertexIndex < 3) {
        return geometryFactory.linearRing();
      } else if (editor.isModified()) {
        return editor.newLinearRing();
      } else {
        return this;
      }
    }
  }

  @Override
  default LinearRing reverse() {
    return (LinearRing)LineString.super.reverse();
  }
}
