package com.agilejava.docbkx.maven;

import javax.xml.transform.Transformer;

/**
 * A caching {@link TransformerBuilder}, holding on to the Transformer it
 * created. Note that this class is thread-safe.
 * 
 * @author Wilfred Springer
 * 
 */
public class CachingTransformerBuilder implements TransformerBuilder {

    private Transformer transformer;

    private TransformerBuilder builder;

    /**
     * Constructs a new instance.
     * 
     * @param builder
     *            The {@link TransformerBuilder} creating the actual instance of
     *            the Transformer.
     */
    public CachingTransformerBuilder(TransformerBuilder builder) {
        if (builder == null)
            throw new IllegalArgumentException(
                    "TransformerBuilder should not be null.");
        this.builder = builder;
    }

    public synchronized Transformer build() throws TransformerBuilderException {
        if (transformer == null) {
            transformer = builder.build();
        }
        return transformer;
    }

}
