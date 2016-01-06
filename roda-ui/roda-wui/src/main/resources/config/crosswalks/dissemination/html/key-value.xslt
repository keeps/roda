<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />

	<xsl:template match="/">
		<div class="descriptiveMetadata">
			<xsl:apply-templates />
		</div>
	</xsl:template>
	<xsl:template match="field">
		<xsl:if test="text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="@name" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="text()" />
				</div>
			</div>	
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
