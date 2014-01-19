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

import javax.xml.transform.Transformer;

/**
 * A caching {@link TransformerBuilder}, holding on to the Transformer it created. Note that
 * this class is thread-safe.
 *
 * @author Wilfred Springer
 */
public class CachingTransformerBuilder implements TransformerBuilder {
  private Transformer transformer;
  private TransformerBuilder builder;

  /**
   * Constructs a new instance.
   * 
   * @param builder
   *            The {@link TransformerBuilder} creating the actual instance of
   *            the Transformer.
   */
  public CachingTransformerBuilder(TransformerBuilder builder) {
    if (builder == null)
      throw new IllegalArgumentException("TransformerBuilder should not be null.");

    this.builder = builder;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws TransformerBuilderException DOCUMENT ME!
   */
  public synchronized Transformer build() throws TransformerBuilderException {
    if (transformer == null) {
      transformer = builder.build();
    }

    return transformer;
  }
}
