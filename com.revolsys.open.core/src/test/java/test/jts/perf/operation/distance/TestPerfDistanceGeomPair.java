package test.jts.perf.operation.distance;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.util.SineStarFactory;
import com.revolsys.jts.operation.distance.IndexedFacetDistance;
import com.revolsys.jts.util.GeometricShapeFactory;
import com.revolsys.jts.util.Stopwatch;

public class TestPerfDistanceGeomPair {

  static final int MAX_ITER = 100;

  public static void main(final String[] args) {
    final TestPerfDistanceGeomPair test = new TestPerfDistanceGeomPair();
    // test.test();
    test.test2();
  }

  boolean testFailed = false;

  boolean verbose = true;

  double size = 100;

  double separationDist = this.size * 2;

  public TestPerfDistanceGeomPair() {
  }

  void computeDistanceToAllPoints(final Geometry[] geom) {
    final Coordinate[] pts = geom[1].getCoordinates();
    double dist = 0.0;
    final double dist2 = 0.0;
    final IndexedFacetDistance fastDist = new IndexedFacetDistance(geom[0]);
    for (final Coordinate p : pts) {
      // slow N^2 distance
      dist = geom[0].distance(geom[1].getGeometryFactory().createPoint(p));

      // dist2 = fastDist.getDistance(geom[1].getFactory().createPoint(p));

      // if (dist != dist2) System.out.println("distance discrepancy found!");
    }
  }

  Geometry[] createCircles(final int nPts) {
    final GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setCentre(new Coordinate(0, 0));
    gsf.setSize(100);
    gsf.setNumPoints(nPts);

    final Polygon gRect = gsf.createCircle();

    gsf.setCentre(new Coordinate(0, this.separationDist));

    final Polygon gRect2 = gsf.createCircle();

    return new Geometry[] {
      gRect, gRect2
    };

  }

  Geometry[] createSineStars(final int nPts) {
    final SineStarFactory gsf = new SineStarFactory();
    gsf.setCentre(new Coordinate(0, 0));
    gsf.setSize(100);
    gsf.setNumPoints(nPts);

    final Geometry g = gsf.createSineStar().getBoundary();

    gsf.setCentre(new Coordinate(0, this.separationDist));

    final Geometry g2 = gsf.createSineStar().getBoundary();

    return new Geometry[] {
      g, g2
    };

  }

  public void test() {

    // test(5000);
    // test(8001);

    test(10);
    test(10);
    test(100);
    test(500);
    test(1000);
    test(5000);
    test(10000);
    test(50000);
    test(100000);
  }

  public void test(final Geometry[] geom) {
    final Stopwatch sw = new Stopwatch();
    final double dist = 0.0;
    final double dist2 = 0.0;
    for (int i = 0; i < MAX_ITER; i++) {

      // dist = geom[0].distance(geom[1]);
      // dist = SortedBoundsFacetDistance.distance(g1, g2);
      // dist2 = BranchAndBoundFacetDistance.distance(geom[0], geom[1]);
      // if (dist != dist2) System.out.println("distance discrepancy found!");

      computeDistanceToAllPoints(geom);
    }
    if (!this.verbose) {
      System.out.println(sw.getTimeString());
    }
    if (this.verbose) {
      System.out.println("Finished in " + sw.getTimeString());
      System.out.println("       (Distance = " + dist + ")");
    }
  }

  public void test(final int nPts) {

    // Geometry[] geom = createCircles(nPts);
    final Geometry[] geom = createSineStars(nPts);

    if (this.verbose) {
      System.out.println("Running with " + nPts + " points");
    }
    if (!this.verbose) {
      System.out.print(nPts + ": ");
    }
    test(geom);
  }

  public void test2() {
    this.verbose = false;

    for (int i = 100; i <= 2000; i += 100) {
      test(i);
    }
  }
}