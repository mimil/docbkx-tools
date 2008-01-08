package com.agilejava.docbkx.maven;

import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

public class ExpressionUtilsTest extends TestCase {

    public void testCreateTreeTest() {
        Properties properties = new Properties();
        properties.setProperty("foo.1", "bar");
        properties.setProperty("foo.2", "foo");
        Map tree = ExpressionUtils.createTree(properties);
        assertEquals(1, tree.size());
        assertTrue(tree.containsKey("foo"));
        Map subtree = (Map) tree.get("foo");
        assertEquals(2, subtree.size());
        assertTrue(subtree.containsKey("1"));
        assertTrue(subtree.containsKey("2"));
    }

}
