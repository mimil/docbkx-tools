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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;

import com.agilejava.maven.docbkx.ZipFileProcessor.ZipEntryVisitor;

import junit.framework.TestCase;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
 */
public class ZipFileProcessorTest extends TestCase {
  private static List FILENAMES = Arrays.asList(new String[] { "test1.txt", "test2.txt", "test3.txt" });

  /**
   * DOCUMENT ME!
   *
   * @throws Exception DOCUMENT ME!
   */
  public void testProcessFile() throws Exception {
    File file = new File(getDirectory(), "sample.zip");
    ZipFileProcessor processor = new ZipFileProcessor(file);
    final int[] count = new int[1];
    processor.process(new ZipEntryVisitor() {
      public void visit(ZipEntry entry, InputStream in) throws IOException {
        count[0]++;
        assertTrue(FILENAMES.contains(entry.getName()));

        if ((entry.getCompressedSize() > 0) && entry.getName().endsWith("test2.txt")) {
          assertEquals("i have a dream", IOUtils.toString(in));
          in.close();
        }
      }
    });
    assertEquals(3, count[0]);
  }

  private File getDirectory() {
    String dirname = System.getProperty("basedir");
    dirname = (dirname == null) ? System.getProperty("user.dir") : dirname;

    File dir = new File(dirname);
    dir = new File(dir, "src");
    dir = new File(dir, "test");
    dir = new File(dir, "resources");

    return dir;
  }
}
