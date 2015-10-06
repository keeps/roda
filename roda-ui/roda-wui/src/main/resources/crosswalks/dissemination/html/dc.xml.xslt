<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dc="http://purl.org/dc/elements/1.1/"
	exclude-result-prefixes="dc">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />
	<xsl:param name="binaryToHtml.dc.xml.title" />
	<xsl:param name="binaryToHtml.dc.xml.description" />
	<xsl:param name="binaryToHtml.dc.xml.contributor" />
	<xsl:param name="binaryToHtml.dc.xml.coverage" />
	<xsl:param name="binaryToHtml.dc.xml.creator" />
	<xsl:param name="binaryToHtml.dc.xml.date" />
	<xsl:param name="binaryToHtml.dc.xml.format" />
	<xsl:param name="binaryToHtml.dc.xml.identifier" />
	<xsl:param name="binaryToHtml.dc.xml.language" />
	<xsl:param name="binaryToHtml.dc.xml.publisher" />
	<xsl:param name="binaryToHtml.dc.xml.relation" />
	<xsl:param name="binaryToHtml.dc.xml.rights" />
	<xsl:param name="binaryToHtml.dc.xml.source" />
	<xsl:param name="binaryToHtml.dc.xml.rights" />
	<xsl:param name="binaryToHtml.dc.xml.source" />



	<xsl:template match="/">
		<div class="descriptiveMetadata">
			<xsl:apply-templates />
		</div>
	</xsl:template>
	<xsl:template match="metadata">
		<xsl:if test="dc:title/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.dc.xml.title" />
				</div>
				<xsl:for-each select="dc:title">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:description/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.dc.xml.description" />
				</div>
				<xsl:for-each select="dc:description">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:contributor/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.dc.xml.contributor" />
				</div>
				<xsl:for-each select="dc:contributor">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:coverage/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.dc.xml.coverage" />
				</div>
				<xsl:for-each select="dc:coverage">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:creator/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.dc.xml.creator" />
				</div>
				<xsl:for-each select="dc:creator">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:date/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.dc.xml.date" />
				</div>
				<xsl:for-each select="dc:date">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:format/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.dc.xml.format" />
				</div>
				<xsl:for-each select="dc:format">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:identifier/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.dc.xml.identifier" />
				</div>
				<xsl:for-each select="dc:identifier">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:language/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.dc.xml.language" />
				</div>
				<xsl:for-each select="dc:language">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:publisher/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.dc.xml.publisher" />
				</div>
				<xsl:for-each select="dc:publisher">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:relation/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.dc.xml.relation" />
				</div>
				<xsl:for-each select="dc:relation">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:rights/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.dc.xml.rights" />
				</div>
				<xsl:for-each select="dc:rights">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:source/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.dc.xml.source" />
				</div>
				<xsl:for-each select="dc:source">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:subject/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.dc.xml.subject" />
				</div>
				<xsl:for-each select="dc:subject">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:type/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.dc.xml.type" />
				</div>
				<xsl:for-each select="dc:type">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>