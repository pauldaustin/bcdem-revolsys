package com.revolsys.swing.map.layer.dataobject;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.event.CDockableStateListener;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.mode.ExtendedMode;

import com.revolsys.beans.InvokeMethodCallable;
import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.algorithm.index.DataObjectQuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.DockingFramesUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.SwingWorkerManager;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.action.enablecheck.AndEnableCheck;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.form.DataObjectLayerForm;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.dataobject.component.MergeObjectsDialog;
import com.revolsys.swing.map.layer.dataobject.renderer.AbstractDataObjectLayerRenderer;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.overlay.AddGeometryCompleteAction;
import com.revolsys.swing.map.overlay.EditGeometryOverlay;
import com.revolsys.swing.map.table.DataObjectLayerTableModel;
import com.revolsys.swing.map.table.DataObjectLayerTablePanel;
import com.revolsys.swing.map.table.DataObjectMetaDataTableModel;
import com.revolsys.swing.map.util.LayerUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.table.BaseJxTable;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.TreeItemRunnable;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractDataObjectLayer extends AbstractLayer implements
  DataObjectLayer, DataObjectFactory, AddGeometryCompleteAction {

  public static final String FORM_FACTORY_EXPRESSION = "formFactoryExpression";

  static {
    final MenuFactory menu = ObjectTreeModel.getMenu(AbstractDataObjectLayer.class);
    menu.addGroup(0, "table");
    menu.addGroup(2, "edit");

    menu.addMenuItem("table", new InvokeMethodAction("View Attributes",
      "View Attributes", SilkIconLoader.getIcon("table_go"), LayerUtil.class,
      "showViewAttributes"));

    menu.addMenuItem("zoom", new InvokeMethodAction("Zoom to Selected",
      "Zoom to Selected", SilkIconLoader.getIcon("magnifier_zoom_selected"),
      LayerUtil.class, "zoomToLayerSelected"));

    final EnableCheck editable = new TreeItemPropertyEnableCheck("editable");
    final EnableCheck readonly = new TreeItemPropertyEnableCheck("readOnly",
      false);
    final EnableCheck hasChanges = new TreeItemPropertyEnableCheck("hasChanges");
    final EnableCheck canAdd = new TreeItemPropertyEnableCheck("canAddObjects");
    final EnableCheck canEdit = new TreeItemPropertyEnableCheck(
      "canEditObjects");
    final EnableCheck canDelete = new TreeItemPropertyEnableCheck(
      "canDeleteObjects");
    final EnableCheck hasSelectedObjects = new TreeItemPropertyEnableCheck(
      "hasSelectedObjects");
    final EnableCheck canMergeObjects = new TreeItemPropertyEnableCheck(
      "canMergeObjects");

    menu.addCheckboxMenuItem("edit", new InvokeMethodAction("Editable",
      "Editable", SilkIconLoader.getIcon("pencil"), readonly, LayerUtil.class,
      "toggleEditable"), editable);

    menu.addMenuItem("edit", TreeItemRunnable.createAction("Save Changes",
      "table_save", hasChanges, "saveChanges"));

    menu.addMenuItem("edit", TreeItemRunnable.createAction("Cancel Changes",
      "table_cancel", hasChanges, "cancelChanges"));

    menu.addMenuItem("edit", TreeItemRunnable.createAction("Add New Record",
      "table_row_insert", canAdd, "addNewObject"));

    menu.addMenuItem("edit", TreeItemRunnable.createAction(
      "Delete Selected Records", "table_row_delete", new AndEnableCheck(
        hasSelectedObjects, canDelete), "deleteSelectedObjects"));

    menu.addMenuItem("edit", TreeItemRunnable.createAction(
      "Merged Selected Records", "shape_group", canMergeObjects,
      "mergeSelectedObjects"));

    menu.addMenuItem("layer", 0, "Layer Style", "palette", LayerUtil.class,
      "showProperties", "Style");

  }

  private BoundingBox boundingBox = new BoundingBox();

  private boolean canAddObjects = true;

  private boolean canDeleteObjects = true;

  private boolean canEditObjects = true;

  private Set<LayerDataObject> deletedObjects = new LinkedHashSet<LayerDataObject>();

  private final Object editSync = new Object();

  private Map<DataObject, Window> forms = new HashMap<DataObject, Window>();

  private DataObjectMetaData metaData;

  private Set<LayerDataObject> modifiedObjects = new LinkedHashSet<LayerDataObject>();

  private Set<LayerDataObject> newObjects = new LinkedHashSet<LayerDataObject>();

  protected Query query;

  private Set<LayerDataObject> selectedObjects = new LinkedHashSet<LayerDataObject>();

  private DataObjectQuadTree selectedObjectsIndex;

  private List<String> columnNames;

  private List<String> columnNameOrder = Collections.emptyList();

  public AbstractDataObjectLayer() {
    this("");
  }

  public AbstractDataObjectLayer(final DataObjectMetaData metaData) {
    this(metaData.getTypeName());
    setMetaData(metaData);
  }

  public AbstractDataObjectLayer(final String name) {
    this(name, GeometryFactory.getFactory(4326));
    setReadOnly(false);
    setSelectSupported(true);
    setQuerySupported(true);
    setRenderer(new GeometryStyleRenderer(this));
  }

  public AbstractDataObjectLayer(final String name,
    final GeometryFactory geometryFactory) {
    super(name);
    setGeometryFactory(geometryFactory);
  }

  @Override
  public LayerDataObject addComplete(final EditGeometryOverlay overlay,
    final Geometry geometry) {
    final DataObjectMetaData metaData = getMetaData();
    final String geometryAttributeName = metaData.getGeometryAttributeName();
    final Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put(geometryAttributeName, geometry);
    return showAddForm(parameters);
  }

  protected void addModifiedObject(final LayerDataObject object) {
    synchronized (modifiedObjects) {
      modifiedObjects.add(object);
    }
  }

  @Override
  public void addNewObject() {
    final DataObjectMetaData metaData = getMetaData();
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    if (geometryAttribute == null) {
      showAddForm(null);
    } else {
      final MapPanel map = MapPanel.get(this);
      if (map != null) {
        final EditGeometryOverlay addGeometryOverlay = map.getMapOverlay(EditGeometryOverlay.class);
        synchronized (addGeometryOverlay) {
          // TODO what if there is another feature being edited?
          addGeometryOverlay.addObject(this, this);
          // TODO cancel action
        }
      }
    }
  }

  protected void addSelectedObject(final LayerDataObject object) {
    if (isLayerObject(object)) {
      clearSelectedObjectsIndex();
      selectedObjects.add(object);
    }
  }

  @Override
  public void addSelectedObjects(
    final Collection<? extends LayerDataObject> objects) {
    for (final LayerDataObject object : objects) {
      addSelectedObject(object);
    }
    fireSelected();
  }

  @Override
  public void addSelectedObjects(final LayerDataObject... objects) {
    addSelectedObjects(Arrays.asList(objects));
  }

  public void cancelChanges() {
    synchronized (editSync) {
      internalCancelChanges();
      refresh();
    }
  }

  protected void clearChanges() {
    clearSelectedObjects();
    newObjects = new LinkedHashSet<LayerDataObject>();
    modifiedObjects = new LinkedHashSet<LayerDataObject>();
    deletedObjects = new LinkedHashSet<LayerDataObject>();
  }

  @Override
  public void clearSelectedObjects() {
    selectedObjects = new LinkedHashSet<LayerDataObject>();
    firePropertyChange("selected", true, false);
  }

  protected void clearSelectedObjectsIndex() {
    selectedObjectsIndex = null;
  }

  public <V extends LayerDataObject> V copyObject(final V object) {
    final LayerDataObject copy = createObject();
    copy.setValues(object);
    copy.setIdValue(null);
    return (V)copy;
  }

  @Override
  public LayerDataObject createDataObject(final DataObjectMetaData metaData) {
    if (metaData.equals(getMetaData())) {
      return new LayerDataObject(this);
    } else {
      throw new IllegalArgumentException("Cannot create objects for "
        + metaData);
    }
  }

  protected DataObjectLayerForm createDefaultForm(final LayerDataObject object) {
    return new DataObjectLayerForm(this, object);
  }

  public DataObjectLayerForm createForm(final LayerDataObject object) {
    final String formFactoryExpression = getProperty(FORM_FACTORY_EXPRESSION);
    if (StringUtils.hasText(formFactoryExpression)) {
      try {
        final SpelExpressionParser parser = new SpelExpressionParser();
        final Expression expression = parser.parseExpression(formFactoryExpression);
        final EvaluationContext context = new StandardEvaluationContext(this);
        context.setVariable("object", object);
        return expression.getValue(context, DataObjectLayerForm.class);
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error(
          "Unable to create form for " + this, e);
        return null;
      }
    } else {
      return createDefaultForm(object);
    }
  }

  @Override
  public LayerDataObject createObject() {
    if (!isReadOnly() && isEditable() && isCanAddObjects()) {
      final LayerDataObject object = createDataObject(getMetaData());
      newObjects.add(object);
      return object;
    } else {
      return null;
    }
  }

  @Override
  public TabbedValuePanel createPropertiesPanel() {
    final TabbedValuePanel propertiesPanel = super.createPropertiesPanel();

    final DataObjectMetaData metaData = getMetaData();
    final BaseJxTable fieldTable = DataObjectMetaDataTableModel.createTable(metaData);

    final JPanel fieldPanel = new JPanel(new BorderLayout());
    final JScrollPane fieldScroll = new JScrollPane(fieldTable);
    fieldPanel.add(fieldScroll, BorderLayout.CENTER);
    propertiesPanel.addTab("Fields", fieldPanel);

    final LayerRenderer<? extends Layer> renderer = getRenderer();
    final ValueField stylePanel = renderer.createStylePanel();
    if (stylePanel != null) {
      propertiesPanel.addTab(stylePanel);
    }
    return propertiesPanel;
  }

  @Override
  public Component createTablePanel() {
    final JTable table = DataObjectLayerTableModel.createTable(this);
    if (table == null) {
      return null;
    } else {
      return new DataObjectLayerTablePanel(this, table);
    }
  }

  @Override
  public void delete() {
    super.delete();
    for (final Window window : forms.values()) {
      if (window != null) {
        window.dispose();
      }
    }
    this.deletedObjects = null;
    this.forms = null;
    this.metaData = null;
    this.modifiedObjects = null;
    this.newObjects = null;
    this.query = null;
    this.selectedObjects = null;
    System.gc();
  }

  protected void deleteObject(final LayerDataObject object) {
    final boolean trackDeletions = true;
    deleteObject(object, trackDeletions);
  }

  protected void deleteObject(final LayerDataObject object,
    final boolean trackDeletions) {
    if (isLayerObject(object)) {
      clearSelectedObjectsIndex();
      if (!newObjects.remove(object)) {
        modifiedObjects.remove(object);
        if (trackDeletions) {
          deletedObjects.add(object);
        }
        selectedObjects.remove(object);
      }
      object.setState(DataObjectState.Deleted);
    }
  }

  @Override
  public void deleteObjects(final Collection<? extends LayerDataObject> objects) {
    synchronized (editSync) {
      unselectObjects(objects);
      for (final LayerDataObject object : objects) {
        deleteObject(object);
      }
    }
    fireObjectsChanged();
  }

  @Override
  public void deleteObjects(final LayerDataObject... objects) {
    deleteObjects(Arrays.asList(objects));
  }

  public void deleteSelectedObjects() {
    final List<LayerDataObject> selectedObjects = getSelectedObjects();
    deleteObjects(selectedObjects);
  }

  @Override
  protected boolean doSaveChanges() {
    return true;
  }

  protected void fireObjectsChanged() {
    firePropertyChange("objectsChanged", false, true);
  }

  protected void fireSelected() {
    final boolean selected = !selectedObjects.isEmpty();
    firePropertyChange("selected", !selected, selected);
    firePropertyChange("selectionCount", -1, selectedObjects.size());
  }

  @Override
  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  @Override
  public int getChangeCount() {
    int changeCount = 0;
    changeCount += newObjects.size();
    changeCount += modifiedObjects.size();
    changeCount += deletedObjects.size();
    return changeCount;
  }

  @Override
  public List<LayerDataObject> getChanges() {
    synchronized (editSync) {
      final List<LayerDataObject> objects = new ArrayList<LayerDataObject>();
      objects.addAll(newObjects);
      objects.addAll(modifiedObjects);
      objects.addAll(deletedObjects);
      return objects;
    }
  }

  @Override
  public List<String> getColumnNames() {
    synchronized (this) {
      if (columnNames == null) {
        final Set<String> columnNames = new LinkedHashSet<String>(
          columnNameOrder);
        final DataObjectMetaData metaData = getMetaData();
        final List<String> attributeNames = metaData.getAttributeNames();
        columnNames.addAll(attributeNames);
        this.columnNames = new ArrayList<String>(columnNames);
        updateColumnNames();
      }
    }
    return columnNames;
  }

  public CoordinateSystem getCoordinateSystem() {
    return getGeometryFactory().getCoordinateSystem();
  }

  @Override
  public List<LayerDataObject> getDataObjects(final BoundingBox boundingBox) {
    return Collections.emptyList();
  }

  @Override
  public DataObjectStore getDataStore() {
    return getMetaData().getDataObjectStore();
  }

  public Set<LayerDataObject> getDeletedObjects() {
    return new LinkedHashSet<LayerDataObject>(deletedObjects);
  }

  public String getGeometryAttributeName() {
    return getMetaData().getGeometryAttributeName();
  }

  @Override
  public DataType getGeometryType() {
    final DataObjectMetaData metaData = getMetaData();
    if (metaData == null) {
      return null;
    } else {
      final Attribute geometryAttribute = metaData.getGeometryAttribute();
      if (geometryAttribute == null) {
        return null;
      } else {
        return geometryAttribute.getType();
      }
    }
  }

  @Override
  public List<LayerDataObject> getMergeableSelectedObjects() {
    final List<LayerDataObject> selectedObjects = getSelectedObjects();
    for (final Iterator<LayerDataObject> iterator = selectedObjects.iterator(); iterator.hasNext();) {
      final LayerDataObject mergedDataObject = iterator.next();
      if (mergedDataObject.isDeleted()) {
        iterator.remove();
      }
    }
    return selectedObjects;
  }

  @Override
  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public Set<LayerDataObject> getModifiedObjects() {
    if (modifiedObjects == null) {
      return Collections.emptySet();
    } else {
      return new LinkedHashSet<LayerDataObject>(modifiedObjects);
    }
  }

  @Override
  public int getNewObjectCount() {
    if (newObjects == null) {
      return 0;
    } else {
      return newObjects.size();
    }
  }

  @Override
  public List<LayerDataObject> getNewObjects() {
    if (newObjects == null) {
      return Collections.emptyList();
    } else {
      return new ArrayList<LayerDataObject>(newObjects);
    }
  }

  @Override
  public LayerDataObject getObject(final int row) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LayerDataObject getObjectById(final Object id) {
    return null;
  }

  @Override
  public List<LayerDataObject> getObjects() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Query getQuery() {
    if (query == null) {
      return null;
    } else {
      return query.clone();
    }
  }

  @Override
  public int getRowCount() {
    final DataObjectMetaData metaData = getMetaData();
    final Query query = new Query(metaData);
    return getRowCount(query);
  }

  @Override
  public int getRowCount(final Query query) {
    LoggerFactory.getLogger(getClass()).error("Get row count not implemented");
    return 0;
  }

  @Override
  public BoundingBox getSelectedBoundingBox() {
    BoundingBox boundingBox = super.getSelectedBoundingBox();
    for (final DataObject object : getSelectedObjects()) {
      final Geometry geometry = object.getGeometryValue();
      boundingBox = boundingBox.expandToInclude(geometry);
    }
    return boundingBox;
  }

  @Override
  public List<LayerDataObject> getSelectedObjects() {
    return new ArrayList<LayerDataObject>(selectedObjects);
  }

  @Override
  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public List<LayerDataObject> getSelectedObjects(final BoundingBox boundingBox) {
    final DataObjectQuadTree index = getSelectedObjectsIndex();
    return (List)index.queryIntersects(boundingBox);
  }

  protected DataObjectQuadTree getSelectedObjectsIndex() {
    if (selectedObjectsIndex == null) {
      final List<LayerDataObject> selectedObjects = getSelectedObjects();
      final DataObjectQuadTree index = new DataObjectQuadTree(
        getProject().getGeometryFactory(), selectedObjects);
      this.selectedObjectsIndex = index;
    }
    return selectedObjectsIndex;
  }

  @Override
  public int getSelectionCount() {
    return selectedObjects.size();
  }

  protected boolean hasPermission(final String permission) {
    if (metaData == null) {
      return true;
    } else {
      final Collection<String> permissions = metaData.getProperty("permissions");
      if (permissions == null) {
        return true;
      } else {
        final boolean hasPermission = permissions.contains(permission);
        return hasPermission;
      }
    }
  }

  protected void internalCancelChanges() {
    clearChanges();
  }

  @Override
  public boolean isCanAddObjects() {
    return !super.isReadOnly() && isEditable() && canAddObjects
      && hasPermission("INSERT");
  }

  @Override
  public boolean isCanDeleteObjects() {
    return !super.isReadOnly() && isEditable() && canDeleteObjects
      && hasPermission("DELETE");
  }

  @Override
  public boolean isCanEditObjects() {
    return !super.isReadOnly() && isEditable() && canEditObjects
      && hasPermission("UPDATE");
  }

  public boolean isCanMergeObjects() {
    if (isCanAddObjects()) {
      if (isCanDeleteObjects()) {
        if (isCanDeleteObjects()) {
          final DataType geometryType = getGeometryType();
          if (DataTypes.LINE_STRING.equals(geometryType)) {
            if (getMergeableSelectedObjects().size() > 1) {
              return true;
            }
          } // TODO allow merging other type
        }
      }
    }
    return false;
  }

  @Override
  public boolean isDeleted(final LayerDataObject object) {
    return deletedObjects != null && deletedObjects.contains(object);
  }

  @Override
  public boolean isHasChanges() {
    if (isEditable()) {
      synchronized (editSync) {
        if (!newObjects.isEmpty()) {
          return true;
        } else if (!modifiedObjects.isEmpty()) {
          return true;
        } else if (!deletedObjects.isEmpty()) {
          return true;
        } else {
          return false;
        }
      }
    } else {
      return false;
    }
  }

  public boolean isHasSelectedObjects() {
    return getSelectionCount() > 0;
  }

  @Override
  public boolean isHidden(final LayerDataObject object) {
    if (isCanDeleteObjects() && isDeleted(object)) {
      return true;
    } else if (isSelected(object)) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isLayerObject(final DataObject object) {
    if (object.getMetaData() == getMetaData()) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean isModified(final LayerDataObject object) {
    return modifiedObjects.contains(object);
  }

  @Override
  public boolean isNew(final LayerDataObject object) {
    return newObjects.contains(object);
  }

  @Override
  public boolean isReadOnly() {
    if (super.isReadOnly()) {
      return true;
    } else {
      if (canAddObjects && hasPermission("INSERT")) {
        return false;
      } else if (canDeleteObjects && hasPermission("DELETE")) {
        return false;
      } else if (canEditObjects && hasPermission("UPDATE")) {
        return false;
      } else {
        return true;
      }
    }
  }

  @Override
  public boolean isSelected(final LayerDataObject object) {
    if (object == null || selectedObjects == null) {
      return false;
    } else {
      return selectedObjects.contains(object);
    }
  }

  @Override
  public boolean isVisible(final LayerDataObject object) {
    if (isVisible()) {
      final AbstractDataObjectLayerRenderer renderer = getRenderer();
      if (renderer != null && renderer.isVisible(object)) {
        return true;
      }
    }
    return false;
  }

  public void mergeSelectedObjects() {
    if (isCanMergeObjects()) {
      SwingUtil.invokeLater(MergeObjectsDialog.class, "showDialog", this);
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    final Object source = event.getSource();
    final String propertyName = event.getPropertyName();
    if (!"errorsUpdated".equals(propertyName)) {
      if (source instanceof LayerDataObject) {
        final LayerDataObject dataObject = (LayerDataObject)source;
        if (dataObject.getLayer() == this) {
          if (EqualsRegistry.equal(propertyName, getGeometryAttributeName())) {
            final Geometry oldGeometry = (Geometry)event.getOldValue();
            updateSpatialIndex(dataObject, oldGeometry);
          }
          clearSelectedObjectsIndex();
          final DataObjectState state = dataObject.getState();
          if (state == DataObjectState.Modified) {
            addModifiedObject(dataObject);
          } else if (state == DataObjectState.Persisted) {
            removeModifiedObject(dataObject);
          }
        }
      }
    }
  }

  @Override
  public List<LayerDataObject> query(final Geometry geometry,
    final double distance) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<LayerDataObject> query(final Query query) {
    throw new UnsupportedOperationException("Query not currently supported");
  }

  protected void removeDeletedObject(final LayerDataObject object) {
    synchronized (deletedObjects) {
      deletedObjects.remove(object);
    }
  }

  protected void removeModifiedObject(final LayerDataObject object) {
    synchronized (modifiedObjects) {
      modifiedObjects.remove(object);
    }
  }

  protected void removeNewObject(final LayerDataObject object) {
    synchronized (newObjects) {
      newObjects.remove(object);
    }
  }

  @Override
  public void revertChanges(final LayerDataObject object) {
    synchronized (modifiedObjects) {
      if (isLayerObject(object)) {
        removeModifiedObject(object);
        deletedObjects.remove(object);
        object.revertChanges();
      }
    }
  }

  @Override
  public boolean saveChanges() {
    synchronized (editSync) {
      final boolean saved = doSaveChanges();
      if (saved) {
        clearChanges();
      }
      refresh();
      return saved;
    }
  }

  @Override
  public boolean saveChanges(final LayerDataObject object) {
    return false;
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setCanAddObjects(final boolean canAddObjects) {
    this.canAddObjects = canAddObjects;
    firePropertyChange("canAddObjects", !isCanAddObjects(), isCanAddObjects());
  }

  public void setCanDeleteObjects(final boolean canDeleteObjects) {
    this.canDeleteObjects = canDeleteObjects;
    firePropertyChange("canDeleteObjects", !isCanDeleteObjects(),
      isCanDeleteObjects());
  }

  public void setCanEditObjects(final boolean canEditObjects) {
    this.canEditObjects = canEditObjects;
    firePropertyChange("canEditObjects", !isCanEditObjects(),
      isCanEditObjects());
  }

  public void setColumnNameOrder(final Collection<String> columnNameOrder) {
    this.columnNameOrder = new ArrayList<String>(columnNameOrder);
  }

  public void setColumnNames(final Collection<String> columnNames) {
    this.columnNames = new ArrayList<String>(columnNames);
    updateColumnNames();
  }

  @Override
  public void setEditable(final boolean editable) {
    if (SwingUtilities.isEventDispatchThread()) {
      SwingWorkerManager.execute("Set editable", this, "setEditable", editable);
    } else {
      synchronized (editSync) {
        if (editable == false) {
          firePropertyChange("preEditable", false, true);
          if (isHasChanges()) {
            final Integer result = InvokeMethodCallable.invokeAndWait(
              JOptionPane.class,
              "showConfirmDialog",
              JOptionPane.getRootFrame(),
              "The layer has unsaved changes. Click Yes to save changes. Click No to discard changes. Click Cancel to continue editing.",
              "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION);

            if (result == JOptionPane.YES_OPTION) {
              if (!saveChanges()) {
                return;
              }
            } else if (result == JOptionPane.NO_OPTION) {
              cancelChanges();
            } else {
              // Don't allow state change if cancelled
              return;
            }

          }
        }
        super.setEditable(editable);
        setCanAddObjects(canAddObjects);
        setCanDeleteObjects(canDeleteObjects);
        setCanEditObjects(canEditObjects);
      }
    }
  }

  @Override
  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    super.setGeometryFactory(geometryFactory);
    if (geometryFactory != null && boundingBox.isNull()) {
      boundingBox = geometryFactory.getCoordinateSystem().getAreaBoundingBox();
    }
  }

  protected void setMetaData(final DataObjectMetaData metaData) {
    this.metaData = metaData;
    if (metaData != null) {

      setGeometryFactory(metaData.getGeometryFactory());
      if (metaData.getGeometryAttributeIndex() == -1) {
        setSelectSupported(false);
        setRenderer(null);
      }
      updateColumnNames();
    }
  }

  @Override
  public void setProperty(final String name, final Object value) {
    if ("style".equals(name)) {
      if (value instanceof Map) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> style = (Map<String, Object>)value;
        final LayerRenderer<DataObjectLayer> renderer = AbstractDataObjectLayerRenderer.getRenderer(
          this, style);
        if (renderer != null) {
          setRenderer(renderer);
        }
      }
    } else {
      super.setProperty(name, value);
    }
  }

  @Override
  public void setQuery(final Query query) {
    final Query oldValue = this.query;
    this.query = query;
    firePropertyChange("query", oldValue, query);
  }

  @Override
  public void setSelectedObjects(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerDataObject> objects = getDataObjects(boundingBox);
      for (final Iterator<LayerDataObject> iterator = objects.iterator(); iterator.hasNext();) {
        final LayerDataObject layerDataObject = iterator.next();
        if (!isVisible(layerDataObject)
          || deletedObjects.contains(layerDataObject)) {
          iterator.remove();
        }
      }
      if (!objects.isEmpty()) {
        showViewAttributes();
      }
      setSelectedObjects(objects);
    }
  }

  @Override
  public void setSelectedObjects(
    final Collection<LayerDataObject> selectedObjects) {
    clearSelectedObjectsIndex();
    this.selectedObjects = new LinkedHashSet<LayerDataObject>(selectedObjects);
    fireSelected();
  }

  @Override
  public void setSelectedObjects(final LayerDataObject... selectedObjects) {
    setSelectedObjects(Arrays.asList(selectedObjects));
  }

  @Override
  public void setSelectedObjectsById(final Object id) {
    final DataObjectMetaData metaData = getMetaData();
    final String idAttributeName = metaData.getIdAttributeName();
    if (idAttributeName == null) {
      clearSelectedObjects();
    } else {
      final Query query = Query.equal(metaData, idAttributeName, id);
      final List<LayerDataObject> objects = query(query);
      setSelectedObjects(objects);
    }
  }

  @Override
  public int setSelectedWithinDistance(final boolean selected,
    final Geometry geometry, final int distance) {
    clearSelectedObjectsIndex();
    final List<LayerDataObject> objects = query(geometry, distance);
    for (final Iterator<LayerDataObject> iterator = objects.iterator(); iterator.hasNext();) {
      final LayerDataObject layerDataObject = iterator.next();
      if (!isVisible(layerDataObject)) {
        iterator.remove();
      }
    }
    if (selected) {
      selectedObjects.addAll(objects);
    } else {
      selectedObjects.removeAll(objects);
    }
    return objects.size();
  }

  @Override
  public LayerDataObject showAddForm(final Map<String, Object> parameters) {
    if (isCanAddObjects()) {
      final LayerDataObject newObject = createObject();
      if (parameters != null) {
        newObject.setValues(parameters);
      }
      final DataObjectLayerForm form = createForm(newObject);
      if (form == null) {
        return null;
      } else {
        final LayerDataObject object = form.showAddDialog();
        return object;
      }
    } else {
      final Window window = SwingUtil.getActiveWindow();
      JOptionPane.showMessageDialog(window,
        "Adding records is not enabled for the " + getPath()
          + " layer. If possible make the layer editable", "Cannot Add Record",
        JOptionPane.ERROR_MESSAGE);
      return null;
    }

  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends JComponent> V showForm(final LayerDataObject object) {
    if (object == null) {
      return null;
    } else {
      synchronized (forms) {
        Window window = forms.get(object);
        if (window == null) {
          final Object id = object.getIdValue();
          final Component form = createForm(object);
          if (form == null) {
            return null;
          } else {
            String title;
            if (object.getState() == DataObjectState.New) {
              title = "Add NEW " + getName();
            } else if (isCanEditObjects()) {
              title = "Edit " + getName() + " #" + id;
            } else {
              title = "View " + getName() + " #" + id;
              if (form instanceof DataObjectLayerForm) {
                final DataObjectLayerForm dataObjectForm = (DataObjectLayerForm)form;
                dataObjectForm.setEditable(false);
              }
            }
            window = new JFrame(title);
            window.add(new JScrollPane(form));
            window.pack();
            window.setLocation(50, 50);
            // TODO smart location
            window.setVisible(true);
            forms.put(object, window);
            window.addWindowListener(new WindowAdapter() {
              @Override
              public void windowClosing(final WindowEvent e) {
                form.removeNotify();
                removeForm(object);
              }
            });
            window.requestFocus();
            return (V)form;
          }
        } else {
          window.requestFocus();
          final Component component = window.getComponent(0);
          if (component instanceof JScrollPane) {
            final JScrollPane scrollPane = (JScrollPane)component;
            return (V)scrollPane.getComponent(0);
          }
          return null;
        }
      }
    }

  }

  @Override
  public void showViewAttributes() {
    DefaultSingleCDockable dockable = getProperty("TableView");
    if (dockable == null) {
      final Project project = getProject();

      final Component component = createTablePanel();
      if (component != null) {
        final String id = getClass().getName() + "." + getId();
        dockable = DockingFramesUtil.addDockable(project,
          MapPanel.MAP_TABLE_WORKING_AREA, id, getName(), component);

        dockable.setCloseable(true);
        setProperty("TableView", dockable);
        dockable.addCDockableStateListener(new CDockableStateListener() {
          @Override
          public void extendedModeChanged(final CDockable dockable,
            final ExtendedMode mode) {
          }

          @Override
          public void visibilityChanged(final CDockable dockable) {
            final boolean visible = dockable.isVisible();
            if (!visible) {
              dockable.getControl()
                .getOwner()
                .remove((SingleCDockable)dockable);
              setProperty("TableView", null);
            }
          }
        });
        dockable.toFront();
      }
    }
  }

  @Override
  public void unselectObjects(
    final Collection<? extends LayerDataObject> objects) {
    clearSelectedObjectsIndex();
    selectedObjects.removeAll(objects);
    fireSelected();
  }

  @Override
  public void unselectObjects(final LayerDataObject... objects) {
    unselectObjects(Arrays.asList(objects));
  }

  protected void updateColumnNames() {
    if (columnNames != null && this.metaData != null) {
      final List<String> attributeNames = this.metaData.getAttributeNames();
      columnNames.retainAll(attributeNames);
    }
  }

  protected void updateSpatialIndex(final LayerDataObject object,
    final Geometry oldGeometry) {
  }

  protected void removeForm(final LayerDataObject object) {
    forms.remove(object);
  }
}
