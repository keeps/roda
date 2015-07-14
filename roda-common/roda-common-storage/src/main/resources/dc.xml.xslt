<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dc="http://purl.org/dc/elements/1.1/"
	exclude-result-prefixes="dc">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />

	<xsl:template match="/">
		<doc>
			<xsl:apply-templates />
		</doc>
	</xsl:template>
	<xsl:template match="metadata">
		<xsl:if test="dc:title/text()">
			<field name="title">
				<xsl:value-of select="dc:title/text()" />
			</field>
		</xsl:if>
		<xsl:if test="dc:description/text()">
			<field name="description">
				<xsl:value-of select="dc:description/text()" />
			</field>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>