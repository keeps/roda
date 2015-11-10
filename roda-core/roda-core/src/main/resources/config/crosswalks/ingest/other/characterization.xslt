<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:jhove="http://hul.harvard.edu/ois/xml/ns/jhove"
	xmlns:fits="http://hul.harvard.edu/ois/xml/ns/fits/fits_output"
	exclude-result-prefixes="dc">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />

	<xsl:template match="/">
		<doc>
			<xsl:apply-templates />
		</doc>
	</xsl:template>
	<xsl:template match="//jhove:jhove">
		<xsl:if test="jhove:repInfo/jhove:status">
			<field name="status_s">
				<xsl:value-of select="jhove:repInfo/jhove:status/text()" />
			</field>
		</xsl:if>
	</xsl:template>
	<xsl:template match="//fits:fits">
		<xsl:if test="fits:filestatus/fits:well-formed">
			<field name="wellformed_s">
				<xsl:value-of select="fits:filestatus/fits:well-formed/text()" />
			</field>
		</xsl:if>
		<xsl:if test="fits:filestatus/fits:valid">
			<field name="valid_s">
				<xsl:value-of select="fits:filestatus/fits:valid/text()" />
			</field>
		</xsl:if>
		<xsl:if test="fits:metadata/fits:image/fits:compressionScheme">
			<field name="compressionScheme_s">
				<xsl:value-of select="fits:metadata/fits:image/fits:compressionScheme/text()" />
			</field>
		</xsl:if>		
	</xsl:template>
</xsl:stylesheet>