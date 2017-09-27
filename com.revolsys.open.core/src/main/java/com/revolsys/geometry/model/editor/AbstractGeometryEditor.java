package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.util.Exceptions;

public abstract class AbstractGeometryEditor implements GeometryEditor {
  private static final long serialVersionUID = 1L;

  private AbstractGeometryEditor parentEditor;

  private GeometryFactory geometryFactory;

  private boolean modified;

  public AbstractGeometryEditor(final AbstractGeometryEditor parentEditor,
    final Geometry geometry) {
    this(geometry.getGeometryFactory());
    this.parentEditor = parentEditor;
  }

  public AbstractGeometryEditor(final Geometry geometry) {
    this(null, geometry);
  }

  public AbstractGeometryEditor(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  /**
   * Creates and returns a full copy of this {@link Polygon} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public Geometry clone() {
    try {
      return (Geometry)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw Exceptions.wrap(e);
    }
  }

  /**
   * Tests whether this geometry is structurally and numerically equal
   * to a given <code>Object</code>.
   * If the argument <code>Object</code> is not a <code>Geometry</code>,
   * the result is <code>false</code>.
   * Otherwise, the result is computed using
   * {@link #equals(2,Geometry)}.
   * <p>
   * This method is provided to fulfill the Java contract
   * for value-based object equality.
   * In conjunction with {@link #hashCode()}
   * it provides semantics which are most useful
   * for using
   * <code>Geometry</code>s as keys and values in Java collections.
   * <p>
   * Note that to produce the expected result the input geometries
   * should be in normal form.  It is the caller's
   * responsibility to perform this where required
   * (using {@link Geometry#norm()
   * or {@link #normalize()} as appropriate).
   *
   * @param other the Object to compare
   * @return true if this geometry is exactly equal to the argument
   *
   * @see #equals(2,Geometry)
   * @see #hashCode()
   * @see #norm()
   * @see #normalize()
   */
  @Override
  public boolean equals(final Object other) {
    if (other instanceof Geometry) {
      final Geometry geometry = (Geometry)other;
      return equals(2, geometry);
    } else {
      return false;
    }
  }

  @Override
  public int getAxisCount() {
    return this.geometryFactory.getAxisCount();
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public GeometryEditor getParentEditor() {
    return this.parentEditor;
  }

  /**
   * Gets a hash code for the Geometry.
   *
   * @return an integer value suitable for use as a hashcode
   */

  @Override
  public int hashCode() {
    return getBoundingBox().hashCode();
  }

  @Override
  public boolean isModified() {
    return this.modified;
  }

  @Override
  public int setAxisCount(final int axisCount) {
    final int oldAxisCount = this.geometryFactory.getAxisCount();
    if (oldAxisCount != axisCount) {
      this.geometryFactory = this.geometryFactory.convertAxisCount(axisCount);
    }
    return oldAxisCount;
  }

  protected void setModified(final boolean modified) {
    this.modified = modified;
    if (this.parentEditor != null) {
      this.parentEditor.setModified(modified);
    }
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
