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

import java.util.List;

import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * A {@link XMLFilter} managing a collection of {@link ProcessingInstructionHandler
 * ProcessingInstructionHandlers}, using them to handle processing instructions encountered while
 * parsing the XML document.
 *
 * @author Wilfred Springer
 */
public class PreprocessingFilter extends XMLFilterImpl {
  /**
   * The list of {@link ProcessingInstructionHandler} instances to which this object might
   * delegate.
   */
  private List handlers;

  /**
   * Constructs a new instance.
   *
   * @param reader The parent reader.
   */
  public PreprocessingFilter(XMLReader parent) {
    super(parent);
  }

  // JavaDoc inherited
  /**
   * DOCUMENT ME!
   *
   * @param target DOCUMENT ME!
   * @param data DOCUMENT ME!
   *
   * @throws SAXException DOCUMENT ME!
   */
  public void processingInstruction(String target, String data) throws SAXException {
    for (int i = handlers.size() - 1; i >= 0; i--) {
      ProcessingInstructionHandler handler = (ProcessingInstructionHandler) handlers.get(i);

      if (handler.matches(target)) {
        handler.handle(data, this);

        return;
      }
    }

    super.processingInstruction(target, data);
  }

  /**
   * Sets the list of handlers.
   *
   * @param handlers The list of handlers to which this filter might delegate.
   */
  public void setHandlers(List handlers) {
    this.handlers = handlers;
  }

  /**
   * Returns the list of handlers.
   *
   * @return The list of handlers to which this filter might delegate.
   */
  public List getHandlers() {
    return handlers;
  }
}
