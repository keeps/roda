<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet  [
	<!ENTITY crarr  "&#13;">
	<!ENTITY crarr  "&#xD;">
]>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ead="urn:isbn:1-931666-22-9" exclude-result-prefixes="ead">
	<xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="yes"/>

	<xsl:param name="i18n.identityarea"/>
	<xsl:param name="i18n.reference"/>
	<xsl:param name="i18n.title"/>
	<xsl:param name="i18n.initialdate"/>
	<xsl:param name="i18n.finaldate"/>
	<xsl:param name="i18n.unitdate"/>
	<xsl:param name="i18n.countrycode"/>
	<xsl:param name="i18n.level"/>
	<xsl:param name="i18n.materialspec"/>
	<xsl:param name="i18n.origination"/>
	<xsl:param name="i18n.physicaldescription"/>
	<xsl:param name="i18n.extent"/>
	<xsl:param name="i18n.dimensions"/>
	<xsl:param name="i18n.physfacet"/>
	<xsl:param name="i18n.quote"/>


	<xsl:param name="i18n.contextarea"/>
	<xsl:param name="i18n.bioghist"/>
	<xsl:param name="i18n.creator"/>
	<xsl:param name="i18n.producer"/>
	<xsl:param name="i18n.repositorycode"/>
	<xsl:param name="i18n.repository"/>
	<xsl:param name="i18n.custodialhistory"/>
	<xsl:param name="i18n.acquisitioninformation"/>


	<xsl:param name="i18n.contentarea"/>
	<xsl:param name="i18n.description"/>
	<xsl:param name="i18n.appraisal"/>
	<xsl:param name="i18n.accruals"/>
	<xsl:param name="i18n.arrangement"/>


	<xsl:param name="i18n.accessarea"/>
	<xsl:param name="i18n.administrativeandbiographicalhistory"/>
	<xsl:param name="i18n.accessrestrictions"/>
	<xsl:param name="i18n.userestrict"/>
	<xsl:param name="i18n.languages"/>
	<xsl:param name="i18n.languageScriptNotes"/>
	<xsl:param name="i18n.phystech"/>


	<xsl:param name="i18n.alliedarea"/>
	<xsl:param name="i18n.originalsloc"/>
	<xsl:param name="i18n.altformavail"/>
	<xsl:param name="i18n.relatedmaterials"/>
	<xsl:param name="i18n.bibliography"/>
	<xsl:param name="i18n.otherfindaids"/>


	<xsl:param name="i18n.notesarea"/>
	<xsl:param name="i18n.notes"/>


	<xsl:param name="i18n.descriptioncontrolarea"/>
	<xsl:param name="i18n.rules"/>

	<xsl:param name="i18n.statusdescription"/>
	<xsl:param name="i18n.statusdescription.final"/>
	<xsl:param name="i18n.statusdescription.revised"/>
	<xsl:param name="i18n.statusdescription.draft"/>


	<xsl:param name="i18n.levelofdetail"/>
	<xsl:param name="i18n.levelofdetail.full"/>
	<xsl:param name="i18n.levelofdetail.partial"/>
	<xsl:param name="i18n.levelofdetail.minimal"/>

	<xsl:param name="i18n.processdates"/>
	<xsl:param name="i18n.sources"/>
	<xsl:param name="i18n.archivistNotes"/>

	<!-- Standard description levels -->
	<xsl:param name="i18n.level.fonds" />
	<xsl:param name="i18n.level.class" />
	<xsl:param name="i18n.level.collection" />
	<xsl:param name="i18n.level.recordgrp" />
	<xsl:param name="i18n.level.subgrp" />
	<xsl:param name="i18n.level.subfonds" />
	<xsl:param name="i18n.level.series" />
	<xsl:param name="i18n.level.subseries" />
	<xsl:param name="i18n.level.file" />
	<xsl:param name="i18n.level.item" />


	<!-- Portuguese National Archives description levels -->
	<xsl:param name="i18n.level.P" />
	<xsl:param name="i18n.level.F" />
	<xsl:param name="i18n.level.SF" />
	<xsl:param name="i18n.level.SSF" />
	<xsl:param name="i18n.level.C" />
	<xsl:param name="i18n.level.CL" />
	<xsl:param name="i18n.level.SCL" />
	<xsl:param name="i18n.level.SSCL" />
	<xsl:param name="i18n.level.SC" />
	<xsl:param name="i18n.level.SSC" />
	<xsl:param name="i18n.level.SSSC" />
	<xsl:param name="i18n.level.SR" />
	<xsl:param name="i18n.level.SSR" />
	<xsl:param name="i18n.level.SSSR" />
	<xsl:param name="i18n.level.UI" />
	<xsl:param name="i18n.level.SUI" />
	<xsl:param name="i18n.level.SSUI" />
	<xsl:param name="i18n.level.AG" />
	<xsl:param name="i18n.level.DC" />
	<xsl:param name="i18n.level.DS" />
	<xsl:param name="i18n.level.D" />



	<!-- Translation maps to be used by templates  -->
	<xsl:variable name="statusDescriptionTranslationMap">
		<entry key="final"><xsl:value-of select="$i18n.statusdescription.final"/></entry>
		<entry key="revised"><xsl:value-of select="$i18n.statusdescription.revised"/></entry>
		<entry key="draft"><xsl:value-of select="$i18n.statusdescription.draft"/></entry>
	</xsl:variable>


	<xsl:variable name="levelOfDetailTranslationMap">
		<entry key="full"><xsl:value-of select="$i18n.levelofdetail.full"/></entry>
		<entry key="partial"><xsl:value-of select="$i18n.levelofdetail.partial"/></entry>
		<entry key="minimal"><xsl:value-of select="$i18n.levelofdetail.minimal"/></entry>
	</xsl:variable>


	<xsl:variable name="descriptionLevelTranslationMap">
		<entry key="fonds"><xsl:value-of select="$i18n.level.fonds"/></entry>
		<entry key="class"><xsl:value-of select="$i18n.level.class"/></entry>
		<entry key="collection"><xsl:value-of select="$i18n.level.collection"/></entry>
		<entry key="recordgrp"><xsl:value-of select="$i18n.level.recordgrp"/></entry>
		<entry key="recordgrp"><xsl:value-of select="$i18n.level.recordgrp"/></entry>
		<entry key="subgrp"><xsl:value-of select="$i18n.level.subgrp"/></entry>
		<entry key="subfonds"><xsl:value-of select="$i18n.level.subfonds"/></entry>
		<entry key="series"><xsl:value-of select="$i18n.level.series"/></entry>
		<entry key="subseries"><xsl:value-of select="$i18n.level.subseries"/></entry>
		<entry key="file"><xsl:value-of select="$i18n.level.file"/></entry>
		<entry key="item"><xsl:value-of select="$i18n.level.item"/></entry>

		<entry key="P"><xsl:value-of select="$i18n.level.P"/></entry>
		<entry key="C"><xsl:value-of select="$i18n.level.C"/></entry>
		<entry key="CL"><xsl:value-of select="$i18n.level.CL"/></entry>
		<entry key="SCL"><xsl:value-of select="$i18n.level.SCL"/></entry>
		<entry key="SSCL"><xsl:value-of select="$i18n.level.SSCL"/></entry>
		<entry key="F"><xsl:value-of select="$i18n.level.F"/></entry>
		<entry key="SF"><xsl:value-of select="$i18n.level.SF"/></entry>
		<entry key="SSF"><xsl:value-of select="$i18n.level.SSF"/></entry>
		<entry key="SC"><xsl:value-of select="$i18n.level.SC"/></entry>
		<entry key="SSC"><xsl:value-of select="$i18n.level.SSC"/></entry>
		<entry key="SSSC"><xsl:value-of select="$i18n.level.SSSC"/></entry>
		<entry key="SR"><xsl:value-of select="$i18n.level.SR"/></entry>
		<entry key="SSR"><xsl:value-of select="$i18n.level.SSR"/></entry>
		<entry key="SSSR"><xsl:value-of select="$i18n.level.SSSR"/></entry>
		<entry key="UI"><xsl:value-of select="$i18n.level.UI"/></entry>
		<entry key="SUI"><xsl:value-of select="$i18n.level.SUI"/></entry>
		<entry key="SSUI"><xsl:value-of select="$i18n.level.SSUI"/></entry>
		<entry key="AG"><xsl:value-of select="$i18n.level.AG"/></entry>
		<entry key="DC"><xsl:value-of select="$i18n.level.DC"/></entry>
		<entry key="DS"><xsl:value-of select="$i18n.level.DS"/></entry>
		<entry key="D"><xsl:value-of select="$i18n.level.D"/></entry>
	</xsl:variable>


	<xsl:template match="/">
		<div class="descriptiveMetadata">


			<xsl:if
				test="
					/ead:ead/ead:archdesc/ead:did/*:unitid/text() |
					/ead:ead/ead:archdesc/@level |
					/ead:ead//ead:did/ead:unittitle/text() |
					/ead:ead/ead:did/ead:unitdate/@normal |
					/ead:ead//ead:did/ead:unitdate/text() |
					/ead:ead//ead:did/ead:unitid/@countrycode |
					/ead:ead/ead:did/ead:unitid/@repositorycode |
					/ead:ead/ead:archdesc/ead:did/ead:repository/ead:corpname/text() |
					/ead:ead/ead:archdesc/ead:did/ead:physdesc/ead:dimensions/text() |
					/ead:ead/ead:archdesc/ead:did/ead:physdesc/ead:extent/text() |
					/ead:ead/ead:archdesc/ead:did/ead:materialspec/text()">

				<div class="form-separator">
					<xsl:value-of select="$i18n.identityarea"/>
				</div>


				<!-- Unit id -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.reference"/>
					<xsl:with-param name="value" select="/*:ead/*:archdesc/*:did/*:unitid/text()"/>
				</xsl:call-template>

				<!-- Description level -->
				<xsl:call-template name="descriptionLevel"/>


				<!-- Unit title -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.title"/>
					<xsl:with-param name="value" select="/ead:ead//ead:did/ead:unittitle/text()"/>
				</xsl:call-template>


				<!-- Range dates-->
				<xsl:call-template name="UnitDates"/>

				<!-- Descriptive date -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.unitdate"/>
					<xsl:with-param name="value" select="/ead:ead//ead:did/ead:unitdate/text()"/>
				</xsl:call-template>

				<!-- Country code -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.countrycode"/>
					<xsl:with-param name="value"
						select="/*:ead/*:archdesc/*:did/*:unitid/@countrycode"/>
				</xsl:call-template>

				<!-- Repository code -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.repositorycode"/>
					<xsl:with-param name="value"
						select="/ead:ead/ead:archdesc/ead:did/ead:unitid/@repositorycode"/>
				</xsl:call-template>

				<!-- Repository name -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.repository"/>
					<xsl:with-param name="value"
						select="/*:ead/*:archdesc/*:did/*:repository/*:corpname/text()"/>
				</xsl:call-template>

				<!-- Dimensions -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.dimensions"/>
					<xsl:with-param name="value"
						select="/ead:ead/ead:archdesc/ead:did/ead:physdesc/ead:dimensions/text()"/>
				</xsl:call-template>

				<!-- Extent -->
				<xsl:if test="/ead:ead/ead:archdesc/ead:did/ead:physdesc/ead:extent/text()">
					<xsl:call-template name="simpleField">
						<xsl:with-param name="label" select="$i18n.extent"/>
						<xsl:with-param name="value"
							select="string-join((/ead:ead/ead:archdesc/ead:did/ead:physdesc/ead:extent/text(),/ead:ead/ead:archdesc/ead:did/ead:physdesc/ead:extent/@unit), ' ')" />
					</xsl:call-template>
				</xsl:if>
				<!-- Material specification -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.materialspec"/>
					<xsl:with-param name="value"
						select="/ead:ead/ead:archdesc/ead:did/ead:materialspec/text()"/>
				</xsl:call-template>


			</xsl:if>
			<!-- closes Identity Area -->



			<!-- Opens context area -->
			<xsl:if
				test="
					/*:ead/*:archdesc/*:did/*:origination[@label = 'creator']/*:name/text() |
					/*:ead/*:archdesc/*:did/*:origination[@label = 'producer']/*:name/text() |
					/*:ead/*:archdesc/*:bioghist/*:p/text() |
					/*:ead/*:archdesc/*:custodhist/*:p/text() |
					/*:ead/*:archdesc/*:acqinfo/*:p/text()">


				<div class="form-separator">
					<xsl:value-of select="$i18n.contextarea"/>
				</div>




				<!-- Creator -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.creator"/>
					<xsl:with-param name="value"
						select="/*:ead/*:archdesc/*:did/*:origination[@label = 'creator']/*:name/text()"
					/>
				</xsl:call-template>

				<!-- Producer  -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.producer"/>
					<xsl:with-param name="value"
						select="/*:ead/*:archdesc/*:did/*:origination[@label = 'producer']/*:name/text()"
					/>
				</xsl:call-template>


				<!-- Administrative and biographical history -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.bioghist"/>
					<xsl:with-param name="value" select="/*:ead/*:archdesc/*:bioghist/*:p/text()"/>
				</xsl:call-template>


				<!-- Custodial history -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.custodialhistory"/>
					<xsl:with-param name="value" select="/*:ead/*:archdesc/*:custodhist/*:p/text()"
					/>
				</xsl:call-template>

				<!--  Immediate source of acquisition or transfer -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.acquisitioninformation"/>
					<xsl:with-param name="value" select="/*:ead/*:archdesc/*:acqinfo/*:p/text()"/>
				</xsl:call-template>


			</xsl:if>
			<!-- Closes context area -->


			<!-- Opens content area -->
			<xsl:if
				test="
					/*:ead/*:archdesc/*:scopecontent/*:p/text() |
					/*:ead/*:archdesc/*:appraisal/*:p/text() |
					/*:ead/*:archdesc/*:accruals/*:p/text() |
					/*:ead/*:archdesc/*:arrangement/*:p/text()">


				<div class="form-separator">
					<xsl:value-of select="$i18n.contentarea"/>
				</div>

				<!--  Scope and content -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.description"/>
					<xsl:with-param name="value"
						select="/*:ead/*:archdesc/*:scopecontent/*:p/text()"/>
				</xsl:call-template>


				<!--  Appraisal, destruction and scheduling -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.appraisal"/>
					<xsl:with-param name="value" select="/*:ead/*:archdesc/*:appraisal/*:p/text()"/>
				</xsl:call-template>


				<!--  Acruals -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.accruals"/>
					<xsl:with-param name="value" select="/*:ead/*:archdesc/*:accruals/*:p/text()"/>
				</xsl:call-template>


				<!--  System of arrangement -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.arrangement"/>
					<xsl:with-param name="value" select="/*:ead/*:archdesc/*:arrangement/*:p/text()"
					/>
				</xsl:call-template>



			</xsl:if>
			<!-- Closes content area -->



			<!-- Opens access area -->
			<xsl:if
				test="
					/*:ead/*:archdesc/*:accessrestrict/*:p/text() |
					/*:ead/*:archdesc/*:userestrict/*:p/text() |
					/*:ead/*:archdesc/*:did/*:langmaterial/*:language |
					/*:ead/*:archdesc/*:did/*:langmaterial">


				<div class="form-separator">
					<xsl:value-of select="$i18n.accessarea"/>
				</div>



				<!-- Conditions governing access -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.accessrestrictions"/>
					<xsl:with-param name="value"
						select="/*:ead/*:archdesc/*:accessrestrict/*:p/text()"/>
				</xsl:call-template>



				<!--  Conditions governing reproduction -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.userestrict"/>
					<xsl:with-param name="value" select="/*:ead/*:archdesc/*:userestrict/*:p/text()"
					/>
				</xsl:call-template>



				<!--   -->
				<xsl:call-template name="listField">
					<xsl:with-param name="label" select="$i18n.languages"/>
					<xsl:with-param name="value"
						select="/*:ead/*:archdesc/*:did/*:langmaterial/*:language"/>
				</xsl:call-template>



				<!-- Language and script notes  -->
				<xsl:call-template name="listField">
					<xsl:with-param name="label" select="$i18n.languageScriptNotes"/>
					<xsl:with-param name="value" select="/*:ead/*:archdesc/*:did/*:langmaterial"/>
				</xsl:call-template>



				<!-- Physical Characteristics and technical requirements  -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.phystech"/>
					<xsl:with-param name="value" select="/*:ead/*:archdesc/*:phystech/*:p/text()"/>
				</xsl:call-template>



			</xsl:if>
			<!-- Closes access area -->




			<!-- Opens allied materials area -->
			<xsl:if
				test="
					/*:ead/*:archdesc/*:originalsloc/*:p/text() |
					/*:ead/*:archdesc/*:altformavail/*:p/text() |
					/*:ead/*:archdesc/*:relatedmaterial/*:p/text() |
					/*:ead/*:archdesc/*:otherfindaid/*:p/text() |
					/*:ead/*:archdesc/*:bibliography/*:p/text()">


				<div class="form-separator">
					<xsl:value-of select="$i18n.alliedarea"/>
				</div>

				<!-- Existence and location of originals -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.originalsloc"/>
					<xsl:with-param name="value"
						select="/*:ead/*:archdesc/*:originalsloc/*:p/text()"/>
				</xsl:call-template>


				<!--  -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.altformavail"/>
					<xsl:with-param name="value"
						select="/*:ead/*:archdesc/*:altformavail/*:p/text()"/>
				</xsl:call-template>


				<!-- Related units of description -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.relatedmaterials"/>
					<xsl:with-param name="value"
						select="/*:ead/*:archdesc/*:relatedmaterial/*:p/text()"/>
				</xsl:call-template>


				<!-- Other finding aids -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.otherfindaids"/>
					<xsl:with-param name="value"
						select="/*:ead/*:archdesc/*:otherfindaid/*:p/text()"/>
				</xsl:call-template>

				<!-- Publication notes -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.bibliography"/>
					<xsl:with-param name="value"
						select="/*:ead/*:archdesc/*:bibliography/*:p/text()"/>
				</xsl:call-template>

			</xsl:if>
			<!-- Closes allied materials area -->




			<!-- Opens notes area -->
			<xsl:if test="/*:ead/*:archdesc/*:did/*:note[@type = 'generalNote']/*:p/text()">
				<div class="form-separator">
					<xsl:value-of select="$i18n.notesarea"/>
				</div>

				<!-- Notes -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.notes"/>
					<xsl:with-param name="value"
						select="/*:ead/*:archdesc/*:did/*:note[@type = 'generalNote']/*:p/text()"/>
				</xsl:call-template>

			</xsl:if>
			<!-- Closes notes area -->


			<!-- Opens description control area -->
			<xsl:if
				test="
				/*:ead/*:eadheader/*:profiledesc/*:descrules/text() |
				/*:ead/*:archdesc/*:odd[@type = 'statusDescription']/*:p/text() |
				/*:ead/*:archdesc/*:odd[@type = 'levelOfDetail']/*:p/text() |
				/*:ead/*:archdesc/*:processinfo/*:p/*:date/text() |
				/*:ead/*:archdesc/*:did/*:note[@type = 'sourcesDescription']/*:p/text() |
				/*:ead/*:archdesc/*:processinfo/*:p/text()">
				<div class="form-separator">
					<xsl:value-of select="$i18n.descriptioncontrolarea"/>
				</div>

				<!-- Rules or conventions -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.rules"/>
					<xsl:with-param name="value"
						select="/*:ead/*:eadheader/*:profiledesc/*:descrules/text()"/>
				</xsl:call-template>



				<!-- Status of description -->
				<xsl:if test="/*:ead/*:archdesc/*:odd[@type = 'statusDescription']/*:p/text()">
					<xsl:variable name="statusDescriptionTranslated">
						<xsl:variable name="statusDescriptionValue">
							<xsl:value-of select="/*:ead/*:archdesc/*:odd[@type = 'statusDescription']/*:p/text()"/>
						</xsl:variable>
						<xsl:value-of select="$statusDescriptionTranslationMap/entry[@key=$statusDescriptionValue]/text()"/>
					</xsl:variable>

					<xsl:call-template name="simpleField">
						<xsl:with-param name="label" select="$i18n.statusdescription"/>
						<xsl:with-param name="value" select="$statusDescriptionTranslated"/>
					</xsl:call-template>
				</xsl:if>
<!--
				$statusDescriptionValue = <xsl:value-of select="$statusDescriptionValue"/>
				$statusDescriptionTranslated = <xsl:value-of select="$statusDescriptionTranslated"/>
				levelOfDetailValue = <xsl:value-of select="$levelOfDetailValue"/>
				$levelOfDetailTranslated = <xsl:value-of select="levelOfDetailTranslated"/>
-->

				<!-- Level of detail -->
				<xsl:if test="/*:ead/*:archdesc/*:odd[@type = 'levelOfDetail']/*:p/text()">
					<xsl:variable name="levelOfDetailTranslated">
						<xsl:variable name="levelOfDetailValue">
							<xsl:value-of select="/*:ead/*:archdesc/*:odd[@type = 'levelOfDetail']/*:p/text()"/>
						</xsl:variable>
						<xsl:value-of select="$levelOfDetailTranslationMap/entry[@key=$levelOfDetailValue]/text()"/>
					</xsl:variable>


					<xsl:call-template name="simpleField">
						<xsl:with-param name="label" select="$i18n.levelofdetail"/>
						<xsl:with-param name="value"
							select="$levelOfDetailTranslated"/>
					</xsl:call-template>
				</xsl:if>
				<!-- Date of creation or revision -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.processdates"/>
					<xsl:with-param name="value"
						select="/*:ead/*:archdesc/*:processinfo/*:p/*:date/text()"/>
				</xsl:call-template>

				<!-- Sources -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.sources"/>
					<xsl:with-param name="value"
						select="/*:ead/*:archdesc/*:did/*:note[@type='sourcesDescription']/*:p/text()"
					/>
				</xsl:call-template>

				<!-- Archivist notes -->
				<xsl:call-template name="simpleField">
					<xsl:with-param name="label" select="$i18n.archivistNotes"/>
					<xsl:with-param name="value" select="/*:ead/*:archdesc/*:processinfo/*:p/text()"
					/>
				</xsl:call-template>

			</xsl:if>
			<!-- Closes description control area -->

		</div>
	</xsl:template>








	<!-- ************************ -->
	<!-- * Auxiliary templates  * -->
	<!-- ************************ -->


	<!-- This template handles a simple key value attributes. Values are provided as XPath or String -->
	<xsl:template name="simpleField">
		<xsl:param name="label"/>
		<xsl:param name="value"/>

		<xsl:if test="$value">
			<div class="field">
				<div class="label">
					<xsl:choose>
						<xsl:when test="$label">
							<xsl:value-of select="$label"/>
						</xsl:when>
						<xsl:otherwise>i18n.key missing or not found</xsl:otherwise>
					</xsl:choose>

				</div>
				<div class="value prewrap">
					<xsl:value-of select="$value"/>
				</div>
			</div>
		</xsl:if>
	</xsl:template>


	<!-- This template handles attributes whose values can be lists and renders values in one line separated by commas-->
	<xsl:template name="listField">
		<xsl:param name="label"/>
		<xsl:param name="value"/>

		<xsl:if test="$value">
			<div class="field">
				<div class="label">
					<xsl:choose>
						<xsl:when test="$label">
							<xsl:value-of select="$label"/>
						</xsl:when>
						<xsl:otherwise>i18n.key missing or not found</xsl:otherwise>
					</xsl:choose>

				</div>
				<xsl:for-each select="$value">
					<xsl:if test="normalize-space(string-join(text(), '')) != ''">
						<div class="value">
							<xsl:value-of select="text()"/>
						</div>
					</xsl:if>
				</xsl:for-each>
			</div>
		</xsl:if>
	</xsl:template>


	<!-- This template handles description levels and their translation -->
	<xsl:template name="descriptionLevel">
		<xsl:if test="//ead:archdesc/@level">


			<xsl:variable name="descriptionLevel">
				<xsl:choose>
					<xsl:when test="/ead:ead/ead:archdesc/@level='otherlevel'">
						<xsl:variable name="levelCode">
							<xsl:value-of select="/*:ead/*:archdesc/@otherlevel"/>
						</xsl:variable>
						<xsl:value-of select="$descriptionLevelTranslationMap/entry[@key=$levelCode]/text()"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="levelCode">
							<xsl:value-of select="/*:ead/*:archdesc/@level"/>
						</xsl:variable>

						<xsl:value-of select="$descriptionLevelTranslationMap/entry[@key=$levelCode]/text()"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>

			<xsl:call-template name="simpleField">
				<xsl:with-param name="label" select="$i18n.level"></xsl:with-param>
				<xsl:with-param name="value"><xsl:value-of select="$descriptionLevel"></xsl:value-of> </xsl:with-param>
			</xsl:call-template>
		</xsl:if>

	</xsl:template>

	<!-- This template handles rage dates -->
	<!-- original template made by sleroux -->
	<xsl:template name="UnitDates">
		<xsl:if test="/ead:ead/ead:archdesc/ead:did/ead:unitdate/@normal">
			<xsl:choose>
				<xsl:when test="contains(/ead:ead/ead:archdesc/ead:did/ead:unitdate/@normal, '/')">
					<!-- initial/final -->
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.initialdate"/>
						</div>
						<div class="value">
							<span class="value">
								<xsl:value-of
									select="normalize-space(substring-before(/ead:ead/ead:archdesc/ead:did/ead:unitdate/@normal, '/'))"
								/>
							</span>
						</div>
					</div>
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.finaldate"/>
						</div>
						<div class="value">
							<span class="value">
								<xsl:value-of
									select="normalize-space(substring-after(/ead:ead/ead:archdesc/ead:did/ead:unitdate/@normal, '/'))"
								/>
							</span>
						</div>
					</div>
				</xsl:when>
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when
							test="/ead:ead/ead:archdesc/ead:did/ead:unitdate[@label = 'UnitDateInitial']">
							<!-- initial date. internal 'hack' -->
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate"/>
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of
											select="normalize-space(/ead:ead/ead:archdesc/ead:did/ead:unitdate/@normal)"
										/>
									</span>
								</div>
							</div>
						</xsl:when>
						<xsl:when
							test="/ead:ead/ead:archdesc/ead:did/ead:unitdate[@label = 'UnitDateFinal']">
							<!-- final date. internal 'hack' -->
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.finaldate"/>
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of
											select="normalize-space(/ead:ead/ead:archdesc/ead:did/ead:unitdate/@normal)"
										/>
									</span>
								</div>
							</div>
						</xsl:when>
						<xsl:otherwise>
							<!-- fallback to date initial -->
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate"/>
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of
											select="normalize-space(/ead:ead/ead:archdesc/ead:did/ead:unitdate/@normal)"
										/>
									</span>
								</div>
							</div>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
