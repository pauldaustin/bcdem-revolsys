package com.revolsys.data.record.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.format.directory.DirectoryRecordStore;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.spring.SpringUtil;

public abstract class AbstractRecordIoFactory extends AbstractIoFactoryWithCoordinateSystem
  implements RecordReaderFactory, RecordStoreFactory {

  private final List<String> urlPatterns = new ArrayList<>();

  public AbstractRecordIoFactory(final String name) {
    super(name);
  }

  @Override
  protected void addMediaTypeAndFileExtension(final String mediaType, final String fileExtension) {
    super.addMediaTypeAndFileExtension(mediaType, fileExtension);
    this.urlPatterns.add("(.+)[\\?|&]format=" + fileExtension + "(&.+)?");
  }

  @Override
  public RecordStore createRecordStore(final Map<String, ? extends Object> connectionProperties) {
    final String url = (String)connectionProperties.get("url");
    final Resource resource = SpringUtil.getResource(url);
    final File directory = SpringUtil.getFile(resource);
    final List<String> fileExtensions = getFileExtensions();
    return new DirectoryRecordStore(directory, fileExtensions);
  }

  @Override
  public List<String> getRecordStoreFileExtensions() {
    return Collections.emptyList();
  }

  @Override
  public Class<? extends RecordStore> getRecordStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return RecordStore.class;
  }

  @Override
  public List<String> getUrlPatterns() {
    return this.urlPatterns;
  }

  @Override
  public void init() {
    super.init();
    RecordStoreFactoryRegistry.register(this);
  }
}
