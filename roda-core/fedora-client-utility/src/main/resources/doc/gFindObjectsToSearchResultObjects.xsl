<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:zs="http://www.loc.gov/zing/srw/" xmlns:foxml="info:fedora/fedora-system:def/foxml#"
    xmlns:dc="http://purl.org/dc/elements/1.1/" version="2.0">

    <!-- This xslt stylesheet generates the resultPage with eadpart documents from a Lucene search. -->

    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

    <xsl:param name="QUERY" select="query"/>
    <xsl:param name="HITPAGESTART" select="1"/>
    <xsl:param name="HITPAGESIZE" select="10"/>
    <xsl:param name="RESULTPAGEXSLT" select="resultPageXslt"/>
    <xsl:param name="DATETIME" select="none"/>

    <xsl:template match="lucenesearch">

        <xsl:variable name="INDEXNAME" select="@indexName"/>
        <xsl:variable name="HITTOTAL" select="@hitTotal"/>

        <resultPage dateTime="{$DATETIME}" indexName="{$INDEXNAME}">
            <gfindObjects query="{$QUERY}" hitPageStart="{$HITPAGESTART}"
                hitPageSize="{$HITPAGESIZE}" resultPageXslt="{$RESULTPAGEXSLT}"
                hitTotal="{$HITTOTAL}">
                <objects>
                    <xsl:for-each select="hit">
                        <object>
                            <xsl:attribute name="no">
                                <xsl:value-of select="@no"/>
                            </xsl:attribute>
                            <xsl:attribute name="score">
                                <xsl:value-of select="@score"/>
                            </xsl:attribute>
                            <xsl:copy-of select="node()"/>

                            <!--
                                <xsl:choose>
                                <xsl:when test="dc.title">
                                <field name="title"><xsl:value-of select="dc.title/node()"/>
                                </field>
                                </xsl:otherwise>
                                </xsl:choose>
                                <xsl:choose>
                                <xsl:when test="dc.description">
                                <xsl:variable name="SNIPPET" select="dc.description"/>
                                <xsl:choose>
                                <xsl:when test="string-length($SNIPPET)>50">
                                <field name="snippet"><xsl:value-of select="concat(substring($SNIPPET, 1, 50), '...')"/></field>
                                </xsl:when>
                                <xsl:otherwise>
                                <field name="snippet"><xsl:value-of select="$SNIPPET"/></field>
                                </xsl:otherwise>
                                </xsl:choose>
                                </xsl:when>
                                <xsl:otherwise>
                                <xsl:variable name="SNIPPET" select="description"/>
                                <xsl:choose>
                                <xsl:when test="string-length($SNIPPET)>500">
                                <field name="snippet"><xsl:value-of select="concat(substring($SNIPPET, 1, 50), '...')"/></field>
                                </xsl:when>
                                <xsl:otherwise>
                                <field name="snippet"><xsl:copy-of select="$SNIPPET"/></field>
                                </xsl:otherwise>
                                </xsl:choose>
                                </xsl:otherwise>
                                </xsl:choose>
                            -->

                        </object>
                    </xsl:for-each>
                </objects>
            </gfindObjects>
        </resultPage>
    </xsl:template>

</xsl:stylesheet>
