<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dc="http://purl.org/dc/elements/1.1/"
    exclude-result-prefixes="dc">
    <xsl:output method="xml" indent="yes" encoding="UTF-8"
        omit-xml-declaration="yes" />
    <xsl:param name="i18n.title" />
    <xsl:param name="i18n.description" />
    <xsl:param name="i18n.contributor" />
    <xsl:param name="i18n.coverage" />
    <xsl:param name="i18n.creator" />
    <xsl:param name="i18n.date" />
    <xsl:param name="i18n.format" />
    <xsl:param name="i18n.identifier" />
    <xsl:param name="i18n.language" />
    <xsl:param name="i18n.publisher" />
    <xsl:param name="i18n.relation" />
    <xsl:param name="i18n.rights" />
    <xsl:param name="i18n.source" />
    <xsl:param name="i18n.subject" />
    <xsl:param name="i18n.type" />
    
    
    
    <xsl:template match="/">
        <div class="descriptiveMetadata">
            <xsl:apply-templates />
        </div>
    </xsl:template>
    <xsl:template match="simpledc">
        <xsl:if test="normalize-space(string-join(title/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.title" />
                </div>
                <xsl:for-each select="title">
                	<xsl:if test="normalize-space(text())!=''">
	                    <div class="value">
	                        <xsl:value-of select="text()" />
	                    </div>
	                </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(description/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.description" />
                </div>
                <xsl:for-each select="description">
					<xsl:if test="normalize-space(text())!=''">
						<div class="value prewrap">
							<xsl:value-of select="text()" />
						</div>
					</xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(contributor/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.contributor" />
                </div>
                <xsl:for-each select="contributor">
                	<xsl:if test="normalize-space(text())!=''">
	                    <div class="value">
	                        <xsl:value-of select="text()" />
	                    </div>
	                </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(coverage/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.coverage" />
                </div>
                <xsl:for-each select="coverage">
                	<xsl:if test="normalize-space(text())!=''">
	                    <div class="value">
	                        <xsl:value-of select="text()" />
	                    </div>
	                </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(creator/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.creator" />
                </div>
                <xsl:for-each select="creator">
                	<xsl:if test="normalize-space(text())!=''">
	                    <div class="value">
	                        <xsl:value-of select="text()" />
	                    </div>
	                </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(date/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.date" />
                </div>
                <xsl:for-each select="date">
                	<xsl:if test="normalize-space(text())!=''">
	                    <div class="value">
	                        <xsl:value-of select="text()" />
	                    </div>
	                </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(format/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.format" />
                </div>
                <xsl:for-each select="format">
                	<xsl:if test="normalize-space(text())!=''">
	                    <div class="value">
	                        <xsl:value-of select="text()" />
	                    </div>
	                </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(identifier/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.identifier" />
                </div>
                <xsl:for-each select="identifier">
                	<xsl:if test="normalize-space(text())!=''">
	                    <div class="value">
	                        <xsl:value-of select="text()" />
	                    </div>
	                </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(language/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.language" />
                </div>
                <xsl:for-each select="language">
                	<xsl:if test="normalize-space(text())!=''">
	                    <div class="value">
	                        <xsl:value-of select="text()" />
	                    </div>
	                </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(publisher/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.publisher" />
                </div>
                <xsl:for-each select="publisher">
                	<xsl:if test="normalize-space(text())!=''">
	                    <div class="value">
	                        <xsl:value-of select="text()" />
	                    </div>
                    </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(relation/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.relation" />
                </div>
                <xsl:for-each select="relation">
                	<xsl:if test="normalize-space(text())!=''">
	                    <div class="value">
	                        <xsl:value-of select="text()" />
	                    </div>
                    </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(rights/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.rights" />
                </div>
                <xsl:for-each select="rights">
                	<xsl:if test="normalize-space(text())!=''">
	                    <div class="value">
	                        <xsl:value-of select="text()" />
	                    </div>
                    </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(source/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.source" />
                </div>
                <xsl:for-each select="source">
                	<xsl:if test="normalize-space(text())!=''">
	                    <div class="value">
	                        <xsl:value-of select="text()" />
	                    </div>
                    </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(subject/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.subject" />
                </div>
                <xsl:for-each select="subject">
                	<xsl:if test="normalize-space(text())!=''">
	                    <div class="value">
	                        <xsl:value-of select="text()" />
	                    </div>
	                </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(type/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.type" />
                </div>
                <xsl:for-each select="type">
                	<xsl:if test="normalize-space(text())!=''">
	                    <div class="value">
	                        <xsl:value-of select="text()" />
	                    </div>
                    </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
