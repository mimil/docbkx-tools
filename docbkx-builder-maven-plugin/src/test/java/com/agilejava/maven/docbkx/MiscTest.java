package com.agilejava.maven.docbkx;

import junit.framework.TestCase;

/**
 * Created by mimil on 17/04/16.
 */
public class MiscTest  extends TestCase {

    public static final String sampleCustomization = "/projet/docbkx-tools-github/docbkx-samples/classpath:/docbook/html/docbook.xsl";
    public static final String expectedCustomization = "docbook/html/docbook.xsl";

    public static final String wrongSampleCustomization = "/projet/ddocbook/html/docbook.xsl";

    public void testClasspathSubstitution()  {
        int foundOffcet = sampleCustomization.indexOf("classpath:");
        if(foundOffcet != -1) {
            final String subString = sampleCustomization.substring(foundOffcet + 11);
            assertEquals(expectedCustomization, subString);
        } else {
            fail("Wrong substitution");
        }


    }

}
