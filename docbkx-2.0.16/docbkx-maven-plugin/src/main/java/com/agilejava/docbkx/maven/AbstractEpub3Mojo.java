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
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.archiver.zip.ZipEntry;
import org.codehaus.plexus.archiver.zip.ZipOutputStream;
import org.codehaus.plexus.util.FileUtils;

import javax.xml.transform.Transformer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.zip.CRC32;

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

  private static byte [] makeMimetype () throws UnsupportedEncodingException {
      return "application/epub+zip".getBytes ("UTF-8");
  }
  
  public void postProcessResult(File result) throws MojoExecutionException {
    super.postProcessResult(result);

    final File targetDirectory = result.getParentFile();

    // override current container.xml
    try {
      final URL containerURL = getClass().getResource("/epub/container.epub3.xml"); // reuse of container.cml from epub output
      FileUtils.copyURLToFile(containerURL, new File(targetDirectory, "META-INF" + File.separator + "container.xml"));
    } catch (IOException e) {
      throw new MojoExecutionException("Unable to copy hardcoded container.xml file", e);
    }

    final byte [] mimetypeData;
    try {
        mimetypeData = makeMimetype ();
    } catch ( UnsupportedEncodingException e ) {
        throw new MojoExecutionException("Unable to create mimetype data", e);
    }
    
    try {
      // first delete "mimetype" that is already there
      final File mimetype = new File(targetDirectory,"mimetype");
      mimetype.delete ();
        
      ZipArchiver zipArchiver = new ZipArchiver() {
          protected void initZipOutputStream ( ZipOutputStream zOut ) throws IOException, ArchiverException
        {
            CRC32 crc = new CRC32 ();
            crc.update(mimetypeData);
            
            ZipEntry ze = new ZipEntry ("mimetype");
            ze.setMethod(ZipEntry.STORED);
            ze.setSize(mimetypeData.length );
            ze.setCrc(crc.getValue ());
            zOut.putNextEntry(ze);
            zOut.write(mimetypeData);
        }
      };
      
      // add content
      zipArchiver.addDirectory(targetDirectory);
      zipArchiver.setCompress(true);
      
      // set output file
      zipArchiver.setDestFile(new File(targetDirectory.getParentFile(), result.getName())); // copy it to parent dir
      
      zipArchiver.createArchive();

      getLog().debug("epub file created at: " + zipArchiver.getDestFile().getAbsolutePath());
    } catch (Exception e) {
      throw new MojoExecutionException("Unable to zip epub file", e);
    }
  }

}
