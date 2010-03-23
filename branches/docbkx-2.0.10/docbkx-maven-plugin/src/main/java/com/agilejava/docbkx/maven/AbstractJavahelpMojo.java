package com.agilejava.docbkx.maven;

import java.io.File;

import javax.xml.transform.Transformer;

/**
 * The base class of a Mojo generating JavaHelp documentation.
 *
 * @author Cedric Pronzato
 *
 */
public abstract class AbstractJavahelpMojo extends AbstractTransformerMojo {

    /**
     * {@inheritDoc} This implementation will set the root.filename property,
     * based on the targetFile's name.
     */
    public void adjustTransformer(Transformer transformer,
            String sourceFilename, File targetFile) {
        super.adjustTransformer(transformer, sourceFilename, targetFile);
        String rootFilename = targetFile.getName();
        rootFilename = rootFilename.substring(0, rootFilename.lastIndexOf('.'));
        transformer.setParameter("root.filename", rootFilename);
        transformer.setParameter("base.dir", targetFile.getParent()
                + File.separator);
    }

}