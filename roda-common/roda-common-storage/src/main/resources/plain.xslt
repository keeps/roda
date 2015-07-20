<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" omit-xml-declaration="yes" />
	<xsl:param name="prefix" />
	<xsl:strip-space elements="*" />

	<xsl:template match="text()" />

	<xsl:template match="/">
		<doc>
			<xsl:apply-templates />
		</doc>
	</xsl:template>


	<xsl:template match="*">

		<xsl:for-each select="@*">

			<xsl:variable name="attributeValue">
				<xsl:value-of select="." />
			</xsl:variable>
			<xsl:variable name="attributeName">
				<xsl:for-each select="ancestor-or-self::*">
					<xsl:value-of select="concat(local-name(),'.')" />
				</xsl:for-each>
				<xsl:value-of select="local-name()" />
			</xsl:variable>
			<xsl:if
				test="not(normalize-space($attributeValue)='') and not(normalize-space($attributeName)='')">
				<field>
					<xsl:attribute name="name">
                        <xsl:value-of select="$prefix" />.<xsl:value-of
						select="$attributeName" />_txt</xsl:attribute>
					<xsl:value-of select="$attributeValue" />
				</field>
				<xsl:text>&#xA;</xsl:text>
			</xsl:if>

		</xsl:for-each>

		<xsl:variable name="value">
			<xsl:if test="not(*)">
				<xsl:for-each select="ancestor-or-self::*">
					<xsl:if test="not(*)">
						<xsl:value-of select="text()" />
					</xsl:if>
				</xsl:for-each>
			</xsl:if>
		</xsl:variable>

		<xsl:variable name="path">
			<xsl:if test="not(*)">
				<xsl:for-each select="ancestor-or-self::*">
					<xsl:if test="not(*)">
						<xsl:value-of select="local-name()" />
					</xsl:if>
					<xsl:if test="*">
						<xsl:value-of select="concat(local-name(),'.')" />
					</xsl:if>
				</xsl:for-each>
			</xsl:if>
		</xsl:variable>
		<xsl:if
			test="not(normalize-space($path)='') and not(normalize-space($value)='')">
			<field>
				<xsl:attribute name="name">
                    <xsl:value-of select="$prefix" />.<xsl:value-of
					select="$path" />_txt</xsl:attribute>
				<xsl:value-of select="$value" />
			</field>
			<xsl:text>&#xA;</xsl:text>
		</xsl:if>
		<xsl:apply-templates select="node()" />
	</xsl:template>

</xsl:stylesheet>