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

import java.net.URI;

/**
 * Font information, required if you want to customize the fonts used to generate PDF.
 *
 * @author Wilfred Springer
 */
public class Font {
  private File metricsFile;
  private boolean kerning;
  private File embedFile;
  private String name;
  private String style;
  private String weight;

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public File getMetricsFile() {
    return metricsFile;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public URI getMetricsURI() {
    return (metricsFile == null) ? null : metricsFile.toURI();
  }

  /**
   * DOCUMENT ME!
   *
   * @param metricsFile DOCUMENT ME!
   */
  public void setMetricsFile(File metricsFile) {
    this.metricsFile = metricsFile;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public boolean isKerning() {
    return kerning;
  }

  /**
   * DOCUMENT ME!
   *
   * @param kerning DOCUMENT ME!
   */
  public void setKerning(boolean kerning) {
    this.kerning = kerning;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public File getEmbedFile() {
    return embedFile;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public URI getEmbedURI() {
    return (embedFile == null) ? null : embedFile.toURI();
  }

  /**
   * DOCUMENT ME!
   *
   * @param embedURL DOCUMENT ME!
   */
  public void setEmbedFile(File embedURL) {
    this.embedFile = embedURL;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getName() {
    return name;
  }

  /**
   * DOCUMENT ME!
   *
   * @param name DOCUMENT ME!
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getStyle() {
    return style;
  }

  /**
   * DOCUMENT ME!
   *
   * @param style DOCUMENT ME!
   */
  public void setStyle(String style) {
    this.style = style;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getWeight() {
    return weight;
  }

  /**
   * DOCUMENT ME!
   *
   * @param weight DOCUMENT ME!
   */
  public void setWeight(String weight) {
    this.weight = weight;
  }
}
