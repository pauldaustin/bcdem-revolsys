package com.revolsys.util;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ValueHolderWrapper<R> extends ValueWrapper<R> {

  @Override
  default void close() {
  }

  @Override
  default ValueWrapper<R> connect() {
    final ValueHolder<R> valueHolder = getValueHolder();
    return valueHolder.connect();
  }

  @Override
  default R getValue() {
    final ValueHolder<R> valueHolder = getValueHolder();
    return valueHolder.getValue();
  }

  ValueHolder<R> getValueHolder();

  @Override
  default void valueConsume(final Consumer<R> action) {
    final ValueHolder<R> valueHolder = getValueHolder();
    valueHolder.valueConsume(action);
  }

  @Override
  default void valueConsumeSync(final Consumer<R> action) {
    final ValueHolder<R> valueHolder = getValueHolder();
    valueHolder.valueConsumeSync(action);
  }

  @Override
  default <V> V valueFunction(final Function<R, V> action) {
    final ValueHolder<R> valueHolder = getValueHolder();
    return valueHolder.valueFunction(action);
  }

  @Override
  default <V> V valueFunction(final Function<R, V> action, final V defaultValue) {
    final ValueHolder<R> valueHolder = getValueHolder();
    return valueHolder.valueFunction(action, defaultValue);
  }

  @Override
  default <V> V valueFunctionSync(final Function<R, V> action) {
    final ValueHolder<R> valueHolder = getValueHolder();
    return valueHolder.valueFunctionSync(action);
  }

  @Override
  default <V> V valueFunctionSync(final Function<R, V> action, final V defaultValue) {
    final ValueHolder<R> valueHolder = getValueHolder();
    return valueHolder.valueFunctionSync(action, defaultValue);
  }

}
