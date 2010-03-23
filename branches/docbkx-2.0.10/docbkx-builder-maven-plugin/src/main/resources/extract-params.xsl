<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:t="http://www.w3.org/1999/XSL/Transform"
		exclude-result-prefixes="t"
		version="1.0">

  <xsl:output method="text"/>

  <xsl:template match="/">
    <xsl:apply-templates select="t:stylesheet"/>
  </xsl:template>

  <xsl:template match="t:stylesheet">
    <xsl:apply-templates select="t:param|t:include|t:import"/>
  </xsl:template>

  <xsl:template match="t:param">
    <xsl:value-of select="@name"/>
    <xsl:text>
</xsl:text>
  </xsl:template>

  <xsl:template match="t:include|t:import">
    <xsl:apply-templates select="document(@href)"/>
  </xsl:template>

</xsl:stylesheet>