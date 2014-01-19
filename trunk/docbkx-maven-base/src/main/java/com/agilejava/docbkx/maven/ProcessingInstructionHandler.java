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

import org.xml.sax.ContentHandler;

/**
 * The interface to be implemented by objects that are able to deal with
 * processing instructions.
 *
 * @author Wilfred Springer
 *
 */
public interface ProcessingInstructionHandler {
  /**
   * Returns a boolean indicating if this filter is interested in handling the processing
   * instruction.
   *
   * @param target The <code>target</code> part of the processing instruction.
   *
   * @return A boolean indicating if this object has an interest in handling this processing
   *         instruction.
   */
  boolean matches(String target);

  /**
   * Handles the processing instruction, optionally using the <code>handler</code> to replace
   * content. Note that implementations need to take care that the result is still going to be a
   * well-balanced XML document.
   *
   * @param data The <code>data</code> part of the processing instruction.
   * @param handler The {@link org.xml.sax.ContentHandler} optionally receiving some additional
   *        events.
   */
  void handle(String data, ContentHandler handler);
}
