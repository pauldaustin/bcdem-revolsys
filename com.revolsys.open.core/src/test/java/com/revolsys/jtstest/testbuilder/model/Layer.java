package com.revolsys.jtstest.testbuilder.model;

import java.awt.Graphics2D;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jtstest.testbuilder.Viewport;
import com.revolsys.jtstest.testbuilder.ui.ColorUtil;
import com.revolsys.jtstest.testbuilder.ui.render.GeometryPainter;
import com.revolsys.jtstest.testbuilder.ui.style.ArrowEndpointStyle;
import com.revolsys.jtstest.testbuilder.ui.style.ArrowLineStyle;
import com.revolsys.jtstest.testbuilder.ui.style.BasicStyle;
import com.revolsys.jtstest.testbuilder.ui.style.CircleEndpointStyle;
import com.revolsys.jtstest.testbuilder.ui.style.PolygonStructureStyle;
import com.revolsys.jtstest.testbuilder.ui.style.SegmentIndexStyle;
import com.revolsys.jtstest.testbuilder.ui.style.Style;
import com.revolsys.jtstest.testbuilder.ui.style.StyleList;
import com.revolsys.jtstest.testbuilder.ui.style.VertexStyle;

public class Layer 
{
  private String name = "";
  private GeometryContainer geomCont;
  private boolean isEnabled = true;
  
  private BasicStyle style = new BasicStyle();
  
  private StyleList styleList;
  
  private StyleList.StyleFilter vertexFilter = new StyleList.StyleFilter() {
  	public boolean isFiltered(Style style) {
  		return ! TestBuilderModel.isShowingVertices();
  	}
  };
  
  private StyleList.StyleFilter decorationFilter = new StyleList.StyleFilter() {
    public boolean isFiltered(Style style) {
      return ! TestBuilderModel.isShowingOrientation();
    }
  };
    
  private StyleList.StyleFilter structureFilter = new StyleList.StyleFilter() {
    public boolean isFiltered(Style style) {
      return ! TestBuilderModel.isShowingStructure();
    }
  };
    
  public Layer(String name) {
    this.name = name;
  }

  public String getName() { return name; }
  
  public void setEnabled(boolean isEnabled)
  {
    this.isEnabled = isEnabled;
  }
  
  public void setSource(GeometryContainer geomCont)
  {
    this.geomCont = geomCont;
  }
  
  public GeometryContainer getSource()
  {
    return geomCont;
  }
  
  public boolean isEnabled()
  {
  	return isEnabled;
  }
  public StyleList getStyles()
  {
  	return styleList;
  }
  public void setStyle(BasicStyle style)
  {
    this.style = style;
    VertexStyle vertexStyle = new VertexStyle(style.getLineColor());
    ArrowLineStyle segArrowStyle = new ArrowLineStyle(ColorUtil.lighter(style.getLineColor(), 0.8));
    ArrowEndpointStyle lineArrowStyle = new ArrowEndpointStyle(ColorUtil.lighter(style.getLineColor()), false, true);
    CircleEndpointStyle lineCircleStyle = new CircleEndpointStyle(style.getLineColor(), true, false);
    PolygonStructureStyle polyStyle = new PolygonStructureStyle(ColorUtil.opaque(style.getLineColor()));
    SegmentIndexStyle indexStyle = new SegmentIndexStyle(ColorUtil.opaque(style.getLineColor().darker()));
    
    // order is important here
    styleList = new StyleList();
    styleList.add(vertexStyle, vertexFilter);
    styleList.add(segArrowStyle, decorationFilter);
    styleList.add(lineArrowStyle, decorationFilter);
    styleList.add(lineCircleStyle, decorationFilter);
    styleList.add(style);
    styleList.add(polyStyle, structureFilter);
    styleList.add(indexStyle, structureFilter);
  }
  
  public Geometry getGeometry()
  {
    if (geomCont == null) return null;
    return geomCont.getGeometry();
  }

  public void paint(Graphics2D g, Viewport viewport)
  {
    if (! isEnabled) return;
    if (geomCont == null) return;
    
    try {
      Geometry geom = geomCont.getGeometry();
      if (geom == null) return;
      
      // cull non-visible geometries
      if (! viewport.intersectsInModel(geom.getBoundingBox())) 
        return;
      
      GeometryPainter.paint(g, viewport, geom, styleList);
      
    } 
    catch (Exception ex) {
      // not much we can do about an exception while rendering, so just carry on
      System.out.println("Exception in Layer.paint(): " + ex);
    }
  }
  

}
