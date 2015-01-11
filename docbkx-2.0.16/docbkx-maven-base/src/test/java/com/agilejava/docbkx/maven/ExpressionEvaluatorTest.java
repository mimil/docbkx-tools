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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.FunctionMapper;
import javax.servlet.jsp.el.VariableResolver;

import org.apache.commons.el.ExpressionEvaluatorImpl;

import junit.framework.TestCase;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
 */
public class ExpressionEvaluatorTest extends TestCase {
  /**
   * DOCUMENT ME!
   *
   * @throws ELException DOCUMENT ME!
   */
  public void testEvaluator() throws ELException {
    ExpressionEvaluator evaluator = new ExpressionEvaluatorImpl();
    final Map foo = new HashMap();
    foo.put("bar", "whatever");

    Object result = evaluator.evaluate("${foo.bar}", Object.class, new VariableResolver() {
      public Object resolveVariable(String name) throws ELException {
        System.out.println(name);

        return foo;
      }
    }, new FunctionMapper() {
      public Method resolveFunction(String arg0, String arg1) {
        // TODO Auto-generated method stub
        return null;
      }
    });

    System.out.println(result);
  }
}
