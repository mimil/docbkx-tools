package com.agilejava.docbkx.maven;

/*
 * Copyright 2006 Wilfred Springer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.avalon.framework.logger.Logger;
import org.apache.maven.plugin.logging.Log;

public class AvalonMavenBridgeLogger implements Logger {

    private Log mavenLog;

    private boolean errorIsDebug = false;

    private boolean warnIsDebug = false;

    public AvalonMavenBridgeLogger(Log mavenLog) {
        this.mavenLog = mavenLog;
    }

    public AvalonMavenBridgeLogger(Log mavenLog, boolean errorIsDebug,
            boolean warnIsDebug) {
        this(mavenLog);
        this.errorIsDebug = errorIsDebug;
        this.warnIsDebug = warnIsDebug;
    }

    public void debug(String arg0) {
        mavenLog.debug(arg0);
    }

    public void debug(String arg0, Throwable arg1) {
        mavenLog.debug(arg0, arg1);
    }

    public void error(String arg0) {
        if (errorIsDebug) {
            debug(arg0);
        } else {
            mavenLog.error(arg0);
        }
    }

    public void error(String arg0, Throwable arg1) {
        if (errorIsDebug) {
            debug(arg0, arg1);
        } else {
            mavenLog.error(arg0, arg1);
        }
    }

    public void fatalError(String arg0) {
        mavenLog.error(arg0);
    }

    public void fatalError(String arg0, Throwable arg1) {
        mavenLog.error(arg0, arg1);
    }

    public Logger getChildLogger(String arg0) {
        return null;
    }

    public void info(String arg0) {
        mavenLog.info(arg0);
    }

    public void info(String arg0, Throwable arg1) {
        mavenLog.info(arg0, arg1);
    }

    public boolean isDebugEnabled() {
        return mavenLog.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return mavenLog.isErrorEnabled();
    }

    public boolean isFatalErrorEnabled() {
        return mavenLog.isErrorEnabled();
    }

    public boolean isInfoEnabled() {
        return mavenLog.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return mavenLog.isWarnEnabled();
    }

    public void warn(String arg0) {
        if (warnIsDebug) {
            debug(arg0);
        } else {
            mavenLog.warn(arg0);
        }
    }

    public void warn(String arg0, Throwable arg1) {
        if (warnIsDebug) {
            debug(arg0, arg1);
        } else {
            mavenLog.warn(arg0, arg1);
        }
    }

}
