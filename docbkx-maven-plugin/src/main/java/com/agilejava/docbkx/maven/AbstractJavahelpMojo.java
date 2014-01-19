/*
 * #%L
 * Docbkx Maven Plugin
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

import java.io.File;

import javax.xml.transform.Transformer;

/**
 * The base class of a Mojo generating JavaHelp documentation.
 *
 * @author Cedric Pronzato
 */
public abstract class AbstractJavahelpMojo extends AbstractTransformerMojo {
  /**
   * {@inheritDoc} This implementation will set the root.filename property, based on the
   * targetFile's name.
   */
  public void adjustTransformer(Transformer transformer, String sourceFilename, File targetFile) {
    super.adjustTransformer(transformer, sourceFilename, targetFile);

    String rootFilename = targetFile.getName();
    rootFilename = rootFilename.substring(0, rootFilename.lastIndexOf('.'));
    transformer.setParameter("root.filename", rootFilename);
    transformer.setParameter("base.dir", targetFile.getParent() + File.separator);
  }
}
