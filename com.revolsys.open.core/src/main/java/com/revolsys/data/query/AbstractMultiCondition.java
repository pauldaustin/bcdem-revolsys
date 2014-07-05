package com.revolsys.data.query;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.io.RecordStore;
import com.revolsys.util.CollectionUtil;

public abstract class AbstractMultiCondition extends Condition {

  private List<QueryValue> values;

  private String operator;

  public AbstractMultiCondition(final Collection<? extends QueryValue> values) {
    this.values = new ArrayList<QueryValue>(values);
  }

  public AbstractMultiCondition(final String operator,
    final Collection<? extends QueryValue> values) {
    this.operator = operator;
    this.values = new ArrayList<QueryValue>(values);
  }

  public void add(final QueryValue value) {
    values.add(value);
  }

  public void add(final String sql) {
    final SqlCondition value = new SqlCondition(sql);
    add(value);
  }

  @Override
  public void appendDefaultSql(Query query,
    final RecordStore dataStore, final StringBuffer buffer) {
    buffer.append("(");
    boolean first = true;

    for (final QueryValue value : getQueryValues()) {
      if (first) {
        first = false;
      } else {
        buffer.append(" ");
        buffer.append(operator);
        buffer.append(" ");
      }
      value.appendSql(query, dataStore, buffer);
    }
    buffer.append(")");
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    for (final QueryValue value : getQueryValues()) {
      index = value.appendParameters(index, statement);
    }
    return index;
  }

  public void clear() {
    values.clear();
  }

  @Override
  public AbstractMultiCondition clone() {
    final AbstractMultiCondition clone = (AbstractMultiCondition)super.clone();
    clone.values = cloneQueryValues(this.values);
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof AbstractMultiCondition) {
      final AbstractMultiCondition value = (AbstractMultiCondition)obj;
      if (EqualsRegistry.equal(getOperator(), value.getOperator())) {
        final List<QueryValue> values1 = getQueryValues();
        final List<QueryValue> values2 = value.getQueryValues();
        if (values1.size() == values2.size()) {
          for (int i = 0; i < values1.size(); i++) {
            final QueryValue value1 = values1.get(i);
            final QueryValue value2 = values2.get(i);
            if (!EqualsRegistry.equal(value1, value2)) {
              return false;
            }
          }
          return true;
        }
      }
    }
    return false;
  }

  public String getOperator() {
    return operator;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Collections.<QueryValue> unmodifiableList(values);

  }

  @Override
  public boolean isEmpty() {
    return values.isEmpty();
  }

  @Override
  public String toString() {
    return "("
      + CollectionUtil.toString(") " + operator + " (", getQueryValues()) + ")";
  }
}