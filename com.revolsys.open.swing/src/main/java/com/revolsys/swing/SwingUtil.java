package com.revolsys.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.MenuContainer;
import java.awt.MenuItem;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;
import java.util.Date;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.FileUtil;
import com.revolsys.swing.field.CodeTableComboBoxModel;
import com.revolsys.swing.field.CodeTableObjectToStringConverter;
import com.revolsys.swing.field.DateTextField;
import com.revolsys.swing.field.NumberTextField;
import com.revolsys.util.PreferencesUtil;

public class SwingUtil {

  public static JLabel addLabel(final Container container, final String text) {
    final JLabel label = new JLabel(text);
    label.setFont(label.getFont().deriveFont(Font.BOLD));
    container.add(label);
    return label;
  }

  public static JFileChooser createFileChooser(Class<?> preferencesClass,
    String preferenceName) {
    final JFileChooser fileChooser = new JFileChooser();
    String currentDirectoryName = PreferencesUtil.getString(preferencesClass, preferenceName);
    if (StringUtils.hasText(currentDirectoryName)) {
      File directory = new File(currentDirectoryName);
      if (directory.exists() && directory.canRead()) {
        fileChooser.setCurrentDirectory(directory);
      }
    }
    return fileChooser;
  }

  public static void saveFileChooserDirectory(Class<?> preferencesClass,
    String preferenceName, JFileChooser fileChooser) {
    File currentDirectory = fileChooser.getCurrentDirectory();
    String path = FileUtil.getCanonicalPath(currentDirectory);
    PreferencesUtil.setString(preferencesClass, preferenceName, path);
  }

  @SuppressWarnings("unchecked")
  public static <T extends JComponent> T createField(
    final DataObjectMetaData metaData, final String fieldName,
    final boolean enabled) {
    JComponent field;
    final Attribute attribute = metaData.getAttribute(fieldName);
    if (attribute == null) {
      throw new IllegalArgumentException("Cannot find field " + fieldName);
    } else {
      final boolean required = attribute.isRequired();
      final int length = attribute.getLength();
      final CodeTable codeTable = metaData.getCodeTableByColumn(fieldName);
      final DataType type = attribute.getType();
      int size = length;
      if (size == 0) {
        size = 10;
      } else if (size > 50) {
        size = 50;
      }
      if (!enabled) {
        field = new JTextField(1);
        field.setEnabled(false);
      } else if (codeTable != null) {
        final JComboBox comboBox = CodeTableComboBoxModel.create(codeTable,
          !required);
        comboBox.setSelectedIndex(0);
        final CodeTableObjectToStringConverter stringConverter = new CodeTableObjectToStringConverter(
          codeTable);
        AutoCompleteDecorator.decorate(comboBox, stringConverter);
        field = comboBox;
      } else if (Number.class.isAssignableFrom(type.getJavaClass())) {
        final int scale = attribute.getScale();
        field = new NumberTextField(type, length, scale);
      } else if (type.equals(DataTypes.DATE)) {
        final JXDatePicker captureDateField = new JXDatePicker();
        captureDateField.setFormats("yyyy-MM-dd", "yyyy/MM/dd", "yyyy-MMM-dd",
          "yyyy/MMM/dd");
        field = captureDateField;
      } else {
        final JTextField textField = new JTextField(size);
        field = textField;
      }
    }
    return (T)field;
  }

  @SuppressWarnings("unchecked")
  public static <V> V getValue(final JComponent component) {
    if (component instanceof JXDatePicker) {
      final JXDatePicker dateField = (JXDatePicker)component;
      return (V)dateField.getDate();
    } else if (component instanceof NumberTextField) {
      final NumberTextField numberField = (NumberTextField)component;
      return (V)numberField.getFieldValue();
    } else if (component instanceof JTextComponent) {
      final JTextComponent textComponent = (JTextComponent)component;
      final String text = textComponent.getText();
      if (StringUtils.hasText(text)) {
        return (V)text;
      } else {
        return null;
      }
    } else if (component instanceof JComboBox) {
      final JComboBox comboBox = (JComboBox)component;
      return (V)comboBox.getSelectedItem();
    } else if (component instanceof JList) {
      final JList list = (JList)component;
      return (V)list.getSelectedValue();
    } else if (component instanceof JCheckBox) {
      final JCheckBox checkBox = (JCheckBox)component;
      return (V)(Object)checkBox.isSelected();
    } else {
      return null;
    }
  }

