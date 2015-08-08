package com.revolsys.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * A visitor implementation which adds all the visited items to a List.
 *
 * @author Paul Austin
 * @param <T> The type of item to visit.
 */
public class CreateListVisitor<T> extends BaseVisitor<T> {
  private final List<T> list = new ArrayList<T>();

  public CreateListVisitor() {
  }

  public CreateListVisitor(final Predicate<T> filter) {
    super(filter);
  }

  @Override
  public boolean doVisit(final T item) {
    this.list.add(item);
    return true;
  }

  public List<T> getList() {
    return this.list;
  }
}
