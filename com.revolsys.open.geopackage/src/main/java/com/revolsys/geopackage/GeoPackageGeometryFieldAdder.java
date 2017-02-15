package com.revolsys.geopackage;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.PathName;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.field.JdbcFieldAdder;
import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.logging.Logs;
import com.revolsys.record.property.FieldProperties;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;

public class GeoPackageGeometryFieldAdder extends JdbcFieldAdder {
  private static final Map<String, DataType> DATA_TYPE_MAP = new HashMap<>();

  static {
    DATA_TYPE_MAP.put("GEOMETRY", DataTypes.GEOMETRY);
    DATA_TYPE_MAP.put("POINT", DataTypes.POINT);
    DATA_TYPE_MAP.put("LINESTRING", DataTypes.LINE_STRING);
    DATA_TYPE_MAP.put("POLYGON", DataTypes.POLYGON);
    DATA_TYPE_MAP.put("MULTIPOINT", DataTypes.MULTI_POINT);
    DATA_TYPE_MAP.put("MULTILINESTRING", DataTypes.MULTI_LINE_STRING);
    DATA_TYPE_MAP.put("MULTIPOLYGON", DataTypes.MULTI_POLYGON);
  }

  public GeoPackageGeometryFieldAdder() {
  }

  @Override
  public FieldDefinition addField(final AbstractJdbcRecordStore recordStore,
    final RecordDefinitionImpl recordDefinition, final String dbName, final String name,
    final String dataTypeName, final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    final PathName typePath = recordDefinition.getPathName();
    final String tableName = recordStore.getDatabaseTableName(typePath);
    final String columnName = name.toLowerCase();
    try {
      int srid = 0;
      String type = "geometry";
      int axisCount = 2;
      try {
        final String sql = "select geometry_type_name, srs_id, Z, M from gpkg_geometry_columns where UPPER(TABLE_NAME) = UPPER(?) AND UPPER(COLUMN_NAME) = UPPER(?)";
        final MapEx values = JdbcUtils.selectMap(recordStore, sql, tableName, columnName);
        srid = values.getInteger("srs_id", 0);
        type = values.getString("geometry_type_name", "GEOMETRY");
        if (values.getInteger("z", 0) > 0) {
          axisCount = 3;
        }
        if (values.getInteger("m", 0) > 0) {
          axisCount = 4;
        }
      } catch (final IllegalArgumentException e) {
        Logs.warn(this, "Cannot get geometry column metadata for " + typePath + "." + columnName);
      }

      final DataType dataType = DATA_TYPE_MAP.get(type);
      final GeometryFactory storeGeometryFactory = recordStore.getGeometryFactory();
      final GeometryFactory geometryFactory;
      if (storeGeometryFactory == null) {
        geometryFactory = GeometryFactory.floating(srid, axisCount);
      } else {
        geometryFactory = GeometryFactory.fixed(srid, axisCount, storeGeometryFactory.getScaleX(),
          storeGeometryFactory.getScaleY(), storeGeometryFactory.getScaleZ());
      }
      final FieldDefinition field = new GeoPackageGeometryJdbcFieldDefinition(dbName, name,
        dataType, required, description, null, srid, axisCount, geometryFactory);
      recordDefinition.addField(field);
      field.setProperty(FieldProperties.GEOMETRY_FACTORY, geometryFactory);
      return field;
    } catch (final Throwable e) {
      Logs.error(this,
        "Attribute not registered in GEOMETRY_COLUMN table " + tableName + "." + name, e);
      return null;
    }
  }
}
