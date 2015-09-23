<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dc="http://purl.org/dc/elements/1.1/"
	exclude-result-prefixes="dc">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />
	<xsl:param name="dcxmldublincore" />
	<xsl:param name="dcxmltitle" />
	<xsl:param name="dcxmldescription" />
	<xsl:param name="dcxmlcontributor" />
	<xsl:param name="dcxmlcoverage" />
	<xsl:param name="dcxmlcreator" />
	<xsl:param name="dcxmldate" />
	<xsl:param name="dcxmlformat" />
	<xsl:param name="dcxmlidentifier" />
	<xsl:param name="dcxmllanguage" />
	<xsl:param name="dcxmlpublisher" />
	<xsl:param name="dcxmlrelation" />
	<xsl:param name="dcxmlrights" />
	<xsl:param name="dcxmlsource" />
	<xsl:param name="dcxmlrights" />
	<xsl:param name="dcxmlsource" />



	<xsl:template match="/">
		<div class="descriptiveMetadata">
			<div class='title'>
				<xsl:value-of select="$dcxmldublincore" />
			</div>
			<xsl:apply-templates />
		</div>
	</xsl:template>
	<xsl:template match="metadata">
		<xsl:if test="dc:title/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$dcxmltitle" />
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
					<xsl:value-of select="$dcxmldescription" />
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
					<xsl:value-of select="$dcxmlcontributor" />
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
					<xsl:value-of select="$dcxmlcoverage" />
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
					<xsl:value-of select="$dcxmlcreator" />
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
					<xsl:value-of select="$dcxmldate" />
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
					<xsl:value-of select="$dcxmlformat" />
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
					<xsl:value-of select="$dcxmlidentifier" />
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
					<xsl:value-of select="$dcxmllanguage" />
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
					<xsl:value-of select="$dcxmlpublisher" />
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
					<xsl:value-of select="$dcxmlrelation" />
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
					<xsl:value-of select="$dcxmlrights" />
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
					<xsl:value-of select="$dcxmlsource" />
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
					<xsl:value-of select="$dcxmlsubject" />
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
					<xsl:value-of select="$dcxmltype" />
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