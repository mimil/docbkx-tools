<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pom="http://maven.apache.org/POM/4.0.0"
                version="1.0">

  <xsl:output method="text"/>

  <xsl:template name="nl">
    <xsl:text>
</xsl:text>
  </xsl:template>

  <xsl:template match="/">
    <xsl:text>#!/bin/sh</xsl:text>

    <xsl:call-template name="nl"/>
    <xsl:call-template name="nl"/>

    <xsl:text>mkdir -p target/tmp</xsl:text>
    <xsl:call-template name="nl"/>

    <xsl:for-each select="//pom:profile[not(pom:id='docbkx.release')]">
      <xsl:text>mvn -P </xsl:text>
      <xsl:value-of select="pom:id"/>
      <xsl:text> docbkx:generate-html &gt;</xsl:text>
      <xsl:text> target/tmp/</xsl:text>
      <xsl:value-of select="pom:id"/>
      <xsl:text>.out</xsl:text>
      <xsl:call-template name="nl"/>
    </xsl:for-each>

  </xsl:template>

</xsl:stylesheet>
