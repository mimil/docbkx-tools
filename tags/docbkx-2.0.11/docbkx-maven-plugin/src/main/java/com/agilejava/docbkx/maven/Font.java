package com.agilejava.docbkx.maven;

import java.io.File;
import java.net.URI;

/**
 * Font information, required if you want to customize the fonts used to
 * generate PDF.
 * 
 * 
 * @author Wilfred Springer
 * 
 */
public class Font {

    private File metricsFile;

    private boolean kerning;

    private File embedFile;

    private String name;

    private String style;

    private String weight;

    public File getMetricsFile() {
        return metricsFile;
    }

    public URI getMetricsURI() {
        return metricsFile == null ? null : metricsFile.toURI();
    }

    public void setMetricsFile(File metricsFile) {
        this.metricsFile = metricsFile;
    }

    public boolean isKerning() {
        return kerning;
    }

    public void setKerning(boolean kerning) {
        this.kerning = kerning;
    }

    public File getEmbedFile() {
        return embedFile;
    }

    public URI getEmbedURI() {
        return embedFile == null ? null : embedFile.toURI();
    }

    public void setEmbedFile(File embedURL) {
        this.embedFile = embedURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

}
