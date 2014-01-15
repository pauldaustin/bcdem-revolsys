package com.revolsys.gis.data.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.Attribute;

public class F {

  public static And and(final Condition... conditions) {
    final List<Condition> list = Arrays.asList(conditions);
    return and(list);
  }

  public static And and(final List<? extends Condition> conditions) {
    return new And(conditions);
  }

  public static Between between(final Attribute attribute, final Object min,
    final Object max) {
    final Column column = new Column(attribute);
    final Value minCondition = new Value(attribute, min);
    final Value maxCondition = new Value(attribute, max);
    return new Between(column, minCondition, maxCondition);
  }

  public static Condition binary(final Attribute field, final String operator,
    final Object value) {
    final Column column = new Column(field);
    final Value queryValue = new Value(field, value);
    return binary(column, operator, queryValue);
  }

  public static Condition binary(final QueryValue left, final String operator,
    final QueryValue right) {
    if ("=".equals(operator)) {
      return F.equal(left, right);
    } else if ("<>".equals(operator) || "!=".equals(operator)) {
      return F.notEqual(left, right);
    } else if ("<".equals(operator)) {
      return F.lessThan(left, right);
    } else if ("<=".equals(operator)) {
      return F.lessThanEqual(left, right);
    } else if (">".equals(operator)) {
      return F.greaterThan(left, right);
    } else if (">=".equals(operator)) {
      return F.greaterThanEqual(left, right);
    } else {
      throw new IllegalArgumentException("Operator " + operator
        + " not supported");
    }
  }

  public static Condition binary(final String fieldName, final String operator,
    final Object value) {
    final Column column = new Column(fieldName);
    final Value queryValue = new Value(value);
    return binary(column, operator, queryValue);

  }

  public static Equal equal(final Attribute attribute, final Object value) {
    final String name = attribute.getName();
    final Value valueCondition = new Value(attribute, value);
    return equal(name, valueCondition);
  }

  public static Equal equal(final QueryValue left, final Object value) {
    final Value valueCondition = new Value(value);
    return new Equal(left, valueCondition);
  }

  public static Equal equal(final QueryValue left, final QueryValue right) {
    return new Equal(left, right);
  }

  public static Equal equal(final String name, final Object value) {
    final Value valueCondition = new Value(value);
    return equal(name, valueCondition);
  }

  public static Equal equal(final String left, final QueryValue right) {
    final Column leftCondition = new Column(left);
    return new Equal(leftCondition, right);
  }

  public static GreaterThan greaterThan(final Attribute attribute,
    final Object value) {
    final String name = attribute.getName();
    final Value valueCondition = new Value(attribute, value);
    return greaterThan(name, valueCondition);
  }

  public static GreaterThan greaterThan(final QueryValue left,
    final QueryValue right) {
    return new GreaterThan(left, right);
  }

  public static GreaterThan greaterThan(final String name, final Object value) {
    final Value valueCondition = new Value(value);
    return greaterThan(name, valueCondition);
  }

  public static GreaterThan greaterThan(final String name,
    final QueryValue right) {
    final Column column = new Column(name);
    return new GreaterThan(column, right);
  }

  public static GreaterThanEqual greaterThanEqual(final Attribute attribute,
    final Object value) {
    final String name = attribute.getName();
    final Value valueCondition = new Value(attribute, value);
    return greaterThanEqual(name, valueCondition);
  }

  public static GreaterThanEqual greaterThanEqual(final QueryValue left,
    final QueryValue right) {
    return new GreaterThanEqual(left, right);
  }

  public static GreaterThanEqual greaterThanEqual(final String name,
    final Object value) {
    final Value valueCondition = new Value(value);
    return greaterThanEqual(name, valueCondition);
  }

  public static GreaterThanEqual greaterThanEqual(final String name,
    final QueryValue right) {
    final Column column = new Column(name);
    return greaterThanEqual(column, right);
  }

  public static ILike iLike(final Attribute attribute, final Object value) {
    final String name = attribute.getName();
    final Value valueCondition = new Value(attribute, value);
    return iLike(name, valueCondition);
  }

  public static ILike iLike(final QueryValue left, final Object value) {
    final Value valueCondition = new Value(value);
    return new ILike(left, valueCondition);
  }

  public static ILike iLike(final String name, final Object value) {
    final Value valueCondition = new Value(value);
    return iLike(name, valueCondition);
  }

  public static ILike iLike(final String left, final QueryValue right) {
    final Column leftCondition = new Column(left);
    return new ILike(leftCondition, right);
  }

  public static Condition iLike(final String left, final String right) {
    return F.like(Function.upper(new Cast(left, "varchar(4000)")),
      ("%" + right + "%").toUpperCase());
  }

  public static In in(final Attribute attribute,
    final Collection<? extends Object> values) {
    return new In(attribute, values);
  }

