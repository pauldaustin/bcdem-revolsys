package com.revolsys.swing.map.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.ListCoordinatesList;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.map.layer.dataobject.style.MarkerStyle;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

@SuppressWarnings("serial")
public class EditGeometryOverlay extends SelectFeaturesOverlay {

  private final Project project;

  private final Viewport2D viewport;

  private ListCoordinatesList points = new ListCoordinatesList(2);

  private Geometry geometry;

  private Point firstPoint;

  private Point previousPoint;

  private GeometryFactory geometryFactory;

  private static final MarkerStyle XOR_POINT_STYLE = MarkerStyle.marker(
    "ellipse", 10, new Color(0, 0, 255), 1, new Color(0, 0, 255));

  private static final GeometryStyle XOR_LINE_STYLE = GeometryStyle.line(
    new Color(0, 0, 255), 2);

  private DataType geometryDataType;

  private DataObjectLayer editFeatureLayer;

  private Geometry completedGeometry;

  private int actionId = 0;

  private Geometry xorGeometry;

  private EventListenerList completedActions = new EventListenerList();

  public boolean hasEditableLayers() {
    return hasSelectableLayers();
  }

  @Override
  protected boolean isSelectable(DataObjectLayer dataObjectLayer) {
    return isEditable(dataObjectLayer);
  }

  protected boolean isEditable(DataObjectLayer dataObjectLayer) {
    return dataObjectLayer.isCanEditObjects();
  }

  public EditGeometryOverlay(final MapPanel map) {
    super(map, new Color(0, 255, 255));
    this.viewport = map.getViewport();
    this.project = map.getProject();
    this.geometryFactory = viewport.getGeometryFactory();

    project.addPropertyChangeListener(this);

    map.addMapOverlay(this);
    updateSelectableLayers();
  }

  public void addCompletedAction(final ActionListener listener) {
    completedActions.add(ActionListener.class, listener);
  }
  
  @Override
  public void selectObjects(BoundingBox boundingBox) {
    for (final DataObjectLayer layer : getEditableLayers()) {
      layer.setEditingObjects(boundingBox);
    }
  }
  public List<DataObjectLayer> getEditableLayers() {
    return getSelectableLayers();
  }
  public void clearCompletedActions() {
    completedActions = new EventListenerList();
  }

