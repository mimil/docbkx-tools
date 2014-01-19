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

/*
 * Copyright 2013 Cedric Pronzato
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.util.FileUtils;

import javax.xml.transform.Transformer;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * A dedicated base class for plugins generating ePub version 3 output, in order to allow
 * the specific stylesheet chosen to be dependent on the {@link #chunkedOutput}
 * property.
 *
 * @author Cedric Pronzato
 */
public abstract class AbstractEpub3Mojo extends AbstractMojoBase {

  /**
   * {@inheritDoc} This implementation will set the root.filename property,
   * based on the targetFile's name.
   */
  public void adjustTransformer(Transformer transformer, String sourceFilename, File targetFile) {
    super.adjustTransformer(transformer, sourceFilename, targetFile);

    String rootFilename = targetFile.getName();
    rootFilename = rootFilename.substring(0, rootFilename.lastIndexOf('.'));
    transformer.setParameter("root.filename", rootFilename);
    transformer.setParameter("base.dir", targetFile.getParent() + File.separator);
    transformer.setParameter("epub.package.dir",  targetFile.getParent()  + File.separator);
    transformer.setParameter("epub.metainf.dir", File.separator + "META-INF" + File.separator);
    transformer.setParameter("chunk.base.dir", targetFile.getParent()  + File.separator);
    transformer.setParameter("epub.package.filename", "content.opf"); // hack to reuse hard coded container.xml

  }

  public void postProcessResult(File result) throws MojoExecutionException {
    super.postProcessResult(result);

    final File targetDirectory = result.getParentFile();

    // override current container.xml
    try {
      final URL containerURL = getClass().getResource("/epub/container.xml"); // reuse of container.cml from epub output
      FileUtils.copyURLToFile(containerURL, new File(targetDirectory, "META-INF" + File.separator + "container.xml"));
    } catch (IOException e) {
      throw new MojoExecutionException("Unable to copy hardcoded container.xml file", e);
    }


    try {
      ZipArchiver zipArchiver = new ZipArchiver();
      zipArchiver.addDirectory(targetDirectory);
      zipArchiver.setCompress(true); // seems to not be a problem to have mimetype compressed
      zipArchiver.setDestFile(new File(targetDirectory.getParentFile(), result.getName())); // copy it to parent dir
      zipArchiver.createArchive();

      getLog().debug("epub file created at: " + zipArchiver.getDestFile().getAbsolutePath());
    } catch (Exception e) {
      throw new MojoExecutionException("Unable to zip epub file", e);
    }
  }

}
