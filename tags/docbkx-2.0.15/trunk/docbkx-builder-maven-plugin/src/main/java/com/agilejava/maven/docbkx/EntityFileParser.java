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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.codehaus.plexus.util.StringInputStream;

/**
 * A crude parser for parsing an entity file. Required to extract the files describing the
 * parameters from the parameter declarations.
 *
 * @author Wilfred Springer
 */
public class EntityFileParser {
  /**
   * Parses the entity file, invoking operations on the visitor for every entity found.
   *
   * @param in The InputStream to parse.
   * @param visitor The visitor receiving the events.
   *
   * @throws IOException The exception thrown when we can't read from the InputStream.
   */
  public static void parse(InputStream in, EntityVisitor visitor) throws IOException {
    LineNumberReader reader = new LineNumberReader(new InputStreamReader(in));
    String line = null;

    while ((line = reader.readLine()) != null) {
      if (line.indexOf("SYSTEM") > -1) {
        String[] parts = line.split(" ");
        String name = parts[1];
        String systemId = parts[3].substring(1, parts[3].length() - 2);
        visitor.visitSystemEntity(name, systemId);
      }
    }
  }

  /**
   * Tests the parser. TODO: Take this out.
   *
   * @param args
   */
  public final static void main(String[] args) {
    StringBuffer builder = new StringBuffer();
    builder.append("<!ENTITY foo SYSTEM \"bar\">\n");
    builder.append("<!ENTITY bar SYSTEM \"foo\">\n");

    StringInputStream in = new StringInputStream(builder.toString());

    try {
      EntityFileParser.parse(in, new EntityVisitor() {
        public void visitSystemEntity(String name, String systemId) {
          System.out.println("Name: " + name);
          System.out.println("SystemId: " + systemId);
        }
      });
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  /**
   * A simple interface to be implemented by objects traversing the contents
   * of an entity file.
   *
   */
  public interface EntityVisitor {
    /**
     * Invoked whenever the parser encounters a system entity.
     *
     * @param name The name of the entity.
     * @param systemId The system id of the entity.
     */
    void visitSystemEntity(String name, String systemId);
  }
}
