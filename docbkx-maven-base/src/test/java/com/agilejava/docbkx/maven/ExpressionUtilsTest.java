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

import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
 */
public class ExpressionUtilsTest extends TestCase {
  /**
   * DOCUMENT ME!
   */
  public void testCreateTreeTest() {
    Properties properties = new Properties();
    properties.setProperty("foo.1", "bar");
    properties.setProperty("foo.2", "foo");

    Map tree = ExpressionUtils.createTree(properties);
    assertEquals(1, tree.size());
    assertTrue(tree.containsKey("foo"));

    Map subtree = (Map) tree.get("foo");
    assertEquals(2, subtree.size());
    assertTrue(subtree.containsKey("1"));
    assertTrue(subtree.containsKey("2"));
  }
}
