/*
 * #%L
 * Docbkx FOP Support
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
package com.agilejava.docbkx.support.fop;

import java.io.File;

import org.codehaus.plexus.PlexusTestCase;

/**
 * DOCUMENT ME!
 *
 * @author Cedric Pronzato
 */
public class FontmetricsMojoTest extends PlexusTestCase {
  /**
   * DOCUMENT ME!
   *
   * @param mojo DOCUMENT ME!
   */
  public void initMojoDefault(FontmetricsMojo mojo) {
    mojo.sourceDirectory = new File(PlexusTestCase.getBasedir(), "src/test/resources/fonts/");
    assertTrue("Default sourceDirectory should be valid", mojo.sourceDirectory.isDirectory());
    mojo.targetDirectory = new File(PlexusTestCase.getBasedir(), "target/test/fonts/");
    mojo.targetDirectory.mkdirs();
  }

  /**
   * DOCUMENT ME!
   *
   * @param mojo DOCUMENT ME!
   */
  public void validateGen(FontmetricsMojo mojo) {
    File expectedGen = new File(mojo.targetDirectory, "Pecita-metrics.xml");
    assertTrue("metrics file not generated: " + expectedGen.getAbsolutePath(), expectedGen.exists());
    // space in font name
    expectedGen = new File(mojo.targetDirectory, "Pe cita-metrics.xml");
    assertTrue("metrics file not generated: " + expectedGen.getAbsolutePath(), expectedGen.exists());
  }

  /**
   * DOCUMENT ME!
   *
   * @throws Exception DOCUMENT ME!
   */
  public void testFontmetricsGen() throws Exception {
    FontmetricsMojo mojo = new FontmetricsMojo();
    initMojoDefault(mojo);
    mojo.execute();

    validateGen(mojo);
  }

  /**
   * DOCUMENT ME!
   *
   * @throws Exception DOCUMENT ME!
   */
  public void testFontmetricsGenSpaceDir() throws Exception {
    FontmetricsMojo mojo = new FontmetricsMojo();
    initMojoDefault(mojo);
    mojo.targetDirectory = new File(PlexusTestCase.getBasedir(), "target/test/fon ts/");
    mojo.targetDirectory.mkdirs();
    mojo.execute();

    validateGen(mojo);
  }
}
