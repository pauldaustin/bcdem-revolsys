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
package com.revolsys.ui.web.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Menu extends MenuItem {

  private ArrayList items = new ArrayList();

  private Map itemMap = new HashMap();

  public Menu() {
  }

  public Menu(final String name, final String title, final String uri,
    final String anchor, final String condition) throws Exception {
    super(name, title, uri, anchor, condition);
  }

  public void addMenuItem(final MenuItem menuItem) {
    items.add(menuItem);
    String menuName = menuItem.getName();
    if (menuName != null) {
      itemMap.put(menuName, menuItem);
    }
  }

  public Collection getItems() {
    return items;
  }

  public Map getNamedItems() {
    return itemMap;
  }

  public MenuItem getItem(final String name) {
    return (MenuItem)itemMap.get(name);
  }

  public boolean contains(final String name) {
    if (this.getName().equals(name)) {
      return true;
    }
    Iterator i = getItems().iterator();
    while (i.hasNext()) {
      Menu m = (Menu)i.next();
      if (m.contains(name)) {
        return true;
      }
    }
    return false;
  }

  public MenuItem findHighlighted(final String name) {
    Iterator i = getItems().iterator();
    while (i.hasNext()) {
      MenuItem item = (MenuItem)i.next();
      if (item instanceof Menu) {
        Menu menu = (Menu)item;
        if (menu.contains(name)) {
          return menu;
        }
      }
    }
    return null;
  }

  public MenuItem findSelected(final String name) {
    if (this.getName().equals(name)) {
      return this;
    }
    Iterator i = getItems().iterator();
    while (i.hasNext()) {
      MenuItem item = (MenuItem)i.next();
      if (item instanceof Menu) {
        MenuItem menu = ((Menu)item).findSelected(name);
        if (menu != null) {
          return menu;
        }
      } else if (this.getName().equals(item.getName())) {
        return item;
      }
    }
    return null;
  }
 }
