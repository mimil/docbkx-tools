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

import org.apache.avalon.framework.logger.Logger;

import org.apache.maven.plugin.logging.Log;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
 */
public class AvalonMavenBridgeLogger implements Logger {
  private Log mavenLog;
  private boolean errorIsDebug = false;
  private boolean warnIsDebug = false;

  /**
   * Creates a new AvalonMavenBridgeLogger object.
   *
   * @param mavenLog DOCUMENT ME!
   */
  public AvalonMavenBridgeLogger(Log mavenLog) {
    this.mavenLog = mavenLog;
  }

  /**
   * Creates a new AvalonMavenBridgeLogger object.
   *
   * @param mavenLog DOCUMENT ME!
   * @param errorIsDebug DOCUMENT ME!
   * @param warnIsDebug DOCUMENT ME!
   */
  public AvalonMavenBridgeLogger(Log mavenLog, boolean errorIsDebug, boolean warnIsDebug) {
    this(mavenLog);
    this.errorIsDebug = errorIsDebug;
    this.warnIsDebug = warnIsDebug;
  }

  /**
   * DOCUMENT ME!
   *
   * @param arg0 DOCUMENT ME!
   */
  public void debug(String arg0) {
    if (mavenLog.isDebugEnabled())
      mavenLog.debug(arg0);
  }

  /**
   * DOCUMENT ME!
   *
   * @param arg0 DOCUMENT ME!
   * @param arg1 DOCUMENT ME!
   */
  public void debug(String arg0, Throwable arg1) {
    if (mavenLog.isDebugEnabled())
      mavenLog.debug(arg0, arg1);
  }

  /**
   * DOCUMENT ME!
   *
   * @param arg0 DOCUMENT ME!
   */
  public void error(String arg0) {
    if (errorIsDebug) {
      debug(arg0);
    } else {
      mavenLog.error(arg0);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param arg0 DOCUMENT ME!
   * @param arg1 DOCUMENT ME!
   */
  public void error(String arg0, Throwable arg1) {
    if (errorIsDebug) {
      debug(arg0, arg1);
    } else {
      mavenLog.error(arg0, arg1);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param arg0 DOCUMENT ME!
   */
  public void fatalError(String arg0) {
    mavenLog.error(arg0);
  }

  /**
   * DOCUMENT ME!
   *
   * @param arg0 DOCUMENT ME!
   * @param arg1 DOCUMENT ME!
   */
  public void fatalError(String arg0, Throwable arg1) {
    mavenLog.error(arg0, arg1);
  }

  /**
   * DOCUMENT ME!
   *
   * @param arg0 DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Logger getChildLogger(String arg0) {
    return null;
  }

  /**
   * DOCUMENT ME!
   *
   * @param arg0 DOCUMENT ME!
   */
  public void info(String arg0) {
    mavenLog.info(arg0);
  }

  /**
   * DOCUMENT ME!
   *
   * @param arg0 DOCUMENT ME!
   * @param arg1 DOCUMENT ME!
   */
  public void info(String arg0, Throwable arg1) {
    mavenLog.info(arg0, arg1);
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public boolean isDebugEnabled() {
    return mavenLog.isDebugEnabled();
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public boolean isErrorEnabled() {
    return mavenLog.isErrorEnabled();
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public boolean isFatalErrorEnabled() {
    return mavenLog.isErrorEnabled();
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public boolean isInfoEnabled() {
    return mavenLog.isInfoEnabled();
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public boolean isWarnEnabled() {
    return mavenLog.isWarnEnabled();
  }

  /**
   * DOCUMENT ME!
   *
   * @param arg0 DOCUMENT ME!
   */
  public void warn(String arg0) {
    if (warnIsDebug) {
      debug(arg0);
    } else {
      mavenLog.warn(arg0);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param arg0 DOCUMENT ME!
   * @param arg1 DOCUMENT ME!
   */
  public void warn(String arg0, Throwable arg1) {
    if (warnIsDebug) {
      debug(arg0, arg1);
    } else {
      mavenLog.warn(arg0, arg1);
    }
  }
}
