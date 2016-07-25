<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ead="urn:isbn:1-931666-22-9"
	exclude-result-prefixes="ead">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />

	<xsl:template match="/">
		<doc>
			<xsl:apply-templates />
		</doc>
	</xsl:template>
	<xsl:template match="ead:eadheader">
		<xsl:if test="ead:profiledesc/ead:descrules/text()">
			<field name="rules_ss">
				<xsl:value-of select="ead:profiledesc/ead:descrules/text()" />
			</field>
		</xsl:if>
	</xsl:template>
	<xsl:template match="ead:archdesc">
		<xsl:if test="ead:did/ead:unitid/text()">
			<field name="unitId_ss">
				<xsl:value-of select="ead:did/ead:unitid/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:did/ead:unittitle/text()">
			<field name="title">
				<xsl:value-of select="ead:did/ead:unittitle/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:did/ead:unitid/@repositorycode">
			<field name="repositoryCode_ss">
				<xsl:value-of select="ead:did/ead:unitid/@repositorycode" />
			</field>
		</xsl:if>
		<xsl:if test="ead:did/ead:unitdate/@normal">
			<xsl:choose>
				<xsl:when test="contains(ead:did/ead:unitdate/@normal, '/')">	<!-- initial/final -->
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$"
						select="normalize-space(substring-before(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateInitial">
								<xsl:value-of select="regex-group(1)" />
								<xsl:text>T00:00:00Z</xsl:text>
							</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$"
						select="normalize-space(substring-before(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateInitial">
								<xsl:value-of select="regex-group(1)" />
								<xsl:text>-01T00:00:00Z</xsl:text>
							</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})$"
						select="normalize-space(substring-before(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateInitial">
								<xsl:value-of select="regex-group(1)" />
								<xsl:text>-01-01T00:00:00Z</xsl:text>
							</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$"
						select="normalize-space(substring-before(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateInitial">
								<xsl:value-of select="regex-group(1)" />
								<xsl:text>-</xsl:text>
								<xsl:value-of select="regex-group(2)" />
								<xsl:text>-</xsl:text>
								<xsl:value-of select="regex-group(3)" />
								<xsl:text>T00:00:00Z</xsl:text>
							</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})$"
						select="normalize-space(substring-before(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateInitial">
								<xsl:value-of select="regex-group(1)" />
								<xsl:text>-</xsl:text>
								<xsl:value-of select="regex-group(2)" />
								<xsl:text>-01T00:00:00Z</xsl:text>
							</field>
						</xsl:matching-substring>
					</xsl:analyze-string>

					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$"
						select="normalize-space(substring-after(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateFinal">
								<xsl:value-of select="regex-group(1)" />
								<xsl:text>T00:00:00Z</xsl:text>
							</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$"
						select="normalize-space(substring-after(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateFinal">
								<xsl:value-of select="regex-group(1)" />
								<xsl:text>-01T00:00:00Z</xsl:text>
							</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})$"
						select="normalize-space(substring-after(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateFinal">
								<xsl:value-of select="regex-group(1)" />
								<xsl:text>-01-01T00:00:00Z</xsl:text>
							</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$"
						select="normalize-space(substring-after(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateFinal">
								<xsl:value-of select="regex-group(1)" />
								<xsl:text>-</xsl:text>
								<xsl:value-of select="regex-group(2)" />
								<xsl:text>-</xsl:text>
								<xsl:value-of select="regex-group(3)" />
								<xsl:text>T00:00:00Z</xsl:text>
							</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})$"
						select="normalize-space(substring-after(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateFinal">
								<xsl:value-of select="regex-group(1)" />
								<xsl:text>-</xsl:text>
								<xsl:value-of select="regex-group(2)" />
								<xsl:text>-01T00:00:00Z</xsl:text>
							</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
				</xsl:when>
				<xsl:otherwise>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$"
						select="normalize-space(ead:did/ead:unitdate/@normal)">
						<xsl:matching-substring>
							<field name="dateInitial">
								<xsl:value-of select="regex-group(1)" />
								<xsl:text>T00:00:00Z</xsl:text>
							</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$"
						select="normalize-space(ead:did/ead:unitdate/@normal)">
						<xsl:matching-substring>
							<field name="dateInitial">
								<xsl:value-of select="regex-group(1)" />
								<xsl:text>-01T00:00:00Z</xsl:text>
							</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})$"
						select="normalize-space(ead:did/ead:unitdate/@normal)">
						<xsl:matching-substring>
							<field name="dateInitial">
								<xsl:value-of select="regex-group(1)" />
								-01-01T00:00:00Z
							</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$"
						select="normalize-space(ead:did/ead:unitdate/@normal)">
						<xsl:matching-substring>
							<field name="dateInitial">
								<xsl:value-of select="regex-group(1)" />
								<xsl:text>-</xsl:text>
								<xsl:value-of select="regex-group(2)" />
								-
								<xsl:value-of select="regex-group(3)" />
								<xsl:text>T00:00:00Z</xsl:text>
							</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})$"
						select="normalize-space(ead:did/ead:unitdate/@normal)">
						<xsl:matching-substring>
							<field name="dateInitial">
								<xsl:value-of select="regex-group(1)" />
								<xsl:text>-</xsl:text>
								<xsl:value-of select="regex-group(2)" />
								<xsl:text>-01T00:00:00Z</xsl:text>
							</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		<xsl:if test="ead:did/ead:unitdate/text()">
			<field name="descriptiveDate_ss">
				<xsl:value-of select="ead:did/ead:unitdate/text()" />
			</field>
		</xsl:if>
		<xsl:if test="@level">
			<xsl:choose>
				<xsl:when test="@level = 'otherlevel'">
					<field name="level">
						<xsl:value-of select="@otherlevel" />
					</field>
				</xsl:when>
				<xsl:otherwise>
					<field name="level">
						<xsl:value-of select="@level" />
					</field>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		<xsl:for-each select="ead:did/ead:physdesc/ead:extent">
			<xsl:if test="./text()">
				<field name="extent_ss">
					<xsl:value-of select="./text()" /><xsl:if test="./@unit"><xsl:text> </xsl:text><xsl:value-of select="./@unit" /></xsl:if>
				</field>
			</xsl:if>
		</xsl:for-each>
		<xsl:if test="ead:did/ead:origination[@label='creator']/ead:name/text()">
			<field name="creator_ss">
				<xsl:value-of select="ead:did/ead:origination[@label='creator']/ead:name/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:did/ead:origination[@label='producer']/ead:name/text()">
			<field name="producer_ss">
				<xsl:value-of select="ead:did/ead:origination[@label='producer']/ead:name/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:did/ead:repository/ead:corpname/text()">
			<field name="repository_ss">
				<xsl:value-of select="ead:did/ead:repository/ead:corpname/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:did/ead:note[@type='sourcesDescription']/ead:p/text()">
			<field name="sources_ss">
				<xsl:value-of select="ead:did/ead:note[@type='sourcesDescription']/ead:p/text()" />
			</field>
		</xsl:if>	
		<xsl:if test="ead:did/ead:note[@type='generalNote']/ead:p/text()">
			<field name="notes_ss">
				<xsl:value-of select="ead:did/ead:note[@type='generalNote']/ead:p/text()" />
			</field>
		</xsl:if>		
		<xsl:if test="ead:custodhist/ead:p/text()">
			<field name="custodhist_ss">
				<xsl:value-of select="ead:custodhist/ead:p/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:acqinfo/ead:p/text()">
			<field name="acqinfo_ss">
				<xsl:value-of select="ead:acqinfo/ead:p/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:scopecontent/ead:p/text()">
			<field name="description">
				<xsl:value-of select="ead:scopecontent/ead:p/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:appraisal/ead:p/text()">
			<field name="appraisal_ss">
				<xsl:value-of select="ead:appraisal/ead:p/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:accruals/ead:p/text()">
			<field name="accruals_ss">
				<xsl:value-of select="ead:accruals/ead:p/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:arrangement/ead:p/text()">
			<field name="arrangement_ss">
				<xsl:value-of select="ead:arrangement/ead:p/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:accessrestrict/ead:p/text()">
			<field name="accessrestrict_ss">
					<xsl:value-of select="ead:accessrestrict/ead:p/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:userestrict/ead:p/text()">
			<field name="userestrict_ss">
				<xsl:value-of select="ead:userestrict/ead:p/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:userestrict/ead:p/text()">
			<field name="userestrict_ss">
				<xsl:value-of select="ead:userestrict/ead:p/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:did/ead:langmaterial/ead:language">
			<field name="language_ss">
				<xsl:value-of select="ead:did/ead:langmaterial/ead:language/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:did/ead:langmaterial">
			<field name="langmaterial_ss">
				<xsl:value-of select="ead:did/ead:langmaterial/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:originalsloc/ead:p/text()">
			<field name="originalsloc_ss">
				<xsl:value-of select="ead:originalsloc/ead:p/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:altformavail/ead:p/text()">
			<field name="altformavail_ss">
					<xsl:value-of select="ead:altformavail/ead:p/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:relatedmaterial">
			<field name="relatedmaterial_ss">
					<xsl:value-of select="ead:relatedmaterial/ead:p/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:bibliography/ead:p/text()">
			<field name="bibliography_ss">
				<xsl:value-of select="ead:bibliography/ead:p/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:odd[@type='statusDescription']/ead:p/text()">
			<field name="statusDescription_ss">
				<xsl:value-of select="ead:odd[@type='statusDescription']/ead:p/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:odd[@type='levelOfDetail']/ead:p/text()">
			<field name="levelOfDetail_ss">
				<xsl:value-of select="ead:odd[@type='levelOfDetail']/ead:p/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:processinfo/ead:p[not(*)]">
			<field name="archivistnotes_ss">
				<xsl:value-of select="ead:processinfo/ead:p[not(*)]/text()" />
			</field>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>