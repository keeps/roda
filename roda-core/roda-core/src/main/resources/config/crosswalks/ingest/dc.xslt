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
	 	<xsl:if test="count(title)  &gt; 0">
			<xsl:if test="title[1]/text()">
				<field name="title">
					<xsl:value-of select="title[1]/text()" />
				</field>
			</xsl:if>
			<xsl:for-each select="title">
				<xsl:if test="normalize-space(text())!=''">
					<field name="title_txt">
						<xsl:value-of select="text()" />
					</field>
				</xsl:if>
			</xsl:for-each>
		</xsl:if>
		<xsl:if test="count(description)  &gt; 0">
			<xsl:if test="description[1]/text()">
				<field name="description">
					<xsl:value-of select="description[1]/text()" />
				</field>
			</xsl:if>
			<xsl:for-each select="description">
				<xsl:if test="normalize-space(text())!=''">
					<field name="description_txt">
						<xsl:value-of select="text()" />
					</field>
				</xsl:if>
			</xsl:for-each>
		</xsl:if>
		<xsl:for-each select="contributor">
			<xsl:if test="normalize-space(text())!=''">
				<field name="contributor_txt">
					<xsl:value-of select="text()" />
				</field>
			</xsl:if>
		</xsl:for-each>
		<xsl:for-each select="coverage">
			<xsl:if test="normalize-space(text())!=''">
				<field name="coverage_txt">
					<xsl:value-of select="text()" />
				</field>
			</xsl:if>
		</xsl:for-each>
		<xsl:for-each select="creator">
			<xsl:if test="normalize-space(text())!=''">
				<field name="creator_txt">
					<xsl:value-of select="text()" />
				</field>
			</xsl:if>
		</xsl:for-each>
		<xsl:if test="count(date)  &gt; 0">
			<xsl:if test="count(date)  &lt; 2">
				<xsl:analyze-string regex="^\d{{4}}-\d{{2}}-\d{{2}}$"
				select="date[1]/text()">
				<xsl:matching-substring>
					<xsl:variable name="date">
						<xsl:value-of select="regex-group(0)" />
					</xsl:variable>
					<xsl:if test="not(normalize-space($date)='')">
						<field name="dateInitial">
							<xsl:value-of select="$date" />
							<xsl:text>T00:00:00Z</xsl:text>
						</field>
						<field name="dateFinal">
							<xsl:value-of select="$date" />
							<xsl:text>T00:00:00Z</xsl:text>
						</field>
					</xsl:if>
				</xsl:matching-substring>
			</xsl:analyze-string>
			</xsl:if>
			<xsl:if test="count(date)  &gt; 1">
				<xsl:analyze-string regex="^\d{{4}}-\d{{2}}-\d{{2}}$" select="date[1]/text()">
					<xsl:matching-substring>
						<xsl:variable name="date">
							<xsl:value-of select="regex-group(0)" />
						</xsl:variable>
						<xsl:if test="not(normalize-space($date)='')">
							<field name="dateInitial">
								<xsl:value-of select="$date" /><xsl:text>T00:00:00Z</xsl:text>
							</field>
						</xsl:if>
					</xsl:matching-substring>
				</xsl:analyze-string>
				<xsl:analyze-string regex="^\d{{4}}-\d{{2}}-\d{{2}}$" select="date[2]/text()">
					<xsl:matching-substring>
						<xsl:variable name="date">
							<xsl:value-of select="regex-group(0)" />
						</xsl:variable>
						<xsl:if test="not(normalize-space($date)='')">
							<field name="dateFinal">
								<xsl:value-of select="$date" />
								<xsl:text>T00:00:00Z</xsl:text>
							</field>
						</xsl:if>
					</xsl:matching-substring>
				</xsl:analyze-string>
			</xsl:if>
			<xsl:for-each select="date">
				<xsl:if test="normalize-space(text())!=''">
					<field name="date_txt">
						<xsl:value-of select="text()" />
					</field>
				</xsl:if>
			</xsl:for-each>
		</xsl:if>

		<xsl:for-each select="format">
			<xsl:if test="normalize-space(text())!=''">
				<field name="format_txt">
					<xsl:value-of select="text()" />
				</field>
			</xsl:if>
		</xsl:for-each>
		<xsl:for-each select="identifier">
			<xsl:if test="normalize-space(text())!=''">
				<field name="identifier_txt">
					<xsl:value-of select="text()" />
				</field>
			</xsl:if>
		</xsl:for-each>
		<xsl:for-each select="language">
			<xsl:if test="normalize-space(text())!=''">
				<field name="language_txt">
					<xsl:value-of select="text()" />
				</field>
			</xsl:if>
		</xsl:for-each>
		<xsl:for-each select="publisher">
			<xsl:if test="normalize-space(text())!=''">
				<field name="publisher_txt">
					<xsl:value-of select="text()" />
				</field>
			</xsl:if>
		</xsl:for-each>
		<xsl:for-each select="relation">
			<xsl:if test="normalize-space(text())!=''">
				<field name="relation_txt">
					<xsl:value-of select="text()" />
				</field>
			</xsl:if>
		</xsl:for-each>
		<xsl:for-each select="rights">
			<xsl:if test="normalize-space(text())!=''">
				<field name="rights_txt">
					<xsl:value-of select="text()" />
				</field>
			</xsl:if>
		</xsl:for-each>
		<xsl:for-each select="source">
			<xsl:if test="normalize-space(text())!=''">
				<field name="source_txt">
					<xsl:value-of select="text()" />
				</field>
			</xsl:if>
		</xsl:for-each>
		<xsl:for-each select="subject">
			<xsl:if test="normalize-space(text())!=''">
				<field name="subject_txt">
					<xsl:value-of select="text()" />
				</field>
			</xsl:if>
		</xsl:for-each>
		<xsl:for-each select="type">
			<xsl:if test="normalize-space(text())!=''">
				<field name="type_txt">
					<xsl:value-of select="text()" />
				</field>
				<field name="level"><xsl:value-of select="translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')" /></field>
			</xsl:if>
		</xsl:for-each>
		<xsl:if test="not(type)">
			<field name="level">item</field>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>