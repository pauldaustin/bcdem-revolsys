/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.40
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.swig;

class EsriFileGdbJNI {
  public final static native long new_VectorOfString__SWIG_0();
  public final static native long new_VectorOfString__SWIG_1(long jarg1);
  public final static native long VectorOfString_size(long jarg1, VectorOfString jarg1_);
  public final static native long VectorOfString_capacity(long jarg1, VectorOfString jarg1_);
  public final static native void VectorOfString_reserve(long jarg1, VectorOfString jarg1_, long jarg2);
  public final static native boolean VectorOfString_isEmpty(long jarg1, VectorOfString jarg1_);
  public final static native void VectorOfString_clear(long jarg1, VectorOfString jarg1_);
  public final static native void VectorOfString_add(long jarg1, VectorOfString jarg1_, String jarg2);
  public final static native String VectorOfString_get(long jarg1, VectorOfString jarg1_, int jarg2);
  public final static native void VectorOfString_set(long jarg1, VectorOfString jarg1_, int jarg2, String jarg3);
  public final static native void delete_VectorOfString(long jarg1);
  public final static native long new_VectorOfWString__SWIG_0();
  public final static native long new_VectorOfWString__SWIG_1(long jarg1);
  public final static native long VectorOfWString_size(long jarg1, VectorOfWString jarg1_);
  public final static native long VectorOfWString_capacity(long jarg1, VectorOfWString jarg1_);
  public final static native void VectorOfWString_reserve(long jarg1, VectorOfWString jarg1_, long jarg2);
  public final static native boolean VectorOfWString_isEmpty(long jarg1, VectorOfWString jarg1_);
  public final static native void VectorOfWString_clear(long jarg1, VectorOfWString jarg1_);
  public final static native void VectorOfWString_add(long jarg1, VectorOfWString jarg1_, String jarg2);
  public final static native String VectorOfWString_get(long jarg1, VectorOfWString jarg1_, int jarg2);
  public final static native void VectorOfWString_set(long jarg1, VectorOfWString jarg1_, int jarg2, String jarg3);
  public final static native void delete_VectorOfWString(long jarg1);
  public final static native long new_FloatArray();
  public final static native float FloatArray_get(long jarg1, FloatArray jarg1_, int jarg2);
  public final static native void FloatArray_set(long jarg1, FloatArray jarg1_, int jarg2, float jarg3);
  public final static native void delete_FloatArray(long jarg1);
  public final static native long new_IntArray();
  public final static native int IntArray_get(long jarg1, IntArray jarg1_, int jarg2);
  public final static native void IntArray_set(long jarg1, IntArray jarg1_, int jarg2, int jarg3);
  public final static native void delete_IntArray(long jarg1);
  public final static native long new_DoubleArray();
  public final static native double DoubleArray_get(long jarg1, DoubleArray jarg1_, int jarg2);
  public final static native void DoubleArray_set(long jarg1, DoubleArray jarg1_, int jarg2, double jarg3);
  public final static native void delete_DoubleArray(long jarg1);
  public final static native long new_UnsignedCharArray();
  public final static native short UnsignedCharArray_get(long jarg1, UnsignedCharArray jarg1_, int jarg2);
  public final static native void UnsignedCharArray_set(long jarg1, UnsignedCharArray jarg1_, int jarg2, short jarg3);
  public final static native void delete_UnsignedCharArray(long jarg1);
  public final static native long new_PointArray();
  public final static native long PointArray_get(long jarg1, PointArray jarg1_, int jarg2);
  public final static native void PointArray_set(long jarg1, PointArray jarg1_, int jarg2, long jarg3, Point jarg3_);
  public final static native void delete_PointArray(long jarg1);
  public final static native void ErrorRecord_errorNumber_set(long jarg1, ErrorRecord jarg1_, int jarg2);
  public final static native int ErrorRecord_errorNumber_get(long jarg1, ErrorRecord jarg1_);
  public final static native void ErrorRecord_errorDescription_set(long jarg1, ErrorRecord jarg1_, String jarg2);
  public final static native String ErrorRecord_errorDescription_get(long jarg1, ErrorRecord jarg1_);
  public final static native long new_ErrorRecord();
  public final static native void delete_ErrorRecord(long jarg1);
  public final static native String getErrorDescription(int jarg1);
  public final static native long getErrorRecord(int jarg1);
  public final static native int getErrorRecordCount();
  public final static native long createGeodatabase(String jarg1);
  public final static native long openGeodatabase(String jarg1);
  public final static native int CloseGeodatabase(long jarg1, Geodatabase jarg1_);
  public final static native int DeleteGeodatabase(String jarg1);
  public final static native int Geodatabase_GetDatasetTypes(long jarg1, Geodatabase jarg1_, long jarg2, VectorOfWString jarg2_);
  public final static native int Geodatabase_GetDatasetRelationshipTypes(long jarg1, Geodatabase jarg1_, long jarg2, VectorOfWString jarg2_);
  public final static native int Geodatabase_GetChildDatasets(long jarg1, Geodatabase jarg1_, String jarg2, String jarg3, long jarg4, VectorOfWString jarg4_);
  public final static native int Geodatabase_GetRelatedDatasets(long jarg1, Geodatabase jarg1_, String jarg2, String jarg3, String jarg4, long jarg5, VectorOfWString jarg5_);
  public final static native int Geodatabase_GetChildDatasetDefinitions(long jarg1, Geodatabase jarg1_, String jarg2, String jarg3, long jarg4, VectorOfString jarg4_);
  public final static native int Geodatabase_GetRelatedDatasetDefinitions(long jarg1, Geodatabase jarg1_, String jarg2, String jarg3, String jarg4, long jarg5, VectorOfString jarg5_);
  public final static native int Geodatabase_CreateFeatureDataset(long jarg1, Geodatabase jarg1_, String jarg2);
  public final static native int Geodatabase_CloseTable(long jarg1, Geodatabase jarg1_, long jarg2, Table jarg2_);
  public final static native int Geodatabase_Rename(long jarg1, Geodatabase jarg1_, String jarg2, String jarg3, String jarg4);
  public final static native int Geodatabase_Move(long jarg1, Geodatabase jarg1_, String jarg2, String jarg3);
  public final static native int Geodatabase_Delete(long jarg1, Geodatabase jarg1_, String jarg2, String jarg3);
  public final static native int Geodatabase_GetDomains(long jarg1, Geodatabase jarg1_, long jarg2, VectorOfWString jarg2_);
  public final static native int Geodatabase_CreateDomain(long jarg1, Geodatabase jarg1_, String jarg2);
  public final static native int Geodatabase_AlterDomain(long jarg1, Geodatabase jarg1_, String jarg2);
  public final static native int Geodatabase_DeleteDomain(long jarg1, Geodatabase jarg1_, String jarg2);
  public final static native int Geodatabase_ExecuteSQL(long jarg1, Geodatabase jarg1_, String jarg2, boolean jarg3, long jarg4, EnumRows jarg4_);
  public final static native long new_Geodatabase();
  public final static native void delete_Geodatabase(long jarg1);
  public final static native int createGeodatabase2(String jarg1, long jarg2, Geodatabase jarg2_);
  public final static native int openGeodatabase2(String jarg1, long jarg2, Geodatabase jarg2_);
  public final static native int closeGeodatabase2(long jarg1, Geodatabase jarg1_);
  public final static native int deleteGeodatabase2(String jarg1);
  public final static native String Geodatabase_getDatasetDefinition(long jarg1, Geodatabase jarg1_, String jarg2, String jarg3);
  public final static native String Geodatabase_getDatasetDocumentation(long jarg1, Geodatabase jarg1_, String jarg2, String jarg3);
  public final static native String Geodatabase_getDomainDefinition(long jarg1, Geodatabase jarg1_, String jarg2);
  public final static native String Geodatabase_getQueryName(long jarg1, Geodatabase jarg1_, String jarg2);
  public final static native long Geodatabase_openTable(long jarg1, Geodatabase jarg1_, String jarg2);
  public final static native long Geodatabase_createTable(long jarg1, Geodatabase jarg1_, String jarg2, String jarg3);
  public final static native int Table_SetDocumentation(long jarg1, Table jarg1_, String jarg2);
  public final static native int Table_GetFieldInformation(long jarg1, Table jarg1_, long jarg2, FieldInfo jarg2_);
  public final static native int Table_AddField(long jarg1, Table jarg1_, String jarg2);
  public final static native int Table_AlterField(long jarg1, Table jarg1_, String jarg2);
  public final static native int Table_DeleteField(long jarg1, Table jarg1_, String jarg2);
  public final static native int Table_GetIndexes(long jarg1, Table jarg1_, long jarg2, VectorOfString jarg2_);
  public final static native int Table_AddIndex(long jarg1, Table jarg1_, String jarg2);
  public final static native int Table_DeleteIndex(long jarg1, Table jarg1_, String jarg2);
  public final static native int Table_CreateSubtype(long jarg1, Table jarg1_, String jarg2);
  public final static native int Table_AlterSubtype(long jarg1, Table jarg1_, String jarg2);
  public final static native int Table_DeleteSubtype(long jarg1, Table jarg1_, String jarg2);
  public final static native int Table_EnableSubtypes(long jarg1, Table jarg1_, String jarg2, String jarg3);
  public final static native int Table_SetDefaultSubtypeCode(long jarg1, Table jarg1_, int jarg2);
  public final static native int Table_DisableSubtypes(long jarg1, Table jarg1_);
  public final static native int Table_Insert(long jarg1, Table jarg1_, long jarg2, Row jarg2_);
  public final static native int Table_Update(long jarg1, Table jarg1_, long jarg2, Row jarg2_);
  public final static native int Table_Delete(long jarg1, Table jarg1_, long jarg2, Row jarg2_);
  public final static native int Table_GetExtent(long jarg1, Table jarg1_, long jarg2, Envelope jarg2_);
  public final static native int Table_SetWriteLock(long jarg1, Table jarg1_);
  public final static native int Table_FreeWriteLock(long jarg1, Table jarg1_);
  public final static native int Table_LoadOnlyMode(long jarg1, Table jarg1_, boolean jarg2);
  public final static native long new_Table();
  public final static native void delete_Table(long jarg1);
  public final static native boolean Table_isEditable(long jarg1, Table jarg1_);
  public final static native String Table_getDefinition(long jarg1, Table jarg1_);
  public final static native String Table_getDocumentation(long jarg1, Table jarg1_);
  public final static native int Table_getRowCount(long jarg1, Table jarg1_);
  public final static native int Table_getDefaultSubtypeCode(long jarg1, Table jarg1_);
  public final static native long Table_createRowObject(long jarg1, Table jarg1_);
  public final static native long Table_search__SWIG_0(long jarg1, Table jarg1_, String jarg2, String jarg3, long jarg4, Envelope jarg4_, boolean jarg5);
  public final static native long Table_search__SWIG_1(long jarg1, Table jarg1_, String jarg2, String jarg3, boolean jarg4);
  public final static native int Row_SetNull(long jarg1, Row jarg1_, String jarg2);
  public final static native int Row_GetGeometry(long jarg1, Row jarg1_, long jarg2, ShapeBuffer jarg2_);
  public final static native int Row_SetGeometry(long jarg1, Row jarg1_, long jarg2, ShapeBuffer jarg2_);
  public final static native int Row_SetShort(long jarg1, Row jarg1_, String jarg2, short jarg3);
  public final static native int Row_SetInteger(long jarg1, Row jarg1_, String jarg2, int jarg3);
  public final static native int Row_SetFloat(long jarg1, Row jarg1_, String jarg2, float jarg3);
  public final static native int Row_SetDouble(long jarg1, Row jarg1_, String jarg2, double jarg3);
  public final static native int Row_SetDate(long jarg1, Row jarg1_, String jarg2, long jarg3);
  public final static native int Row_SetString(long jarg1, Row jarg1_, String jarg2, String jarg3);
  public final static native int Row_SetGUID(long jarg1, Row jarg1_, String jarg2, long jarg3, Guid jarg3_);
  public final static native int Row_SetXML(long jarg1, Row jarg1_, String jarg2, String jarg3);
  public final static native int Row_SetRaster(long jarg1, Row jarg1_, String jarg2, long jarg3, Raster jarg3_);
  public final static native int Row_GetBinary(long jarg1, Row jarg1_, String jarg2, long jarg3, ByteArray jarg3_);
  public final static native int Row_SetBinary(long jarg1, Row jarg1_, String jarg2, long jarg3, ByteArray jarg3_);
  public final static native int Row_GetFieldInformation(long jarg1, Row jarg1_, long jarg2, FieldInfo jarg2_);
  public final static native long new_Row();
  public final static native void delete_Row(long jarg1);
  public final static native boolean Row_isNull(long jarg1, Row jarg1_, String jarg2);
  public final static native long Row_getDate(long jarg1, Row jarg1_, String jarg2);
  public final static native double Row_getDouble(long jarg1, Row jarg1_, String jarg2);
  public final static native float Row_getFloat(long jarg1, Row jarg1_, String jarg2);
  public final static native long Row_getGuid(long jarg1, Row jarg1_, String jarg2);
  public final static native int Row_getOid(long jarg1, Row jarg1_);
  public final static native short Row_getShort(long jarg1, Row jarg1_, String jarg2);
  public final static native int Row_getInteger(long jarg1, Row jarg1_, String jarg2);
  public final static native String Row_getString(long jarg1, Row jarg1_, String jarg2);
  public final static native String Row_getXML(long jarg1, Row jarg1_, String jarg2);
  public final static native long Row_getGeometry(long jarg1, Row jarg1_);
  public final static native void Texture_count_set(long jarg1, Texture jarg1_, int jarg2);
  public final static native int Texture_count_get(long jarg1, Texture jarg1_);
  public final static native void Texture_dimension_set(long jarg1, Texture jarg1_, int jarg2);
  public final static native int Texture_dimension_get(long jarg1, Texture jarg1_);
  public final static native long Texture_getParts(long jarg1, Texture jarg1_);
  public final static native long Texture_getCoords(long jarg1, Texture jarg1_);
  public final static native long new_Texture();
  public final static native void delete_Texture(long jarg1);
  public final static native void Material_count_set(long jarg1, Material jarg1_, int jarg2);
  public final static native int Material_count_get(long jarg1, Material jarg1_);
  public final static native void Material_compressionType_set(long jarg1, Material jarg1_, int jarg2);
  public final static native int Material_compressionType_get(long jarg1, Material jarg1_);
  public final static native long Material_getParts(long jarg1, Material jarg1_);
  public final static native long Material_getMaterials(long jarg1, Material jarg1_);
  public final static native long new_Material();
  public final static native void delete_Material(long jarg1);
  public final static native void EnumRows_Close(long jarg1, EnumRows jarg1_);
  public final static native int EnumRows_GetFieldInformation(long jarg1, EnumRows jarg1_, long jarg2, FieldInfo jarg2_);
  public final static native long new_EnumRows();
  public final static native void delete_EnumRows(long jarg1);
  public final static native long EnumRows_next(long jarg1, EnumRows jarg1_);
  public final static native long new_FieldInfo();
  public final static native void delete_FieldInfo(long jarg1);
  public final static native int FieldInfo_getFieldCount(long jarg1, FieldInfo jarg1_);
  public final static native String FieldInfo_getFieldName(long jarg1, FieldInfo jarg1_, int jarg2);
  public final static native int FieldInfo_getFieldLength(long jarg1, FieldInfo jarg1_, int jarg2);
  public final static native boolean FieldInfo_isNullable(long jarg1, FieldInfo jarg1_, int jarg2);
  public final static native int FieldInfo_getFieldType(long jarg1, FieldInfo jarg1_, int jarg2);
  public final static native boolean ShapeBuffer_Allocate(long jarg1, ShapeBuffer jarg1_, long jarg2);
  public final static native long new_ShapeBuffer__SWIG_0(long jarg1);
  public final static native long new_ShapeBuffer__SWIG_1();
  public final static native void delete_ShapeBuffer(long jarg1);
  public final static native void ShapeBuffer_allocatedLength_set(long jarg1, ShapeBuffer jarg1_, long jarg2);
  public final static native long ShapeBuffer_allocatedLength_get(long jarg1, ShapeBuffer jarg1_);
  public final static native void ShapeBuffer_inUseLength_set(long jarg1, ShapeBuffer jarg1_, long jarg2);
  public final static native long ShapeBuffer_inUseLength_get(long jarg1, ShapeBuffer jarg1_);
  public final static native boolean ShapeBuffer_IsEmpty(long jarg1, ShapeBuffer jarg1_);
  public final static native void ShapeBuffer_SetEmpty(long jarg1, ShapeBuffer jarg1_);
  public final static native boolean ShapeBuffer_HasZs(int jarg1);
  public final static native boolean ShapeBuffer_HasMs(int jarg1);
  public final static native boolean ShapeBuffer_HasIDs(int jarg1);
  public final static native boolean ShapeBuffer_HasCurves(int jarg1);
  public final static native boolean ShapeBuffer_HasNormals(int jarg1);
  public final static native boolean ShapeBuffer_HasTextures(int jarg1);
  public final static native boolean ShapeBuffer_HasMaterials(int jarg1);
  public final static native short ShapeBuffer_get(long jarg1, ShapeBuffer jarg1_, int jarg2);
  public final static native void ShapeBuffer_set(long jarg1, ShapeBuffer jarg1_, int jarg2, short jarg3);
  public final static native long ShapeBuffer_getShapeBuffer(long jarg1, ShapeBuffer jarg1_);
  public final static native int ShapeBuffer_getShapeType(long jarg1, ShapeBuffer jarg1_);
  public final static native int ShapeBuffer_getGeometryType(long jarg1, ShapeBuffer jarg1_);
  public final static native int PointShapeBuffer_Setup(long jarg1, PointShapeBuffer jarg1_, int jarg2);
  public final static native double PointShapeBuffer_getM(long jarg1, PointShapeBuffer jarg1_);
  public final static native double PointShapeBuffer_getZ(long jarg1, PointShapeBuffer jarg1_);
  public final static native long PointShapeBuffer_getPoint(long jarg1, PointShapeBuffer jarg1_);
  public final static native int PointShapeBuffer_getID(long jarg1, PointShapeBuffer jarg1_);
  public final static native long new_PointShapeBuffer();
  public final static native void delete_PointShapeBuffer(long jarg1);
  public final static native int MultiPointShapeBuffer_Setup(long jarg1, MultiPointShapeBuffer jarg1_, int jarg2, int jarg3);
  public final static native int MultiPointShapeBuffer_CalculateExtent(long jarg1, MultiPointShapeBuffer jarg1_);
  public final static native long MultiPointShapeBuffer_getExtent(long jarg1, MultiPointShapeBuffer jarg1_);
  public final static native long MultiPointShapeBuffer_getMExtent(long jarg1, MultiPointShapeBuffer jarg1_);
  public final static native long MultiPointShapeBuffer_getZExtent(long jarg1, MultiPointShapeBuffer jarg1_);
  public final static native long MultiPointShapeBuffer_getZs(long jarg1, MultiPointShapeBuffer jarg1_);
  public final static native long MultiPointShapeBuffer_getMs(long jarg1, MultiPointShapeBuffer jarg1_);
  public final static native long MultiPointShapeBuffer_getIDs(long jarg1, MultiPointShapeBuffer jarg1_);
  public final static native int MultiPointShapeBuffer_getNumPoints(long jarg1, MultiPointShapeBuffer jarg1_);
  public final static native long MultiPointShapeBuffer_getPoints(long jarg1, MultiPointShapeBuffer jarg1_);
  public final static native long new_MultiPointShapeBuffer();
  public final static native void delete_MultiPointShapeBuffer(long jarg1);
  public final static native int MultiPartShapeBuffer_Setup__SWIG_0(long jarg1, MultiPartShapeBuffer jarg1_, int jarg2, int jarg3, int jarg4, int jarg5);
  public final static native int MultiPartShapeBuffer_Setup__SWIG_1(long jarg1, MultiPartShapeBuffer jarg1_, int jarg2, int jarg3, int jarg4);
  public final static native int MultiPartShapeBuffer_CalculateExtent(long jarg1, MultiPartShapeBuffer jarg1_);
  public final static native int MultiPartShapeBuffer_PackCurves(long jarg1, MultiPartShapeBuffer jarg1_);
  public final static native long MultiPartShapeBuffer_getPoints(long jarg1, MultiPartShapeBuffer jarg1_);
  public final static native long MultiPartShapeBuffer_getCurves(long jarg1, MultiPartShapeBuffer jarg1_);
  public final static native long MultiPartShapeBuffer_getExtent(long jarg1, MultiPartShapeBuffer jarg1_);
  public final static native long MultiPartShapeBuffer_getMExtent(long jarg1, MultiPartShapeBuffer jarg1_);
  public final static native long MultiPartShapeBuffer_getZExtent(long jarg1, MultiPartShapeBuffer jarg1_);
  public final static native long MultiPartShapeBuffer_getZs(long jarg1, MultiPartShapeBuffer jarg1_);
  public final static native long MultiPartShapeBuffer_getMs(long jarg1, MultiPartShapeBuffer jarg1_);
  public final static native long MultiPartShapeBuffer_getIDs(long jarg1, MultiPartShapeBuffer jarg1_);
  public final static native long MultiPartShapeBuffer_getParts(long jarg1, MultiPartShapeBuffer jarg1_);
  public final static native int MultiPartShapeBuffer_getNumPoints(long jarg1, MultiPartShapeBuffer jarg1_);
  public final static native int MultiPartShapeBuffer_getNumParts(long jarg1, MultiPartShapeBuffer jarg1_);
  public final static native int MultiPartShapeBuffer_getNumCurves(long jarg1, MultiPartShapeBuffer jarg1_);
  public final static native long new_MultiPartShapeBuffer();
  public final static native void delete_MultiPartShapeBuffer(long jarg1);
  public final static native int MultiPatchShapeBuffer_Setup__SWIG_0(long jarg1, MultiPatchShapeBuffer jarg1_, int jarg2, int jarg3, int jarg4, int jarg5, int jarg6, int jarg7);
  public final static native int MultiPatchShapeBuffer_Setup__SWIG_1(long jarg1, MultiPatchShapeBuffer jarg1_, int jarg2, int jarg3, int jarg4, int jarg5, int jarg6);
  public final static native int MultiPatchShapeBuffer_Setup__SWIG_2(long jarg1, MultiPatchShapeBuffer jarg1_, int jarg2, int jarg3, int jarg4, int jarg5);
  public final static native int MultiPatchShapeBuffer_Setup__SWIG_3(long jarg1, MultiPatchShapeBuffer jarg1_, int jarg2, int jarg3, int jarg4);
  public final static native int MultiPatchShapeBuffer_CalculateExtent(long jarg1, MultiPatchShapeBuffer jarg1_);
  public final static native long MultiPatchShapeBuffer_getPoints(long jarg1, MultiPatchShapeBuffer jarg1_);
  public final static native long MultiPatchShapeBuffer_getNormals(long jarg1, MultiPatchShapeBuffer jarg1_);
  public final static native long MultiPatchShapeBuffer_getExtent(long jarg1, MultiPatchShapeBuffer jarg1_);
  public final static native long MultiPatchShapeBuffer_getMExtent(long jarg1, MultiPatchShapeBuffer jarg1_);
  public final static native long MultiPatchShapeBuffer_getZExtent(long jarg1, MultiPatchShapeBuffer jarg1_);
  public final static native long MultiPatchShapeBuffer_getZs(long jarg1, MultiPatchShapeBuffer jarg1_);
  public final static native long MultiPatchShapeBuffer_getMs(long jarg1, MultiPatchShapeBuffer jarg1_);
  public final static native long MultiPatchShapeBuffer_getIDs(long jarg1, MultiPatchShapeBuffer jarg1_);
  public final static native long MultiPatchShapeBuffer_getParts(long jarg1, MultiPatchShapeBuffer jarg1_);
  public final static native long MultiPatchShapeBuffer_getPartDescriptors(long jarg1, MultiPatchShapeBuffer jarg1_);
  public final static native int MultiPatchShapeBuffer_getNumPoints(long jarg1, MultiPatchShapeBuffer jarg1_);
  public final static native int MultiPatchShapeBuffer_getNumParts(long jarg1, MultiPatchShapeBuffer jarg1_);
  public final static native long new_MultiPatchShapeBuffer();
  public final static native void delete_MultiPatchShapeBuffer(long jarg1);
  public final static native boolean ByteArray_Allocate(long jarg1, ByteArray jarg1_, long jarg2);
  public final static native long new_ByteArray__SWIG_0(long jarg1);
  public final static native long new_ByteArray__SWIG_1();
  public final static native void delete_ByteArray(long jarg1);
  public final static native void ByteArray_allocatedLength_set(long jarg1, ByteArray jarg1_, long jarg2);
  public final static native long ByteArray_allocatedLength_get(long jarg1, ByteArray jarg1_);
  public final static native void ByteArray_inUseLength_set(long jarg1, ByteArray jarg1_, long jarg2);
  public final static native long ByteArray_inUseLength_get(long jarg1, ByteArray jarg1_);
  public final static native short ByteArray_get(long jarg1, ByteArray jarg1_, int jarg2);
  public final static native void ByteArray_set(long jarg1, ByteArray jarg1_, int jarg2, short jarg3);
  public final static native boolean Envelope_IsEmpty(long jarg1, Envelope jarg1_);
  public final static native void Envelope_SetEmpty(long jarg1, Envelope jarg1_);
  public final static native long new_Envelope__SWIG_0();
  public final static native long new_Envelope__SWIG_1(double jarg1, double jarg2, double jarg3, double jarg4);
  public final static native void delete_Envelope(long jarg1);
  public final static native void Envelope_xMin_set(long jarg1, Envelope jarg1_, double jarg2);
  public final static native double Envelope_xMin_get(long jarg1, Envelope jarg1_);
  public final static native void Envelope_yMin_set(long jarg1, Envelope jarg1_, double jarg2);
  public final static native double Envelope_yMin_get(long jarg1, Envelope jarg1_);
  public final static native void Envelope_xMax_set(long jarg1, Envelope jarg1_, double jarg2);
  public final static native double Envelope_xMax_get(long jarg1, Envelope jarg1_);
  public final static native void Envelope_yMax_set(long jarg1, Envelope jarg1_, double jarg2);
  public final static native double Envelope_yMax_get(long jarg1, Envelope jarg1_);
  public final static native void Envelope_zMin_set(long jarg1, Envelope jarg1_, double jarg2);
  public final static native double Envelope_zMin_get(long jarg1, Envelope jarg1_);
  public final static native void Envelope_zMax_set(long jarg1, Envelope jarg1_, double jarg2);
  public final static native double Envelope_zMax_get(long jarg1, Envelope jarg1_);
  public final static native void Point_x_set(long jarg1, Point jarg1_, double jarg2);
  public final static native double Point_x_get(long jarg1, Point jarg1_);
  public final static native void Point_y_set(long jarg1, Point jarg1_, double jarg2);
  public final static native double Point_y_get(long jarg1, Point jarg1_);
  public final static native long new_Point();
  public final static native void delete_Point(long jarg1);
  public final static native long new_Guid();
  public final static native void delete_Guid(long jarg1);
  public final static native void Guid_SetNull(long jarg1, Guid jarg1_);
  public final static native void Guid_Create(long jarg1, Guid jarg1_);
  public final static native int Guid_FromString(long jarg1, Guid jarg1_, String jarg2);
  public final static native boolean Guid_equal(long jarg1, Guid jarg1_, long jarg2, Guid jarg2_);
  public final static native boolean Guid_notEqual(long jarg1, Guid jarg1_, long jarg2, Guid jarg2_);
  public final static native String Guid_toString(long jarg1, Guid jarg1_);
  public final static native void ClearErrors();
  public final static native void SpatialReferenceInfo_auth_name_set(long jarg1, SpatialReferenceInfo jarg1_, String jarg2);
  public final static native String SpatialReferenceInfo_auth_name_get(long jarg1, SpatialReferenceInfo jarg1_);
  public final static native void SpatialReferenceInfo_auth_srid_set(long jarg1, SpatialReferenceInfo jarg1_, int jarg2);
  public final static native int SpatialReferenceInfo_auth_srid_get(long jarg1, SpatialReferenceInfo jarg1_);
  public final static native void SpatialReferenceInfo_srtext_set(long jarg1, SpatialReferenceInfo jarg1_, String jarg2);
  public final static native String SpatialReferenceInfo_srtext_get(long jarg1, SpatialReferenceInfo jarg1_);
  public final static native void SpatialReferenceInfo_srname_set(long jarg1, SpatialReferenceInfo jarg1_, String jarg2);
  public final static native String SpatialReferenceInfo_srname_get(long jarg1, SpatialReferenceInfo jarg1_);
  public final static native long new_SpatialReferenceInfo();
  public final static native void delete_SpatialReferenceInfo(long jarg1);
  public final static native long new_EnumSpatialReferenceInfo();
  public final static native void delete_EnumSpatialReferenceInfo(long jarg1);
  public final static native boolean EnumSpatialReferenceInfo_NextGeographicSpatialReference(long jarg1, EnumSpatialReferenceInfo jarg1_, long jarg2, SpatialReferenceInfo jarg2_);
  public final static native boolean EnumSpatialReferenceInfo_NextProjectedSpatialReference(long jarg1, EnumSpatialReferenceInfo jarg1_, long jarg2, SpatialReferenceInfo jarg2_);
  public final static native void EnumSpatialReferenceInfo_Reset(long jarg1, EnumSpatialReferenceInfo jarg1_);
  public final static native boolean FindSpatialReferenceBySRID(int jarg1, long jarg2, SpatialReferenceInfo jarg2_);
  public final static native boolean FindSpatialReferenceByName(String jarg1, long jarg2, SpatialReferenceInfo jarg2_);
  public final static native long new_Raster();
  public final static native void delete_Raster(long jarg1);
  public final static native long SWIGPointShapeBufferUpcast(long jarg1);
  public final static native long SWIGMultiPointShapeBufferUpcast(long jarg1);
  public final static native long SWIGMultiPartShapeBufferUpcast(long jarg1);
  public final static native long SWIGMultiPatchShapeBufferUpcast(long jarg1);
}
