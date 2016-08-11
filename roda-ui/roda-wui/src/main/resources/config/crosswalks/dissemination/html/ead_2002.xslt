<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet  [
	<!ENTITY crarr  "&#13;">
	<!ENTITY crarr  "&#xD;">
]>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ead="urn:isbn:1-931666-22-9"
	exclude-result-prefixes="ead">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />

	<xsl:param name="i18n.identityarea" />	
	<xsl:param name="i18n.reference" />
	<xsl:param name="i18n.title" />
	<xsl:param name="i18n.repositorycode" />
	<xsl:param name="i18n.unitdate" />
	<xsl:param name="i18n.initialdate" />
	<xsl:param name="i18n.finaldate" />
	<xsl:param name="i18n.level" />
	<xsl:param name="i18n.extent" />
	
	<xsl:param name="i18n.contextarea" />	
	<xsl:param name="i18n.creator" />	
	<xsl:param name="i18n.producer" />	
	<xsl:param name="i18n.repository" />	
	<xsl:param name="i18n.custodialhistory" />
	<xsl:param name="i18n.acquisitioninformation" />
	
	<xsl:param name="i18n.contentarea" />	
	<xsl:param name="i18n.description" />
	<xsl:param name="i18n.appraisal" />
	<xsl:param name="i18n.accruals" />
	<xsl:param name="i18n.arrangement" />
	
	<xsl:param name="i18n.accessarea" />	
	<xsl:param name="i18n.accessrestrictions" />
	<xsl:param name="i18n.userestrict" />
	<xsl:param name="i18n.languages" />
	<xsl:param name="i18n.languagesMaterial" />
	
	<xsl:param name="i18n.alliedarea" />	
	<xsl:param name="i18n.originalsloc" />
	<xsl:param name="i18n.altformavail" />
	<xsl:param name="i18n.relatedmaterials" />
	<xsl:param name="i18n.bibliography" />
	
	<xsl:param name="i18n.notesarea" />	
	<xsl:param name="i18n.notes" />
	
	<xsl:param name="i18n.descriptioncontrolarea" />
	<xsl:param name="i18n.rules" />
	<xsl:param name="i18n.statusDescription" />
	<xsl:param name="i18n.levelOfDetail" />
	<xsl:param name="i18n.processDates" />
	<xsl:param name="i18n.sources" />
	<xsl:param name="i18n.archivistNotes" />
	
	
	<xsl:param name="i18n.titletype" />
	
	<xsl:param name="i18n.acquisitionnumber" />
	<xsl:param name="i18n.origination" />
	<xsl:param name="i18n.acquisitiondate" />
	<xsl:param name="i18n.materialspecification" />
	<xsl:param name="i18n.physicaldescription" />
	<xsl:param name="i18n.dateofinitialphysicaldescription" />
	<xsl:param name="i18n.dateoffinalphysicaldescription" />
	<xsl:param name="i18n.dimensions" />
	<xsl:param name="i18n.facetorappearance" />
	<xsl:param name="i18n.quote" />
	<xsl:param name="i18n.administrativeandbiographicalhistory" />
	
	<xsl:param name="i18n.physicalcharacteristicsandtechnicalrequirements" />
	<xsl:param name="i18n.otherfindaids" />
	<xsl:template match="/">
		<div class="descriptiveMetadata">
			<xsl:if test="//ead:did/ead:unitid/text()|//ead:did/ead:unittitle/text()|//ead:did/ead:unitid/@repositorycode|//ead:did/ead:unitdate/text()|//ead:did/ead:unitdate/@normal|//ead:archdesc/@level|//ead:extent">
				<div class="form-separator"><xsl:value-of select="$i18n.identityarea" /></div>
			</xsl:if>
			<xsl:if test="//ead:did/ead:unitid/text()">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.reference" />
					</div>
					<div class="value">
						<xsl:value-of select="//ead:did/ead:unitid/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="//ead:did/ead:unittitle/text()">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.title" />
					</div>
					<div class="value">
						<xsl:value-of select="//ead:did/ead:unittitle/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="//ead:did/ead:unitid/@repositorycode">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.repositorycode" />
					</div>
					<div class="value">
						<xsl:value-of select="//ead:did/ead:unitid/@repositorycode" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="//ead:did/ead:unitdate/text()">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.unitdate" />
					</div>
					<div class="value">
						<span class="value">
							<xsl:value-of select="//ead:did/ead:unitdate/text()" />
						</span>
					</div>
				</div>
			</xsl:if>
			<xsl:if test="//ead:did/ead:unitdate/@normal">
				<xsl:choose>
					<xsl:when test="contains(//ead:did/ead:unitdate/@normal, '/')">	<!-- initial/final -->
						<div class="field">
							<div class="label">
								<xsl:value-of select="$i18n.initialdate" />
							</div>
							<div class="value">
								<span class="value">
									<xsl:value-of select="normalize-space(substring-before(//ead:did/ead:unitdate/@normal, '/'))" />
								</span>
							</div>
						</div>
						<div class="field">
							<div class="label">
								<xsl:value-of select="$i18n.finaldate" />
							</div>
							<div class="value">
								<span class="value">
									<xsl:value-of select="normalize-space(substring-after(//ead:did/ead:unitdate/@normal, '/'))" />
								</span>
							</div>
						</div>
					</xsl:when>
					<xsl:otherwise>
						<xsl:choose>
							<xsl:when test="//ead:did/ead:unitdate[@label='UnitDateInitial']">	<!-- initial date. internal 'hack' -->
								<div class="field">
									<div class="label">
										<xsl:value-of select="$i18n.initialdate" />
									</div>
									<div class="value">
										<span class="value">
											<xsl:value-of select="normalize-space(//ead:did/ead:unitdate/@normal)" />
										</span>
									</div>
								</div>
							</xsl:when>
							<xsl:when test="//ead:did/ead:unitdate[@label='UnitDateFinal']">	<!-- final date. internal 'hack' -->
								<div class="field">
									<div class="label">
										<xsl:value-of select="$i18n.finaldate" />
									</div>
									<div class="value">
										<span class="value">
											<xsl:value-of select="normalize-space(//ead:did/ead:unitdate/@normal)" />
										</span>
									</div>
								</div>
							</xsl:when>
							<xsl:otherwise> <!-- fallback to date initial -->
								<div class="field">
									<div class="label">
										<xsl:value-of select="$i18n.initialdate" />
									</div>
									<div class="value">
										<span class="value">
											<xsl:value-of select="normalize-space(//ead:did/ead:unitdate/@normal)" />
										</span>
									</div>
								</div>
							</xsl:otherwise>	
						</xsl:choose>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
			<xsl:if test="//ead:archdesc/@level">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.level" />
					</div>
					<div class="value">
						<xsl:value-of select="//ead:archdesc/@level" />
					</div>
				</div>
			</xsl:if>
			<xsl:for-each select="//ead:extent">
				<xsl:if test="./text()">
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.extent" />
						</div>
						<div class="value">
							<span class="value">
								<xsl:value-of select="./text()" />
							</span>
							<xsl:if test="./@unit">
								<span class="unit">
									<xsl:value-of select="./@unit" />
								</span>
							</xsl:if>
						</div>
					</div>
				</xsl:if>
			</xsl:for-each>
			<xsl:if test="//ead:did/ead:origination[@label='creator']/ead:name/text()|//ead:did/ead:origination[@label='producer']/ead:name/text()|//ead:did/ead:repository/ead:corpname/text()|//ead:custodhist/ead:p/text()|//ead:acqinfo/ead:p/text()">
				<div class="form-separator"><xsl:value-of select="$i18n.contextarea" /></div>
			</xsl:if>
			<xsl:if test="//ead:did/ead:origination[@label='creator']/ead:name/text()">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.creator" />
					</div>
					<div class="value">
						<xsl:value-of select="//ead:did/ead:origination[@label='creator']/ead:name/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="//ead:did/ead:origination[@label='producer']/ead:name/text()">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.producer" />
					</div>
					<div class="value">
						<xsl:value-of select="//ead:did/ead:origination[@label='producer']/ead:name/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="//ead:did/ead:repository/ead:corpname/text()">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.repository" />                                                                                                                                                                                                                               
					</div>
					<div class="value">
						<span class="value">
							<xsl:value-of select="//ead:did/ead:repository/ead:corpname/text()" />
						</span>
					</div>
				</div>
			</xsl:if>
			<xsl:if test="//ead:custodhist/ead:p/text()">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.custodialhistory" />
					</div>
					<div class="value prewrap">
						<xsl:value-of select="//ead:custodhist/ead:p/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="//ead:acqinfo/ead:p/text()">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.acquisitioninformation" />
					</div>
					<div class="value">
						<xsl:value-of select="//ead:acqinfo/ead:p/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="//ead:scopecontent/ead:p/text()|//ead:appraisal/ead:p/text()|//ead:accruals/ead:p/text()|//ead:arrangement/ead:p/text()|ead:arrangement/ead:table">
				<div class="form-separator"><xsl:value-of select="$i18n.contentarea" /></div>
			</xsl:if>
			<xsl:if
				test="normalize-space(string-join(//ead:scopecontent/ead:p/text(),''))!=''">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.description" />
					</div>
					<div class="value prewrap">
						<xsl:value-of select="//ead:scopecontent/ead:p/text()"/>
					</div>
				</div>
			</xsl:if>
			<xsl:if test="//ead:appraisal/ead:p/text()">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.appraisal" />
					</div>
					<div class="value">
						<xsl:value-of select="//ead:appraisal/ead:p/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="//ead:accruals/ead:p/text()">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.accruals" />
					</div>
					<div class="value">
						<xsl:value-of select="//ead:accruals/ead:p/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="//ead:arrangement/ead:p/text()">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.arrangement" />
					</div>
					<div class="value">
						<xsl:value-of select="//ead:arrangement/ead:p/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="ead:arrangement/ead:table">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.arrangement" />
					</div>
					<xsl:variable name="output">
						<table>
							<thead>
								<tr>
									<xsl:for-each
										select="ead:arrangement/ead:table/ead:tgroup/ead:thead/ead:row/ead:entry">
										<td>
											<xsl:value-of select="text()" />
										</td>
									</xsl:for-each>
								</tr>
							</thead>
							<tbody>
								<xsl:for-each
									select="ead:arrangement/ead:table/ead:tgroup/ead:tbody/ead:row">
									<tr>
										<xsl:for-each select="ead:entry">
											<td>
												<xsl:value-of select="text()" />
											</td>
										</xsl:for-each>
									</tr>
								</xsl:for-each>
							</tbody>
						</table>
					</xsl:variable>
					<div class="value">
						<xsl:copy-of select="$output" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="//ead:accessrestrict/ead:p/text()|//ead:userestrict/ead:p/text()|//ead:did/ead:langmaterial/text()">
				<div class="form-separator"><xsl:value-of select="$i18n.accessarea" /></div>
			</xsl:if>
			<xsl:if test="//ead:accessrestrict/ead:p/text()">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.accessrestrictions" />
					</div>
					<div class="value">
						<xsl:value-of select="//ead:accessrestrict/ead:p/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="//ead:userestrict/ead:p/text()">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.userestrict" />
					</div>
					<div class="value">
						<xsl:value-of select="//ead:userestrict/ead:p/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if
				test="normalize-space(string-join(//ead:did/ead:langmaterial/ead:language/text(),''))!=''">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.languages" />
					</div>
					<xsl:for-each select="//ead:did/ead:langmaterial/ead:language">
						<xsl:if test="normalize-space(string-join(text(),''))!=''">
							<div class="value">
								<xsl:value-of select="text()" />
							</div>
						</xsl:if>
					</xsl:for-each>
				</div>
			</xsl:if>
			<xsl:if
				test="normalize-space(string-join(//ead:did/ead:langmaterial[not(*)]/text(),''))!=''">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.languagesMaterial" />
					</div>
					<xsl:for-each select="//ead:did/ead:langmaterial[not(*)]">
						<xsl:if test="normalize-space(string-join(text(),''))!=''">
							<div class="value">
								<xsl:value-of select="text()" />
							</div>
						</xsl:if>
					</xsl:for-each>
				</div>
			</xsl:if>
			
			
			
			
			
			
			
			
			<xsl:if test="//ead:originalsloc/ead:p/text()|//ead:altformavail/ead:p/text()|//ead:relatedmaterial/ead:p/text()|//ead:bibliography/ead:p/text()">
				<div class="form-separator"><xsl:value-of select="$i18n.alliedarea" /></div>
			</xsl:if>
			<xsl:if test="//ead:originalsloc/ead:p/text()">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.originalsloc" />
					</div>
					<div class="value prewrap">
						<xsl:value-of select="//ead:originalsloc/ead:p/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="//ead:altformavail/ead:p/text()">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.altformavail" />
					</div>
					<div class="value">
						<xsl:value-of select="//ead:altformavail/ead:p/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="//ead:relatedmaterial/ead:p/text()">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.relatedmaterials" />
					</div>
					<div class="value">
						<xsl:value-of select="//ead:relatedmaterial/ead:p/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="//ead:bibliography/ead:p/text()">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.bibliography" />
					</div>
					<div class="value">
						<xsl:value-of select="//ead:bibliography/ead:p/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="//ead:note[@type='generalNote']/ead:p/text()">
				<div class="form-separator"><xsl:value-of select="$i18n.notesarea" /></div>
			</xsl:if>
			<xsl:if test="//ead:note[@type='generalNote']/ead:p/text()">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.notes" />
					</div>
					<div class="value prewrap">
						<xsl:value-of select="//ead:note[@type='generalNote']/ead:p/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="//ead:profiledesc/ead:descrules|//ead:odd[@type='statusDescription']/ead:p|//ead:odd[@type='levelOfDetail']/ead:p|//ead:processinfo/ead:p/ead:date|//ead:did/ead:note[@type='sourcesDescription']/ead:p|//ead:processinfo/ead:p[not(*)]">
				<div class="form-separator"><xsl:value-of select="$i18n.descriptioncontrolarea" /></div>
			</xsl:if>
			<xsl:for-each select="//ead:profiledesc/ead:descrules">
				<xsl:if test="./text()">
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.rules" />
						</div>
						<div class="value">
							<xsl:value-of select="./text()" />
						</div>
					</div>
				</xsl:if>
			</xsl:for-each>
			<xsl:for-each select="//ead:odd[@type='statusDescription']/ead:p">
				<xsl:if test="./text()">
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.statusDescription" />
						</div>
						<div class="value">
							<xsl:value-of select="./text()" />
						</div>
					</div>
				</xsl:if>
			</xsl:for-each>
			<xsl:for-each select="//ead:odd[@type='levelOfDetail']/ead:p">
				<xsl:if test="./text()">
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.levelOfDetail" />
						</div>
						<div class="value">
							<xsl:value-of select="./text()" />
						</div>
					</div>
				</xsl:if>
			</xsl:for-each>
			<xsl:for-each select="//ead:processinfo/ead:p/ead:date">
				<xsl:if test="./text()">
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.processDates" />
						</div>
						<div class="value">
							<xsl:value-of select="./text()" />
						</div>
					</div>
				</xsl:if>
			</xsl:for-each>
			<xsl:for-each select="//ead:did/ead:note[@type='sourcesDescription']/ead:p">
				<xsl:if test="./text()">
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.sources" />
						</div>
						<div class="value">
							<xsl:value-of select="./text()" />
						</div>
					</div>
				</xsl:if>
			</xsl:for-each>
			<xsl:for-each select="//ead:processinfo/ead:p[not(*)]">
				<xsl:if test="./text()">
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.archivistNotes" />
						</div>
						<div class="value">
							<xsl:value-of select="./text()" />
						</div>
					</div>
				</xsl:if>
			</xsl:for-each>
		</div>
	</xsl:template>

</xsl:stylesheet>