<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dc="http://purl.org/dc/elements/1.1/"
	exclude-result-prefixes="dc">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />

	<xsl:template match="/">
		<doc>
			<xsl:apply-templates />
		</doc>
	</xsl:template>
	<xsl:template match="simpledc">
		<xsl:if test="title/text()">
			<field name="title">
				<xsl:value-of select="title/text()" />
			</field>
		</xsl:if>
		<xsl:if test="description/text()">
			<field name="description">
				<xsl:value-of select="description/text()" />
			</field>
		</xsl:if>
		<xsl:if test="contributor/text()">
			<field name="dc.contributor_txt">
				<xsl:value-of select="contributor/text()" />
			</field>
		</xsl:if>
		<xsl:if test="coverage/text()">
			<field name="dc.coverage_txt">
				<xsl:value-of select="coverage/text()" />
			</field>
		</xsl:if>
		<xsl:if test="creator/text()">
			<field name="dc.creator_txt">
				<xsl:value-of select="creator/text()" />
			</field>
		</xsl:if>
		<xsl:if test="date/text()">
			<xsl:analyze-string regex="^\d{{4}}-\d{{2}}-\d{{2}}$"
				select="date/text()">
				<xsl:matching-substring>
					<xsl:variable name="date">
						<xsl:value-of select="regex-group(0)" />
					</xsl:variable>
					<xsl:if test="not(normalize-space($date)='')">
						<field name="dateInitial">
							<xsl:value-of select="$date" />
							T00:00:00Z
						</field>
						<field name="dateFinal">
							<xsl:value-of select="$date" />
							T00:00:00Z
						</field>
					</xsl:if>
				</xsl:matching-substring>
			</xsl:analyze-string>
		</xsl:if>
		<xsl:if test="format/text()">
			<field name="dc.format_txt">
				<xsl:value-of select="format/text()" />
			</field>
		</xsl:if>
		<xsl:if test="identifier/text()">
			<field name="dc.identifier_txt">
				<xsl:value-of select="identifier/text()" />
			</field>
		</xsl:if>
		<xsl:if test="language/text()">
			<field name="dc.language_txt">
				<xsl:value-of select="language/text()" />
			</field>
		</xsl:if>
		<xsl:if test="publisher/text()">
			<field name="dc.publisher_txt">
				<xsl:value-of select="publisher/text()" />
			</field>
		</xsl:if>
		<xsl:if test="relation/text()">
			<field name="dc.relation_txt">
				<xsl:value-of select="relation/text()" />
			</field>
		</xsl:if>
		<xsl:if test="rights/text()">
			<field name="dc.rights_txt">
				<xsl:value-of select="rights/text()" />
			</field>
		</xsl:if>
		<xsl:if test="source/text()">
			<field name="dc.source_txt">
				<xsl:value-of select="source/text()" />
			</field>
		</xsl:if>
		<xsl:if test="subject/text()">
			<field name="dc.subject_txt">
				<xsl:value-of select="subject/text()" />
			</field>
		</xsl:if>
		<xsl:if test="type/text()">
			<field name="dc.type_txt">
				<xsl:value-of select="type/text()" />
			</field>
		</xsl:if>
		<field name="level">item</field>
	</xsl:template>
</xsl:stylesheet>