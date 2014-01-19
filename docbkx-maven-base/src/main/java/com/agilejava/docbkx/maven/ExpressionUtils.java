/*
 * #%L
 * Docbkx Maven Base
 * %%
 * Copyright (C) 2006 - 2014 Wilfred Springer, Cedric Pronzato
 * %%
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
 * #L%
 */
package com.agilejava.docbkx.maven;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
 */
public class ExpressionUtils {
  /**
   * DOCUMENT ME!
   *
   * @param properties DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public final static Map createTree(Properties properties) {
    Map map = new HashMap();
    Iterator iterator = properties.keySet().iterator();

    while (iterator.hasNext()) {
      String key = (String) iterator.next();
      splitToTree(key, properties.get(key), map);
    }

    return map;
  }

  private static void splitToTree(String key, Object object, Map map) {
    int i = key.indexOf('.');

    if (i > 0) {
      String part = key.substring(0, i);
      Map submap = (Map) map.get(part);
      submap = (submap == null) ? new HashMap() : submap;
      map.put(part, submap);
      splitToTree(key.substring(i + 1), object, submap);
    } else {
      map.put(key, object);
    }
  }
}
