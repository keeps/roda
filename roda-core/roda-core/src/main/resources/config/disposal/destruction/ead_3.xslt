<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ead="http://ead3.archivists.org/schema/"
                exclude-result-prefixes="ead">
    <xsl:output method="xml" indent="yes" encoding="UTF-8"
                omit-xml-declaration="yes" />

    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>