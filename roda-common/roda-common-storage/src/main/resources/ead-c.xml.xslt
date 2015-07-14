<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:eadc="http://roda.dgarq.gov.pt/2008/EADCSchema"
	exclude-result-prefixes="eadc">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />

	<xsl:template match="/">
		<doc>
			<xsl:apply-templates />
		</doc>
	</xsl:template>
	<xsl:template match="eadc:ead-c">
		<xsl:if test="@level">
			<field name="level">
				<xsl:value-of select="@level" />
			</field>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:unittitle/text()">
			<field name="title">
				<xsl:value-of select="eadc:did/eadc:unittitle/text()" />
			</field>
		</xsl:if>
		<xsl:if test="eadc:scopecontent/eadc:p/text()">
			<field name="description">
				<xsl:value-of select="eadc:scopecontent/eadc:p/text()" />
			</field>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>