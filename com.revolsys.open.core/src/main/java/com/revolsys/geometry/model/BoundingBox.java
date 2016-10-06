package com.revolsys.geometry.model;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.ProjectionFactory;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.model.impl.LineStringDouble;
import com.revolsys.geometry.model.impl.PointDoubleGf;
import com.revolsys.geometry.util.BoundingBoxUtil;
import com.revolsys.io.FileUtil;
import com.revolsys.logging.Logs;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.wkt.WktParser;
import com.revolsys.util.Emptyable;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Property;
import com.revolsys.util.number.Doubles;

public interface BoundingBox extends Emptyable, GeometryFactoryProxy, Cloneable {
  BoundingBox EMPTY = new BoundingBoxDoubleGf();

  public static boolean isEmpty(final double minX, final double maxX) {
    if (Double.isNaN(minX)) {
      return true;
    } else if (Double.isNaN(maxX)) {
      return true;
    } else {
      return maxX < minX;
    }
  }

  static BoundingBox newBoundingBox(final Object value) {
    if (value == null) {
      return EMPTY;
    } else if (value instanceof BoundingBox) {
      return (BoundingBox)value;
    } else if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      return geometry.getBoundingBox();
    } else {
      final String string = DataTypes.toString(value);
      return BoundingBox.newBoundingBox(string);
    }
  }

  static BoundingBox newBoundingBox(final String wkt) {
    if (Property.hasValue(wkt)) {
      try {
        GeometryFactory geometryFactory = null;
        final PushbackReader reader = new PushbackReader(new StringReader(wkt));
        WktParser.skipWhitespace(reader);
        if (WktParser.hasText(reader, "SRID=")) {
          final Integer srid = WktParser.parseInteger(reader);
          if (srid != null) {
            geometryFactory = GeometryFactory.floating(srid, 2);
          }
          WktParser.hasText(reader, ";");
        }
        if (WktParser.hasText(reader, "BBOX(")) {
          final double x1 = WktParser.parseDouble(reader);
          if (WktParser.hasText(reader, ",")) {
            WktParser.skipWhitespace(reader);
            final double y1 = WktParser.parseDouble(reader);
            WktParser.skipWhitespace(reader);
            final double x2 = WktParser.parseDouble(reader);
            if (WktParser.hasText(reader, ",")) {
              WktParser.skipWhitespace(reader);
              final double y2 = WktParser.parseDouble(reader);
              return new BoundingBoxDoubleGf(geometryFactory, 2, x1, y1, x2, y2);
            } else {
              throw new IllegalArgumentException(
                "Expecting a ',' not " + FileUtil.getString(reader));
            }

          } else {
            throw new IllegalArgumentException("Expecting a ',' not " + FileUtil.getString(reader));
          }
        } else if (WktParser.hasText(reader, "BBOX EMPTY")) {
          return new BoundingBoxDoubleGf(geometryFactory);
        }
      } catch (final IOException e) {
        throw Exceptions.wrap("Error reading WKT:" + wkt, e);
      }
    }

    return EMPTY;
  }

  static String toString(final BoundingBox boundingBox) {
    final StringBuilder s = new StringBuilder();
    final int srid = boundingBox.getCoordinateSystemId();
    if (srid > 0) {
      s.append("SRID=");
      s.append(srid);
      s.append(";");
    }
    if (boundingBox.isEmpty()) {
      s.append("BBOX EMPTY");
    } else {
      s.append("BBOX");
      final int axisCount = boundingBox.getAxisCount();
      if (axisCount == 3) {
        s.append(" Z");
      } else if (axisCount == 4) {
        s.append(" ZM");
      } else if (axisCount != 2) {
        s.append(" ");
        s.append(axisCount);
      }
      s.append("(");
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        if (axisIndex > 0) {
          s.append(',');
        }
        s.append(Doubles.toString(boundingBox.getMin(axisIndex)));
      }
      s.append(' ');
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        if (axisIndex > 0) {
          s.append(',');
        }
        s.append(Doubles.toString(boundingBox.getMax(axisIndex)));
      }
      s.append(')');
    }
    return s.toString();
  }

  default BoundingBox clipToCoordinateSystem() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    if (coordinateSystem == null) {
      return this;
    } else {
      final BoundingBox areaBoundingBox = coordinateSystem.getAreaBoundingBox();
      return intersection(areaBoundingBox);
    }
  }

  BoundingBox clone();

  default BoundingBox convert(final GeometryFactory geometryFactory) {
    final GeometryFactory factory = getGeometryFactory();
    if (geometryFactory == null) {
      return this;
    } else if (factory == geometryFactory) {
      return this;
    } else if (factory == null || factory.getCoordinateSystem() == null) {
      return newBoundingBox(geometryFactory, getAxisCount(), getBounds());
    } else if (isEmpty()) {
      return newBoundingBoxEmpty();
    } else {
      final CoordinatesOperation operation = ProjectionFactory.getCoordinatesOperation(factory,
        geometryFactory);
      if (operation != null) {

        double xStep = getWidth() / 10;
        double yStep = getHeight() / 10;
        final double scaleXY = geometryFactory.getScaleXY();
        if (scaleXY > 0) {
          if (xStep < 1 / scaleXY) {
            xStep = 1 / scaleXY;
          }
          if (yStep < 1 / scaleXY) {
            yStep = 1 / scaleXY;
          }
        }

        final int axisCount = getAxisCount();

        final double minX = getMinX();
        final double maxX = getMaxX();
        final double minY = getMinY();
        final double maxY = getMaxY();

        final double[] bounds = getBounds();
        bounds[0] = Double.NaN;
        bounds[1] = Double.NaN;
        bounds[axisCount] = Double.NaN;
        bounds[axisCount + 1] = Double.NaN;

        final double[] to = new double[2];
        expand(geometryFactory, bounds, operation, to, minX, minY);
        expand(geometryFactory, bounds, operation, to, minX, maxY);
        expand(geometryFactory, bounds, operation, to, minX, minY);
        expand(geometryFactory, bounds, operation, to, maxX, minY);

        if (xStep != 0) {
          for (double x = minX + xStep; x < maxX; x += xStep) {
            expand(geometryFactory, bounds, operation, to, x, minY);
            expand(geometryFactory, bounds, operation, to, x, maxY);
          }
        }
        if (yStep != 0) {
          for (double y = minY + yStep; y < maxY; y += yStep) {
            expand(geometryFactory, bounds, operation, to, minX, y);
            expand(geometryFactory, bounds, operation, to, maxX, y);
          }
        }
        return newBoundingBox(geometryFactory, axisCount, bounds);
      } else {
        return this;
      }
    }
  }

  default BoundingBox convert(GeometryFactory geometryFactory, final int axisCount) {
    final GeometryFactory sourceGeometryFactory = getGeometryFactory();
    if (geometryFactory == null || sourceGeometryFactory == null) {
      return this;
    } else {
      geometryFactory = geometryFactory.convertAxisCount(axisCount);
      boolean copy = false;
      if (geometryFactory != null && sourceGeometryFactory != geometryFactory) {
        final int srid = getCoordinateSystemId();
        final int srid2 = geometryFactory.getCoordinateSystemId();
        if (srid <= 0) {
          if (srid2 > 0) {
            copy = true;
          }
        } else if (srid != srid2) {
          copy = true;
        }
        if (!copy) {
          for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
            final double scale = sourceGeometryFactory.getScale(axisIndex);
            final double scale1 = geometryFactory.getScale(axisIndex);
            if (!Doubles.equal(scale, scale1)) {
              copy = true;
            }
          }
        }
      }
      if (copy) {
        return convert(geometryFactory);
      } else {
        return this;
      }
    }
  }

  default boolean coveredBy(final double... bounds) {
    final double minX1 = bounds[0];
    final double minY1 = bounds[1];
    final double maxX1 = bounds[2];
    final double maxY1 = bounds[3];
    final double minX2 = getMinX();
    final double minY2 = getMinY();
    final double maxX2 = getMaxX();
    final double maxY2 = getMaxY();
    return BoundingBoxUtil.covers(minX1, minY1, maxX1, maxY1, minX2, minY2, maxX2, maxY2);
  }

  /**
   * Tests if the <code>BoundingBoxDoubleGf other</code>
   * lies wholely inside this <code>BoundingBoxDoubleGf</code> (inclusive of the boundary).
   *
   *@param  other the <code>BoundingBoxDoubleGf</code> to check
   *@return true if this <code>BoundingBoxDoubleGf</code> covers the <code>other</code>
   */
  default boolean covers(BoundingBox other) {
    if (other == null || isEmpty() || other.isEmpty()) {
      return false;
    } else {
      other = other.convert(getGeometryFactory());
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();

      return other.coveredBy(minX, minY, maxX, maxY);
    }
  }

  /**
   * Tests if the given point lies in or on the envelope.
   *
   *@param  x  the x-coordinate of the point which this <code>BoundingBoxDoubleGf</code> is
   *      being checked for containing
   *@param  y  the y-coordinate of the point which this <code>BoundingBoxDoubleGf</code> is
   *      being checked for containing
   *@return    <code>true</code> if <code>(x, y)</code> lies in the interior or
   *      on the boundary of this <code>BoundingBoxDoubleGf</code>.
   */
  default boolean covers(final double x, final double y) {
    if (isEmpty()) {
      return false;
    } else {
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();

      return BoundingBoxUtil.covers(minX, minY, maxX, maxY, x, y, x, y);
    }
  }

  default boolean covers(final Geometry geometry) {
    if (geometry == null) {
      return false;
    } else {
      final BoundingBox boundingBox = geometry.getBoundingBox();
      return covers(boundingBox);
    }
  }

  /**
   * Tests if the given point lies in or on the envelope.
   *
   *@param  p  the point which this <code>BoundingBoxDoubleGf</code> is
   *      being checked for containing
   *@return    <code>true</code> if the point lies in the interior or
   *      on the boundary of this <code>BoundingBoxDoubleGf</code>.
   */
  default boolean covers(final Point point) {
    if (point == null || point.isEmpty()) {
      return false;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Point projectedPoint = point.convertGeometry(geometryFactory);
      final double x = projectedPoint.getX();
      final double y = projectedPoint.getY();
      return covers(x, y);
    }
  }

  /**
   * Computes the distance between this and another
   * <code>BoundingBoxDoubleGf</code>.
   * The distance between overlapping Envelopes is 0.  Otherwise, the
   * distance is the Euclidean distance between the closest points.
   */
  default double distance(BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    boundingBox = boundingBox.convert(geometryFactory);
    final double minX = getMinX();
    final double minY = getMinY();
    final double maxX = getMaxX();
    final double maxY = getMaxY();

    final double minX2 = boundingBox.getMinX();
    final double minY2 = boundingBox.getMinY();
    final double maxX2 = boundingBox.getMaxX();
    final double maxY2 = boundingBox.getMaxY();

    if (isEmpty(minX, maxX) || isEmpty(minX2, maxX2)) {
      // Empty
      return Double.MAX_VALUE;
    } else if (!(minX2 > maxX || maxX2 < minX || minY2 > maxY || maxY2 < minY)) {
      // Intersects
      return 0;
    } else {
      double dx;
      if (maxX < minX2) {
        dx = minX2 - maxX;
      } else {
        if (minX > maxX2) {
          dx = minX - maxX2;
        } else {
          dx = 0;
        }
      }

      double dy;
      if (maxY < minY2) {
        dy = minY2 - maxY;
      } else if (minY > maxY2) {
        dy = minY - maxY2;
      } else {
        dy = 0;
      }

      if (dx == 0.0) {
        return dy;
      } else if (dy == 0.0) {
        return dx;
      } else {
        return Math.sqrt(dx * dx + dy * dy);
      }
    }
  }

  default double distance(final Geometry geometry) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    return distance(boundingBox);
  }

  /**
   * Computes the distance between this and another
   * <code>BoundingBoxDoubleGf</code>.
   * The distance between overlapping Envelopes is 0.  Otherwise, the
   * distance is the Euclidean distance between the closest points.
   */
  default double distance(Point point) {
    point = point.convertGeometry(getGeometryFactory());
    if (intersects(point)) {
      return 0;
    } else {
      final double x = point.getX();
      final double y = point.getY();

      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();

      double dx = 0.0;
      if (maxX < x) {
        dx = x - maxX;
      } else if (minX > x) {
        dx = minX - x;
      }

      double dy = 0.0;
      if (maxY < y) {
        dy = y - maxY;
      } else if (minY > y) {
        dy = minY - y;
      }

      // if either is zero, the envelopes overlap either vertically or
      // horizontally
      if (dx == 0.0) {
        return dy;
      }
      if (dy == 0.0) {
        return dx;
      }
      return Math.sqrt(dx * dx + dy * dy);
    }
  }

  default boolean equals(final BoundingBox boundingBox) {
    if (isEmpty()) {
      return boundingBox.isEmpty();
    } else if (boundingBox.isEmpty()) {
      return false;
    } else if (getCoordinateSystemId() == boundingBox.getCoordinateSystemId()) {
      if (getMaxX() == boundingBox.getMaxX()) {
        if (getMaxY() == boundingBox.getMaxY()) {
          if (getMinX() == boundingBox.getMinX()) {
            if (getMinY() == boundingBox.getMinY()) {
              return true;
            }
          }
        }
      }

    }
    return false;
  }

  /**
   * Return a new bounding box expanded by delta.
   *
   * @param delta
   * @return
   */
  default BoundingBox expand(final double delta) {
    return expand(delta, delta);
  }

  /**
   * Return a new bounding box expanded by deltaX, deltaY.
   *
   * @param delta
   * @return
   */
  default BoundingBox expand(final double deltaX, final double deltaY) {
    if (isEmpty() || deltaX == 0 && deltaY == 0) {
      return this;
    } else {
      final double x1 = getMinX() - deltaX;
      final double x2 = getMaxX() + deltaX;
      final double y1 = getMinY() - deltaY;
      final double y2 = getMaxY() + deltaY;

      if (x1 > x2 || y1 > y2) {
        return newBoundingBoxEmpty();
      } else {
        return newBoundingBox(x1, y1, x2, y2);
      }
    }
  }

  default void expand(final GeometryFactory geometryFactory, final double[] bounds,
    final CoordinatesOperation operation, final double[] to, final double... from) {

    operation.perform(2, from, 2, to);
    BoundingBoxUtil.expand(geometryFactory, bounds, to);
  }

  default BoundingBox expand(final Point point) {
    if (isEmpty()) {
      return newBoundingBox(point);
    } else {
      final double x = point.getX();
      final double y = point.getY();

      double minX = getMinX();
      double maxX = getMaxX();
      double minY = getMinY();
      double maxY = getMaxY();

      if (x < minX) {
        minX = x;
      }
      if (x > maxX) {
        maxX = x;
      }
      if (y < minY) {
        minY = y;
      }
      if (y > maxY) {
        maxY = y;
      }
      return newBoundingBox(minX, minY, maxX, maxY);
    }
  }

  default BoundingBox expandPercent(final double factor) {
    return expandPercent(factor, factor);
  }

  default BoundingBox expandPercent(final double factorX, final double factorY) {
    if (isEmpty()) {
      return this;
    } else {
      final double deltaX = getWidth() * factorX / 2;
      final double deltaY = getHeight() * factorY / 2;
      return expand(deltaX, deltaY);
    }
  }

  default BoundingBox expandToInclude(final BoundingBox other) {
    if (other == null || other.isEmpty()) {
      return this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final BoundingBox convertedOther = other.convert(geometryFactory);
      if (isEmpty()) {
        return convertedOther;
      } else if (covers(convertedOther)) {
        return this;
      } else {
        final double minX = Math.min(getMinX(), convertedOther.getMinX());
        final double maxX = Math.max(getMaxX(), convertedOther.getMaxX());
        final double minY = Math.min(getMinY(), convertedOther.getMinY());
        final double maxY = Math.max(getMaxY(), convertedOther.getMaxY());
        return newBoundingBox(minX, minY, maxX, maxY);
      }
    }
  }

  default BoundingBox expandToInclude(final double... coordinates) {
    if (coordinates == null || coordinates.length < 2) {
      return this;
    } else {
      final double[] bounds;
      final int axisCount = getAxisCount();
      if (isEmpty()) {
        bounds = BoundingBoxUtil.newBounds(axisCount);
      } else {
        bounds = getBounds();
      }
      BoundingBoxUtil.expand(bounds, axisCount, coordinates);
      return newBoundingBox(axisCount, bounds);
    }
  }

  default BoundingBox expandToInclude(final Geometry geometry) {
    if (geometry == null || geometry.isEmpty()) {
      return this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Geometry convertedGeometry = geometry.convertGeometry(geometryFactory);
      final BoundingBox box = convertedGeometry.getBoundingBox();
      return expandToInclude(box);
    }
  }

  default BoundingBox expandToInclude(final Point point) {
    return expandToInclude((Geometry)point);
  }

  default BoundingBox expandToInclude(final Record object) {
    if (object != null) {
      final Geometry geometry = object.getGeometry();
      return expandToInclude(geometry);
    }
    return this;
  }

  /**
   * Gets the area of this envelope.
   *
   * @return the area of the envelope
   * @return 0.0 if the envelope is null
   */
  default double getArea() {
    if (getAxisCount() < 2) {
      return 0;
    } else {
      final double width = getWidth();
      final double height = getHeight();
      return width * height;
    }
  }

  /**
   * Get the aspect ratio x:y.
   *
   * @return The aspect ratio.
   */
  default double getAspectRatio() {
    final double width = getWidth();
    final double height = getHeight();
    final double aspectRatio = width / height;
    return aspectRatio;
  }

  int getAxisCount();

  default Point getBottomLeftPoint() {
    return getGeometryFactory().point(getMinX(), getMinY());
  }

  default Point getBottomRightPoint() {
    return getGeometryFactory().point(getMaxX(), getMinY());
  }

  double[] getBounds();

  default double[] getBounds(final int axisCount) {
    if (isEmpty()) {
      return null;
    } else {
      final double[] bounds = new double[2 * axisCount];
      for (int i = 0; i < axisCount; i++) {
        bounds[i] = getMin(i);
        bounds[i + axisCount] = getMax(i);
      }
      return bounds;
    }
  }

  default Point getCentre() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isEmpty()) {
      return geometryFactory.point();
    } else {
      final double centreX = getCentreX();
      final double centreY = getCentreY();
      return geometryFactory.point(centreX, centreY);
    }
  }

  default double getCentreX() {
    return getMinX() + getWidth() / 2;
  }

  default double getCentreY() {
    return getMinY() + getHeight() / 2;
  }

  /**
   * Get the geometry factory.
   *
   * @return The geometry factory.
   */
  @Override
  default CoordinateSystem getCoordinateSystem() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      return null;
    } else {
      return geometryFactory.getCoordinateSystem();
    }
  }

  /**
   * maxX,minY
   * minX,minY
   * minX,maxY
   * maxX,maxY
   */
  default Point getCornerPoint(int index) {
    if (isEmpty()) {
      return null;
    } else {
      final double minX = getMinX();
      final double maxX = getMaxX();
      final double minY = getMinY();
      final double maxY = getMaxY();
      index = index % 4;
      switch (index) {
        case 0:
          return new PointDoubleGf(getGeometryFactory(), maxX, minY);
        case 1:
          return new PointDoubleGf(getGeometryFactory(), minX, minY);
        case 2:
          return new PointDoubleGf(getGeometryFactory(), minX, maxY);
        default:
          return new PointDoubleGf(getGeometryFactory(), maxX, maxY);
      }
    }
  }

  default LineString getCornerPoints() {
    final double minX = getMinX();
    final double maxX = getMaxX();
    final double minY = getMinY();
    final double maxY = getMaxY();
    return new LineStringDouble(2, maxX, minY, minX, minY, minX, maxY, maxX, maxY);
  }

  /**
   *  Returns the difference between the maximum and minimum y values.
   *
   *@return    max y - min y, or 0 if this is a null <code>BoundingBoxDoubleGf</code>
   */
  default double getHeight() {
    if (getAxisCount() < 2) {
      return 0;
    } else {
      return getMaxY() - getMinY();
    }
  }

  default Measure<Length> getHeightLength() {
    final double height = getHeight();
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    if (coordinateSystem == null) {
      return Measure.valueOf(height, SI.METRE);
    } else {
      return Measure.valueOf(height, coordinateSystem.getLengthUnit());
    }
  }

  double getMax(int i);

  default <Q extends Quantity> Measurable<Q> getMaximum(final int axisIndex) {
    final Unit<Q> unit = getUnit();
    final double max = this.getMax(axisIndex);
    return Measure.valueOf(max, unit);
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  default <Q extends Quantity> double getMaximum(final int axisIndex, final Unit convertUnit) {
    final Measurable<Quantity> max = getMaximum(axisIndex);
    return max.doubleValue(convertUnit);
  }

  /**
   *  Returns the <code>BoundingBoxDoubleGf</code>s maximum x-value. min x > max x
   *  indicates that this is a null <code>BoundingBoxDoubleGf</code>.
   *
   *@return    the maximum x-coordinate
   */
  default double getMaxX() {
    return getMax(0);
  }

  /**
   *  Returns the <code>BoundingBoxDoubleGf</code>s maximum y-value. min y > max y
   *  indicates that this is a null <code>BoundingBoxDoubleGf</code>.
   *
   *@return    the maximum y-coordinate
   */
  default double getMaxY() {
    return getMax(1);
  }

  double getMin(int i);

  default <Q extends Quantity> Measurable<Q> getMinimum(final int axisIndex) {
    final Unit<Q> unit = getUnit();
    final double min = this.getMin(axisIndex);
    return Measure.valueOf(min, unit);
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  default <Q extends Quantity> double getMinimum(final int axisIndex, final Unit convertUnit) {
    final Measurable<Quantity> min = getMinimum(axisIndex);
    return min.doubleValue(convertUnit);
  }

  /**
   *  Returns the <code>BoundingBoxDoubleGf</code>s minimum x-value. min x > max x
   *  indicates that this is a null <code>BoundingBoxDoubleGf</code>.
   *
   *@return    the minimum x-coordinate
   */
  default double getMinX() {
    return getMin(0);
  }

  /**
   *  Returns the <code>BoundingBoxDoubleGf</code>s minimum y-value. min y > max y
   *  indicates that this is a null <code>BoundingBoxDoubleGf</code>.
   *
   *@return    the minimum y-coordinate
   */
  default double getMinY() {
    return getMin(1);
  }

  default Point getRandomPointWithin() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final double x = getMinX() + getWidth() * Math.random();
    final double y = getMinY() + getHeight() * Math.random();
    return geometryFactory.point(x, y);
  }

  default Point getTopLeftPoint() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.point(getMinX(), getMaxY());
  }

  default Point getTopRightPoint() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.point(getMaxX(), getMaxY());
  }

  @SuppressWarnings("unchecked")
  default <Q extends Quantity> Unit<Q> getUnit() {
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    if (coordinateSystem == null) {
      return (Unit<Q>)SI.METRE;
    } else {
      return coordinateSystem.<Q> getUnit();
    }
  }

  /**
   *  Returns the difference between the maximum and minimum x values.
   *
   *@return    max x - min x, or 0 if this is a null <code>BoundingBoxDoubleGf</code>
   */
  default double getWidth() {
    if (getAxisCount() < 2) {
      return 0;
    } else {
      final double minX = getMinX();
      final double maxX = getMaxX();

      return maxX - minX;
    }
  }

  default Measure<Length> getWidthLength() {
    final double width = getWidth();
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    if (coordinateSystem == null) {
      return Measure.valueOf(width, SI.METRE);
    } else {
      return Measure.valueOf(width, coordinateSystem.getLengthUnit());
    }
  }

  /**
   * Computes the intersection of two {@link BoundingBoxDoubleGf}s.
   *
   * @param env the envelope to intersect with
   * @return a new BoundingBox representing the intersection of the envelopes (this will be
   * the null envelope if either argument is null, or they do not intersect
   */
  default BoundingBox intersection(final BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final BoundingBox convertedBoundingBox = boundingBox.convert(geometryFactory);
    if (isEmpty() || convertedBoundingBox.isEmpty() || !intersects(convertedBoundingBox)) {
      return newBoundingBoxEmpty();
    } else {
      final double intMinX = Math.max(getMinX(), convertedBoundingBox.getMinX());
      final double intMinY = Math.max(getMinY(), convertedBoundingBox.getMinY());
      final double intMaxX = Math.min(getMaxX(), convertedBoundingBox.getMaxX());
      final double intMaxY = Math.min(getMaxY(), convertedBoundingBox.getMaxY());
      return newBoundingBox(intMinX, intMinY, intMaxX, intMaxY);
    }
  }

  /**
   *  Check if the region defined by <code>other</code>
   *  overlaps (intersects) the region of this <code>BoundingBoxDoubleGf</code>.
   *
   *@param  other  the <code>BoundingBoxDoubleGf</code> which this <code>BoundingBoxDoubleGf</code> is
   *          being checked for overlapping
   *@return        <code>true</code> if the <code>BoundingBoxDoubleGf</code>s overlap
   */
  default boolean intersects(final BoundingBox other) {
    if (isEmpty() || other.isEmpty()) {
      return false;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final BoundingBox convertedBoundingBox = other.convert(geometryFactory, 2);
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();

      final double minX2 = convertedBoundingBox.getMinX();
      final double minY2 = convertedBoundingBox.getMinY();
      final double maxX2 = convertedBoundingBox.getMaxX();
      final double maxY2 = convertedBoundingBox.getMaxY();
      return !(minX2 > maxX || maxX2 < minX || minY2 > maxY || maxY2 < minY);
    }
  }

  /**
   *  Check if the point <code>(x, y)</code>
   *  overlaps (lies inside) the region of this <code>BoundingBoxDoubleGf</code>.
   *
   *@param  x  the x-ordinate of the point
   *@param  y  the y-ordinate of the point
   *@return        <code>true</code> if the point overlaps this <code>BoundingBoxDoubleGf</code>
   */
  default boolean intersects(final double x, final double y) {
    if (isEmpty()) {
      return false;
    } else {
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();
      return !(x > maxX || x < minX || y > maxY || y < minY);
    }
  }

  default boolean intersects(final double minX2, final double minY2, final double maxX2,
    final double maxY2) {
    final double minX = getMinX();
    final double minY = getMinY();
    final double maxX = getMaxX();
    final double maxY = getMaxY();
    return BoundingBoxUtil.intersects(minX, minY, maxX, maxY, minX2, minY2, maxX2, maxY2);
  }

  default boolean intersects(final Geometry geometry) {
    return geometry.intersects(this);
  }

  /**
   *  Check if the point <code>p</code>
   *  overlaps (lies inside) the region of this <code>BoundingBoxDoubleGf</code>.
   *
   *@param  p  the <code>Coordinate</code> to be tested
   *@return        <code>true</code> if the point overlaps this <code>BoundingBoxDoubleGf</code>
   */
  default boolean intersects(final Point point) {
    return point.intersects(this);
  }

  @Override
  default boolean isEmpty() {
    final double minX = getMinX();
    final double maxX = getMaxX();
    if (Double.isNaN(minX)) {
      return true;
    } else if (Double.isNaN(maxX)) {
      return true;
    } else {
      return maxX < minX;
    }
  }

  default boolean isWithinDistance(final BoundingBox boundingBox, final double maxDistance) {
    final double distance = boundingBox.distance(boundingBox);
    if (distance < maxDistance) {
      return true;
    } else {
      return false;
    }
  }

  default boolean isWithinDistance(final Geometry geometry, final double maxDistance) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    return isWithinDistance(boundingBox, maxDistance);
  }

  /**
   * <p>Construct a new new BoundingBox by moving the min/max x coordinates by xDisplacement and
   * the min/max y coordinates by yDisplacement. If the bounding box is null or the xDisplacement
   * and yDisplacement are 0 then this bounding box will be returned.</p>
   *
   * @param xDisplacement The distance to move the min/max x coordinates.
   * @param yDisplacement The distance to move the min/max y coordinates.
   * @return The moved bounding box.
   */
  default BoundingBox move(final double xDisplacement, final double yDisplacement) {
    if (isEmpty() || xDisplacement == 0 && yDisplacement == 0) {
      return this;
    } else {
      final double x1 = getMinX() + xDisplacement;
      final double x2 = getMaxX() + xDisplacement;
      final double y1 = getMinY() + yDisplacement;
      final double y2 = getMaxY() + yDisplacement;
      return newBoundingBox(x1, y1, x2, y2);
    }
  }

  BoundingBox newBoundingBox(double x, double y);

  BoundingBox newBoundingBox(double minX, double minY, double maxX, double maxY);

  default BoundingBox newBoundingBox(final GeometryFactory geometryFactory, final int axisCount,
    final double[] bounds) {
    return newBoundingBox(axisCount, bounds);
  }

  default BoundingBox newBoundingBox(final int axisCount, final double... bounds) {
    final double minX = bounds[0];
    final double minY = bounds[1];
    final double maxX = bounds[axisCount];
    final double maxY = bounds[axisCount + 1];
    return newBoundingBox(minX, minY, maxX, maxY);
  }

  default BoundingBox newBoundingBox(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    return newBoundingBox(x, y);
  }

  BoundingBox newBoundingBoxEmpty();

  /**
   * Creates a {@link Geometry} with the same extent as the given envelope.
   * The Geometry returned is guaranteed to be valid.
   * To provide this behaviour, the following cases occur:
   * <p>
   * If the <code>BoundingBoxDoubleGf</code> is:
   * <ul>
   * <li>null : returns an empty {@link Point}
   * <li>a point : returns a non-empty {@link Point}
   * <li>a line : returns a two-point {@link LineString}
   * <li>a rectangle : returns a {@link Polygon}> whose points are (minx, miny),
   *  (minx, maxy), (maxx, maxy), (maxx, miny), (minx, miny).
   * </ul>
   *
   *@param  envelope the <code>BoundingBoxDoubleGf</code> to convert
   *@return an empty <code>Point</code> (for null <code>BoundingBoxDoubleGf</code>s),
   *  a <code>Point</code> (when min x = max x and min y = max y) or a
   *      <code>Polygon</code> (in all other cases)
   */

  default Geometry toGeometry() {
    GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      geometryFactory = GeometryFactory.floating(0, 2);
    }
    if (isEmpty()) {
      return geometryFactory.point();
    } else {
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();
      final double width = getWidth();
      final double height = getHeight();
      if (width == 0 && height == 0) {
        return geometryFactory.point(minX, minY);
      } else if (width == 0 || height == 0) {
        return geometryFactory.lineString(2, minX, minY, maxX, maxY);
      } else {
        return geometryFactory.polygon(geometryFactory.linearRing(2, minX, minY, minX, maxY, maxX,
          maxY, maxX, minY, minX, minY));
      }
    }
  }

  default Polygon toPolygon() {
    return toPolygon(100, 100);

  }

  default Polygon toPolygon(final GeometryFactory factory) {
    return toPolygon(factory, 100, 100);
  }

  default Polygon toPolygon(final GeometryFactory factory, final int numSegments) {
    return toPolygon(factory, numSegments, numSegments);
  }

  default Polygon toPolygon(GeometryFactory geometryFactory, int numX, int numY) {
    if (isEmpty()) {
      return geometryFactory.polygon();
    } else {
      final GeometryFactory factory = getGeometryFactory();
      if (geometryFactory == null) {
        if (factory == null) {
          geometryFactory = GeometryFactory.floating(0, 2);
        } else {
          geometryFactory = factory;
        }
      }
      try {
        double minStep = 0.00001;
        final CoordinateSystem coordinateSystem = factory.getCoordinateSystem();
        if (coordinateSystem instanceof ProjectedCoordinateSystem) {
          minStep = 1;
        } else {
          minStep = 0.00001;
        }

        double xStep;
        final double width = getWidth();
        if (numX <= 1) {
          numX = 1;
          xStep = width;
        } else {
          xStep = width / numX;
          if (xStep < minStep) {
            xStep = minStep;
          }
          numX = Math.max(1, (int)Math.ceil(width / xStep));
        }

        double yStep;
        if (numY <= 1) {
          numY = 1;
          yStep = getHeight();
        } else {
          yStep = getHeight() / numY;
          if (yStep < minStep) {
            yStep = minStep;
          }
          numY = Math.max(1, (int)Math.ceil(getHeight() / yStep));
        }

        final double minX = getMinX();
        final double maxX = getMaxX();
        final double minY = getMinY();
        final double maxY = getMaxY();
        final int numCoordinates = 1 + 2 * (numX + numY);
        final double[] coordinates = new double[numCoordinates * 2];
        int i = 0;

        CoordinatesListUtil.setCoordinates(coordinates, 2, i, maxX, minY);
        i++;
        for (int j = 0; j < numX - 1; j++) {
          CoordinatesListUtil.setCoordinates(coordinates, 2, i, maxX - j * xStep, minY);
          i++;
        }
        CoordinatesListUtil.setCoordinates(coordinates, 2, i, minX, minY);
        i++;

        for (int j = 0; j < numY - 1; j++) {
          CoordinatesListUtil.setCoordinates(coordinates, 2, i, minX, minY + j * yStep);
          i++;
        }
        CoordinatesListUtil.setCoordinates(coordinates, 2, i, minX, maxY);
        i++;

        for (int j = 0; j < numX - 1; j++) {
          CoordinatesListUtil.setCoordinates(coordinates, 2, i, minX + j * xStep, maxY);
          i++;
        }

        CoordinatesListUtil.setCoordinates(coordinates, 2, i, maxX, maxY);
        i++;

        for (int j = 0; j < numY - 1; j++) {
          CoordinatesListUtil.setCoordinates(coordinates, 2, i, maxX, minY + (numY - j) * yStep);
          i++;
        }
        CoordinatesListUtil.setCoordinates(coordinates, 2, i, maxX, minY);

        final LinearRing ring = factory.linearRing(2, coordinates);
        final Polygon polygon = factory.polygon(ring);
        if (geometryFactory == null) {
          return polygon;
        } else {
          return (Polygon)polygon.convertGeometry(geometryFactory);
        }
      } catch (final IllegalArgumentException e) {
        Logs.error(this, "Unable to convert to polygon: " + this, e);
        return geometryFactory.polygon();
      }
    }
  }

  default Polygon toPolygon(final int numSegments) {
    return toPolygon(numSegments, numSegments);
  }

  default Polygon toPolygon(final int numX, final int numY) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return toPolygon(geometryFactory, numX, numY);
  }
}
