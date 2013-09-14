package com.revolsys.swing.action.enablecheck;

import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.util.Property;

public class ObjectPropertyEnableCheck extends AbstractEnableCheck {
  private final Object object;

  private final String propertyName;

  private final Object value;

  private boolean inverse = false;

  public ObjectPropertyEnableCheck(final Object object,
    final String propertyName) {
    this(object, propertyName, true);
  }

  public ObjectPropertyEnableCheck(final Object object,
    final String propertyName, final Object value) {
    this(object, propertyName, value, false);
  }

  public ObjectPropertyEnableCheck(final Object object,
    final String propertyName, final Object value, final boolean inverse) {
    this.object = object;
    Property.addListener(object, propertyName, this);
    this.propertyName = propertyName;
    this.value = value;
    this.inverse = inverse;
  }

  @Override
  public boolean isEnabled() {
    final Object value = Property.get(this.object, this.propertyName);
    final boolean equal = EqualsRegistry.equal(value, this.value);
    if (equal == !this.inverse) {
      return enabled();
    } else {
      return disabled();
    }
  }

  @Override
  public String toString() {
    return this.object + "." + this.propertyName + "=" + this.value;
  }
}
