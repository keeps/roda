<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dc="http://purl.org/dc/elements/1.1/"
                exclude-result-prefixes="dc">
    <xsl:output method="xml" indent="yes" encoding="UTF-8"
                omit-xml-declaration="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="language"/>
    <xsl:template match="publisher"/>
    <xsl:template match="creator"/>

</xsl:stylesheet>