package com.revolsys.jts.geom;

import java.util.List;

import com.revolsys.collection.list.Lists;

public enum Direction {
  FORWARDS, BACKWARDS;

  public static List<Direction> VALUES = Lists.array(FORWARDS, BACKWARDS);

  public static boolean isBackwards(final Direction direction) {
    return direction == BACKWARDS;
  }

  public static boolean isForwards(final Direction direction) {
    return direction == FORWARDS;
  }

  public static Direction opposite(final Direction direction) {
    if (direction == FORWARDS) {
      return BACKWARDS;
    } else if (direction == BACKWARDS) {
      return FORWARDS;
    } else {
      return null;
    }
  }

  public boolean isBackwards() {
    return this == BACKWARDS;
  }

  public boolean isForwards() {
    return this == FORWARDS;
  }

  public Direction opposite() {
    if (isForwards()) {
      return BACKWARDS;
    } else {
      return FORWARDS;
    }
  }

  public boolean isOpposite(Direction direction) {
    if (direction == null) {
      return false;
    } else {
      return isForwards() !=direction.isForwards();
    }
  }
}
