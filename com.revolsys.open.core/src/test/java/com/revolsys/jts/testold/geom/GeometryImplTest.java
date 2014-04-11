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

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKTReader;

/**
 * @version 1.7
 */
public class GeometryImplTest extends TestCase {
  private interface CollectionFactory {
    Geometry createCollection(Geometry[] geometries);
  }

  public static void main(final String[] args) throws Exception {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {
    return new TestSuite(GeometryImplTest.class);
  }

  private final GeometryFactory geometryFactory = GeometryFactory.getFactory(0,
    3, 1, 1);

  private final WKTReader reader = new WKTReader(this.geometryFactory);

  private final WKTReader readerFloat = new WKTReader();

  public GeometryImplTest(final String name) {
    super(name);
  }

  private void doTestEquals(final Geometry a, final Geometry b,
    final boolean equalsGeometry, final boolean equalsObject,
    final boolean equalsExact, final boolean equalsHash) {
    assertEquals(equalsGeometry, a.equals(b));
    assertEquals(equalsObject, a.equals((Object)b));
    assertEquals(equalsExact, a.equalsExact(b));
    assertEquals(equalsHash, a.hashCode() == b.hashCode());
  }

  private void doTestEqualsExact(final Geometry x,
    final Geometry somethingExactlyEqual,
    final Geometry somethingEqualButNotExactly,
    final Geometry somethingNotEqualButSameClass) throws Exception {
    Geometry differentClass;

    if (x instanceof Point) {
      differentClass = this.reader.read("POLYGON ((0 0, 0 50, 50 43949, 50 0, 0 0))");
    } else {
      differentClass = this.reader.read("POINT ( 2351 1563 )");
    }

    assertTrue(x.equalsExact(x));
    assertTrue(x.equalsExact(somethingExactlyEqual));
    assertTrue(somethingExactlyEqual.equalsExact(x));
    assertTrue(!x.equalsExact(somethingEqualButNotExactly));
    assertTrue(!somethingEqualButNotExactly.equalsExact(x));
    assertTrue(!x.equalsExact(somethingEqualButNotExactly));
    assertTrue(!somethingEqualButNotExactly.equalsExact(x));
    assertTrue(!x.equalsExact(differentClass));
    assertTrue(!differentClass.equalsExact(x));
  }

  private void doTestEqualsExact(final Geometry x,
    final Geometry somethingExactlyEqual,
    final Geometry somethingNotEqualButSameClass,
    final Geometry sameClassButEmpty, final Geometry anotherSameClassButEmpty,
    final CollectionFactory collectionFactory) throws Exception {
    Geometry emptyDifferentClass;

    if (x instanceof Point) {
      emptyDifferentClass = this.geometryFactory.createGeometryCollection();
    } else {
      emptyDifferentClass = this.geometryFactory.createPoint((Coordinates)null);
    }

    final Geometry somethingEqualButNotExactly = this.geometryFactory.createGeometryCollection(new Geometry[] {
      x
    });

    doTestEqualsExact(x, somethingExactlyEqual,
      collectionFactory.createCollection(new Geometry[] {
        x
      }), somethingNotEqualButSameClass);

    doTestEqualsExact(sameClassButEmpty, anotherSameClassButEmpty,
      emptyDifferentClass, x);

    /**
     * Test comparison of non-empty versus empty.
     */
    doTestEqualsExact(x, somethingExactlyEqual, sameClassButEmpty,
      sameClassButEmpty);

    doTestEqualsExact(collectionFactory.createCollection(new Geometry[] {
      x, x
    }), collectionFactory.createCollection(new Geometry[] {
      x, somethingExactlyEqual
    }), somethingEqualButNotExactly,
      collectionFactory.createCollection(new Geometry[] {
        x, somethingNotEqualButSameClass
      }));
  }

  private void doTestFromCommcast2003AtYahooDotCa(final WKTReader reader)
    throws ParseException {
    this.readerFloat.read(
      "POLYGON ((708653.498611049 2402311.54647056, 708708.895756966 2402203.47250014, 708280.326454234 2402089.6337791, 708247.896591321 2402252.48269854, 708367.379593851 2402324.00761653, 708248.882609455 2402253.07294874, 708249.523621829 2402244.3124463, 708261.854734465 2402182.39086576, 708262.818392579 2402183.35452387, 708653.498611049 2402311.54647056))")
      .intersection(
        reader.read("POLYGON ((708258.754920656 2402197.91172757, 708257.029447455 2402206.56901508, 708652.961095455 2402312.65463437, 708657.068786251 2402304.6356364, 708258.754920656 2402197.91172757))"));
  }

  public void testDepthMismatchAssertionFailedException() throws Exception {
    // register@robmeek.com reported an assertion failure
    // ("depth mismatch at (160.0, 300.0, Nan)") [Jon Aquino 10/28/2003]
    this.reader.read(
      "MULTIPOLYGON (((100 300, 100 400, 200 400, 200 300, 100 300)),"
        + "((160 300, 160 400, 260 400, 260 300, 160 300)),"
        + "((160 300, 160 200, 260 200, 260 300, 160 300)))").buffer(0);
  }

  public void testEmptyGeometryCentroid() throws Exception {
    assertTrue(this.reader.read("POINT EMPTY").getCentroid().isEmpty());
    assertTrue(this.reader.read("POLYGON EMPTY").getCentroid().isEmpty());
    assertTrue(this.reader.read("LINESTRING EMPTY").getCentroid().isEmpty());
    assertTrue(this.reader.read("GEOMETRYCOLLECTION EMPTY")
      .getCentroid()
      .isEmpty());
    assertTrue(this.reader.read(
      "GEOMETRYCOLLECTION(GEOMETRYCOLLECTION EMPTY, GEOMETRYCOLLECTION EMPTY)")
      .getCentroid()
      .isEmpty());
    assertTrue(this.reader.read("MULTIPOLYGON EMPTY").getCentroid().isEmpty());
    assertTrue(this.reader.read("MULTILINESTRING EMPTY")
      .getCentroid()
      .isEmpty());
    assertTrue(this.reader.read("MULTIPOINT EMPTY").getCentroid().isEmpty());
  }

  public void testEquals() throws Exception {
    final Geometry g = this.reader.read("POLYGON ((0 0, 0 50, 50 50, 50 0, 0 0))");
    final Geometry same = this.reader.read("POLYGON ((0 0, 0 50, 50 50, 50 0, 0 0))");
    final Geometry differentStart = this.reader.read("POLYGON ((0 50, 50 50, 50 0, 0 0, 0 50))");
    final Geometry differentFourth = this.reader.read("POLYGON ((0 0, 0 50, 50 50, 50 -99, 0 0))");
    final Geometry differentSecond = this.reader.read("POLYGON ((0 0, 0 99, 50 50, 50 0, 0 0))");
    doTestEquals(g, same, true, true, true, true);
    doTestEquals(g, differentStart, true, false, false, true);
    doTestEquals(g, differentFourth, false, false, false, false);
    doTestEquals(g, differentSecond, false, false, false, false);
  }

  public void testEquals1() throws Exception {
    final Geometry polygon1 = this.reader.read("POLYGON ((0 0, 0 50, 50 50, 50 0, 0 0))");
    final Geometry polygon2 = this.reader.read("POLYGON ((50 50, 50 0, 0 0, 0 50, 50 50))");
    assertTrue(polygon1.equals(polygon2));
  }

  public void testEqualsExactForGeometryCollections() throws Exception {
    final Geometry polygon1 = this.reader.read("POLYGON ((0 0, 0 50, 50 50, 50 0, 0 0))");
    final Geometry polygon2 = this.reader.read("POLYGON ((50 50, 50 0, 0 0, 0 50, 50 50))");
    final GeometryCollection x = this.geometryFactory.createGeometryCollection(new Geometry[] {
      polygon1, polygon2
    });
    final GeometryCollection somethingExactlyEqual = this.geometryFactory.createGeometryCollection(new Geometry[] {
      polygon1, polygon2
    });
    final GeometryCollection somethingNotEqualButSameClass = this.geometryFactory.createGeometryCollection(new Geometry[] {
      polygon2
    });
    final GeometryCollection sameClassButEmpty = this.geometryFactory.createGeometryCollection();
    final GeometryCollection anotherSameClassButEmpty = this.geometryFactory.createGeometryCollection();
    final CollectionFactory collectionFactory = new CollectionFactory() {
      @Override
      public Geometry createCollection(final Geometry[] geometries) {
        return GeometryImplTest.this.geometryFactory.createGeometryCollection(geometries);
      }
    };

    doTestEqualsExact(x, somethingExactlyEqual, somethingNotEqualButSameClass,
      sameClassButEmpty, anotherSameClassButEmpty, collectionFactory);
  }

  // public void testEquals2() throws Exception {
  // Geometry lineString =
  // reader.read("LINESTRING(0 0, 0 50, 50 50, 50 0, 0 0)");
  // Geometry geometryCollection =
  // reader.read("GEOMETRYCOLLECTION ( LINESTRING(0 0  , 0  50), "
  // + "LINESTRING(0 50 , 50 50), "
  // + "LINESTRING(50 50, 50 0 ), "
  // + "LINESTRING(50 0 , 0  0 ) )");
  // assertTrue(lineString.equals(geometryCollection));
  // }
  public void testEqualsExactForLinearRings() throws Exception {
    final LinearRing x = this.geometryFactory.createLinearRing(0.0, 0,
      Coordinates.NULL_ORDINATE, 100.0, 0, Coordinates.NULL_ORDINATE, 100.0,
      100, Coordinates.NULL_ORDINATE, 0.0, 0, Coordinates.NULL_ORDINATE);
    final LinearRing somethingExactlyEqual = this.geometryFactory.createLinearRing(
      0.0, 0, Coordinates.NULL_ORDINATE, 100.0, 0, Coordinates.NULL_ORDINATE,
      100.0, 100, Coordinates.NULL_ORDINATE, 0.0, 0, Coordinates.NULL_ORDINATE);
    final LinearRing somethingNotEqualButSameClass = this.geometryFactory.createLinearRing(
      0.0, 0, Coordinates.NULL_ORDINATE, 100.0, 0, Coordinates.NULL_ORDINATE,
      100.0, 555, Coordinates.NULL_ORDINATE, 0.0, 0, Coordinates.NULL_ORDINATE);
    final LinearRing sameClassButEmpty = this.geometryFactory.createLinearRing((CoordinatesList)null);
    final LinearRing anotherSameClassButEmpty = this.geometryFactory.createLinearRing((CoordinatesList)null);
    final CollectionFactory collectionFactory = new CollectionFactory() {
      @Override
      public Geometry createCollection(final Geometry[] geometries) {
        return GeometryImplTest.this.geometryFactory.createMultiLineString(GeometryFactory.toLineStringArray(Arrays.asList(geometries)));
      }
    };

    doTestEqualsExact(x, somethingExactlyEqual, somethingNotEqualButSameClass,
      sameClassButEmpty, anotherSameClassButEmpty, collectionFactory);

    // LineString somethingEqualButNotExactly =
    // geometryFactory.createLineString(new Coordinates[] {
    // new Coordinate((double)0, 0), new Coordinate((double)100, 0), new
    // Coordinate((double)100, 100),
    // new Coordinate((double)0, 0) });
    //
    // doTestEqualsExact(x, somethingExactlyEqual, somethingEqualButNotExactly,
    // somethingNotEqualButSameClass);
  }

  public void testEqualsExactForLineStrings() throws Exception {
    final LineString x = this.geometryFactory.createLineString(0.0, 0,
      Coordinates.NULL_ORDINATE, 100.0, 0, Coordinates.NULL_ORDINATE, 100.0,
      100, Coordinates.NULL_ORDINATE);
    final LineString somethingExactlyEqual = this.geometryFactory.createLineString(
      0.0, 0, Coordinates.NULL_ORDINATE, 100.0, 0, Coordinates.NULL_ORDINATE,
      100.0, 100, Coordinates.NULL_ORDINATE);
    final LineString somethingNotEqualButSameClass = this.geometryFactory.createLineString(
      0.0, 0, Coordinates.NULL_ORDINATE, 100.0, 0, Coordinates.NULL_ORDINATE,
      100.0, 555, Coordinates.NULL_ORDINATE);
    final LineString sameClassButEmpty = this.geometryFactory.createLineString();
    final LineString anotherSameClassButEmpty = this.geometryFactory.createLineString();
    final CollectionFactory collectionFactory = new CollectionFactory() {
      @Override
      public Geometry createCollection(final Geometry[] geometries) {
        return GeometryImplTest.this.geometryFactory.createMultiLineString(GeometryFactory.toLineStringArray(Arrays.asList(geometries)));
      }
    };

    doTestEqualsExact(x, somethingExactlyEqual, somethingNotEqualButSameClass,
      sameClassButEmpty, anotherSameClassButEmpty, collectionFactory);

    final CollectionFactory collectionFactory2 = new CollectionFactory() {
      @Override
      public Geometry createCollection(final Geometry[] geometries) {
        return GeometryImplTest.this.geometryFactory.createMultiLineString(GeometryFactory.toLineStringArray(Arrays.asList(geometries)));
      }
    };

    doTestEqualsExact(x, somethingExactlyEqual, somethingNotEqualButSameClass,
      sameClassButEmpty, anotherSameClassButEmpty, collectionFactory2);
  }

  public void testEqualsExactForPoints() throws Exception {
    final Point x = this.geometryFactory.createPoint(100.0, 100.0);
    final Point somethingExactlyEqual = this.geometryFactory.createPoint(100.0,
      100);
    final Point somethingNotEqualButSameClass = this.geometryFactory.createPoint(
      999.0, 100);
    final Point sameClassButEmpty = this.geometryFactory.createPoint((Coordinates)null);
    final Point anotherSameClassButEmpty = this.geometryFactory.createPoint((Coordinates)null);
    final CollectionFactory collectionFactory = new CollectionFactory() {
      @Override
      public Geometry createCollection(final Geometry[] geometries) {
        return GeometryImplTest.this.geometryFactory.createMultiPoint(GeometryFactory.toPointArray(Arrays.asList(geometries)));
      }
    };

    doTestEqualsExact(x, somethingExactlyEqual, somethingNotEqualButSameClass,
      sameClassButEmpty, anotherSameClassButEmpty, collectionFactory);
  }

  public void testEqualsExactForPolygons() throws Exception {
    final Polygon x = (Polygon)this.reader.read("POLYGON ((0 0, 0 50, 50 50, 50 0, 0 0))");
    final Polygon somethingExactlyEqual = (Polygon)this.reader.read("POLYGON ((0 0, 0 50, 50 50, 50 0, 0 0))");
    final Polygon somethingNotEqualButSameClass = (Polygon)this.reader.read("POLYGON ((50 50, 50 0, 0 0, 0 50, 50 50))");
    final Polygon sameClassButEmpty = (Polygon)this.reader.read("POLYGON EMPTY");
    final Polygon anotherSameClassButEmpty = (Polygon)this.reader.read("POLYGON EMPTY");
    final CollectionFactory collectionFactory = new CollectionFactory() {
      @Override
      public Geometry createCollection(final Geometry[] geometries) {
        return GeometryImplTest.this.geometryFactory.createMultiPolygon(GeometryFactory.toPolygonArray(Arrays.asList(geometries)));
      }
    };

    doTestEqualsExact(x, somethingExactlyEqual, somethingNotEqualButSameClass,
      sameClassButEmpty, anotherSameClassButEmpty, collectionFactory);
  }

  public void testEqualsWithNull() throws Exception {
    final Geometry polygon = this.reader.read("POLYGON ((0 0, 0 50, 50 50, 50 0, 0 0))");
    assertTrue(!polygon.equals(null));
    final Object g = null;
    assertTrue(!polygon.equals(g));
  }

  // public void testInvalidateEnvelope() throws Exception {
  // final Geometry g =
  // this.reader.read("POLYGON((0 0, 0 50, 50 50, 50 0, 0 0))");
  // assertEquals(new Envelope(0, 50, 0, 50), g.getEnvelopeInternal());
  // g.apply(new CoordinateFilter() {
  // @Override
  // public void filter(final Coordinates coord) {
  // coord.setX(coord.getX() + 1);
  // coord.setY(coord.getY() + 1);
  // }
  // });
  // assertEquals(new Envelope(0, 50, 0, 50), g.getEnvelopeInternal());
  // g.geometryChanged();
  // assertEquals(new Envelope(1, 51, 1, 51), g.getEnvelopeInternal());
  // }

  public void testNoOutgoingDirEdgeFound() throws Exception {
    doTestFromCommcast2003AtYahooDotCa(this.reader);
  }

  // public void testOutOfMemoryError() throws Exception {
  // doTestFromCommcast2003AtYahooDotCa(new WKTReader());
  // }

  public void testPolygonRelate() throws Exception {
    final Geometry bigPolygon = this.reader.read("POLYGON ((0 0, 0 50, 50 50, 50 0, 0 0))");
    final Geometry smallPolygon = this.reader.read("POLYGON ((10 10, 10 30, 30 30, 30 10, 10 10))");
    assertTrue(bigPolygon.contains(smallPolygon));
  }
}