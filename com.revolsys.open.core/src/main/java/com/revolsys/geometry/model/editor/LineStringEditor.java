package com.revolsys.geometry.model.editor;

import java.util.Arrays;
import java.util.Collections;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.LinearRingDoubleGf;
import com.revolsys.util.number.Doubles;

public class LineStringEditor extends AbstractGeometryEditor<LineStringEditor>
  implements LineString, LinealEditor {
  private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

  private static final long serialVersionUID = 1L;

  private static int hugeCapacity(final int minCapacity) {
    if (minCapacity < 0) {
      throw new OutOfMemoryError();
    }
    return minCapacity > MAX_ARRAY_SIZE ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
  }

  public static LineStringEditor newLineStringEditor(final LineString line) {
    final GeometryFactory geometryFactory = line.getGeometryFactory();
    final int axisCount = line.getAxisCount();
    final double[] coordinates = line.getCoordinates();
    return new LineStringEditor(geometryFactory, axisCount, coordinates);
  }

  private int axisCount;

  private double[] coordinates;

  private LineString line;

  private int vertexCount;

  private BoundingBox boundingBox;

  public LineStringEditor(final AbstractGeometryEditor<?> parentEditor, final LineString line) {
    super(parentEditor, line);
    this.axisCount = line.getAxisCount();
    this.line = line;
    this.vertexCount = line.getVertexCount();
  }

  public LineStringEditor(final GeometryFactory geometryFactory) {
    super(geometryFactory);
    this.axisCount = geometryFactory.getAxisCount();
    this.coordinates = new double[0];
    this.vertexCount = this.coordinates.length / this.axisCount;
  }

  public LineStringEditor(final GeometryFactory geometryFactory, int vertexCapacity) {
    super(geometryFactory);
    if (vertexCapacity < 0) {
      vertexCapacity = 0;
    }
    this.axisCount = geometryFactory.getAxisCount();
    this.coordinates = new double[vertexCapacity * this.axisCount];
    Arrays.fill(this.coordinates, Double.NaN);
    this.vertexCount = 0;
  }

  public LineStringEditor(final GeometryFactory geometryFactory, final int axisCount,
    final double... coordinates) {
    super(geometryFactory.convertAxisCount(axisCount));
    if (axisCount < 2) {
      throw new IllegalArgumentException("axisCount=" + axisCount + " must be >= 2");
    }
    this.axisCount = axisCount;
    if (coordinates == null || coordinates.length == 0) {
      this.coordinates = new double[0];
    } else {
      setCoordinates(coordinates);
    }
    this.vertexCount = this.coordinates.length / axisCount;
  }

  public LineStringEditor(final int axisCount, final int vertexCount) {
    super(GeometryFactory.floating(0, axisCount));
    this.axisCount = axisCount;
    this.coordinates = new double[axisCount * vertexCount];
    this.vertexCount = 0;
  }

  public LineStringEditor(final int axisCount, final int vertexCount, final double... coordinates) {
    super(GeometryFactory.floating(0, axisCount));
    if (coordinates == null || coordinates.length == 0) {
      this.axisCount = 2;
      this.coordinates = new double[0];
      this.vertexCount = 0;
    } else {
      assert axisCount >= 2;
      this.axisCount = (byte)axisCount;
      final int coordinateCount = vertexCount * axisCount;
      if (coordinates.length % axisCount != 0) {
        throw new IllegalArgumentException("coordinates.length=" + coordinates.length
          + " must be a multiple of axisCount=" + axisCount);
      } else if (coordinateCount == coordinates.length) {
        setCoordinates(coordinates);
      } else if (coordinateCount > coordinates.length) {
        throw new IllegalArgumentException("axisCount=" + axisCount + " * vertexCount="
          + vertexCount + " > coordinates.length=" + coordinates.length);
      } else {
        setCoordinates(coordinates);
        this.vertexCount = 0;
      }
    }
  }

  public LineStringEditor(final LineString line) {
    this(null, line);
  }

  public LineStringEditor appendVertex(final double... coordinates) {
    final int vertexIndex = getVertexCount();
    setVertexCount(vertexIndex + 1);

    return setVertex(vertexIndex, coordinates);
  }

  public LineStringEditor appendVertex(final double x, final double y) {
    final int vertexIndex = getVertexCount();
    setVertexCount(vertexIndex + 1);
    return setVertex(vertexIndex, x, y);
  }

  public LineStringEditor appendVertex(final double x, final double y, final double z) {
    final int vertexIndex = getVertexCount();
    setVertexCount(vertexIndex + 1);
    return setVertex(vertexIndex, x, y, z);
  }

  @Override
  public LineStringEditor appendVertex(final int[] geometryId, final Point point) {
    if (geometryId == null || geometryId.length == 0) {
      appendVertex(point);
    }
    return this;
  }

  public LineStringEditor appendVertex(final Point point) {
    if (point == null || point.isEmpty()) {
      return this;
    } else {
      final int vertexIndex = getVertexCount();
      setVertexCount(vertexIndex + 1);
      return setVertex(vertexIndex, point);
    }
  }

  public LineStringEditor appendVertex(final Point point, final boolean allowRepeated) {
    if (point == null || point.isEmpty()) {
      return this;
    } else if (allowRepeated || !equalsVertex(getLastVertexIndex(), point)) {
      return appendVertex(point);
    } else {
      return this;
    }
  }

  public LineStringEditor appendVertices(final Geometry points) {
    ensureCapacity(this.vertexCount + points.getVertexCount());
    final Iterable<? extends Point> vertices = points.vertices();
    appendVertices(vertices);
    return this;
  }

  public LineStringEditor appendVertices(final Iterable<? extends Point> points) {
    for (final Point point : points) {
      appendVertex(point);
    }
    return this;
  }

  public void clear() {
    this.vertexCount = 0;
  }

  @Override
  public LineStringEditor clone() {
    final LineStringEditor clone = (LineStringEditor)super.clone();
    if (clone.coordinates != null) {
      clone.coordinates = this.coordinates.clone();
    }
    return clone;
  }

  public void deleteVertex(final int vertexIndex) {
    if (!isEmpty()) {
      final int vertexCount = getVertexCount();
      if (vertexIndex >= 0 && vertexIndex < vertexCount) {
        final int axisCount = getAxisCount();

        final int beforeLength = vertexIndex * axisCount;
        final int sourceIndex = (vertexIndex + 1) * axisCount;
        final int length = (vertexCount - vertexIndex - 1) * axisCount;
        final double[] oldCoordinates;
        if (this.coordinates == null) {
          if (this.line == null) {
            return;
          }
          oldCoordinates = this.line.getCoordinates(axisCount);
          setModified(true);
          this.coordinates = new double[axisCount * (vertexCount - 1)];
          System.arraycopy(oldCoordinates, 0, this.coordinates, 0, beforeLength);
          System.arraycopy(oldCoordinates, sourceIndex, this.coordinates, beforeLength, length);
          this.vertexCount--;
        } else {
          oldCoordinates = this.coordinates;
          System.arraycopy(oldCoordinates, sourceIndex, this.coordinates, beforeLength, length);
          Arrays.fill(this.coordinates, this.coordinates.length - axisCount,
            this.coordinates.length, Double.NaN);
          this.vertexCount--;
        }
      } else {
        throw new IllegalArgumentException("Vertex index must be between 0 and " + vertexCount);
      }
    }
  }

  @Override
  public LineStringEditor deleteVertex(final int[] vertexId) {
    final int vertexIndex = getVertexId(vertexId);
    deleteVertex(vertexIndex);
    return this;
  }

  @Override
  public Iterable<LineStringEditor> editors() {
    return Collections.singletonList(this);
  }

  private double[] ensureCapacity(final int vertexCount) {
    if (vertexCount < this.vertexCount) {
      return getCoordinatesModified();
    } else {
      if (this.coordinates == null) {
        setModified(true);
        this.coordinates = new double[vertexCount * this.axisCount];
        if (this.line != null) {
          this.line.copyCoordinates(this.axisCount, Double.NaN, this.coordinates, 0);
        }
      } else {
        final int coordinateCount = vertexCount * this.axisCount;
        if (coordinateCount - this.coordinates.length > 0) {
          grow(coordinateCount);
        }
      }
    }
    return this.coordinates;
  }

  @Override
  public boolean equalsVertex(final int axisCount, final int vertexIndex, final Point point) {
    return LineString.super.equalsVertex(axisCount, vertexIndex, point);
  }

  @Override
  public boolean equalsVertex(final int axisCount, final int[] geometryId, final int vertexIndex,
    final Point point) {
    if (geometryId == null || geometryId.length == 0) {
      return equalsVertex(axisCount, vertexIndex, point);
    } else {
      return false;
    }
  }

  @Override
  public boolean equalsVertex(final int axisCount, final int[] vertexId, final Point point) {
    final int vertexIndex = getVertexId(vertexId);
    return equalsVertex(axisCount, vertexIndex, point);
  }

  @Override
  public int getAxisCount() {
    return this.axisCount;
  }

  @Override
  public BoundingBox getBoundingBox() {
    BoundingBox boundingBox = this.boundingBox;
    if (boundingBox == null) {
      this.boundingBox = boundingBox = newBoundingBox();
    }
    return boundingBox;
  }

  @Override
  public double getCoordinate(final int vertexIndex, final int axisIndex) {
    if (this.coordinates == null) {
      if (this.line != null) {
        return this.line.getCoordinate(vertexIndex, axisIndex);
      }
    } else {
      final int axisCount = this.axisCount;
      if (isInVertexRange(vertexIndex) && axisIndex < axisCount) {
        return this.coordinates[vertexIndex * axisCount + axisIndex];
      }
    }
    return Double.NaN;
  }

  @Override
  public double[] getCoordinates() {
    if (this.coordinates == null) {
      if (this.line == null) {
        return null;
      } else {
        return this.line.getCoordinates();
      }
    } else {
      final double[] coordinates = new double[this.vertexCount * this.axisCount];
      System.arraycopy(this.coordinates, 0, coordinates, 0, coordinates.length);
      return coordinates;
    }
  }

  private double[] getCoordinatesModified() {
    if (this.coordinates == null) {
      setModified(true);

      this.coordinates = new double[this.vertexCount * this.axisCount];
      if (this.line != null) {
        this.line.copyCoordinates(this.axisCount, Double.NaN, this.coordinates, 0);
      }
    }
    return this.coordinates;
  }

  public LineString getOriginalGeometry() {
    return this.line;
  }

  @Override
  public int getVertexCount() {
    return this.vertexCount;
  }

  @Override
  public int getVertexCount(final int[] geometryId, final int idLength) {
    if (geometryId == null || idLength == 0) {
      return this.vertexCount;
    } else {
      return 0;
    }
  }

  private int getVertexId(final int[] vertexId) {
    if (vertexId == null || vertexId.length != 1) {
      throw new IllegalArgumentException("Geometry id's for " + getGeometryType()
        + " must have length 1. " + Arrays.toString(vertexId));
    } else {
      return vertexId[0];
    }
  }

  @Override
  public double getX(final int vertexIndex) {
    if (this.coordinates == null) {
      if (this.line != null) {
        return this.line.getX(vertexIndex);
      }
    } else {
      final int axisCount = this.axisCount;
      if (isInVertexRange(vertexIndex)) {
        return this.coordinates[vertexIndex * axisCount];
      }
    }
    return Double.NaN;
  }

  @Override
  public double getY(final int vertexIndex) {
    if (this.coordinates == null) {
      if (this.line != null) {
        return this.line.getX(vertexIndex);
      }
    } else {
      if (isInVertexRange(vertexIndex)) {
        return this.coordinates[vertexIndex * this.axisCount + Y];
      }
    }
    return Double.NaN;
  }

  @Override
  public double getZ(final int vertexIndex) {
    if (this.axisCount > 2) {
      if (this.coordinates == null) {
        if (this.line != null) {
          return this.line.getX(vertexIndex);
        }
      } else {
        final int axisCount = this.axisCount;
        if (isInVertexRange(vertexIndex)) {
          return this.coordinates[vertexIndex * axisCount + Z];
        }
      }
    }
    return Double.NaN;
  }

  private void grow(final int minCapacity) {
    // overflow-conscious code
    final int oldCapacity = this.coordinates.length;
    int newCapacity;
    if (oldCapacity == 0) {
      newCapacity = 10;
    } else {
      newCapacity = oldCapacity + (oldCapacity >> 1);
    }
    if (newCapacity - minCapacity < 0) {
      newCapacity = minCapacity;
    }
    if (newCapacity - MAX_ARRAY_SIZE > 0) {
      newCapacity = hugeCapacity(minCapacity);
    }
    // minCapacity is usually close to size, so this is a win:
    this.coordinates = Arrays.copyOf(this.coordinates, newCapacity);
    Arrays.fill(this.coordinates, oldCapacity, this.coordinates.length, Double.NaN);
  }

  public LineStringEditor insertVertex(final int vertexIndex, final double... coordinates) {
    if (isInVertexRange(vertexIndex)) {
      insertVertexShift(vertexIndex);
    } else if (isVertexCount(vertexIndex)) {
      setVertexCount(this.vertexCount + 2);

    }
    return setVertex(vertexIndex, coordinates);
  }

  public LineStringEditor insertVertex(final int vertexIndex, final double x, final double y) {
    if (isInVertexRange(vertexIndex)) {
      insertVertexShift(vertexIndex);
    } else if (isVertexCount(vertexIndex)) {
      setVertexCount(this.vertexCount + 2);
    }
    return setVertex(vertexIndex, x, y);
  }

  public LineStringEditor insertVertex(final int vertexIndex, final double x, final double y,
    final double z) {
    if (isInVertexRange(vertexIndex)) {
      insertVertexShift(vertexIndex);
    } else if (isVertexCount(vertexIndex)) {
      setVertexCount(this.vertexCount + 2);

    }
    return setVertex(vertexIndex, x, y, z);
  }

  public LineStringEditor insertVertex(final int vertexIndex, final Point point) {
    if (point == null || point.isEmpty()) {
      return this;
    } else {
      if (isInVertexRange(vertexIndex)) {
        insertVertexShift(vertexIndex);
      } else if (isVertexCount(vertexIndex)) {
        setVertexCount(this.vertexCount + 2);

      }
      return setVertex(vertexIndex, point);
    }
  }

  public LineStringEditor insertVertex(final int vertexIndex, final Point point,
    final boolean allowRepeated) {
    if (!allowRepeated) {
      final int vertexCount = getVertexCount();
      if (vertexCount > 0) {
        if (vertexIndex > 0) {
          if (equalsVertex(vertexIndex - 1, point)) {
            return this;
          }
        }
        if (vertexIndex < vertexCount) {
          if (equalsVertex(vertexIndex, point)) {
            return this;
          }
        }
      }
    }
    insertVertex(vertexIndex, point);
    return this;
  }

  @Override
  public LineStringEditor insertVertex(final int[] vertexId, final Point newPoint) {
    final int vertexIndex = getVertexId(vertexId);
    insertVertex(vertexIndex, newPoint);
    return this;
  }

  private void insertVertexShift(final int index) {
    final int axisCount = getAxisCount();
    ensureCapacity(this.vertexCount + 1);
    final int offset = index * axisCount;
    final int newOffset = offset + axisCount;
    System.arraycopy(this.coordinates, offset, this.coordinates, newOffset,
      this.coordinates.length - newOffset);
    this.vertexCount++;
  }

  @Override
  public boolean isEmpty() {
    return this.vertexCount == 0;
  }

  public boolean isInVertexRange(final int vertexIndex) {
    if (vertexIndex < 0) {
      throw new IllegalArgumentException("Vertex index must be >=0");
    } else {
      return vertexIndex < this.vertexCount;
    }
  }

  private boolean isVertexCount(final int vertexIndex) {
    if (vertexIndex == this.vertexCount) {
      return true;
    } else {
      throw new IllegalArgumentException(
        "Vertex index=" + vertexIndex + " not in 0.." + this.vertexCount);
    }
  }

  public Geometry newBestGeometry() {
    final int vertexCount = getVertexCount();
    if (vertexCount == 1) {
      return newPoint();
    } else if (vertexCount == 2) {
      return newLineString();
    } else if (vertexCount == 3) {
      if (isClosed()) {
        final GeometryFactory geometryFactory = getGeometryFactory();
        return geometryFactory.lineString(this.axisCount, 2, this.coordinates);
      }
    }
    return newPolygon();
  }

  @Override
  public LineString newGeometry() {
    if (this.coordinates == null) {
      if (this.line == null) {
        final GeometryFactory geometryFactory = getGeometryFactory();
        return geometryFactory.lineString();
      } else {
        return this.line.newLineString();
      }
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final int axisCount = geometryFactory.getAxisCount();
      return newLineString(geometryFactory, axisCount, this.vertexCount, this.coordinates);
    }
  }

  @Override
  public LinearRing newLinearRing() {
    final int coordinateCount = this.vertexCount * this.axisCount;
    final double[] coordinates = new double[coordinateCount];
    System.arraycopy(this.coordinates, 0, coordinates, 0, coordinateCount);
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new LinearRingDoubleGf(geometryFactory, this.axisCount, this.vertexCount, coordinates);
  }

  @Override
  public LineString newLineString(final GeometryFactory geometryFactory, final int axisCount,
    final int vertexCount, final double... coordinates) {
    final GeometryFactory geometryFactoryAxisCount = geometryFactory.convertAxisCount(axisCount);
    return geometryFactoryAxisCount.lineString(axisCount, vertexCount, coordinates);
  }

  public Point newPoint() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.point(this.coordinates);
  }

  public Polygon newPolygon() {
    final LinearRing ring = newLinearRing();
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.polygon(ring);
  }

  @Override
  public LineStringEditor setAxisCount(final int axisCount) {
    final int oldAxisCount = getAxisCount();
    if (oldAxisCount != axisCount) {
      this.boundingBox = null;
      this.coordinates = getCoordinates(axisCount);
      this.axisCount = axisCount;
      super.setAxisCount(axisCount);
    }
    return this;
  }

  @Override
  public double setCoordinate(final int vertexIndex, final int axisIndex, final double coordinate) {
    if (isInVertexRange(vertexIndex) && axisIndex >= 0 && axisIndex < this.axisCount) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final double preciseCoordinate = geometryFactory.makePrecise(axisIndex, coordinate);
      final double oldValue = getCoordinate(vertexIndex, axisIndex);
      final boolean changed = !Doubles.equal(preciseCoordinate, oldValue);
      if (changed) {
        this.boundingBox = null;
        final double[] lineCoordinates = getCoordinatesModified();
        lineCoordinates[vertexIndex * this.axisCount + axisIndex] = preciseCoordinate;
        return oldValue;
      }
    }
    return Double.NaN;
  }

  @Override
  public double setCoordinate(final int partIndex, final int vertexIndex, final int axisIndex,
    final double coordinate) {
    if (partIndex == 0) {
      return setCoordinate(vertexIndex, axisIndex, coordinate);
    } else {
      return Double.NaN;
    }
  }

  @Override
  public LineStringEditor setCoordinate(final int[] vertexId, final int axisIndex,
    final double coordinate) {
    final int vertexIndex = getVertexId(vertexId);
    setCoordinate(vertexIndex, axisIndex, coordinate);
    return this;
  }

  private void setCoordinates(final double[] coordinates) {
    setModified(true);
    this.boundingBox = null;
    this.coordinates = coordinates;
  }

  private void setCoordinatesNaN(int offset, final int startAxisCount) {
    for (int axisIndex = startAxisCount; axisIndex < this.axisCount; axisIndex++) {
      this.coordinates[offset++] = Double.NaN;
    }
  }

  public LineStringEditor setVertex(final int vertexIndex, final double... coordinates) {
    if (isInVertexRange(vertexIndex)) {
      this.boundingBox = null;
      final double[] lineCoordinates = getCoordinatesModified();
      int offset = vertexIndex * this.axisCount;
      final GeometryFactory geometryFactory = getGeometryFactory();
      lineCoordinates[offset++] = geometryFactory.makeXPrecise(coordinates[0]);
      lineCoordinates[offset++] = geometryFactory.makeYPrecise(coordinates[1]);
      final int pointAxisCount = coordinates.length;
      for (int axisIndex = 2; axisIndex < pointAxisCount
        && axisIndex < this.axisCount; axisIndex++) {
        lineCoordinates[offset++] = geometryFactory.makePrecise(axisIndex, coordinates[axisIndex]);
      }
      setCoordinatesNaN(offset, pointAxisCount);
    }
    return this;
  }

  public LineStringEditor setVertex(final int vertexIndex, final double x, final double y) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isInVertexRange(vertexIndex)) {
      this.boundingBox = null;
      final double[] lineCoordinates = getCoordinatesModified();
      final int axisCount = getAxisCount();
      int offset = vertexIndex * axisCount;
      lineCoordinates[offset++] = geometryFactory.makeXPrecise(x);
      lineCoordinates[offset++] = geometryFactory.makeYPrecise(y);
      if (axisCount > 2) {
        setCoordinatesNaN(offset, axisCount);
      }
    }
    return this;
  }

  public LineStringEditor setVertex(final int vertexIndex, final double x, final double y,
    final double z) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isInVertexRange(vertexIndex)) {
      this.boundingBox = null;
      final double[] lineCoordinates = getCoordinatesModified();
      final int axisCount = getAxisCount();
      int offset = vertexIndex * axisCount;
      lineCoordinates[offset++] = geometryFactory.makeXPrecise(x);
      lineCoordinates[offset++] = geometryFactory.makeYPrecise(y);
      if (this.axisCount > 2) {
        lineCoordinates[offset++] = geometryFactory.makeZPrecise(z);
        if (axisCount > 3) {
          setCoordinatesNaN(offset, 3);
        }
      }
    }
    return this;
  }

  public LineStringEditor setVertex(final int index, final Point point) {
    if (isInVertexRange(index) && point != null && !point.isEmpty()) {
      this.boundingBox = null;
      final double[] lineCoordinates = getCoordinatesModified();
      final int axisCount = getAxisCount();
      final int pointAxisCount = point.getAxisCount();
      int offset = index * axisCount;
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Point convertPoint2d = point.convertPoint2d(geometryFactory);
      lineCoordinates[offset++] = geometryFactory.makeXPrecise(convertPoint2d.getX());
      lineCoordinates[offset++] = geometryFactory.makeYPrecise(convertPoint2d.getY());
      for (int axisIndex = 2; axisIndex < pointAxisCount && axisIndex < axisCount; axisIndex++) {
        final double coordinate = point.getCoordinate(axisIndex);
        lineCoordinates[offset++] = geometryFactory.makePrecise(axisIndex, coordinate);
      }
      setCoordinatesNaN(offset, pointAxisCount);
    }
    return this;
  }

  public void setVertexCount(final int vertexCount) {
    this.boundingBox = null;
    this.vertexCount = vertexCount;
    ensureCapacity(vertexCount);
  }

  @Override
  public double setX(final int vertexIndex, final double x) {
    if (isInVertexRange(vertexIndex)) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final double preciseX = geometryFactory.makeXPrecise(x);
      final double oldValue = getX(vertexIndex);
      final boolean changed = !Doubles.equal(preciseX, oldValue);
      if (changed) {
        this.boundingBox = null;
        final double[] lineCoordinates = getCoordinatesModified();
        lineCoordinates[vertexIndex * this.axisCount] = preciseX;
        return oldValue;
      }
    }
    return Double.NaN;
  }

  @Override
  public double setY(final int vertexIndex, final double y) {
    if (isInVertexRange(vertexIndex)) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final double preciseY = geometryFactory.makeYPrecise(y);
      final double oldValue = getX(vertexIndex);
      final boolean changed = !Doubles.equal(preciseY, oldValue);
      if (changed) {
        this.boundingBox = null;
        final double[] lineCoordinates = getCoordinatesModified();
        lineCoordinates[vertexIndex * this.axisCount + 1] = preciseY;
        return oldValue;
      }
    }
    return Double.NaN;
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
