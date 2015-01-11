/*
 * #%L
 * Docbkx Maven Plugin Builder
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
package com.agilejava.maven.docbkx.spec;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
 */
public class Parameter {
  private String name;
  private String value;
  private String description;
  private String type = "string";

  /**
   * DOCUMENT ME!
   *
   * @param type DOCUMENT ME!
   */
  public void setTypeFromRefType(String type) {
    if ("attribute set".equals(type)) {
      this.type = "attributeSet";
    } else {
      this.type = type;
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getType() {
    return type;
  }

  /**
   * DOCUMENT ME!
   *
   * @param name DOCUMENT ME!
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getName() {
    return name;
  }

  /**
   * DOCUMENT ME!
   *
   * @param value DOCUMENT ME!
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getValue() {
    return value;
  }

  /**
   * DOCUMENT ME!
   *
   * @param description DOCUMENT ME!
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getDescription() {
    if ("boolean".equals(type)) {
      String result = description.replaceAll("non-zero (true)", "true");
      result = result.replaceAll("non-zero", "true");

      return result;
    } else {
      return description;
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getJavaIdentifier() {
    StringBuffer builder = new StringBuffer();
    int size = name.length();
    boolean nextUpperCase = false;

    for (int i = 0; i < size; i++) {
      char c = name.charAt(i);

      if ((c == '.') || (c == '-')) {
        nextUpperCase = true;
      } else {
        if (nextUpperCase) {
          builder.append(Character.toUpperCase(c));
          nextUpperCase = false;
        } else {
          builder.append(c);
        }
      }
    }

    return builder.toString();
  }
}
