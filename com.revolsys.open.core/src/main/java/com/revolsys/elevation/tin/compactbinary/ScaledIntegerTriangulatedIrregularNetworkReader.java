package com.revolsys.elevation.tin.compactbinary;

import java.io.EOFException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.tin.IntArrayScaleTriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangleConsumer;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;
import com.revolsys.util.WrappedException;

public class ScaledIntegerTriangulatedIrregularNetworkReader extends BaseObjectWithProperties
  implements BaseCloseable {
  private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

  private final Resource resource;

  private GeometryFactory geometryFactory;

  private BoundingBox boundingBox;

  private ChannelReader in;

  private double scaleX;

  private double scaleY;

  private double scaleZ;

  private boolean closed = false;

  private boolean exists;

  public ScaledIntegerTriangulatedIrregularNetworkReader(final Resource resource,
    final MapEx properties) {
    this.resource = resource;
    setProperties(properties);
    // Make sure all properties are added
    getProperties().putAll(properties);
  }

  @Override
  public void close() {
    if (!this.closed) {
      this.closed = true;
      try {
        this.in.close();
      } finally {
        this.boundingBox = null;
        this.in = null;
      }
    }
  }

  public void forEachTriangle(final TriangleConsumer action) {
    open();
    try {
      final double scaleX = this.scaleX;
      final double scaleY = this.scaleY;
      final double scaleZ = this.scaleZ;
      boolean hasMore = true;
      while (hasMore) {
        int triangleVertexCount = 0;
        try {
          final double x1 = getDouble(scaleX);
          final double y1 = getDouble(scaleY);
          final double z1 = getDouble(scaleZ);
          final double x2 = getDouble(scaleX);
          final double y2 = getDouble(scaleY);
          final double z2 = getDouble(scaleZ);
          final double x3 = getDouble(scaleX);
          final double y3 = getDouble(scaleY);
          final double z3 = getDouble(scaleZ);
          action.accept(x1, y1, z1, x2, y2, z2, x3, y3, z3);
          triangleVertexCount = 9;
        } catch (final WrappedException e) {
          if (Exceptions.isException(e, EOFException.class) && triangleVertexCount == 0) {
            hasMore = false;
          } else {
            throw e;
          }
        }
      }
    } finally {
      close();
    }
  }

  private double getDouble(final double scale) {
    final int intValue = this.in.getInt();
    if (intValue == Integer.MIN_VALUE) {
      return Double.NaN;
    } else {
      return intValue / scale;
    }
  }

  public boolean isClosed() {
    return this.closed || !this.exists;
  }

  public TriangulatedIrregularNetwork newTriangulatedIrregularNetwork() {
    open();
    int capacity = 10;
    int[] triangleXCoordinates = new int[capacity];
    int[] triangleYCoordinates = new int[capacity];
    int[] triangleZCoordinates = new int[capacity];
    int coordinateIndex = 0;
    boolean hasMore = true;
    while (hasMore) {
      try {
        if (coordinateIndex >= capacity) {
          final int minCapacity = coordinateIndex + 1;
          capacity = capacity + (capacity >> 1);
          if (capacity - minCapacity < 0) {
            capacity = minCapacity;
          }
          if (capacity - MAX_ARRAY_SIZE > 0) {
            if (minCapacity < 0) {
              throw new OutOfMemoryError();
            } else if (minCapacity > MAX_ARRAY_SIZE) {
              capacity = Integer.MAX_VALUE;
            } else {
              capacity = MAX_ARRAY_SIZE;
            }
          }

          triangleXCoordinates = Arrays.copyOf(triangleXCoordinates, capacity);
          triangleYCoordinates = Arrays.copyOf(triangleYCoordinates, capacity);
          triangleZCoordinates = Arrays.copyOf(triangleZCoordinates, capacity);
        }

        triangleXCoordinates[coordinateIndex] = this.in.getInt();
        triangleYCoordinates[coordinateIndex] = this.in.getInt();
        triangleZCoordinates[coordinateIndex] = this.in.getInt();
        coordinateIndex++;
      } catch (final WrappedException e) {
        if (Exceptions.isException(e, EOFException.class) && coordinateIndex % 3 == 0) {
          hasMore = false;
        } else {
          throw e;
        }
      }
    }

    triangleXCoordinates = Arrays.copyOf(triangleXCoordinates, coordinateIndex);
    triangleYCoordinates = Arrays.copyOf(triangleYCoordinates, coordinateIndex);
    triangleZCoordinates = Arrays.copyOf(triangleZCoordinates, coordinateIndex);
    final int triangleCount = coordinateIndex / 3;

    final IntArrayScaleTriangulatedIrregularNetwork tin = new IntArrayScaleTriangulatedIrregularNetwork(
      this.geometryFactory, this.boundingBox, triangleCount, triangleXCoordinates,
      triangleYCoordinates, triangleZCoordinates);
    tin.setProperties(getProperties());
    return tin;
  }

  public void open() {
    if (this.in == null && !this.closed) {
      this.in = this.resource.newChannelReader();
      if (this.in == null) {
        this.exists = false;
        this.closed = true;
      } else {
        this.exists = true;
        readHeader();
      }
    }
  }

  private void readHeader() {
    @SuppressWarnings("unused")
    final String fileType = this.in.getString(
      ScaledIntegerTriangulatedIrregularNetwork.FILE_TYPE_BYTES.length,
      StandardCharsets.ISO_8859_1); // File type
    @SuppressWarnings("unused")
    final short version = this.in.getShort();
    final int coordinateSystemId = this.in.getInt(); // Coordinate System ID
    this.scaleX = this.in.getDouble();
    this.scaleY = this.in.getDouble();
    this.scaleZ = this.in.getDouble();
    this.geometryFactory = GeometryFactory.fixed3d(coordinateSystemId, this.scaleX, this.scaleY,
      this.scaleZ);
    final double minX = this.in.getDouble();
    final double minY = this.in.getDouble();
    final double maxX = this.in.getDouble();
    final double maxY = this.in.getDouble();
    this.boundingBox = this.geometryFactory.newBoundingBox(2, minX, minY, maxX, maxY);
  }
}
