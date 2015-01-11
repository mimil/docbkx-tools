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

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * The base class of all other mojos. Introduced to add some common behaviour, outside of the
 * {@link AbstractTransformerMojo}.
 *
 * @author Wilfred Springer
 */
public abstract class AbstractMojoBase extends AbstractTransformerMojo {
  /**
   * DOCUMENT ME!
   *
   * @throws MojoExecutionException DOCUMENT ME!
   */
  public void preProcess() throws MojoExecutionException {
    super.preProcess();
    configureXslthl();
  }

  private void configureXslthl() {
    URL url = this.getClass().getClassLoader().getResource("docbook/highlighting/xslthl-config.xml");

    final String config = getProperty("highlightXslthlConfig");
    final String xslthlSysProp = System.getProperty("xslthl.config");

    if (config != null) {
      url = convertToUrl(config);
    } else if (xslthlSysProp != null) {
      // fallback on system property as in previous version of xslthl
      url = convertToUrl(xslthlSysProp);
    }

    // else using config file provided in the release
    if (url == null) {
      getLog().error("Error while converting XSLTHL config file");
    } else {
      setProperty("highlightXslthlConfig", url.toExternalForm());
    }
  }

  /**
   * Converts a conventional path to url format bzcause XSLTHL only takes as input a
   * configuration file path given as an url.
   *
   * @param path The path to format.
   *
   * @return The formated path or null if an error occurred.
   *
   * @throws IllegalArgumentException If the input path is null.
   */
  private URL convertToUrl(String path) {
    if (path == null) {
      throw new IllegalArgumentException("Config file path must not be null");
    }

    final String s = path.replace("file:///", "/");
    final File file = new File(s);

    if (!file.exists() || !file.isFile() || !file.canRead()) {
      getLog().warn("The given XSLTHL config file seems to not be legal: " + path);
    } else {
      try {
        return file.toURL();
      } catch (MalformedURLException e) {
        getLog().error(e);
      }
    }

    return null;
  }
}
