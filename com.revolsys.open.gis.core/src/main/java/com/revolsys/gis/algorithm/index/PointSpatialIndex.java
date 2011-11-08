package com.revolsys.gis.algorithm.index;

import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.Envelope;

public interface PointSpatialIndex<T> extends Iterable<T> {
  void visit(final Envelope envelope, final Visitor<T> visitor);

  void visit(final Visitor<T> visitor);

  List<T> find(Envelope envelope);

  List<T> findAll();

  void put(Coordinates point, T object);

  boolean remove(Coordinates point, T object);
}
