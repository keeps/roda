<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:eadc="http://roda.dgarq.gov.pt/2008/EADCSchema"
	exclude-result-prefixes="eadc">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />

	<xsl:param name="i18n.title" />
	<xsl:param name="i18n.titletype" />
	<xsl:param name="i18n.level" />
	<xsl:param name="i18n.initialdate" />
	<xsl:param name="i18n.finaldate" />
	<xsl:param name="i18n.repositorycode" />
	<xsl:param name="i18n.reference" />
	<xsl:param name="i18n.acquisitionnumber" />
	<xsl:param name="i18n.origination" />
	<xsl:param name="i18n.acquisitiondate" />
	<xsl:param name="i18n.materialspecification" />
	<xsl:param name="i18n.physicaldescription" />
	<xsl:param name="i18n.dateofinitialphysicaldescription" />
	<xsl:param name="i18n.dateoffinalphysicaldescription" />
	<xsl:param name="i18n.dimensions" />
	<xsl:param name="i18n.facetorappearance" />
	<xsl:param name="i18n.extent" />
	<xsl:param name="i18n.languages" />
	<xsl:param name="i18n.quote" />
	<xsl:param name="i18n.administrativeandbiographicalhistory" />
	<xsl:param name="i18n.custodialhistory" />
	<xsl:param name="i18n.acquisitioninformation" />
	<xsl:param name="i18n.description" />
	<xsl:param name="i18n.organizationandordering" />
	<xsl:param name="i18n.appraisal" />
	<xsl:param name="i18n.accruals" />
	<xsl:param name="i18n.physicalcharacteristicsandtechnicalrequirements" />
	<xsl:param name="i18n.accessrestrictions" />
	<xsl:param name="i18n.altformavail" />
	<xsl:param name="i18n.reproductionrestrictions" />
	<xsl:param name="i18n.relatedmaterials" />
	<xsl:param name="i18n.otherfindaids" />
	<xsl:param name="i18n.notes" />
	<xsl:param name="i18n.bibliography" />
	<xsl:param name="i18n.unitdate" />
	<xsl:template match="/">
		<div class="descriptiveMetadata">
			<xsl:apply-templates />
		</div>
	</xsl:template>
	<xsl:template match="eadc:ead-c">
		<!-- COMPLETE REFERENCE -->
		<!-- HANDLE -->
		<xsl:if test="eadc:did/eadc:unittitle/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.title"/>
				</div>
				<div class="value">
					<xsl:value-of select="eadc:did/eadc:unittitle/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="ead:did/ead:unittitle/@type">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.titletype" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:did/ead:unittitle/@type" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="@level">
			<xsl:choose>
				<xsl:when test="@level = 'otherlevel'">
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.level" />
						</div>
						<div class="value">
							<xsl:value-of select="@otherlevel" />
						</div>
					</div>
				</xsl:when>
				<xsl:otherwise>
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.level" />
						</div>
						<div class="value">
							<xsl:value-of select="@level" />
						</div>
					</div>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		<!-- HANDLE DATE BETTER??? -->
		<xsl:if test="eadc:did/eadc:unitdate/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.unitdate" />
				</div>
				<div class="value">
					<span class="value">
						<xsl:value-of select="eadc:did/eadc:unitdate/text()" />
					</span>
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:unitdate/@normal">
			<xsl:choose>
				<xsl:when test="contains(eadc:did/eadc:unitdate/@normal, '/')">	<!-- initial/final -->
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$" select="normalize-space(substring-before(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$" select="normalize-space(substring-before(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})$" select="normalize-space(substring-before(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-01-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$" select="normalize-space(substring-before(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-<xsl:value-of select="regex-group(3)" />
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})$" select="normalize-space(substring-before(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
				
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$" select="normalize-space(substring-after(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.finaldate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$" select="normalize-space(substring-after(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.finaldate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})$" select="normalize-space(substring-after(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.finaldate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-01-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$" select="normalize-space(substring-after(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.finaldate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-<xsl:value-of select="regex-group(3)" />
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})$" select="normalize-space(substring-after(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.finaldate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
				</xsl:when>
				<xsl:otherwise>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$" select="normalize-space(eadc:did/eadc:unitdate/@normal)">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$" select="normalize-space(eadc:did/eadc:unitdate/@normal)">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})$" select="normalize-space(eadc:did/eadc:unitdate/@normal)">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-01-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$" select="normalize-space(eadc:did/eadc:unitdate/@normal)">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-<xsl:value-of select="regex-group(3)" />
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})$" select="normalize-space(eadc:did/eadc:unitdate/@normal)">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		<!-- COUNTRY CODE -->
		<xsl:if test="eadc:did/eadc:unitid/@repositorycode">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.repositorycode" />
				</div>
				<div class="value">
					<xsl:value-of select="eadc:did/eadc:unitid/@repositorycode" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:unitid/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.reference" />
				</div>
				<div class="value">
					<xsl:value-of select="eadc:did/eadc:unitid/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:origination/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.origination" />
				</div>
				<div class="value">
					<xsl:value-of select="eadc:did/eadc:origination/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:acqinfo/eadc:num/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.acquisitionnumber" />
				</div>
				<div class="value">
					<xsl:value-of select="eadc:acqinfo/eadc:num/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:acqinfo/eadc:date/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.acquisitiondate" />
				</div>
				<div class="value">
					<xsl:value-of select="eadc:acqinfo/eadc:date/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:materialspec/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.materialspecification" />
				</div>
				<div class="value">
					<xsl:value-of select="eadc:did/eadc:materialspec/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:physdesc/eadc:p/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.physicaldescription" />
				</div>
				<div class="value">
					<span class="value">
						<xsl:value-of select="eadc:did/eadc:physdesc/eadc:p/text()" />
					</span>
					<xsl:if test="eadc:did/eadc:physdesc/eadc:p/@unit">
						<span class="unit">
							<xsl:value-of select="eadc:did/eadc:physdesc/eadc:p/@unit" />
						</span>
					</xsl:if>
				</div>
			</div>
		</xsl:if>
		<!-- HANDLE DATE BETTER??? -->
		<xsl:if test="eadc:did/eadc:physdesc/eadc:date/@normal">
			<xsl:choose>
				<xsl:when test="contains(eadc:did/eadc:physdesc/eadc:date/@normal, '/')">
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.dateofinitialphysicaldescription" />
						</div>
						<div class="value">
							<span class="value">
								<xsl:value-of
									select="substring-before(eadc:did/eadc:physdesc/eadc:date/@normal, '/')" />
							</span>
						</div>
					</div>
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.dateoffinalphysicaldescription" />
						</div>
						<div class="value">
							<span class="value">
								<xsl:value-of
									select="substring-after(eadc:did/eadc:physdesc/eadc:date/@normal, '/')" />
							</span>
						</div>
					</div>
				</xsl:when>
				<xsl:otherwise>
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.dateofinitialphysicaldescription" />
						</div>
						<div class="value">
							<span class="value">
								<xsl:value-of select="eadc:did/eadc:physdesc/eadc:date/@normal" />
							</span>
						</div>
					</div>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:physdesc/eadc:dimensions/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.dimensions" />
				</div>
				<div class="value">
					<span class="value">
						<xsl:value-of select="eadc:did/eadc:physdesc/eadc:dimensions/text()" />
					</span>
					<xsl:if test="eadc:did/eadc:physdesc/eadc:dimensions/@unit">
						<span class="unit">
							<xsl:value-of select="eadc:did/eadc:physdesc/eadc:dimensions/@unit" />
						</span>
					</xsl:if>
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:physdesc/eadc:physfacet/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.facetorappearance" />
				</div>
				<div class="value">
					<span class="value">
						<xsl:value-of select="eadc:did/eadc:physdesc/eadc:physfacet/text()" />
					</span>
					<xsl:if test="eadc:did/eadc:physdesc/eadc:physfacet/@unit">
						<span class="unit">
							<xsl:value-of select="eadc:did/eadc:physdesc/eadc:physfacet/@unit" />
						</span>
					</xsl:if>
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:physdesc/eadc:extent/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.extent" />
				</div>
				<div class="value">
					<span class="value">
						<xsl:value-of select="eadc:did/eadc:physdesc/eadc:extent/text()" />
					</span>
					<xsl:if test="eadc:did/eadc:physdesc/eadc:extent/@unit">
						<span class="unit">
							<xsl:value-of select="eadc:did/eadc:physdesc/eadc:extent/@unit" />
						</span>
					</xsl:if>
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:langmaterial">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.languages" />
				</div>
				<xsl:for-each select="eadc:did/eadc:langmaterial/eadc:language">
					<div class="value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="eadc:prefercite/eadc:p/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.quote" />
				</div>
				<div class="value">
					<xsl:value-of select="eadc:prefercite/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:bioghist/eadc:p/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.administrativeandbiographicalhistory" />
				</div>
				<div class="value">
					<xsl:value-of select="eadc:bioghist/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:bioghist/eadc:chronlist">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.administrativeandbiographicalhistory" />
				</div>
				<div class="value">
					<xsl:for-each select="eadc:bioghist/eadc:chronlist/eadc:chronitem">
						<xsl:variable name="line">
							<xsl:if test="eadc:date/@normal">
								<xsl:choose>
									<xsl:when test="contains(eadc:date/@normal, '/')">
										<span class="initialDate">
											<xsl:value-of select="substring-before(eadc:date/@normal, '/')" />
										</span>
										<span class="finalDate">
											<xsl:value-of select="substring-after(eadc:date/@normal, '/')" />
										</span>
									</xsl:when>
									<xsl:otherwise>
										<span class="initialDate">
											<xsl:value-of select="eadc:date/@normal" />
										</span>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:if>
							<xsl:if test="eadc:event/text()">
								<span class="event">
									<xsl:value-of select="eadc:event/text()" />
								</span>
							</xsl:if>
						</xsl:variable>
						<div class="descriptiveMetadata-field-value-level1">
							<xsl:copy-of select="$line" />
						</div>
					</xsl:for-each>
				</div>
			</div>
		</xsl:if>

		<xsl:if test="eadc:custodhist/eadc:p/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.custodialhistory" />
				</div>
				<div class="value">
					<xsl:value-of select="eadc:custodhist/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:acqinfo/eadc:p/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.acquisitioninformation" />
				</div>
				<div class="value">
					<xsl:value-of select="eadc:acqinfo/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:scopecontent/eadc:p/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.description" />
				</div>
				<div class="value prewrap">
					<xsl:value-of select="eadc:scopecontent/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:arrangement/eadc:p/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.organizationandordering" />
				</div>
				<div class="value">
					<xsl:value-of select="eadc:arrangement/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:arrangement/eadc:table">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.organizationandordering" />
				</div>
				<xsl:variable name="output">
					<table>
						<thead>
							<tr>
								<xsl:for-each
									select="eadc:arrangement/eadc:table/eadc:tgroup/eadc:thead/eadc:row/eadc:entry">
									<td>
										<xsl:value-of select="text()" />
									</td>
								</xsl:for-each>
							</tr>
						</thead>
						<tbody>
							<xsl:for-each
								select="eadc:arrangement/eadc:table/eadc:tgroup/eadc:tbody/eadc:row">
								<tr>
									<xsl:for-each select="eadc:entry">
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

		<xsl:if test="eadc:appraisal/eadc:p/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.appraisal" />
				</div>
				<div class="value">
					<xsl:value-of select="eadc:appraisal/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:accruals/eadc:p/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.accruals" />
				</div>
				<div class="value">
					<xsl:value-of select="eadc:accruals/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:phystech/eadc:p/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of
						select="$i18n.physicalcharacteristicsandtechnicalrequirements" />
				</div>
				<div class="value">
					<xsl:value-of select="eadc:phystech/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:accessrestrict/eadc:p/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.accessrestrictions" />
				</div>
				<div class="value">
					<xsl:value-of select="eadc:accessrestrict/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:altformavail/ead:p/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.altformavail" />
				</div>
				<div class="value">
					<xsl:value-of select="eadc:altformavail/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:userrestrict/eadc:p/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.reproductionrestrictions" />
				</div>
				<div class="value">
					<xsl:value-of select="eadc:userrestrict/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:relatedmaterial/eadc:p/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.relatedmaterials" />
				</div>
				<div class="value">
					<xsl:value-of select="eadc:relatedmaterial/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:otherfindingaid/eadc:p/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.otherfindaids" />
				</div>
				<div class="value">
					<xsl:value-of select="eadc:otherfindingaid/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:note/eadc:p/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.notes" />
				</div>
				<div class="value">
					<xsl:value-of select="eadc:note/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:bibliography/eadc:p/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.bibliography" />
				</div>
				<div class="value">
					<xsl:value-of select="eadc:bibliography/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>


















	</xsl:template>


</xsl:stylesheet>