/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.40
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.capi.swig;

public class EsriFileGdb {
  public static Geodatabase createGeodatabase(String path) {
    long cPtr = EsriFileGdbJNI.createGeodatabase(path);
    return (cPtr == 0) ? null : new Geodatabase(cPtr, true);
  }

  public static Geodatabase openGeodatabase(String path) {
    long cPtr = EsriFileGdbJNI.openGeodatabase(path);
    return (cPtr == 0) ? null : new Geodatabase(cPtr, true);
  }

  public static String getSpatialReferenceWkt(int srid) {
    return EsriFileGdbJNI.getSpatialReferenceWkt(srid);
  }

  public static int CloseGeodatabase(Geodatabase geodatabase) {
    return EsriFileGdbJNI.CloseGeodatabase(Geodatabase.getCPtr(geodatabase), geodatabase);
  }

  public static int DeleteGeodatabase(String path) {
    return EsriFileGdbJNI.DeleteGeodatabase(path);
  }

}
