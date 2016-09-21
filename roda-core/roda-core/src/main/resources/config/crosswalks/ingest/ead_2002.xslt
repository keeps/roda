<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ead="urn:isbn:1-931666-22-9"
	exclude-result-prefixes="ead">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />

	<xsl:template match="/">
		<doc>
			<xsl:if test="//ead:archdesc/ead:did/ead:unitid/text()">
				<field name="unitId_ss">
					<xsl:value-of select="//ead:archdesc/ead:did/ead:unitid/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:did/ead:unittitle/text()">
				<field name="title">
					<xsl:value-of select="//ead:archdesc/ead:did/ead:unittitle/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:did/ead:unitdate/@normal">
				<xsl:choose>
					<xsl:when test="contains(//ead:did/ead:unitdate/@normal, '/')">	<!-- initial/final -->
						<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$"
							select="normalize-space(substring-before(//ead:did/ead:unitdate/@normal, '/'))">
							<xsl:matching-substring>
								<field name="dateInitial">
									<xsl:value-of select="regex-group(1)" />
									<xsl:text>T00:00:00Z</xsl:text>
								</field>
							</xsl:matching-substring>
						</xsl:analyze-string>
						<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$"
							select="normalize-space(substring-before(//ead:did/ead:unitdate/@normal, '/'))">
							<xsl:matching-substring>
								<field name="dateInitial">
									<xsl:value-of select="regex-group(1)" />
									<xsl:text>-01T00:00:00Z</xsl:text>
								</field>
							</xsl:matching-substring>
						</xsl:analyze-string>
						<xsl:analyze-string regex="^(\d{{4}})$"
							select="normalize-space(substring-before(//ead:did/ead:unitdate/@normal, '/'))">
							<xsl:matching-substring>
								<field name="dateInitial">
									<xsl:value-of select="regex-group(1)" />
									<xsl:text>-01-01T00:00:00Z</xsl:text>
								</field>
							</xsl:matching-substring>
						</xsl:analyze-string>
						<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$"
							select="normalize-space(substring-before(//ead:did/ead:unitdate/@normal, '/'))">
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
							select="normalize-space(substring-before(//ead:did/ead:unitdate/@normal, '/'))">
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
							select="normalize-space(substring-after(//ead:did/ead:unitdate/@normal, '/'))">
							<xsl:matching-substring>
								<field name="dateFinal">
									<xsl:value-of select="regex-group(1)" />
									<xsl:text>T00:00:00Z</xsl:text>
								</field>
							</xsl:matching-substring>
						</xsl:analyze-string>
						<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$"
							select="normalize-space(substring-after(//ead:did/ead:unitdate/@normal, '/'))">
							<xsl:matching-substring>
								<field name="dateFinal">
									<xsl:value-of select="regex-group(1)" />
									<xsl:text>-01T00:00:00Z</xsl:text>
								</field>
							</xsl:matching-substring>
						</xsl:analyze-string>
						<xsl:analyze-string regex="^(\d{{4}})$"
							select="normalize-space(substring-after(//ead:did/ead:unitdate/@normal, '/'))">
							<xsl:matching-substring>
								<field name="dateFinal">
									<xsl:value-of select="regex-group(1)" />
									<xsl:text>-01-01T00:00:00Z</xsl:text>
								</field>
							</xsl:matching-substring>
						</xsl:analyze-string>
						<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$"
							select="normalize-space(substring-after(//ead:did/ead:unitdate/@normal, '/'))">
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
							select="normalize-space(substring-after(//ead:did/ead:unitdate/@normal, '/'))">
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
						<xsl:choose>
							<xsl:when test="//ead:did/ead:unitdate[@label='UnitDateInitial']">	<!-- initial date. internal 'hack' -->
								<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$"
									select="normalize-space(//ead:did/ead:unitdate/@normal)">
									<xsl:matching-substring>
										<field name="dateInitial">
											<xsl:value-of select="regex-group(1)" />
											<xsl:text>T00:00:00Z</xsl:text>
										</field>
									</xsl:matching-substring>
								</xsl:analyze-string>
								<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$"
									select="normalize-space(//ead:did/ead:unitdate/@normal)">
									<xsl:matching-substring>
										<field name="dateInitial">
											<xsl:value-of select="regex-group(1)" />
											<xsl:text>-01T00:00:00Z</xsl:text>
										</field>
									</xsl:matching-substring>
								</xsl:analyze-string>
								<xsl:analyze-string regex="^(\d{{4}})$"
									select="normalize-space(//ead:did/ead:unitdate/@normal)">
									<xsl:matching-substring>
										<field name="dateInitial">
											<xsl:value-of select="regex-group(1)" />
											-01-01T00:00:00Z
										</field>
									</xsl:matching-substring>
								</xsl:analyze-string>
								<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$"
									select="normalize-space(//ead:did/ead:unitdate/@normal)">
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
									select="normalize-space(//ead:did/ead:unitdate/@normal)">
									<xsl:matching-substring>
										<field name="dateInitial">
											<xsl:value-of select="regex-group(1)" />
											<xsl:text>-</xsl:text>
											<xsl:value-of select="regex-group(2)" />
											<xsl:text>-01T00:00:00Z</xsl:text>
										</field>
									</xsl:matching-substring>
								</xsl:analyze-string>
							</xsl:when>
							<xsl:when test="//ead:did/ead:unitdate[@label='UnitDateFinal']">	<!-- final date. internal 'hack' -->
								<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$"
									select="normalize-space(//ead:did/ead:unitdate/@normal)">
									<xsl:matching-substring>
										<field name="dateFinal">
											<xsl:value-of select="regex-group(1)" />
											<xsl:text>T00:00:00Z</xsl:text>
										</field>
									</xsl:matching-substring>
								</xsl:analyze-string>
								<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$"
									select="normalize-space(//ead:did/ead:unitdate/@normal)">
									<xsl:matching-substring>
										<field name="dateFinal">
											<xsl:value-of select="regex-group(1)" />
											<xsl:text>-01T00:00:00Z</xsl:text>
										</field>
									</xsl:matching-substring>
								</xsl:analyze-string>
								<xsl:analyze-string regex="^(\d{{4}})$"
									select="normalize-space(//ead:did/ead:unitdate/@normal)">
									<xsl:matching-substring>
										<field name="dateFinal">
											<xsl:value-of select="regex-group(1)" />
											-01-01T00:00:00Z
										</field>
									</xsl:matching-substring>
								</xsl:analyze-string>
								<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$"
									select="normalize-space(//ead:did/ead:unitdate/@normal)">
									<xsl:matching-substring>
										<field name="dateFinal">
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
									select="normalize-space(//ead:did/ead:unitdate/@normal)">
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
							<xsl:otherwise> <!-- fallback to date initial -->
								<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$"
									select="normalize-space(//ead:did/ead:unitdate/@normal)">
									<xsl:matching-substring>
										<field name="dateInitial">
											<xsl:value-of select="regex-group(1)" />
											<xsl:text>T00:00:00Z</xsl:text>
										</field>
									</xsl:matching-substring>
								</xsl:analyze-string>
								<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$"
									select="normalize-space(//ead:did/ead:unitdate/@normal)">
									<xsl:matching-substring>
										<field name="dateInitial">
											<xsl:value-of select="regex-group(1)" />
											<xsl:text>-01T00:00:00Z</xsl:text>
										</field>
									</xsl:matching-substring>
								</xsl:analyze-string>
								<xsl:analyze-string regex="^(\d{{4}})$"
									select="normalize-space(//ead:did/ead:unitdate/@normal)">
									<xsl:matching-substring>
										<field name="dateInitial">
											<xsl:value-of select="regex-group(1)" />
											-01-01T00:00:00Z
										</field>
									</xsl:matching-substring>
								</xsl:analyze-string>
								<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$"
									select="normalize-space(//ead:did/ead:unitdate/@normal)">
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
									select="normalize-space(//ead:did/ead:unitdate/@normal)">
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
	
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:did/ead:unitdate/text()">
				<field name="descriptiveDate_ss">
					<xsl:value-of select="//ead:archdesc/ead:did/ead:unitdate/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:did/ead:unitid/@countrycode">
				<field name="countryCode_ss">
					<xsl:value-of select="//ead:archdesc/ead:did/ead:unitid/@countrycode" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/@level">
				<xsl:choose>
					<xsl:when test="//ead:archdesc/@level = 'otherlevel'">
						<field name="level">
							<xsl:value-of select="//ead:archdesc/@otherlevel" />
						</field>
					</xsl:when>
					<xsl:otherwise>
						<field name="level">
							<xsl:value-of select="//ead:archdesc/@level" />
						</field>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:did/ead:materialspec/text()">
				<field name="materialSpec_ss">
					<xsl:value-of select="//ead:archdesc/ead:did/ead:materialspec/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:did/ead:origination[not(*)]/text()">
				<field name="origination_ss">
					<xsl:value-of select="//ead:archdesc/ead:did/ead:origination[not(*)]/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:did/ead:physdesc/text()">
				<field name="physdesc_ss">
					<xsl:value-of select="//ead:archdesc/ead:did/ead:physdesc/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:did/ead:physdesc/ead:extent/text()">
				<field name="extent_ss">
					<xsl:value-of select="//ead:archdesc/ead:did/ead:physdesc/ead:extent/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:did/ead:physdesc/ead:dimensions/text()">
				<field name="dimensions_ss">
					<xsl:value-of select="//ead:archdesc/ead:did/ead:physdesc/ead:dimensions/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:did/ead:physdesc/ead:physfacet/text()">
				<field name="physfacet_ss">
					<xsl:value-of select="//ead:archdesc/ead:did/ead:physdesc/ead:physfacet/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:prefercite/ead:p/text()">
				<field name="quote_ss">
					<xsl:value-of select="//ead:archdesc/ead:prefercite/ead:p/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:bioghist/text()">
				<field name="bioghist_ss">
					<xsl:value-of select="//ead:archdesc/ead:bioghist/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:did/ead:origination[@label='creator']/ead:name/text()">
				<field name="creator_ss">
					<xsl:value-of select="//ead:archdesc/ead:did/ead:origination[@label='creator']/ead:name/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:did/ead:origination[@label='producer']/ead:name/text()">
				<field name="producer_ss">
					<xsl:value-of select="//ead:archdesc/ead:did/ead:origination[@label='producer']/ead:name/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:did/ead:unitid/@repositorycode">
				<field name="repositoryCode_ss">
					<xsl:value-of select="//ead:archdesc/ead:did/ead:unitid/@repositorycode" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:did/ead:repository/ead:corpname/text()">
				<field name="repository_ss">
					<xsl:value-of select="//ead:archdesc/ead:did/ead:repository/ead:corpname/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:custodhist/ead:p/text()">
				<field name="custodHist_ss">
					<xsl:value-of select="//ead:archdesc/ead:custodhist/ead:p/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:acqinfo/ead:p/text()">
				<field name="acqInfo_ss">
					<xsl:value-of select="//ead:archdesc/ead:acqinfo/ead:p/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:scopecontent/ead:p/text()">
				<field name="description">
					<xsl:value-of select="//ead:archdesc/ead:scopecontent/ead:p/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:appraisal/ead:p/text()">
				<field name="appraisal_ss">
					<xsl:value-of select="//ead:archdesc/ead:appraisal/ead:p/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:accruals/ead:p/text()">
				<field name="accruals_ss">
					<xsl:value-of select="//ead:archdesc/ead:accruals/ead:p/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:arrangement/ead:p/text()">
				<field name="systemOfArrangement_ss">
					<xsl:value-of select="//ead:archdesc/ead:arrangement/ead:p/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:phystech/ead:p/text()">
				<field name="phystech_ss">
					<xsl:value-of select="//ead:archdesc/ead:phystech/ead:p/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:accessrestrict/ead:p/text()">
				<field name="accessRestrict_ss">
					<xsl:value-of select="//ead:archdesc/ead:accessrestrict/ead:p/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:userestrict/ead:p/text()">
				<field name="useRestrict_ss">
					<xsl:value-of select="//ead:archdesc/ead:userestrict/ead:p/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:did/ead:langmaterial/ead:language/text()[normalize-space()]">
				<field name="language_ss">
					<xsl:value-of select="//ead:archdesc/ead:did/ead:langmaterial/ead:language/text()[normalize-space()]" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:did/ead:langmaterial/text()[normalize-space()]">
				<field name="languageScriptNotes_ss">
					<xsl:value-of select="//ead:archdesc/ead:did/ead:langmaterial/text()[normalize-space()]" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:originalsloc/ead:p/text()">
				<field name="originalsLoc_ss">
					<xsl:value-of select="//ead:archdesc/ead:originalsloc/ead:p/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:altformavail/ead:p/text()">
				<field name="altFormAvail_ss">
					<xsl:value-of select="//ead:archdesc/ead:altformavail/ead:p/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:relatedmaterial/ead:p/text()">
				<field name="relatedMaterial_ss">
					<xsl:value-of select="//ead:archdesc/ead:relatedmaterial/ead:p/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:bibliography/ead:p/text()">
				<field name="bibliography_ss">
					<xsl:value-of select="//ead:archdesc/ead:bibliography/ead:p/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:otherfindaid/ead:p/text()">
				<field name="otherFindAid_ss">
					<xsl:value-of select="//ead:archdesc/ead:otherfindaid/ead:p/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:did/ead:note[@type='generalNote']/text()">
				<field name="notes_ss">
					<xsl:value-of select="//ead:archdesc/ead:did/ead:note[@type='generalNote']/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:profiledesc/ead:descrules/text()">
				<field name="rules_ss">
					<xsl:value-of select="//ead:profiledesc/ead:descrules/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:odd[@type='statusDescription']/ead:p/text()">
				<field name="statusDescription_ss">
					<xsl:value-of select="//ead:archdesc/ead:odd[@type='statusDescription']/ead:p/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:odd[@type='levelOfDetail']/ead:p/text()">
				<field name="levelOfDetail_ss">
					<xsl:value-of select="//ead:archdesc/ead:odd[@type='levelOfDetail']/ead:p/text()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:processinfo/ead:p/ead:date/string()[normalize-space()]">
				<field name="processDates_ss">
					<xsl:value-of select="//ead:archdesc/ead:processinfo/ead:p/ead:date/string()[normalize-space()]" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:did/ead:note[@type='sourcesDescription']/string()">
				<field name="sources_ss">
					<xsl:value-of select="//ead:archdesc/ead:did/ead:note[@type='sourcesDescription']/string()" />
				</field>
			</xsl:if>
			<xsl:if test="//ead:archdesc/ead:processinfo/ead:p/text()[normalize-space()]">
				<field name="archivistNotes_ss">
					<xsl:value-of select="//ead:archdesc/ead:processinfo/ead:p/text()[normalize-space()]" />
				</field>
			</xsl:if>
		</doc>
	</xsl:template>
</xsl:stylesheet>