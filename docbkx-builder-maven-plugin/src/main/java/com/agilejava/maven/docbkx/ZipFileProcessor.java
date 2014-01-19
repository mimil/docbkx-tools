/*
 * #%L
 * Docbkx Maven Plugin Builder
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
package com.agilejava.maven.docbkx;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

/**
 * A mechanism for accessing the contents of zip files. The {@link
 * #process(com.agilejava.maven.docbkx.ZipFileProcessor.ZipEntryVisitor)} operation accepts a
 * {@link ZipFileProcessor.ZipEntryVisitor ZipEntryVisitor} that will be notified of every entry
 * encountered in the zip file. This will eventually allow us to send in a single compound visitor
 * executing several actions based on the entries encountered.
 *
 * @author Wilfred Springer
 */
public class ZipFileProcessor {
  /**
   * The zip file wrapped.
   */
  private File file;

  /**
   * Constructs a new instance, wrapping the <code>file</code> passed in.
   *
   * @param file
   *            The zip file wrapped by this object.
   */
  public ZipFileProcessor(File file) {
    this.file = file;
  }

  /**
   * Processes the contents of the zip file by processing all zip file entries in sequence
   * and calling {@link ZipFileProcessor.ZipEntryVisitor#visit(ZipEntry, InputStream)} for every
   * zip file entry encountered.
   *
   * @param visitor The visitor receiving the events.
   *
   * @throws IOException If it turned out to be impossible to read entries from the zip file passed
   *         in.
   */
  public void process(ZipEntryVisitor visitor) throws IOException {
    InputStream in = null;
    ZipInputStream zipIn = null;

    try {
      in = new FileInputStream(file);
      in = new BufferedInputStream(in);
      zipIn = new ZipInputStream(in);

      ZipEntry entry = null;

      while ((entry = zipIn.getNextEntry()) != null) {
        visitor.visit(entry, new SafeZipEntryInputStream(entry, zipIn));
      }
    } finally {
      IOUtils.closeQuietly(zipIn);
      IOUtils.closeQuietly(in);
    }
  }

  /**
   * The interface to be implemented by all objects that want to be notified
   * of entries in a zip file.
   *
   */
  public interface ZipEntryVisitor {
    /**
     * Notifies the visitor of a zip file entry encoutered.
     *
     * @param entry The {@link ZipEntry} detected.
     * @param in An {@link InputStream} allowing you to read directly the decompressed data from
     *        the zip entry.
     *
     * @throws IOException When the visitor fails to read from the <code>InputStream</code>.
     */
    void visit(ZipEntry entry, InputStream in) throws IOException;
  }

  /**
   * An {@link InputStream} wrapping a ZipInputStream preventing you from reading beyond the
   * end of the zip entry.
   */
  private static class SafeZipEntryInputStream extends InputStream {
    /**
     * The current position.
     */
    private int pos = 0;

    /**
     * The size of the entry.
     */
    private long size;

    /**
     * The {@link ZipInputStream} providing the data.
     */
    private ZipInputStream in;

    /**
     * Constructs a new instance.
     *
     * @param entry The {@link ZipEntry} allowing you to access the details of the compressed zip entry.
     * @param in An <code>InputStream</code> providing <em>direct</em> access to the zip file entry.
     */
    public SafeZipEntryInputStream(ZipEntry entry, ZipInputStream in) {
      size = entry.getCompressedSize();
      this.in = in;
    }

    // JavaDoc inherited
    public int read() throws IOException {
      if (pos >= size) {
        return -1;
      } else {
        return in.read();
      }
    }
  }
}
