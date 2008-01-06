<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns="http://maven.apache.org/POM/4.0.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pom="http://maven.apache.org/POM/4.0.0"
		xmlns:redirect="http://xml.apache.org/xalan/redirect"
                xmlns:xalan="http://xml.apache.org/xalan"
                extension-element-prefixes="redirect xalan" 
                exclude-result-prefixes="xsi pom"
                version="1.0">

  <xsl:output method="xml" 
              indent="yes"
              xalan:indent-amount="2"/>
  <xsl:param name="pluginVersion"/>

  <xsl:template match="/">
    <xsl:apply-templates select="//pom:profile[not(pom:id='docbkx.release')]"/>
  </xsl:template>

  <xsl:template match="pom:profile">
    <xsl:variable name="artifactId">
      <xsl:text>sample-</xsl:text>
      <xsl:value-of select="translate(pom:id,'.','-')"/>
    </xsl:variable>
    <xsl:variable name="location">
      <xsl:text>target/samples/</xsl:text>
      <xsl:value-of select="$artifactId"/>
      <xsl:text>.pom</xsl:text>
    </xsl:variable>
    <redirect:write file="{$location}">
      <project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd" 
               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <modelVersion>4.0.0</modelVersion>
        <groupId>com.agilejava.docbkx</groupId>
        <artifactId><xsl:value-of select="$artifactId"/></artifactId>
        <version>n.a.</version>
        <packaging>pom</packaging>
        <name>
          <xsl:text>Example </xsl:text>
          <xsl:value-of select="position()"/>
          <xsl:text>: </xsl:text>
          <xsl:value-of select="pom:properties/pom:example.title/text()"/>
        </name>
        <xsl:apply-templates select="pom:build" mode="copy"/>
      </project>
    </redirect:write>
  </xsl:template>

  <xsl:template match="node()|@*" mode="copy">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="copy"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="pom:artifactId[text()='docbkx-maven-plugin' or text()='docbkx-fop-support']"
                mode="copy">
    <xsl:copy-of select="."/>
    <version><xsl:value-of select="$pluginVersion"/></version>
  </xsl:template>
  
  <xsl:template match="text()[not(normalize-space())]" mode="copy"/>


</xsl:stylesheet>
