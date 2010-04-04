/*
 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.ui.html.layout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.xml.io.XmlWriter;

public class TableBodyLayout implements ElementContainerLayout {
  private static final Logger log = Logger.getLogger(TableLayout.class);

  private String cssClass;

  private int numColumns;

  private List<String> cssClasses = new ArrayList<String>();

  public TableBodyLayout(final int numColumns) {
    this(null, numColumns);
  }

  public TableBodyLayout(final String cssClass, final int numColumns,
    final String... cssClasses) {
    this.cssClass = cssClass;
    this.numColumns = numColumns;
    for (String colCss : cssClasses) {
      this.cssClasses.add(colCss);
    }
    for (int i = cssClasses.length; i < numColumns; i++) {
      this.cssClasses.add("");
    }
  }

  public void serialize(final XmlWriter out, final ElementContainer container)
    throws IOException {
    if (!container.getElements().isEmpty()) {
      serializeTbody(out, container);
    }
  }

  private void serializeTbody(final XmlWriter out,
    final ElementContainer container) throws IOException {
    out.startTag(HtmlUtil.TBODY);
    if (cssClass != null) {
      out.attribute(HtmlUtil.ATTR_CLASS, cssClass);
    }
    List<Element> elementList = container.getElements();
    int i = 0;
    int rowNum = 0;
    int numElements = elementList.size();
    int lastRow = (numElements - 1) / numColumns;
    for (Element element : elementList) {
      int col = i % numColumns;
      String colCss = cssClasses.get(col);
      boolean firstCol = col == 0;
      boolean lastCol = (i + 1) % numColumns == 0 || i == numElements - 1;
      if (firstCol) {
        out.startTag(HtmlUtil.TR);
        String rowCss = "";
        if (rowNum == 0) {
          rowCss += " firstRow";
        }
        if (rowNum == lastRow) {
          rowCss += " lastRow";
        }
        if (rowCss.length() > 0) {
          out.attribute(HtmlUtil.ATTR_CLASS, rowCss);
        }
        colCss += " firstCol";
      }
      if (lastCol) {
        colCss += " lastCol";
      }
      out.startTag(HtmlUtil.TD);
      if (colCss.length() > 0) {
        out.attribute(HtmlUtil.ATTR_CLASS, colCss);
      }
      element.serialize(out);
      out.endTag(HtmlUtil.TD);
      i++;
      if (lastCol) {
        out.endTag(HtmlUtil.TR);
        rowNum++;
      }
    }
    out.endTag(HtmlUtil.TBODY);
  }
}
