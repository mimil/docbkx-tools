package com.agilejava.docbkx.maven;

import java.io.IOException;

import javax.xml.transform.Transformer;

/**
 * An object creating a {@link Transformer}.
 * 
 * @author Wilfred Springer
 * 
 */
public interface TransformerBuilder {

    /**
     * Builds a {@link Transformer}.
     * 
     * @return The {@link Transformer} built.
     * @throws TransformerBuilderException
     *             If the object fails to build a {@link Transformer}.
     */
    Transformer build() throws TransformerBuilderException;

    /**
     * The exception thrown when the {@link TransformerBuilder} will not be able
     * to reproduce the Transformer.
     * 
     */
    public class TransformerBuilderException extends RuntimeException {

        public TransformerBuilderException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}
