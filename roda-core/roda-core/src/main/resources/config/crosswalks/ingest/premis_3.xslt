<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" indent="yes" omit-xml-declaration="yes" />
	<xsl:param name="prefix" />
	<xsl:strip-space elements="*" />

	<xsl:template match="text()" />

	<xsl:template match="*:objectCharacteristics/*:objectCharacteristicsExtension/*:featureExtractor/*:metadata">
		<doc>
			<xsl:variable name="featureType" select="../@featureExtractorType"/>
			<xsl:for-each select="//field">
				<field>
					<xsl:attribute name="name">
						<xsl:value-of select="concat('feature.extractor.', $featureType, '.', replace(lower-case(@name), '[- :]', '_'))" />
						<xsl:choose>
							<xsl:when test="lower-case(@name) = 'tiff:imagewidth' or lower-case(@name) = 'tiff:imagelength'">
								<xsl:text>_d</xsl:text>
							</xsl:when>
							<xsl:when test="lower-case(@name) = 'compression lossless'">
								<xsl:text>_b</xsl:text>
							</xsl:when>
							<xsl:otherwise>
								<xsl:text>_txt</xsl:text>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:attribute>
					<xsl:value-of select="." />
				</field>
				<xsl:text>&#xA;</xsl:text>
			</xsl:for-each>
		</doc>
	</xsl:template>

</xsl:stylesheet>