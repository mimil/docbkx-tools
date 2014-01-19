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

import org.apache.maven.plugin.MojoExecutionException;

import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.transform.Transformer;

/**
 * A dedicated base class for plugins generating ePub version 2 output, in order to allow
 * the specific stylesheet chosen to be dependent on the {@link #chunkedOutput}
 * property.
 *
 * @author Cedric Pronzato
 * @author Brian Richard Jackson
 */
public abstract class AbstractEpubMojo extends AbstractMojoBase {

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
    transformer.setParameter("epub.oebps.dir", targetFile.getParent() + File.separator);
    transformer.setParameter("epub.metainf.dir", targetFile.getParent() + File.separator + "META-INF" + File.separator);
  }

  public void postProcessResult(File result) throws MojoExecutionException {
    super.postProcessResult(result);

    final File targetDirectory = result.getParentFile();
    try {
      final URL containerURL = getClass().getResource("/epub/container.xml");
      FileUtils.copyURLToFile(containerURL, new File(targetDirectory, "META-INF" + File.separator + "container.xml"));
    } catch (IOException e) {
      throw new MojoExecutionException("Unable to copy hardcoded container.xml file", e);
    }

    // copy mimetype file
    try {
      final URL mimetypeURL = getClass().getResource("/epub/mimetype");
      FileUtils.copyURLToFile(mimetypeURL, new File(targetDirectory, "mimetype"));
    } catch (IOException e) {
      throw new MojoExecutionException("Unable to copy hardcoded mimetype file", e);
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
