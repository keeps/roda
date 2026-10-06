<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:ea="https://roda-community.org/schemas/emailarchive/v1"
    exclude-result-prefixes="ea">
  <xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="yes"/>

  <xsl:param name="i18n.custodian"/>
  <xsl:param name="i18n.emailAddress"/>
  <xsl:param name="i18n.dateStart"/>
  <xsl:param name="i18n.dateEnd"/>
  <xsl:param name="i18n.totalMessages"/>
  <xsl:param name="i18n.originalFormat"/>
  <xsl:param name="i18n.archivingMotive"/>
  <xsl:param name="i18n.emails"/>
  <xsl:param name="i18n.emails.messageId"/>
  <xsl:param name="i18n.emails.subject"/>
  <xsl:param name="i18n.emails.sender"/>
  <xsl:param name="i18n.emails.recipients"/>
  <xsl:param name="i18n.emails.sentDate"/>
  <xsl:param name="i18n.emails.folderPath"/>
  <xsl:param name="i18n.emails.hasAttachments"/>

  <xsl:template match="/">
    <div class="descriptiveMetadata">
      <xsl:apply-templates/>
    </div>
  </xsl:template>

  <xsl:template match="*:emailArchive">
    <xsl:if test="normalize-space(*:custodian/text()) != ''">
      <div class="field">
        <div class="label"><xsl:value-of select="$i18n.custodian"/></div>
        <div class="value"><xsl:value-of select="normalize-space(*:custodian/text())"/></div>
      </div>
    </xsl:if>
    <xsl:if test="normalize-space(*:emailAddress/text()) != ''">
      <div class="field">
        <div class="label"><xsl:value-of select="$i18n.emailAddress"/></div>
        <div class="value"><xsl:value-of select="normalize-space(*:emailAddress/text())"/></div>
      </div>
    </xsl:if>
    <xsl:if test="normalize-space(*:dateStart/text()) != ''">
      <div class="field">
        <div class="label"><xsl:value-of select="$i18n.dateStart"/></div>
        <div class="value"><xsl:value-of select="normalize-space(*:dateStart/text())"/></div>
      </div>
    </xsl:if>
    <xsl:if test="normalize-space(*:dateEnd/text()) != ''">
      <div class="field">
        <div class="label"><xsl:value-of select="$i18n.dateEnd"/></div>
        <div class="value"><xsl:value-of select="normalize-space(*:dateEnd/text())"/></div>
      </div>
    </xsl:if>
    <xsl:if test="normalize-space(*:totalMessages/text()) != ''">
      <div class="field">
        <div class="label"><xsl:value-of select="$i18n.totalMessages"/></div>
        <div class="value"><xsl:value-of select="normalize-space(*:totalMessages/text())"/></div>
      </div>
    </xsl:if>
    <xsl:if test="normalize-space(*:originalFormat/text()) != ''">
      <div class="field">
        <div class="label"><xsl:value-of select="$i18n.originalFormat"/></div>
        <div class="value"><xsl:value-of select="normalize-space(*:originalFormat/text())"/></div>
      </div>
    </xsl:if>
    <xsl:if test="normalize-space(*:archivingMotive/text()) != ''">
      <div class="field">
        <div class="label"><xsl:value-of select="$i18n.archivingMotive"/></div>
        <div class="value prewrap"><xsl:value-of select="normalize-space(*:archivingMotive/text())"/></div>
      </div>
    </xsl:if>

    <xsl:if test="count(*:email) &gt; 0">
      <div class="field">
        <div class="label"><xsl:value-of select="$i18n.emails"/></div>
        <div class="value">
          <table class="table-condensed emailarchive-messages">
            <thead>
              <tr>
                <th><xsl:value-of select="$i18n.emails.subject"/></th>
                <th><xsl:value-of select="$i18n.emails.sender"/></th>
                <th><xsl:value-of select="$i18n.emails.sentDate"/></th>
                <th><xsl:value-of select="$i18n.emails.folderPath"/></th>
              </tr>
            </thead>
            <tbody>
              <xsl:for-each select="*:email">
                <tr>
                  <td>
                    <xsl:choose>
                      <xsl:when test="normalize-space(*:subject/text()) != ''">
                        <xsl:value-of select="normalize-space(*:subject/text())"/>
                      </xsl:when>
                      <xsl:otherwise>—</xsl:otherwise>
                    </xsl:choose>
                  </td>
                  <td><xsl:value-of select="normalize-space(*:sender/text())"/></td>
                  <td><xsl:value-of select="normalize-space(*:sentDate/text())"/></td>
                  <td><xsl:value-of select="normalize-space(*:folderPath/text())"/></td>
                </tr>
              </xsl:for-each>
            </tbody>
          </table>
        </div>
      </div>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
