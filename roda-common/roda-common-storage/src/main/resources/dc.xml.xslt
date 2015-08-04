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
		<xsl:if test="dc:contributor/text()">
			<field name="dc.contributor_txt">
				<xsl:value-of select="dc:contributor/text()" />
			</field>
		</xsl:if>
		<xsl:if test="dc:coverage/text()">
			<field name="dc.coverage_txt">
				<xsl:value-of select="dc:coverage/text()" />
			</field>
		</xsl:if>
		<xsl:if test="dc:creator/text()">
			<field name="dc.creator_txt">
				<xsl:value-of select="dc:creator/text()" />
			</field>
		</xsl:if>
		<xsl:if test="dc:date/text()">
			<xsl:analyze-string regex="^\d{{4}}-\d{{2}}-\d{{2}}$"
				select="dc:date/text()">
				<xsl:matching-substring>
					<xsl:variable name="date">
						<xsl:value-of select="regex-group(0)" />
					</xsl:variable>
					<xsl:if test="not(normalize-space($date)='')">
						<field name="dateInitial">
							<xsl:value-of select="$date" />T00:00:00Z</field>
						<field name="dateFinal">
							<xsl:value-of select="$date" />T00:00:00Z</field>
					</xsl:if>
				</xsl:matching-substring>
			</xsl:analyze-string>
		</xsl:if>
		<xsl:if test="dc:format/text()">
			<field name="dc.format_txt">
				<xsl:value-of select="dc:format/text()" />
			</field>
		</xsl:if>
		<xsl:if test="dc:identifier/text()">
			<field name="dc.identifier_txt">
				<xsl:value-of select="dc:identifier/text()" />
			</field>
		</xsl:if>
		<xsl:if test="dc:language/text()">
			<field name="dc.language_txt">
				<xsl:value-of select="dc:language/text()" />
			</field>
		</xsl:if>
		<xsl:if test="dc:publisher/text()">
			<field name="dc.publisher_txt">
				<xsl:value-of select="dc:publisher/text()" />
			</field>
		</xsl:if>
		<xsl:if test="dc:relation/text()">
			<field name="dc.relation_txt">
				<xsl:value-of select="dc:relation/text()" />
			</field>
		</xsl:if>
		<xsl:if test="dc:rights/text()">
			<field name="dc.rights_txt">
				<xsl:value-of select="dc:rights/text()" />
			</field>
		</xsl:if>
		<xsl:if test="dc:source/text()">
			<field name="dc.source_txt">
				<xsl:value-of select="dc:source/text()" />
			</field>
		</xsl:if>
		<xsl:if test="dc:subject/text()">
			<field name="dc.subject_txt">
				<xsl:value-of select="dc:subject/text()" />
			</field>
		</xsl:if>
		<xsl:if test="dc:type/text()">
			<field name="dc.type_txt">
				<xsl:value-of select="dc:type/text()" />
			</field>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>