  public static In in(final Attribute attribute, final Object... values) {
    final List<Object> list = Arrays.asList(values);
    return new In(attribute, list);
  }

  public static IsNotNull isNotNull(final Attribute attribute) {
    final String name = attribute.getName();
    return isNotNull(name);
  }

  public static IsNotNull isNotNull(final String name) {
    final Column condition = new Column(name);
    return new IsNotNull(condition);
  }

  public static IsNull isNull(final Attribute attribute) {
    final String name = attribute.getName();
    return isNull(name);
  }

  public static IsNull isNull(final String name) {
    final Column condition = new Column(name);
    return new IsNull(condition);
  }

  public static LessThan lessThan(final Attribute attribute, final Object value) {
    final String name = attribute.getName();
    final Value valueCondition = new Value(attribute, value);
    return lessThan(name, valueCondition);
  }

  public static LessThan lessThan(final QueryValue left, final QueryValue right) {
    return new LessThan(left, right);
  }

  public static LessThan lessThan(final String name, final Object value) {
    final Value valueCondition = new Value(value);
    return lessThan(name, valueCondition);
  }

  public static LessThan lessThan(final String name, final QueryValue right) {
    final Column column = new Column(name);
    return lessThan(column, right);
  }

  public static LessThanEqual lessThanEqual(final Attribute attribute,
    final Object value) {
    final String name = attribute.getName();
    final Value valueCondition = new Value(attribute, value);
    return lessThanEqual(name, valueCondition);
  }

  public static LessThanEqual lessThanEqual(final QueryValue left,
    final QueryValue right) {
    return new LessThanEqual(left, right);
  }

  public static LessThanEqual lessThanEqual(final String name,
    final Object value) {
    final Value valueCondition = new Value(value);
    return lessThanEqual(name, valueCondition);
  }

  public static LessThanEqual lessThanEqual(final String name,
    final QueryValue right) {
    final Column column = new Column(name);
    return new LessThanEqual(column, right);
  }

  public static Like like(final Attribute attribute, final Object value) {
    final String name = attribute.getName();
    final Value valueCondition = new Value(attribute, value);
    return like(name, valueCondition);
  }

  public static Like like(final QueryValue left, final Object value) {
    final Value valueCondition = new Value(value);
    return new Like(left, valueCondition);
  }

  public static Like like(final String name, final Object value) {
    final Value valueCondition = new Value(value);
    return like(name, valueCondition);
  }

  public static Like like(final String left, final QueryValue right) {
    final Column leftCondition = new Column(left);
    return new Like(leftCondition, right);
  }

  public static Condition likeRegEx(final DataObjectStore dataStore,
    final String fieldName, final Object value) {
    Condition left;
    if (dataStore.getClass().getName().contains("Oracle")) {
      left = new SqlCondition("regexp_replace(upper(" + fieldName
        + "), '[^A-Z0-9]','')");
    } else {
      left = new SqlCondition("regexp_replace(upper(" + fieldName
        + "), '[^A-Z0-9]','', 'g')");
    }
    final String right = "%"
      + StringConverterRegistry.toString(value)
        .toUpperCase()
        .replaceAll("[^A-Z0-0]", "") + "%";
    return F.like(left, right);
  }

  public static Not not(final Condition condition) {
    return new Not(condition);
  }

  public static NotEqual notEqual(final Attribute attribute, final Object value) {
    final String name = attribute.getName();
    final Value valueCondition = new Value(attribute, value);
    return notEqual(name, valueCondition);
  }

  public static NotEqual notEqual(final QueryValue left, final QueryValue right) {
    return new NotEqual(left, right);
  }

  public static NotEqual notEqual(final String name, final Object value) {
    return notEqual(name, new Value(value));
  }

  public static NotEqual notEqual(final String name, final QueryValue right) {
    final Column column = new Column(name);
    return new NotEqual(column, right);
  }

  public static Or or(final Condition... conditions) {
    final List<Condition> list = Arrays.asList(conditions);
    return or(list);
  }

  public static Or or(final List<? extends Condition> conditions) {
    return new Or(conditions);
  }

  public static void setValue(final int index, final Condition condition,
    final Object value) {
    setValueInternal(-1, index, condition, value);

  }

  public static int setValueInternal(int i, final int index,
    final QueryValue condition, final Object value) {
    for (final QueryValue subCondition : condition.getQueryValues()) {
      if (subCondition instanceof Value) {
        final Value valueCondition = (Value)subCondition;
        i++;
        if (i == index) {
          valueCondition.setValue(value);
          return i;
        }
        i = setValueInternal(i, index, subCondition, value);
        if (i >= index) {
          return i;
        }
      }
    }
    return i;
  }

  public static SqlCondition sql(final String sql) {
    return new SqlCondition(sql);
  }

  public static SqlCondition sql(final String sql, final Object... parameters) {
    return new SqlCondition(sql, parameters);
  }
}
