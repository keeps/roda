<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />

	<xsl:template match="/">
		<doc>
			<xsl:apply-templates />
		</doc>
	</xsl:template>
	<xsl:template match="field">
		<xsl:choose>
			<xsl:when
				test="@name='title' or @name='level' or @name='description' or @name='dateInitial' or @name='dateFinal'">
				<field>
					<xsl:attribute name="name"><xsl:value-of
						select="@name" /></xsl:attribute>
					<xsl:value-of select="." />
				</field>
			</xsl:when>
			<xsl:when test="@name='UnitTitle'">
				<field>
					<xsl:attribute name="name">title</xsl:attribute>
					<xsl:value-of select="." />
				</field>
			</xsl:when>
<!-- 			<xsl:when test="@name='UnitDateInitial'"> -->
<!-- 				<field> -->
<!-- 					<xsl:attribute name="name">dateInitial</xsl:attribute> -->
<!-- 					<xsl:value-of select="." /> -->
<!-- 				</field> -->
<!-- 			</xsl:when> -->
<!-- 			<xsl:when test="@name='UnitDateFinal'"> -->
<!-- 				<field> -->
<!-- 					<xsl:attribute name="name">dateFinal</xsl:attribute> -->
<!-- 					<xsl:value-of select="." /> -->
<!-- 				</field> -->
<!-- 			</xsl:when> -->
			<xsl:when test="@name='DescriptionLevel'">
				<field>
					<xsl:attribute name="name">level</xsl:attribute>
					<xsl:value-of select="." />
				</field>
			</xsl:when>
			<xsl:when test="@name='ScopeContent'">
				<field>
					<xsl:attribute name="name">description</xsl:attribute>
					<xsl:value-of select="." />
				</field>
			</xsl:when>
			<xsl:otherwise>
				<field>
					<xsl:attribute name="name"><xsl:value-of
						select="@name" /><xsl:text>_txt</xsl:text></xsl:attribute>
					<xsl:value-of select="." />
				</field>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>