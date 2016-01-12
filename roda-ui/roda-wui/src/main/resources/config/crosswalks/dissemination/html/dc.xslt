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
        <xsl:if test="title/text()">
            <div class="descriptiveMetadata-field">
                <div class="descriptiveMetadata-field-key">
                    <xsl:value-of select="$i18n.title" />
                </div>
                <xsl:for-each select="title">
                    <div class="descriptiveMetadata-field-value">
                        <xsl:value-of select="text()" />
                    </div>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="description/text()">
            <div class="descriptiveMetadata-field">
                <div class="descriptiveMetadata-field-key">
                    <xsl:value-of select="$i18n.description" />
                </div>
                <xsl:for-each select="description">
                    <div class="descriptiveMetadata-field-value">
                        <xsl:value-of select="text()" />
                    </div>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="contributor/text()">
            <div class="descriptiveMetadata-field">
                <div class="descriptiveMetadata-field-key">
                    <xsl:value-of select="$i18n.contributor" />
                </div>
                <xsl:for-each select="contributor">
                    <div class="descriptiveMetadata-field-value">
                        <xsl:value-of select="text()" />
                    </div>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="coverage/text()">
            <div class="descriptiveMetadata-field">
                <div class="descriptiveMetadata-field-key">
                    <xsl:value-of select="$i18n.coverage" />
                </div>
                <xsl:for-each select="coverage">
                    <div class="descriptiveMetadata-field-value">
                        <xsl:value-of select="text()" />
                    </div>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="creator/text()">
            <div class="descriptiveMetadata-field">
                <div class="descriptiveMetadata-field-key">
                    <xsl:value-of select="$i18n.creator" />
                </div>
                <xsl:for-each select="creator">
                    <div class="descriptiveMetadata-field-value">
                        <xsl:value-of select="text()" />
                    </div>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="date/text()">
            <div class="descriptiveMetadata-field">
                <div class="descriptiveMetadata-field-key">
                    <xsl:value-of select="$i18n.date" />
                </div>
                <xsl:for-each select="date">
                    <div class="descriptiveMetadata-field-value">
                        <xsl:value-of select="text()" />
                    </div>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="format/text()">
            <div class="descriptiveMetadata-field">
                <div class="descriptiveMetadata-field-key">
                    <xsl:value-of select="$i18n.format" />
                </div>
                <xsl:for-each select="format">
                    <div class="descriptiveMetadata-field-value">
                        <xsl:value-of select="text()" />
                    </div>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="identifier/text()">
            <div class="descriptiveMetadata-field">
                <div class="descriptiveMetadata-field-key">
                    <xsl:value-of select="$i18n.identifier" />
                </div>
                <xsl:for-each select="identifier">
                    <div class="descriptiveMetadata-field-value">
                        <xsl:value-of select="text()" />
                    </div>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="language/text()">
            <div class="descriptiveMetadata-field">
                <div class="descriptiveMetadata-field-key">
                    <xsl:value-of select="$i18n.language" />
                </div>
                <xsl:for-each select="language">
                    <div class="descriptiveMetadata-field-value">
                        <xsl:value-of select="text()" />
                    </div>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="publisher/text()">
            <div class="descriptiveMetadata-field">
                <div class="descriptiveMetadata-field-key">
                    <xsl:value-of select="$i18n.publisher" />
                </div>
                <xsl:for-each select="publisher">
                    <div class="descriptiveMetadata-field-value">
                        <xsl:value-of select="text()" />
                    </div>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="relation/text()">
            <div class="descriptiveMetadata-field">
                <div class="descriptiveMetadata-field-key">
                    <xsl:value-of select="$i18n.relation" />
                </div>
                <xsl:for-each select="relation">
                    <div class="descriptiveMetadata-field-value">
                        <xsl:value-of select="text()" />
                    </div>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="rights/text()">
            <div class="descriptiveMetadata-field">
                <div class="descriptiveMetadata-field-key">
                    <xsl:value-of select="$i18n.rights" />
                </div>
                <xsl:for-each select="rights">
                    <div class="descriptiveMetadata-field-value">
                        <xsl:value-of select="text()" />
                    </div>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="source/text()">
            <div class="descriptiveMetadata-field">
                <div class="descriptiveMetadata-field-key">
                    <xsl:value-of select="$i18n.source" />
                </div>
                <xsl:for-each select="source">
                    <div class="descriptiveMetadata-field-value">
                        <xsl:value-of select="text()" />
                    </div>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="subject/text()">
            <div class="descriptiveMetadata-field">
                <div class="descriptiveMetadata-field-key">
                    <xsl:value-of select="$i18n.subject" />
                </div>
                <xsl:for-each select="subject">
                    <div class="descriptiveMetadata-field-value">
                        <xsl:value-of select="text()" />
                    </div>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="type/text()">
            <div class="descriptiveMetadata-field">
                <div class="descriptiveMetadata-field-key">
                    <xsl:value-of select="$i18n.type" />
                </div>
                <xsl:for-each select="type">
                    <div class="descriptiveMetadata-field-value">
                        <xsl:value-of select="text()" />
                    </div>
                </xsl:for-each>
            </div>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
