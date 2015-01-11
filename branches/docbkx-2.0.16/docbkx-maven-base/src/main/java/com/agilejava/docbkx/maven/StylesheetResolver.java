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

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

/**
 * A simple {@link URIResolver} decorator that will bail out of the normal way of retrieving
 * entities as soon as the publicId or systemId matches a given URN.
 *
 * @author Wilfred Springer
 */
public class StylesheetResolver implements URIResolver {
  private String urn;
  private Source stylesheet;
  private URIResolver wrapped;

  /**
   * Creates a new StylesheetResolver object.
   *
   * @param urn DOCUMENT ME!
   * @param stylesheet DOCUMENT ME!
   * @param wrapped DOCUMENT ME!
   */
  public StylesheetResolver(String urn, Source stylesheet, URIResolver wrapped) {
    this.urn = urn;
    this.stylesheet = stylesheet;
    this.wrapped = wrapped;
  }

  /**
   * DOCUMENT ME!
   *
   * @param href DOCUMENT ME!
   * @param base DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws TransformerException DOCUMENT ME!
   */
  public Source resolve(String href, String base) throws TransformerException {
    if (urn.equals(href)) {
      // return main stylesheet location
      return stylesheet;
    } else if ((href != null) && href.startsWith(urn)) {
      // return the resource using the main stylesheet location directory as base
      int dirIndex = stylesheet.getSystemId().lastIndexOf("/");
      String dirPath = stylesheet.getSystemId().substring(0, dirIndex);
      String newLocation = dirPath.concat(href.replace(urn, ""));

      return new StreamSource(newLocation);
    } else {
      return wrapped.resolve(href, base);
    }
  }
}
