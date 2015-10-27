/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.capi.swig;

public class Envelope {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected Envelope(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(Envelope obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        EsriFileGdbJNI.delete_Envelope(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public boolean IsEmpty() {
    return EsriFileGdbJNI.Envelope_IsEmpty(swigCPtr, this);
  }

  public void SetEmpty() {
    EsriFileGdbJNI.Envelope_SetEmpty(swigCPtr, this);
  }

  public Envelope() {
    this(EsriFileGdbJNI.new_Envelope__SWIG_0(), true);
  }

  public Envelope(double xmin, double xmax, double ymin, double ymax) {
    this(EsriFileGdbJNI.new_Envelope__SWIG_1(xmin, xmax, ymin, ymax), true);
  }

  public void setXMin(double value) {
    EsriFileGdbJNI.Envelope_xMin_set(swigCPtr, this, value);
  }

  public double getXMin() {
    return EsriFileGdbJNI.Envelope_xMin_get(swigCPtr, this);
  }

  public void setYMin(double value) {
    EsriFileGdbJNI.Envelope_yMin_set(swigCPtr, this, value);
  }

  public double getYMin() {
    return EsriFileGdbJNI.Envelope_yMin_get(swigCPtr, this);
  }

  public void setXMax(double value) {
    EsriFileGdbJNI.Envelope_xMax_set(swigCPtr, this, value);
  }

  public double getXMax() {
    return EsriFileGdbJNI.Envelope_xMax_get(swigCPtr, this);
  }

  public void setYMax(double value) {
    EsriFileGdbJNI.Envelope_yMax_set(swigCPtr, this, value);
  }

  public double getYMax() {
    return EsriFileGdbJNI.Envelope_yMax_get(swigCPtr, this);
  }

  public void setZMin(double value) {
    EsriFileGdbJNI.Envelope_zMin_set(swigCPtr, this, value);
  }

  public double getZMin() {
    return EsriFileGdbJNI.Envelope_zMin_get(swigCPtr, this);
  }

  public void setZMax(double value) {
    EsriFileGdbJNI.Envelope_zMax_set(swigCPtr, this, value);
  }

  public double getZMax() {
    return EsriFileGdbJNI.Envelope_zMax_get(swigCPtr, this);
  }

}