  public static int getX(final Component component) {
    final int x = component.getX();
    final Component parent = component.getParent();
    if (parent == null) {
      return x;
    } else {
      return x + getX(parent);
    }
  }

  public static int getY(final Component component) {
    final int y = component.getY();
    final Component parent = component.getParent();
    if (parent == null) {
      return y;
    } else {
      return y + getY(parent);
    }
  }

  public static void setFieldValue(final JComponent field,
    final String fieldName, final Object value) {
    if (field instanceof NumberTextField) {
      final NumberTextField numberField = (NumberTextField)field;
      numberField.setFieldValue((Number)value);
    } else if (field instanceof DateTextField) {
      final DateTextField dateField = (DateTextField)field;
      dateField.setFieldValue((Date)value);
    } else if (field instanceof JXDatePicker) {
      final JXDatePicker dateField = (JXDatePicker)field;
      dateField.setDate((Date)value);
    } else if (field instanceof JLabel) {
      final JLabel label = (JLabel)field;
      String string;
      if (value == null) {
        string = "";
      } else {
        string = StringConverterRegistry.toString(value);
      }
      label.setText(string);
    } else if (field instanceof JTextField) {
      final JTextField textField = (JTextField)field;
      String string;
      if (value == null) {
        string = "";
      } else {
        string = StringConverterRegistry.toString(value);
      }
      textField.setText(string);
    } else if (field instanceof JTextArea) {
      final JTextArea textField = (JTextArea)field;
      String string;
      if (value == null) {
        string = "";
      } else {
        string = StringConverterRegistry.toString(value);
      }
      textField.setText(string);
    } else if (field instanceof JComboBox) {
      final JComboBox comboField = (JComboBox)field;
      comboField.setSelectedItem(value);
    }
    final Container parent = field.getParent();
    if (parent != null) {
      parent.getLayout().layoutContainer(parent);
      field.revalidate();
    }
  }

  public static void setMaximumWidth(final JComponent component, final int width) {
    final Dimension preferredSize = component.getPreferredSize();
    final Dimension size = new Dimension(width, preferredSize.height);
    component.setMaximumSize(size);
  }

  public static void setSize(final Window window, final int minusX,
    final int minusY) {
    final Toolkit toolkit = Toolkit.getDefaultToolkit();
    final Dimension screenSize = toolkit.getScreenSize();
    final double screenWidth = screenSize.getWidth();
    final double screenHeight = screenSize.getHeight();
    final Dimension size = new Dimension((int)(screenWidth - minusX),
      (int)(screenHeight - minusY));
    window.setBounds(minusX / 2, minusY / 2, size.width, size.height);
    window.setPreferredSize(size);
  }

  public static void setSizeAndMaximize(final JFrame frame, final int minusX,
    final int minusY) {
    final Toolkit toolkit = Toolkit.getDefaultToolkit();
    final Dimension screenSize = toolkit.getScreenSize();
    final double screenWidth = screenSize.getWidth();
    final double screenHeight = screenSize.getHeight();
    final Dimension size = new Dimension((int)(screenWidth - minusX),
      (int)(screenHeight - minusY));
    frame.setSize(size);
    frame.setPreferredSize(size);
    frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
  }

  public static Component getInvoker(final JMenuItem menuItem) {
    MenuContainer menuContainer = menuItem.getParent();
    while (menuContainer != null && !(menuContainer instanceof JPopupMenu)) {
      if (menuContainer instanceof MenuItem) {
        menuContainer = ((MenuItem)menuContainer).getParent();
      } else {
        menuContainer = null;
      }
    }
    if (menuContainer != null) {
      final JPopupMenu menu = (JPopupMenu)menuContainer;
      final Component invoker = menu.getInvoker();
      return invoker;
    } else {
      return null;
    }
  
  }
}
