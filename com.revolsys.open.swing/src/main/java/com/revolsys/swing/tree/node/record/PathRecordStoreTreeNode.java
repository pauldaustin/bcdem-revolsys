package com.revolsys.swing.tree.node.record;

import java.awt.TextField;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import com.revolsys.io.Paths;
import com.revolsys.record.io.RecordStoreConnectionManager;
import com.revolsys.record.io.RecordStoreConnectionMapProxy;
import com.revolsys.record.io.RecordStoreConnectionRegistry;
import com.revolsys.record.io.RecordStoreProxy;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.swing.Borders;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeNodes;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.file.PathTreeNode;
import com.revolsys.util.Property;

public class PathRecordStoreTreeNode extends PathTreeNode
  implements RecordStoreProxy, RecordStoreConnectionMapProxy {
  private static final MenuFactory MENU = new MenuFactory("File Record Store");

  static {
    addRefreshMenuItem(MENU);

    TreeNodes.addMenuItem(MENU, "default", "Add Record Store Connection", "link_add",
      PathRecordStoreTreeNode::isExists, PathRecordStoreTreeNode::addRecordStoreConnection);
  }

  public PathRecordStoreTreeNode(final Path path) {
    super(path);
    setType("Record Store");
    setName(Paths.getFileName(path));
    super.setIcon(PathTreeNode.ICON_FILE_DATABASE);
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public void addRecordStoreConnection() {
    final Path path = getPath();
    final String fileName = Paths.getBaseName(path);

    final ValueField panel = new ValueField();
    panel.setTitle("Add Record Store Connection");
    Borders.titled(panel, "Record Store Connection");

    SwingUtil.addLabel(panel, "File");
    final JLabel fileLabel = new JLabel(Paths.toPathString(path));
    panel.add(fileLabel);

    SwingUtil.addLabel(panel, "Name");
    final TextField nameField = new TextField(20);
    panel.add(nameField);
    nameField.setText(fileName);

    SwingUtil.addLabel(panel, "Record Store Connections");

    final List<RecordStoreConnectionRegistry> registries = new ArrayList<>();
    for (final RecordStoreConnectionRegistry registry : RecordStoreConnectionManager.get()
      .getVisibleConnectionRegistries()) {
      if (!registry.isReadOnly()) {
        registries.add(registry);
      }
    }
    final JComboBox registryField = new JComboBox(
      new Vector<RecordStoreConnectionRegistry>(registries));

    panel.add(registryField);

    GroupLayouts.makeColumns(panel, 2, true);
    panel.showDialog();
    if (panel.isSaved()) {
      final RecordStoreConnectionRegistry registry = (RecordStoreConnectionRegistry)registryField
        .getSelectedItem();
      String connectionName = nameField.getText();
      if (!Property.hasValue(connectionName)) {
        connectionName = fileName;
      }
      final String baseConnectionName = connectionName;
      int i = 0;
      while (registry.getConnection(connectionName) != null) {
        connectionName = baseConnectionName + i;
        i++;
      }
      final Map<String, Object> connection = getRecordStoreConnectionMap();
      final Map<String, Object> config = new HashMap<>();
      config.put("name", connectionName);
      config.put("connection", connection);
      registry.newConnection(config);
    }
  }

  @Override
  protected List<BaseTreeNode> doLoadChildren() {
    final RecordStore recordStore = getRecordStore();
    return RecordStoreConnectionTreeNode.getChildren(getRecordStoreConnectionMap(), recordStore);
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends RecordStore> V getRecordStore() {
    final Path path = getPath();
    return (V)RecordStoreConnectionManager.getRecordStore(path);
  }

  @Override
  public Map<String, Object> getRecordStoreConnectionMap() {
    final BaseTreeNode parent = getParent();
    final Path path = getPath();
    final URL url = getUrl(parent, path);

    return Collections.<String, Object> singletonMap("url", url.toString());
  }

  @Override
  public boolean isAllowsChildren() {
    return true;
  }

  @Override
  protected void setIcon(final Icon icon) {
    super.setIcon(PathTreeNode.ICON_FILE_DATABASE);
  }

}
