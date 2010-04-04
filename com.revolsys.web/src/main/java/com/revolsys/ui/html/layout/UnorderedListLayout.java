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
import java.util.Iterator;

import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.xml.io.XmlWriter;

public class UnorderedListLayout implements ElementContainerLayout {
  private String cssClass;

  public UnorderedListLayout() {
  }

  public UnorderedListLayout(final String cssClass) {
    this.cssClass = cssClass;
  }

  public void serialize(final XmlWriter out, final ElementContainer container)
    throws IOException {
    out.startTag(HtmlUtil.UL);
    if (cssClass != null) {
      out.attribute(HtmlUtil.ATTR_CLASS, cssClass);
    }
    for (Iterator elements = container.getElements().iterator(); elements.hasNext();) {
      Element element = (Element)elements.next();
      out.startTag(HtmlUtil.LI);
      element.serialize(out);
      out.endTag(HtmlUtil.LI);
    }
    out.endTag(HtmlUtil.UL);
  }
}
