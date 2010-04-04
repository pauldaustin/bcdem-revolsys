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
package com.revolsys.ui.html.fields;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.form.Form;
import com.revolsys.xml.io.XmlWriter;

public class SubmitField extends Field {

  public SubmitField(final String name) {
    super(name, false);
  }

  public SubmitField(final String name, final Object value) {
    super(name, false);
    setValue(value);
  }

  public SubmitField(final String name, final boolean required) {
    super(name, required);
  }

  public SubmitField(final String name, final boolean required,
    final Object value) {
    super(name, required);
    setValue(value);
  }

  public void serializeElement(final XmlWriter out) throws IOException {
    HtmlUtil.serializeSubmitInput(out, getName(), getValue());
  }

  public void initialize(final Form form, final HttpServletRequest request) {
  }

}
