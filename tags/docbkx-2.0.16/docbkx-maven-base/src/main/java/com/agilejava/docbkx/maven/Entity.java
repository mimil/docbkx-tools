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

/**
 * The representation of an entity, consisting of a name and a value.
 *
 * @author Wilfred Springer
 */
public class Entity {
  /**
   * The name of the entity reference.
   */
  private String name;

  /**
   * The value of the entity.
   */
  private String value;

  /**
   * Returns the name of the entity.
   *
   * @return The name of the entity.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the entity.
   *
   * @param name Sets the name of the entity.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the value of the entity.
   *
   * @return The value of the entity.
   */
  public String getValue() {
    return value;
  }

  /**
   * Sets the value of the entity.
   *
   * @param value The value of the entity.
   */
  public void setValue(String value) {
    this.value = value;
  }
}
