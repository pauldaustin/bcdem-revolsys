package com.revolsys.data.io;

import java.util.List;
import java.util.Map;

public interface RecordStoreFactory {
  RecordStore createRecordStore(
    Map<String, ? extends Object> connectionProperties);

  Class<? extends RecordStore> getRecordStoreInterfaceClass(
    Map<String, ? extends Object> connectionProperties);

  List<String> getFileExtensions();

  String getName();

  List<String> getUrlPatterns();
}