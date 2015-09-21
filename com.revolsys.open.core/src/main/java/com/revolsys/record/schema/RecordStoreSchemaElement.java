package com.revolsys.record.schema;

import com.revolsys.io.PathName;
import com.revolsys.properties.ObjectWithProperties;

public interface RecordStoreSchemaElement
  extends ObjectWithProperties, Comparable<RecordStoreSchemaElement> {

  default boolean equalPath(final PathName path) {
    return getPathName().equals(path);
  }

  String getName();

  /**
   * Get the path of the object type. Names are described using a path (e.g.
   * /SCHEMA/TABLE).
   *
   * @return The name.
   */
  String getPath();

  PathName getPathName();

  <V extends RecordStore> V getRecordStore();

  RecordStoreSchema getSchema();
}