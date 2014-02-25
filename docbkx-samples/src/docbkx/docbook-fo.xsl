<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:xslthl="http://xslthl.sf.net"
                exclude-result-prefixes="xslthl"
                version="1.0">
  <!-- import the main stylesheet, here pointing to fo/docbook.xsl -->
  <xsl:import href="urn:docbkx:stylesheet"/>
  <!-- highlight.xsl must be imported in order to enable highlighting support, highlightSource=1 parameter
   is not sufficient -->
  <xsl:import href="urn:docbkx:stylesheet/highlight.xsl"/>
  <!-- some customization -->
  <xsl:attribute-set name="section.title.level1.properties">
    <xsl:attribute name="border-top">0.5pt solid black</xsl:attribute>
    <xsl:attribute name="border-bottom">0.5pt solid black</xsl:attribute>
    <xsl:attribute name="padding-top">6pt</xsl:attribute>
    <xsl:attribute name="padding-bottom">3pt</xsl:attribute>
  </xsl:attribute-set>

  <!-- some customization for xslt highlighting -->
  <xsl:template match='xslthl:keyword' mode="xslthl">
    <fo:inline font-weight="bold" color="blue"><xsl:apply-templates/></fo:inline>
  </xsl:template>

  <xsl:template match='xslthl:comment' mode="xslthl">
    <fo:inline font-style="italic" color="grey"><xsl:apply-templates/></fo:inline>
  </xsl:template>

</xsl:stylesheet>
