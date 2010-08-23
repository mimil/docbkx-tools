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

public class ExpressionEvaluatorTest extends TestCase {

    public void testEvaluator() throws ELException {
        ExpressionEvaluator evaluator = new ExpressionEvaluatorImpl();
        final Map foo = new HashMap();
        foo.put("bar", "whatever");
        Object result =
        evaluator.evaluate("${foo.bar}", Object.class, new VariableResolver() {

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