  protected Geometry createGeometry() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final ListCoordinatesList points = getPoints();
    Geometry geometry = null;
    final int size = points.size();
    if (size == 1) {
      geometry = geometryFactory.createPoint(points);
    } else if (size == 2 || DataTypes.LINE_STRING.equals(geometryDataType)
      || DataTypes.MULTI_LINE_STRING.equals(geometryDataType)) {
      geometry = geometryFactory.createLineString(points);
    } else if (DataTypes.POLYGON.equals(geometryDataType)) {
      final Coordinates endPoint = points.get(0);
      final CoordinatesList ring = CoordinatesListUtil.subList(points, null, 0,
        size, endPoint);
      geometry = geometryFactory.createPolygon(ring);
    }
    return geometry;
  }

  protected void drawXorGeometry(final Graphics2D graphics) {
    if (xorGeometry != null) {
      graphics.setXORMode(Color.WHITE);
      if (xorGeometry instanceof Point) {
        Point point = (Point)xorGeometry;
        MarkerStyleRenderer.renderMarker(viewport, graphics, point,
          XOR_POINT_STYLE);
      } else {
        GeometryStyleRenderer.renderGeometry(viewport, graphics, xorGeometry,
          XOR_LINE_STYLE);
      }
    }
  }

  protected void fireActionPerformed(final String command) {
    final ActionEvent actionEvent = new ActionEvent(this, actionId++, command);
    for (final ActionListener listener : completedActions.getListeners(ActionListener.class)) {
      listener.actionPerformed(actionEvent);
    }
  }

  public DataObjectLayer getEditFeatureLayer() {
    return editFeatureLayer;
  }

  @SuppressWarnings("unchecked")
  public <G extends Geometry> G getCompletedGeometry() {
    return (G)completedGeometry;
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  protected Point getPoint(final MouseEvent event) {
    final java.awt.Point eventPoint = event.getPoint();
    final Point point = viewport.toModelPoint(eventPoint);
    return geometryFactory.copy(point);
  }

  protected ListCoordinatesList getPoints() {
    return points;
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    if (SwingUtilities.isLeftMouseButton(event)) {
      // final Point point = getPoint(event);
      // points.add(point);
      // final int size = points.size();
      // if (size == 1) {
      // firstPoint = point;
      // } else {
      // previousPoint = point;
      // }
      //
      // geometry = createGeometry();
      // xorGeometry = null;
      // event.consume();
      // if (DataTypes.POINT.equals(geometryDataType)) {
      // actionEditGeometryCompleted();
      // }
      // if (event.getClickCount() == 2) {
      // actionEditGeometryCompleted();
      // }
      repaint();
    }
  }

  protected void actionEditGeometryCompleted() {
    if (isGeometryValid()) {
      firstPoint = null;
      previousPoint = null;
      xorGeometry = null;
      this.completedGeometry = geometry;
      fireActionPerformed("Geometry Complete");
      geometry = null;
      points = new ListCoordinatesList(2);
    }
  }

  protected boolean isGeometryValid() {
    if (DataTypes.POINT.equals(geometryDataType)) {
      if (geometry instanceof Point) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.MULTI_POINT.equals(geometryDataType)) {
      if ((geometry instanceof Point) || (geometry instanceof MultiPoint)) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.LINE_STRING.equals(geometryDataType)) {
      if (geometry instanceof LineString) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.MULTI_LINE_STRING.equals(geometryDataType)) {
      if ((geometry instanceof LineString)
        || (geometry instanceof MultiLineString)) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.POLYGON.equals(geometryDataType)) {
      if (geometry instanceof Polygon) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.MULTI_POLYGON.equals(geometryDataType)) {
      if ((geometry instanceof Polygon) || (geometry instanceof MultiPolygon)) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
    super.mouseDragged(event);
  }

  @Override
  public void mouseEntered(final MouseEvent event) {
  }

  @Override
  public void mouseExited(final MouseEvent event) {
  }

  @Override
  public void mouseMoved(final MouseEvent event) {
    // final Point point = getPoint(event);
    // final Graphics2D graphics = (Graphics2D)getGraphics();
    // drawXorGeometry(graphics);
    // if (firstPoint == null) {
    // xorGeometry = geometryFactory.copy(point);
    // } else if (previousPoint == null) {
    // xorGeometry = geometryFactory.createLineString(firstPoint, point);
    // } else if (DataTypes.LINE_STRING.equals(geometryDataType)
    // || DataTypes.MULTI_LINE_STRING.equals(geometryDataType)) {
    // xorGeometry = geometryFactory.createLineString(previousPoint, point);
    // } else {
    // xorGeometry = geometryFactory.createLineString(previousPoint, point,
    // firstPoint);
    // }
    // drawXorGeometry(graphics);
  }

  private DataObject editObject;

  @Override
  public void mousePressed(final MouseEvent event) {
    // TODO Don't call super depending on mode
    super.mousePressed(event);
  }


  public boolean isSelectEvent(final MouseEvent event) {
    if (SwingUtilities.isLeftMouseButton(event)) {
      final boolean keyPress = event.isAltDown();
      return keyPress;
    }
    return false;
  }
  @Override
  public void mouseReleased(final MouseEvent event) {
    super.mouseReleased(event);
  }

  protected Collection<DataObject> getSelectedObjects(
    final DataObjectLayer layer) {
    return layer.getEditingObjects();
  }

  @Override
  public void paintComponent(final Graphics graphics) {
    super.paintComponent(graphics);
    final Graphics2D graphics2d = (Graphics2D)graphics;

    if (geometry != null) {
      final GeometryFactory viewGeometryFactory = viewport.getGeometryFactory();
      final Geometry mapGeometry = viewGeometryFactory.copy(geometry);
      if (!(geometry instanceof Point)) {
        GeometryStyleRenderer.renderGeometry(viewport, graphics2d, mapGeometry,
          getHighlightStyle());
        GeometryStyleRenderer.renderOutline(viewport, graphics2d, mapGeometry,
          getOutlineStyle());
      }
      MarkerStyleRenderer.renderMarkerVertices(viewport, graphics2d,
        mapGeometry, getVertexStyle());
    }
    drawXorGeometry(graphics2d);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    final String propertyName = event.getPropertyName();
    if ("editable".equals(propertyName)) {
      updateSelectableLayers();
      repaint();
    }
  }

  /**
   * Set the layer that a new feature is to be added to.
   * 
   * @param editFeatureLayer 
   */
  public void setEditFeatureLayer(final DataObjectLayer editFeatureLayer) {
    // TODO handle case where feature is being edited or added
    if (editFeatureLayer == null) {
      this.editFeatureLayer = editFeatureLayer;
      setEnabled(false);
    } else {
      final DataObjectMetaData metaData = editFeatureLayer.getMetaData();
      final Attribute geometryAttribute = metaData.getGeometryAttribute();
      if (geometryAttribute == null) {
        this.editFeatureLayer = null;
        setEnabled(false);
      } else {
        this.editFeatureLayer = editFeatureLayer;
        this.geometryFactory = metaData.getGeometryFactory();
        this.geometryDataType = geometryAttribute.getType();
        setEnabled(true);
      }
    }
  }

  public void setCompletedAction(final ActionListener listener) {
    clearCompletedActions();
    addCompletedAction(listener);
  }

}
