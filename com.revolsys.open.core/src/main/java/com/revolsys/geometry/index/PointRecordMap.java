package com.revolsys.geometry.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.revolsys.geometry.model.Point;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.predicate.Predicates;
import com.revolsys.record.Record;

public class PointRecordMap {

  public static PointRecordMap newMap(final Iterable<? extends Record> records) {
    return new PointRecordMap(records);
  }

  private Comparator<Record> comparator;

  private Map<Point, List<Record>> recordMap = new HashMap<>();

  private boolean removeEmptyLists;

  private int size = 0;

  public PointRecordMap() {
  }

  public PointRecordMap(final Comparator<Record> comparator) {
    this.comparator = comparator;
  }

  public PointRecordMap(final Comparator<Record> comparator,
    final Iterable<? extends Record> records) {
    this.comparator = comparator;
    addAll(records);
  }

  public PointRecordMap(final Iterable<? extends Record> records) {
    addAll(records);
  }

  /**
   * Add a {@link Point} {@link Record} to the list of objects at the given
   * coordinate.
   *
   * @param pointObjects The map of point objects.
   * @param record The object to add.
   */
  public void addRecord(final Record record) {
    final Point key = getKey(record);
    final List<Record> records = getOrCreateRecords(key);
    records.add(record);
    if (this.comparator != null) {
      Collections.sort(records, this.comparator);
    }
    this.size++;
  }

  public void addAll(final Iterable<? extends Record> records) {
    for (final Record record : records) {
      addRecord(record);
    }
  }

  public void clear() {
    this.size = 0;
    this.recordMap = new HashMap<>();
  }

  public boolean containsKey(final Point point) {
    final Point key = getKey(point);
    return this.recordMap.containsKey(key);
  }

  public List<Record> getAll() {
    final List<Record> records = new ArrayList<>();
    for (final List<Record> recordsAtPoint : this.recordMap.values()) {
      records.addAll(recordsAtPoint);
    }
    return records;
  }

  @SuppressWarnings("unchecked")
  public <V extends Record> V getFirstMatch(final Point point) {
    final List<Record> records = getRecords(point);
    if (records.isEmpty()) {
      return null;
    } else {
      return (V)records.get(0);
    }

  }

  public Record getFirstMatch(final Record record, final Predicate<Record> filter) {
    final List<Record> records = getRecords(record);
    for (final Record matchRecord : records) {
      if (filter.test(matchRecord)) {
        return matchRecord;
      }
    }
    return null;
  }

  private Point getKey(final Point point) {
    return point.newPoint2D();
  }

  private Point getKey(final Record record) {
    final Point point = record.getGeometry();
    return getKey(point);
  }

  public Set<Point> getKeys() {
    return Collections.<Point> unmodifiableSet(this.recordMap.keySet());
  }

  public List<Record> getMatches(final Record record, final Predicate<Record> predicate) {
    final List<Record> records = getRecords(record);
    final List<Record> filteredRecords = Predicates.filter(records, predicate);
    return filteredRecords;
  }

  protected List<Record> getOrCreateRecords(final Point key) {
    List<Record> objects = this.recordMap.get(key);
    if (objects == null) {
      objects = new ArrayList<>(1);
      this.recordMap.put(key, objects);
    }
    return objects;
  }

  public List<Record> getRecords(final Point point) {
    final Point key = getKey(point);
    final List<Record> records = this.recordMap.get(key);
    if (records == null) {
      return Collections.emptyList();
    } else {
      return new ArrayList<>(records);
    }
  }

  public List<Record> getRecords(final Record record) {
    final Point point = record.getGeometry();
    final List<Record> objects = getRecords(point);
    return objects;
  }

  public void initialize(final Point point) {
    if (!isRemoveEmptyLists()) {
      final Point key = getKey(point);
      getOrCreateRecords(key);
    }
  }

  public boolean isRemoveEmptyLists() {
    return this.removeEmptyLists;
  }

  public void removeRecord(final Record record) {
    final Point key = getKey(record);
    final List<Record> objects = this.recordMap.get(key);
    if (objects != null) {
      objects.remove(record);
      if (objects.isEmpty()) {
        if (isRemoveEmptyLists()) {
          this.recordMap.remove(key);
        }
      } else if (this.comparator != null) {
        Collections.sort(objects, this.comparator);
      }
    }
    this.size--;
  }

  public void setRemoveEmptyLists(final boolean removeEmptyLists) {
    this.removeEmptyLists = removeEmptyLists;
  }

  public int size() {
    return this.size;
  }

  public void sort(final Record record) {
    if (this.comparator != null) {
      final List<Record> records = getRecords(record);
      if (records != null) {
        Collections.sort(records, this.comparator);
      }
    }
  }

  public void write(final Channel<Record> out) {
    if (out != null) {
      for (final Point point : getKeys()) {
        final List<Record> points = getRecords(point);
        for (final Record object : points) {
          out.write(object);
        }
      }
    }
  }
}
