<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:prem="info:lc/xmlns/premis-v2"
	exclude-result-prefixes="prem">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />

	<xsl:template match="/">
		<span type="premis">
			<xsl:apply-templates />
		</span>
	</xsl:template>
	<xsl:template match="prem:object">
		<xsl:if test="prem:preservationLevel/prem:preservationLevelValue/text()">
			<span field="preservationLevel">
        		<span type="label">Preservation level</span>
        		<span type="value"><xsl:value-of select="prem:preservationLevel/prem:preservationLevelValue/text()" /></span>
			</span>
		</xsl:if>
	</xsl:template>
	<xsl:template match="prem:event">
		<xsl:if test="prem:eventType/text()">
			<span field="eventType">
        		<span type="label">Event type</span>
        		<span type="value"><xsl:value-of select="prem:eventType/text()" /></span>
			</span>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>