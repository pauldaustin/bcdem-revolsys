package com.revolsys.data.record.io;

import java.util.List;
import java.util.Map;

import com.revolsys.data.record.Available;
import com.revolsys.data.record.schema.RecordStore;

public interface RecordStoreFactory extends Available {
  RecordStore createRecordStore(Map<String, ? extends Object> connectionProperties);

  String getName();

  List<String> getRecordStoreFileExtensions();

  Class<? extends RecordStore> getRecordStoreInterfaceClass(
    Map<String, ? extends Object> connectionProperties);

  List<String> getUrlPatterns();
}
