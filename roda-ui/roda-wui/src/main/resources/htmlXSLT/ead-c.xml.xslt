<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:eadc="http://roda.dgarq.gov.pt/2008/EADCSchema"
	exclude-result-prefixes="eadc">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />

	<xsl:param name="eadcxmlead" />
	<xsl:param name="eadcxmltitle" />
	<xsl:param name="eadcxmllevel" />
	<xsl:param name="eadcxmlinitialdate" />
	<xsl:param name="eadcxmlfinaldate" />
	<xsl:param name="eadcxmlrepositorycode" />
	<xsl:param name="eadcxmlreference" />
	<xsl:param name="eadcxmlacquisitionnumber" />
	<xsl:param name="eadcxmlorigination" />
	<xsl:param name="eadcxmlacquisitiondate" />
	<xsl:param name="eadcxmlmaterialspecification" />
	<xsl:param name="eadcxmlphysicaldescription" />
	<xsl:param name="eadcxmldateofinitialphysicaldescription" />
	<xsl:param name="eadcxmldateoffinalphysicaldescription" />
	<xsl:param name="eadcxmldimensions" />
	<xsl:param name="eadcxmlfacetorappearance" />
	<xsl:param name="eadcxmlextent" />
	<xsl:param name="eadcxmllanguages" />
	<xsl:param name="eadcxmlquote" />
	<xsl:param name="eadcxmladministrativeandbiographicalhistory" />
	<xsl:param name="eadcxmlcustodialhistory" />
	<xsl:param name="eadcxmlacquisitioninformation" />
	<xsl:param name="eadcxmldescription" />
	<xsl:param name="eadcxmlorganizationandordering" />
	<xsl:param name="eadcxmlappraisal" />
	<xsl:param name="eadcxmlaccruals" />
	<xsl:param name="eadcxmlphysicalcharacteristicsandtechnicalrequirements" />
	<xsl:param name="eadcxmlaccessrestrictions" />
	<xsl:param name="eadcxmlreproductionrestrictions" />
	<xsl:param name="eadcxmlrelatedmaterials" />
	<xsl:param name="eadcxmlotherfindaids" />
	<xsl:param name="eadcxmlnotes" />
	<xsl:param name="eadcxmlbibliography" />
	<xsl:template match="/">
		<div class="descriptiveMetadata">
			<div class='title'>
				<xsl:value-of select="$eadcxmlead" />
			</div>
			<xsl:apply-templates />
		</div>
	</xsl:template>
	<xsl:template match="eadc:ead-c">
		<!-- COMPLETE REFERENCE -->
		<!-- HANDLE -->
		<xsl:if test="eadc:did/eadc:unittitle/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmltitle" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:did/eadc:unittitle/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="@level">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmllevel" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="@level" />
				</div>
			</div>
		</xsl:if>
		<!-- HANDLE DATE BETTER??? -->
		<xsl:if test="eadc:did/eadc:unitdate/@normal">
			<xsl:choose>
				<xsl:when test="contains(eadc:did/eadc:unitdate/@normal, '/')">
					<div class="descriptiveMetadata-field">
						<div class="descriptiveMetadata-field-key">
							<xsl:value-of select="$eadcxmlinitialdate" />
						</div>
						<div class="descriptiveMetadata-field-value">
							<span class="value">
								<xsl:value-of
									select="substring-before(eadc:did/eadc:unitdate/@normal, '/')" />
							</span>
						</div>
					</div>
					<div class="descriptiveMetadata-field">
						<div class="descriptiveMetadata-field-key">
							<xsl:value-of select="$eadcxmlfinaldate" />
						</div>
						<div class="descriptiveMetadata-field-value">
							<span class="value">
								<xsl:value-of
									select="substring-after(eadc:did/eadc:unitdate/@normal, '/')" />
							</span>
						</div>
					</div>
				</xsl:when>
				<xsl:otherwise>
					<div class="descriptiveMetadata-field">
						<div class="descriptiveMetadata-field-key">
							<xsl:value-of select="$eadcxmlinitialdate" />
						</div>
						<div class="descriptiveMetadata-field-value">
							<span class="value">
								<xsl:value-of select="eadc:did/eadc:unitdate/@normal" />
							</span>
						</div>
					</div>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		<!-- COUNTRY CODE -->
		<xsl:if test="eadc:did/eadc:unitid/@repositorycode">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmlrepositorycode" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:did/eadc:unitid/@repositorycode" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:unitid/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmlreference" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:did/eadc:unitid/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:origination/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmlorigination" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:did/eadc:origination/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:acqinfo/eadc:num/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmlacquisitionnumber" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:acqinfo/eadc:num/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:acqinfo/eadc:date/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmlacquisitiondate" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:acqinfo/eadc:date/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:materialspec/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmlmaterialspecification" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:did/eadc:materialspec/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:physdesc/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmlphysicaldescription" />
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
							<xsl:value-of select="$eadcxmldateofinitialphysicaldescription" />
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
							<xsl:value-of select="$eadcxmldateoffinalphysicaldescription" />
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
							<xsl:value-of select="$eadcxmldateofinitialphysicaldescription" />
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
					<xsl:value-of select="$eadcxmldimensions" />
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
					<xsl:value-of select="$eadcxmlfacetorappearance" />
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
					<xsl:value-of select="$eadcxmlextent" />
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
					<xsl:value-of select="$eadcxmllanguages" />
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
					<xsl:value-of select="$eadcxmlquote" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:prefercite/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:bioghist/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmladministrativeandbiographicalhistory" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:bioghist/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:bioghist/eadc:chronlist">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmladministrativeandbiographicalhistory" />
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
					<xsl:value-of select="$eadcxmlcustodialhistory" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:custodhist/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:acqinfo/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmlacquisitioninformation" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:acqinfo/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:scopecontent/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmldescription" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:scopecontent/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:arrangement/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmlorganizationandordering" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:arrangement/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:arrangement/eadc:table">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmlorganizationandordering" />
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
					<xsl:value-of select="$eadcxmlappraisal" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:appraisal/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:accruals/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmlaccruals" />
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
						select="$eadcxmlphysicalcharacteristicsandtechnicalrequirements" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:phystech/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:accessrestrict/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmlaccessrestrictions" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:accessrestrict/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:userrestrict/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmlreproductionrestrictions" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:userrestrict/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:relatedmaterial/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmlrelatedmaterials" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:relatedmaterial/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:otherfindingaid/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmlotherfindaids" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:otherfindingaid/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:note/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmlnotes" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:note/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:bibliography/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$eadcxmlbibliography" />
				</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:bibliography/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>


















	</xsl:template>


</xsl:stylesheet>