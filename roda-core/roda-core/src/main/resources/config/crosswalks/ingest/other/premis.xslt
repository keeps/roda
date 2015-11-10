<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:prem="info:lc/xmlns/premis-v2"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	exclude-result-prefixes="prem">
	<!-- <xsl:strip-space elements="*" /> -->
	<xsl:preserve-space elements="*" />
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />

	<xsl:template match="/">
		<doc>
			<xsl:apply-templates />
		</doc>
	</xsl:template>
	<xsl:template match="prem:agent">
		<!-- INDEX AGENT PROPERTIES -->
	</xsl:template>
	<xsl:template match="prem:object">
		<xsl:if
			test='resolve-QName(@xsi:type, .) = QName("info:lc/xmlns/premis-v2", "representation")'>
			<!-- INDEX REPRESENTATION PROPERTIES -->
		</xsl:if>
		<xsl:if
			test='resolve-QName(@xsi:type, .) = QName("info:lc/xmlns/premis-v2", "file")'>
			<xsl:if test="prem:originalName">
				<field name="file_originalName_s">
					<xsl:value-of select="normalize-space(prem:originalName/text())" />
				</field>
			</xsl:if>
			<xsl:if test="prem:objectCharacteristics/prem:size">
				<field name="file_size_s">
					<xsl:value-of select="prem:objectCharacteristics/prem:size/text()" />
				</field>
			</xsl:if>
			<xsl:if test="prem:objectCharacteristics/prem:format">
				<xsl:choose>
					<xsl:when
						test="normalize-space(prem:objectCharacteristics/prem:format/prem:formatRegistry/prem:formatRegistryName/text())='pronom'">
						<field name="file_pronom_s">
							<xsl:value-of
								select="prem:objectCharacteristics/prem:format/prem:formatDesignation/prem:formatName/text()" />
						</field>
					</xsl:when>
					<xsl:otherwise>

					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
		</xsl:if>
	</xsl:template>
	<xsl:template match="prem:event">
		<!-- INDEX EVENT PROPERTIES -->
	</xsl:template>
</xsl:stylesheet>
