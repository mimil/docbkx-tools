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

import java.io.IOException;
import java.io.Writer;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
 */
public class NullWriter extends Writer {
  /**
   * DOCUMENT ME!
   *
   * @throws IOException DOCUMENT ME!
   */
  public void close() throws IOException {
  }

  /**
   * DOCUMENT ME!
   *
   * @throws IOException DOCUMENT ME!
   */
  public void flush() throws IOException {
  }

  /**
   * DOCUMENT ME!
   *
   * @param cbuf DOCUMENT ME!
   * @param off DOCUMENT ME!
   * @param len DOCUMENT ME!
   *
   * @throws IOException DOCUMENT ME!
   */
  public void write(char[] cbuf, int off, int len) throws IOException {
  }
}
