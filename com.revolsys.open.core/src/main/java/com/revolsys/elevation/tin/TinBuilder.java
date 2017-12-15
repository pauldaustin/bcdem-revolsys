package com.revolsys.elevation.tin;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.IntArrayScaleGriddedElevationModel;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;

public interface TinBuilder extends TriangulatedIrregularNetwork {

  @Override
  void forEachTriangle(TriangleConsumer action);

  @Override
  BoundingBox getBoundingBox();

  @Override
  GeometryFactory getGeometryFactory();

  Point insertVertex(double x, double y, double z);

  default void insertVertex(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    final double z = point.getZ();
    insertVertex(x, y, z);
  }

  default void insertVertices(final Iterable<? extends Point> points) {
    for (final Point point : points) {
      insertVertex(point);
    }
  }

  default void insertVertices(final LineString line) {
    final int vertexCount = line.getVertexCount();
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      final double x = line.getX(vertexIndex);
      final double y = line.getY(vertexIndex);
      final double z = line.getZ(vertexIndex);
      insertVertex(x, y, z);
    }
  }

  default GriddedElevationModel newGriddedElevationModel(final int gridCellSize) {
    final BoundingBox boundingBox = getBoundingBox();
    final int minX = (int)Math.floor(boundingBox.getMinX() / gridCellSize) * gridCellSize;
    final int minY = (int)Math.floor(boundingBox.getMinY() / gridCellSize) * gridCellSize;
    final int maxX = (int)Math.ceil(boundingBox.getMaxX() / gridCellSize) * gridCellSize;
    final int maxY = (int)Math.ceil(boundingBox.getMaxY() / gridCellSize) * gridCellSize;

    final int width = maxX - minX;
    final int height = maxY - minY;

    final int gridWidth = width / gridCellSize;
    final int gridHeight = height / gridCellSize;

    final GeometryFactory geometryFactory = getGeometryFactory()//
      .convertAxisCountAndScales(3, 1000.0, 1000.0, 1000.0);
    final IntArrayScaleGriddedElevationModel elevationModel = new IntArrayScaleGriddedElevationModel(
      geometryFactory, minX, minY, gridWidth, gridHeight, gridCellSize);

    forEachTriangle(elevationModel::setElevationsForTriangle);
    return elevationModel;
  }

  TriangulatedIrregularNetwork newTriangulatedIrregularNetwork();
}
