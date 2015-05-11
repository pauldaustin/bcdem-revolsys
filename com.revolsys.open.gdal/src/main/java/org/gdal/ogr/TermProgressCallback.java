/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.40
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.gdal.ogr;

public class TermProgressCallback extends ProgressCallback {
  protected static long getCPtr(final TermProgressCallback obj) {
    return obj == null ? 0 : obj.swigCPtr;
  }

  private long swigCPtr;

  public TermProgressCallback() {
    this(ogrJNI.new_TermProgressCallback(), true);
  }

  protected TermProgressCallback(final long cPtr, final boolean cMemoryOwn) {
    super(ogrJNI.SWIGTermProgressCallbackUpcast(cPtr), cMemoryOwn);
    this.swigCPtr = cPtr;
  }

  @Override
  public synchronized void delete() {
    if (this.swigCPtr != 0) {
      if (this.swigCMemOwn) {
        this.swigCMemOwn = false;
        ogrJNI.delete_TermProgressCallback(this.swigCPtr);
      }
      this.swigCPtr = 0;
    }
    super.delete();
  }

  @Override
  protected void finalize() {
    delete();
  }

  @Override
  public int run(final double dfComplete, final String pszMessage) {
    return ogrJNI.TermProgressCallback_run(this.swigCPtr, this, dfComplete, pszMessage);
  }

}
