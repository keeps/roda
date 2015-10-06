<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:eadc="http://roda.dgarq.gov.pt/2008/EADCSchema"
	exclude-result-prefixes="eadc">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />

	<xsl:param name="binaryToHtml.ead-c.xml.title" />
	<xsl:param name="binaryToHtml.ead-c.xml.level" />
	<xsl:param name="binaryToHtml.ead-c.xml.initialdate" />
	<xsl:param name="binaryToHtml.ead-c.xml.finaldate" />
	<xsl:param name="binaryToHtml.ead-c.xml.repositorycode" />
	<xsl:param name="binaryToHtml.ead-c.xml.reference" />
	<xsl:param name="binaryToHtml.ead-c.xml.acquisitionnumber" />
	<xsl:param name="binaryToHtml.ead-c.xml.origination" />
	<xsl:param name="binaryToHtml.ead-c.xml.acquisitiondate" />
	<xsl:param name="binaryToHtml.ead-c.xml.materialspecification" />
	<xsl:param name="binaryToHtml.ead-c.xml.physicaldescription" />
	<xsl:param name="binaryToHtml.ead-c.xml.dateofinitialphysicaldescription" />
	<xsl:param name="binaryToHtml.ead-c.xml.dateoffinalphysicaldescription" />
	<xsl:param name="binaryToHtml.ead-c.xml.dimensions" />
	<xsl:param name="binaryToHtml.ead-c.xml.facetorappearance" />
	<xsl:param name="binaryToHtml.ead-c.xml.extent" />
	<xsl:param name="binaryToHtml.ead-c.xml.languages" />
	<xsl:param name="binaryToHtml.ead-c.xml.quote" />
	<xsl:param name="binaryToHtml.ead-c.xml.administrativeandbiographicalhistory" />
	<xsl:param name="binaryToHtml.ead-c.xml.custodialhistory" />
	<xsl:param name="binaryToHtml.ead-c.xml.acquisitioninformation" />
	<xsl:param name="binaryToHtml.ead-c.xml.description" />
	<xsl:param name="binaryToHtml.ead-c.xml.organizationandordering" />
	<xsl:param name="binaryToHtml.ead-c.xml.appraisal" />
	<xsl:param name="binaryToHtml.ead-c.xml.accruals" />
	<xsl:param name="binaryToHtml.ead-c.xml.physicalcharacteristicsandtechnicalrequirements" />
	<xsl:param name="binaryToHtml.ead-c.xml.accessrestrictions" />
	<xsl:param name="binaryToHtml.ead-c.xml.reproductionrestrictions" />
	<xsl:param name="binaryToHtml.ead-c.xml.relatedmaterials" />
	<xsl:param name="binaryToHtml.ead-c.xml.otherfindaids" />
	<xsl:param name="binaryToHtml.ead-c.xml.notes" />
	<xsl:param name="binaryToHtml.ead-c.xml.bibliography" />
	<xsl:param name="binaryToHtml.ead-c.xml.unitdate" />
	<xsl:template match="/">
		<div class="descriptiveMetadata">
			<xsl:apply-templates />
		</div>
	</xsl:template>
	<xsl:template match="eadc:ead-c">
		<!-- COMPLETE REFERENCE -->
		<!-- HANDLE -->
		<xsl:if test="eadc:did/eadc:unittitle/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.title"/>
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:did/eadc:unittitle/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="@level">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.level" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="@level" />
				</div>
			</div>
		</xsl:if>
		<!-- HANDLE DATE BETTER??? -->
		<xsl:if test="eadc:did/eadc:unitdate/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.unitdate" />
				</div>
				<div class="descriptiveMetadata-field-value">
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
							<div class="descriptiveMetadata-field">
								<div class="descriptiveMetadata-field-key">
									<xsl:value-of select="$binaryToHtml.ead-c.xml.initialdate" />
								</div>
								<div class="descriptiveMetadata-field-value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$" select="normalize-space(substring-before(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="descriptiveMetadata-field">
								<div class="descriptiveMetadata-field-key">
									<xsl:value-of select="$binaryToHtml.ead-c.xml.initialdate" />
								</div>
								<div class="descriptiveMetadata-field-value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})$" select="normalize-space(substring-before(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="descriptiveMetadata-field">
								<div class="descriptiveMetadata-field-key">
									<xsl:value-of select="$binaryToHtml.ead-c.xml.initialdate" />
								</div>
								<div class="descriptiveMetadata-field-value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-01-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$" select="normalize-space(substring-before(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="descriptiveMetadata-field">
								<div class="descriptiveMetadata-field-key">
									<xsl:value-of select="$binaryToHtml.ead-c.xml.initialdate" />
								</div>
								<div class="descriptiveMetadata-field-value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-<xsl:value-of select="regex-group(3)" />
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})$" select="normalize-space(substring-before(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="descriptiveMetadata-field">
								<div class="descriptiveMetadata-field-key">
									<xsl:value-of select="$binaryToHtml.ead-c.xml.initialdate" />
								</div>
								<div class="descriptiveMetadata-field-value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
				
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$" select="normalize-space(substring-after(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="descriptiveMetadata-field">
								<div class="descriptiveMetadata-field-key">
									<xsl:value-of select="$binaryToHtml.ead-c.xml.finaldate" />
								</div>
								<div class="descriptiveMetadata-field-value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$" select="normalize-space(substring-after(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="descriptiveMetadata-field">
								<div class="descriptiveMetadata-field-key">
									<xsl:value-of select="$binaryToHtml.ead-c.xml.finaldate" />
								</div>
								<div class="descriptiveMetadata-field-value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})$" select="normalize-space(substring-after(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="descriptiveMetadata-field">
								<div class="descriptiveMetadata-field-key">
									<xsl:value-of select="$binaryToHtml.ead-c.xml.finaldate" />
								</div>
								<div class="descriptiveMetadata-field-value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-01-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$" select="normalize-space(substring-after(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="descriptiveMetadata-field">
								<div class="descriptiveMetadata-field-key">
									<xsl:value-of select="$binaryToHtml.ead-c.xml.finaldate" />
								</div>
								<div class="descriptiveMetadata-field-value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-<xsl:value-of select="regex-group(3)" />
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})$" select="normalize-space(substring-after(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="descriptiveMetadata-field">
								<div class="descriptiveMetadata-field-key">
									<xsl:value-of select="$binaryToHtml.ead-c.xml.finaldate" />
								</div>
								<div class="descriptiveMetadata-field-value">
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
							<div class="descriptiveMetadata-field">
								<div class="descriptiveMetadata-field-key">
									<xsl:value-of select="$binaryToHtml.ead-c.xml.initialdate" />
								</div>
								<div class="descriptiveMetadata-field-value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$" select="normalize-space(eadc:did/eadc:unitdate/@normal)">
						<xsl:matching-substring>
							<div class="descriptiveMetadata-field">
								<div class="descriptiveMetadata-field-key">
									<xsl:value-of select="$binaryToHtml.ead-c.xml.initialdate" />
								</div>
								<div class="descriptiveMetadata-field-value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})$" select="normalize-space(eadc:did/eadc:unitdate/@normal)">
						<xsl:matching-substring>
							<div class="descriptiveMetadata-field">
								<div class="descriptiveMetadata-field-key">
									<xsl:value-of select="$binaryToHtml.ead-c.xml.initialdate" />
								</div>
								<div class="descriptiveMetadata-field-value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-01-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$" select="normalize-space(eadc:did/eadc:unitdate/@normal)">
						<xsl:matching-substring>
							<div class="descriptiveMetadata-field">
								<div class="descriptiveMetadata-field-key">
									<xsl:value-of select="$binaryToHtml.ead-c.xml.initialdate" />
								</div>
								<div class="descriptiveMetadata-field-value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-<xsl:value-of select="regex-group(3)" />
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})$" select="normalize-space(eadc:did/eadc:unitdate/@normal)">
						<xsl:matching-substring>
							<div class="descriptiveMetadata-field">
								<div class="descriptiveMetadata-field-key">
									<xsl:value-of select="$binaryToHtml.ead-c.xml.initialdate" />
								</div>
								<div class="descriptiveMetadata-field-value">
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
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.repositorycode" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:did/eadc:unitid/@repositorycode" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:unitid/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.reference" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:did/eadc:unitid/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:origination/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.origination" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:did/eadc:origination/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:acqinfo/eadc:num/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.acquisitionnumber" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:acqinfo/eadc:num/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:acqinfo/eadc:date/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.acquisitiondate" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:acqinfo/eadc:date/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:materialspec/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.materialspecification" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:did/eadc:materialspec/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:physdesc/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.physicaldescription" />
				</div>
				<div class="descriptiveMetadata-field-value">
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
					<div class="descriptiveMetadata-field">
						<div class="descriptiveMetadata-field-key">
							<xsl:value-of select="$binaryToHtml.ead-c.xml.dateofinitialphysicaldescription" />
						</div>
						<div class="descriptiveMetadata-field-value">
							<span class="value">
								<xsl:value-of
									select="substring-before(eadc:did/eadc:physdesc/eadc:date/@normal, '/')" />
							</span>
						</div>
					</div>
					<div class="descriptiveMetadata-field">
						<div class="descriptiveMetadata-field-key">
							<xsl:value-of select="$binaryToHtml.ead-c.xml.dateoffinalphysicaldescription" />
						</div>
						<div class="descriptiveMetadata-field-value">
							<span class="value">
								<xsl:value-of
									select="substring-after(eadc:did/eadc:physdesc/eadc:date/@normal, '/')" />
							</span>
						</div>
					</div>
				</xsl:when>
				<xsl:otherwise>
					<div class="descriptiveMetadata-field">
						<div class="descriptiveMetadata-field-key">
							<xsl:value-of select="$binaryToHtml.ead-c.xml.dateofinitialphysicaldescription" />
						</div>
						<div class="descriptiveMetadata-field-value">
							<span class="value">
								<xsl:value-of select="eadc:did/eadc:physdesc/eadc:date/@normal" />
							</span>
						</div>
					</div>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:physdesc/eadc:dimensions/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.dimensions" />
				</div>
				<div class="descriptiveMetadata-field-value">
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
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.facetorappearance" />
				</div>
				<div class="descriptiveMetadata-field-value">
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
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.extent" />
				</div>
				<div class="descriptiveMetadata-field-value">
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
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.languages" />
				</div>
				<xsl:for-each select="eadc:did/eadc:langmaterial/eadc:language">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="eadc:prefercite/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.quote" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:prefercite/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:bioghist/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.administrativeandbiographicalhistory" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:bioghist/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:bioghist/eadc:chronlist">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.administrativeandbiographicalhistory" />
				</div>
				<div class="descriptiveMetadata-field-value">
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
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.custodialhistory" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:custodhist/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:acqinfo/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.acquisitioninformation" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:acqinfo/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:scopecontent/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.description" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:scopecontent/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:arrangement/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.organizationandordering" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:arrangement/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:arrangement/eadc:table">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.organizationandordering" />
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
				<div class="descriptiveMetadata-field-value">
					<xsl:copy-of select="$output" />
				</div>
			</div>
		</xsl:if>

		<xsl:if test="eadc:appraisal/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.appraisal" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:appraisal/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:accruals/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.accruals" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:accruals/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:phystech/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of
						select="$binaryToHtml.ead-c.xml.physicalcharacteristicsandtechnicalrequirements" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:phystech/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:accessrestrict/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.accessrestrictions" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:accessrestrict/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:userrestrict/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.reproductionrestrictions" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:userrestrict/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:relatedmaterial/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.relatedmaterials" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:relatedmaterial/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:otherfindingaid/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.otherfindaids" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:otherfindingaid/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:note/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.notes" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:note/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:bibliography/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$binaryToHtml.ead-c.xml.bibliography" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:bibliography/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>


















	</xsl:template>


</xsl:stylesheet>