<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />
	<xsl:param name="i18n.expirationDate_dt" />

	<xsl:template match="/">
		<div class="descriptiveMetadata">
			<xsl:apply-templates />
		</div>
	</xsl:template>
	<xsl:template match="field">
		<xsl:if test="normalize-space(text())!=''">
			<div class="field">
				<div class="label">
					<xsl:choose>
						<xsl:when test="@name='expirationDate_dt' and string($i18n.expirationDate_dt)!=''">
							<xsl:value-of select="$i18n.expirationDate_dt"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="@name"/>
						</xsl:otherwise>
					</xsl:choose>
				</div>
				<div class="value">
					<xsl:value-of select="text()" />
				</div>
			</div>	
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
