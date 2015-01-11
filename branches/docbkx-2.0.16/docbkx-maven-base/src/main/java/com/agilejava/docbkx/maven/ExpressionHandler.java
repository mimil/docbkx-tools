/*
 * #%L
 * Docbkx Maven Base
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

import java.lang.reflect.Method;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.FunctionMapper;
import javax.servlet.jsp.el.VariableResolver;

import org.apache.commons.el.ExpressionEvaluatorImpl;

import org.apache.maven.plugin.logging.Log;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A {@link ProcessingInstructionHandler} that evaluates expressions passed as data as JSP
 * expression language expressions.
 *
 * @author Wilfred Springer
 */
public class ExpressionHandler implements ProcessingInstructionHandler {
  /**
   * The name of the processing instruction.
   */
  private final String PI_NAME = "eval";

  /**
   * The object responsible for resolving variables.
   */
  private VariableResolver resolver;

  /**
   * The object used for logging.
   */
  private Log log;

  /**
   * A simplified function mapper that basically does not support any functions at all.
   */
  private FunctionMapper mapper = new FunctionMapper() {
    public Method resolveFunction(String prefix, String localName) {
      return null;
    }
  };

  /**
   * Constructs a new instance.
   * 
   * @param resolver
   *            The object used for resolving variables. (Not
   *            <code>null</code>.)
   * @param log
   *            The object used for logging. (Not <code>null</code>.)
   */
  public ExpressionHandler(VariableResolver resolver, Log log) {
    this.resolver = resolver;
    this.log = log;
  }

  // JavaDoc inherited
  /**
   * DOCUMENT ME!
   *
   * @param data DOCUMENT ME!
   * @param handler DOCUMENT ME!
   */
  public void handle(String data, ContentHandler handler) {
    ExpressionEvaluator evaluator = new ExpressionEvaluatorImpl();
    Object value;

    try {
      value = evaluator.evaluate(data, Object.class, resolver, mapper);

      if (value != null) {
        char[] result = value.toString().toCharArray();
        handler.characters(result, 0, result.length);
      } else {
        log.debug("Failed to resolve " + data);
      }
    } catch (ELException ele) {
      log.error("Failed to handle EL expression.", ele);
    } catch (SAXException saxe) {
      log.error("Failed to generate content.", saxe);
    }
  }

  // JavaDoc inherited
  /**
   * DOCUMENT ME!
   *
   * @param target DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public boolean matches(String target) {
    return PI_NAME.matches(target);
  }
}
