<?xml version="1.0" encoding="UTF-8"?>
<!-- $Id: rodaODToLucene.xslt,v 1.0 2006/11/09 rcastro Exp $ -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:foxml="info:fedora/fedora-system:def/foxml#"
	xmlns:eadc="http://roda.dgarq.gov.pt/2014/EADCSchema"
	xmlns:sparql="http://www.w3.org/2001/sw/DataAccess/rf1/result"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:roda="http://roda.dgarq.gov.pt/#">

	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>

	<xsl:template name="rdfIndexer" match="rdf:RDF">

		<!-- ***  User permissions *** -->
		<xsl:for-each select="rdf:Description/roda:permission-read-user">
			<IndexField IFname="permissions.read.user" index="UN_TOKENIZED" store="YES"
				termVector="NO">
				<xsl:value-of select="text()"/>
			</IndexField>
		</xsl:for-each>

		<!-- ***  Group permissions *** -->
		<xsl:for-each select="rdf:Description/roda:permission-read-group">
			<IndexField IFname="permissions.read.group" index="UN_TOKENIZED" store="YES"
				termVector="NO">
				<xsl:value-of select="text()"/>
			</IndexField>
		</xsl:for-each>

	</xsl:template>

	<xsl:template name="eadcIndexer" match="eadc:ead-c">

		<xsl:param name="pid"/>

		<!-- IDENTIFICATION ZONE -->

		<!-- Complete reference -->
		<IndexField IFname="ead.completereference" index="TOKENIZED" store="YES" termVector="NO">
			
			<xsl:variable name="reference"
				select="document(concat('http://RODACOREHOSTPORT/roda-core/getCompleteReference/', $pid))/completeReference/text()"/>
			
			<xsl:value-of select="$reference"/>

		</IndexField>

		<!-- @level -->
		<xsl:if test="@level">
			<IndexField IFname="ead.level" index="UN_TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="@level"/>
			</IndexField>
		</xsl:if>

		<!-- unitid -->
		<xsl:if test="eadc:did/eadc:unitid/text()">
			<IndexField IFname="ead.unitid" index="UN_TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:did/eadc:unitid/text()"/>
			</IndexField>
		</xsl:if>

		<!-- repositorycode -->
		<xsl:if test="eadc:did/eadc:unitid/@repositorycode">
			<IndexField IFname="ead.repositorycode" index="UN_TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:did/eadc:unitid/@repositorycode"/>
			</IndexField>
		</xsl:if>

		<!-- unittitle -->
		<xsl:if test="eadc:did/eadc:unittitle/text()">
			<IndexField IFname="ead.unittitle" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:did/eadc:unittitle/text()"/>
			</IndexField>
		</xsl:if>
		<!-- abstract -->
		<xsl:if test="eadc:did/eadc:abstract/text()">
			<IndexField IFname="ead.abstract" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:did/eadc:abstract/text()"/>
			</IndexField>
		</xsl:if>

		<!-- unitdate -->
		<xsl:for-each select="eadc:did/eadc:unitdate">
			<xsl:if test="contains(@normal, '/')">
				<IndexField IFname="ead.unitdate.initial" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="translate( substring-before(@normal, '/'), '-', '' )"/>
				</IndexField>
				<IndexField IFname="ead.unitdate.final" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="translate( substring-after(@normal, '/'), '-', '' )"/>
				</IndexField>
			</xsl:if>
			<xsl:if test="not(contains(@normal, '/'))">
				<IndexField IFname="ead.unitdate.initial" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="translate( @normal, '-', '' )"/>
				</IndexField>
				<IndexField IFname="ead.unitdate.final" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="translate( @normal, '-', '' )"/>
				</IndexField>
			</xsl:if>
		</xsl:for-each>

		<!-- physdesc/p -->
		<xsl:for-each select="eadc:did/eadc:physdesc/eadc:p">
			<IndexField IFname="ead.physdesc" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="./text()"/>
			</IndexField>
		</xsl:for-each>

		<!-- physdesc/dimensions -->
		<xsl:for-each select="eadc:did/eadc:physdesc/eadc:dimensions">
			<IndexField IFname="ead.physdesc.dimensions" index="TOKENIZED" store="YES"
				termVector="NO">
				<xsl:value-of select="./text()"/>
			</IndexField>
			<xsl:if test="@unit">
				<IndexField IFname="ead.physdesc.dimensions.unit" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="./@unit"/>
				</IndexField>
			</xsl:if>
		</xsl:for-each>

		<!-- physdesc/physfacet -->
		<xsl:for-each select="eadc:did/eadc:physdesc/eadc:physfacet">
			<IndexField IFname="ead.physdesc.physfacet" index="TOKENIZED" store="YES"
				termVector="NO">
				<xsl:value-of select="./text()"/>
			</IndexField>
			<xsl:if test="@unit">
				<IndexField IFname="ead.physdesc.physfacet.unit" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="./@unit"/>
				</IndexField>
			</xsl:if>
		</xsl:for-each>

		<!-- physdesc/date -->
		<xsl:for-each select="eadc:did/eadc:physdesc/eadc:date">
			<xsl:if test="contains(@normal, '/')">
				<IndexField IFname="ead.physdesc.date.initial" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="translate( substring-before(@normal, '/'), '-', '' )"/>
				</IndexField>
				<IndexField IFname="ead.physdesc.date.final" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="translate( substring-after(@normal, '/'), '-', '' )"/>
				</IndexField>
			</xsl:if>
			<xsl:if test="not(contains(@normal, '/'))">
				<IndexField IFname="ead.physdesc.date.initial" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="translate( @normal, '-', '' )"/>
				</IndexField>
				<IndexField IFname="ead.physdesc.date.final" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="translate( @normal, '-', '' )"/>
				</IndexField>
			</xsl:if>
		</xsl:for-each>

		<!-- physdesc/extent -->
		<xsl:for-each select="eadc:did/eadc:physdesc/eadc:extent">
			<IndexField IFname="ead.physdesc.extent" index="UN_TOKENIZED" store="YES"
				termVector="NO">
				<xsl:value-of select="./text()"/>
			</IndexField>
			<xsl:if test="@unit">
				<IndexField IFname="ead.physdesc.extent.unit" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="./@unit"/>
				</IndexField>
			</xsl:if>
		</xsl:for-each>

		<!-- physdesc/genreform -->
		<xsl:for-each select="eadc:did/eadc:physdesc/eadc:genreform">
			<IndexField IFname="ead.physdesc.genreform" index="TOKENIZED" store="YES"
				termVector="NO">
				<xsl:value-of select="./text()"/>
			</IndexField>
			<xsl:if test="@authfilenumber">
				<IndexField IFname="ead.physdesc.genreform.authfilenumber" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="./@authfilenumber"/>
				</IndexField>
			</xsl:if>
			<xsl:if test="@normal">
				<IndexField IFname="ead.physdesc.genreform.normal" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="./@normal"/>
				</IndexField>
			</xsl:if>
			<xsl:if test="@source">
				<IndexField IFname="ead.physdesc.genreform.source" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="./@source"/>
				</IndexField>
			</xsl:if>
		</xsl:for-each>


		<!-- origination -->
		<xsl:if test="eadc:did/eadc:origination/text()">
			<IndexField IFname="ead.origination" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:did/eadc:origination/text()"/>
			</IndexField>
		</xsl:if>

		<!-- CONTEXT ZONE        -->

		<!-- bioghist/p -->
		<xsl:if test="eadc:bioghist/eadc:p/text()">
			<IndexField IFname="ead.bioghist" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:bioghist/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

		<!-- bioghist/chronlist/chronitem/date -->
		<xsl:for-each select="eadc:bioghist/eadc:chronlist/eadc:chronitem/eadc:date">
			<xsl:if test="contains(@normal, '/')">
				<IndexField IFname="ead.bioghist.chronitem.date.initial" index="UN_TOKENIZED"
					store="YES" termVector="NO">
					<xsl:value-of select="translate( substring-before(@normal, '/'), '-', '' )"/>
				</IndexField>
				<IndexField IFname="ead.bioghist.chronitem.date.final" index="UN_TOKENIZED"
					store="YES" termVector="NO">
					<xsl:value-of select="translate( substring-after(@normal, '/'), '-', '' )"/>
				</IndexField>
			</xsl:if>
			<xsl:if test="not(contains(@normal, '/'))">
				<IndexField IFname="ead.bioghist.chronitem.date.initial" index="UN_TOKENIZED"
					store="YES" termVector="NO">
					<xsl:value-of select="translate( @normal, '-', '' )"/>
				</IndexField>
				<IndexField IFname="ead.bioghist.chronitem.date.final" index="UN_TOKENIZED"
					store="YES" termVector="NO">
					<xsl:value-of select="translate( @normal, '-', '' )"/>
				</IndexField>
			</xsl:if>
		</xsl:for-each>

		<!-- bioghist/chronlist/chronitem//event -->
		<xsl:for-each select="eadc:bioghist/eadc:chronlist/eadc:chronitem//eadc:event/text()">
			<IndexField IFname="ead.bioghist.chronitem.event" index="TOKENIZED" store="YES"
				termVector="NO">
				<xsl:value-of select="."/>
			</IndexField>
		</xsl:for-each>

		<!-- custodhist -->
		<xsl:if test="eadc:custodhist/eadc:p/text()">
			<IndexField IFname="ead.custodhist" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:custodhist/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

		<!-- acqinfo -->
		<xsl:for-each select="eadc:acqinfo">
			<xsl:if test="eadc:p/text()">
				<IndexField IFname="ead.acqinfo" index="TOKENIZED" store="YES" termVector="NO">
					<xsl:value-of select="eadc:p/text()"/>
				</IndexField>
			</xsl:if>
	
			<!-- acqinfo/date -->
			<xsl:for-each select="eadc:p/eadc:date">
				<xsl:if test="contains(@normal, '/')">
					<IndexField IFname="ead.acqinfo.date" index="UN_TOKENIZED" store="YES"
						termVector="NO">
						<xsl:value-of select="translate( substring-before(@normal, '/'), '-', '' )"/>
					</IndexField>
				</xsl:if>
				<xsl:if test="not(contains(@normal, '/'))">
					<IndexField IFname="ead.acqinfo.date" index="UN_TOKENIZED" store="YES"
						termVector="NO">
						<xsl:value-of select="translate( @normal, '-', '' )"/>
					</IndexField>
				</xsl:if>
			</xsl:for-each>
	
			<!-- acqinfo/num and @alterender=FULL_ID -->
			<xsl:if test="@altrender='FULL_ID' and eadc:p/eadc:num/text()">
				<IndexField IFname="ead.acqinfo.num.fullid" index="UN_TOKENIZED" store="YES" termVector="NO">
					<xsl:value-of select="eadc:p/eadc:num/text()"/>
				</IndexField>
			</xsl:if>

			<!-- acqinfo/num -->
			<xsl:if test="eadc:p/eadc:num/text()">
				<IndexField IFname="ead.acqinfo.num" index="TOKENIZED" store="YES" termVector="NO">
					<xsl:value-of select="eadc:p/eadc:num/text()"/>
				</IndexField>
			</xsl:if>
			
			<!-- acqinfo/corpname -->
			<xsl:if test="eadc:p/eadc:corpname/text()">
				<IndexField IFname="ead.acqinfo.corpname" index="TOKENIZED" store="YES" termVector="NO">
					<xsl:value-of select="eadc:p/eadc:corpname/text()"/>
				</IndexField>
			</xsl:if>
		</xsl:for-each>



		<!-- CONTENT ZONE -->
		

		<!-- scopecontent -->
		<xsl:if test="eadc:scopecontent/eadc:p/text()">
			<IndexField IFname="ead.scopecontent" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:scopecontent/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

		<!-- appraisal -->
		<xsl:if test="eadc:appraisal/eadc:p/text()">
			<IndexField IFname="ead.appraisal" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:appraisal/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

		<!-- accruals -->
		<xsl:if test="eadc:accruals/eadc:p/text()">
			<IndexField IFname="ead.accruals" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:accruals/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

		<!-- arrangement -->
		<xsl:for-each select="eadc:arrangement/eadc:table/eadc:tgroup/eadc:tbody/eadc:row">
			<xsl:for-each select="eadc:entry">
				<xsl:variable name="value"><xsl:value-of select="text()"/></xsl:variable>
				<xsl:variable name="position"><xsl:value-of select="position()"/></xsl:variable>
				<xsl:variable name="columnName">
					<xsl:value-of select="../../../eadc:thead/eadc:row/eadc:entry[position()=$position]/text()" />
				</xsl:variable>
				<!-- 
				<IndexField IFname="ead.arrangement.{$columnName}" index="TOKENIZED" store="YES" termVector="NO">
					<xsl:value-of select="$value"/>
				</IndexField>
				-->
				<IndexField IFname="ead.arrangement" index="TOKENIZED" store="YES" termVector="NO">
					<xsl:value-of select="$value"/>
				</IndexField>
			</xsl:for-each>
		</xsl:for-each>

		<!-- ACCESS ZONE -->

		<!-- accessrestrict -->
		<xsl:if test="eadc:accessrestrict/eadc:p/text()">
			<IndexField IFname="ead.accessrestrict" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:accessrestrict/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

		<!-- userestrict -->
		<xsl:if test="eadc:userestrict/eadc:p/text()">
			<IndexField IFname="ead.userestrict" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:userestrict/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

		<!-- did/langmaterial/language -->
		<xsl:for-each select="eadc:did/eadc:langmaterial/eadc:language/text()">
			<IndexField IFname="ead.langmaterial.language" index="TOKENIZED" store="YES"
				termVector="NO">
				<xsl:value-of select="."/>
			</IndexField>
		</xsl:for-each>

		<!-- phystech -->
		<xsl:if test="eadc:phystech/eadc:p/text()">
			<IndexField IFname="ead.phystech" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:phystech/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

		<!-- did/materialspec -->
		<xsl:for-each select="eadc:did/eadc:materialspec">
			<IndexField IFname="ead.materialspec" index="TOKENIZED" store="YES"
				termVector="NO">
				<xsl:value-of select="./text()"/>
			</IndexField>
			<xsl:if test="@label">
				<IndexField IFname="ead.materialspec.label" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="./@label"/>
				</IndexField>
			</xsl:if>
		</xsl:for-each>

		<!-- otherfindaid -->
		<xsl:if test="eadc:otherfindaid/eadc:p/text()">
			<IndexField IFname="ead.otherfindaid" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:otherfindaid/eadc:p/text()"/>
			</IndexField>
		</xsl:if>


		<!-- relatedmaterial -->
		<xsl:for-each select="eadc:relatedmaterial">
			<IndexField IFname="ead.relatedmaterial" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:p/text()"/>
			</IndexField>
			<xsl:for-each select="eadc:archref/eadc:unitid">
				<xsl:variable name="unitIDType" select="./@altrender" />
				<xsl:choose>
	               	<xsl:when test="string($unitIDType)">
	               		<IndexField IFname="ead.relatedmaterial.{$unitIDType}" index="TOKENIZED" store="YES" termVector="NO">
							<xsl:value-of select="text()"/>
						</IndexField>
	               	</xsl:when>
	               	<xsl:otherwise>
	               		<IndexField IFname="ead.relatedmaterial" index="TOKENIZED" store="YES" termVector="NO">
							<xsl:value-of select="text()"/>
						</IndexField>
	               	</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:for-each>
		
		
		

		<!-- bibliography -->
		<xsl:if test="eadc:bibliography/eadc:p/text()">
			<IndexField IFname="ead.bibliography" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:bibliography/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

		<!-- Notas -->

		<xsl:for-each select="eadc:note/eadc:p">
			<IndexField IFname="ead.note" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="text()"/>
			</IndexField>
		</xsl:for-each>

		<xsl:for-each select="eadc:index/eadc:indexentry/eadc:subject">
			<IndexField IFname="ead.index" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="text()"/>
			</IndexField>
		</xsl:for-each>
	

		<!-- Process info -->
		<xsl:if test="eadc:processinfo/eadc:note/eadc:p/text()">
			<IndexField IFname="ead.processinfo.note" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:processinfo/eadc:note/eadc:p/text()"/>
			</IndexField>
		</xsl:if>
		<xsl:for-each select="eadc:processinfo/eadc:p">
			<xsl:variable name="processInfoPType" select="./@altrender" />
			<xsl:for-each select="eadc:archref/eadc:unitid">
				<xsl:variable name="unitIDType" select="./@altrender" />
				<xsl:choose>
                	<xsl:when test="string($processInfoPType)">
                		<xsl:choose>
                			<xsl:when test="string($unitIDType)">
                				<IndexField IFname="ead.processinfo.{$processInfoPType}.{$unitIDType}" index="TOKENIZED" store="YES" termVector="NO">
									<xsl:value-of select="text()"/>
								</IndexField>
                			</xsl:when>
                			<xsl:otherwise>
                				<IndexField IFname="ead.processinfo.{$processInfoPType}" index="TOKENIZED" store="YES" termVector="NO">
									<xsl:value-of select="text()"/>
								</IndexField>
                			</xsl:otherwise>
                		</xsl:choose>
                	</xsl:when>
                	<xsl:otherwise>
                		<xsl:choose>
                			<xsl:when test="string($unitIDType)">
                				<IndexField IFname="ead.processinfo.{$unitIDType}" index="TOKENIZED" store="YES" termVector="NO">
									<xsl:value-of select="text()"/>
								</IndexField>
                			</xsl:when>
                			<xsl:otherwise>
                				<IndexField IFname="ead.processinfo" index="TOKENIZED" store="YES" termVector="NO">
									<xsl:value-of select="text()"/>
								</IndexField>
                			</xsl:otherwise>
                		</xsl:choose>
                	</xsl:otherwise>
                </xsl:choose>
			</xsl:for-each>
		</xsl:for-each>


		<!-- controlaccess -->
		<xsl:for-each select="eadc:controlaccess">
			<xsl:if test="@encodinganalog">
				<IndexField IFname="ead.controlaccess.encodinganalog" index="TOKENIZED" store="YES" termVector="NO">
					<xsl:value-of select="./@encodinganalog"/>
				</IndexField>
			</xsl:if>
			<xsl:if test="./eadc:function/text()">
				<IndexField IFname="ead.controlaccess.function" index="TOKENIZED" store="YES" termVector="NO">
					<xsl:value-of select="./eadc:function/text()"/>
				</IndexField>
			</xsl:if>
			<xsl:if test="./eadc:head/text()">
				<IndexField IFname="ead.controlaccess.head" index="TOKENIZED" store="YES" termVector="NO">
					<xsl:value-of select="./eadc:head/text()"/>
				</IndexField>
			</xsl:if>
			<xsl:if test="./eadc:p/text()">
				<IndexField IFname="ead.controlaccess" index="TOKENIZED" store="YES" termVector="NO">
					<xsl:value-of select="./eadc:p/text()"/>
				</IndexField>
			</xsl:if>
			<xsl:if test="./eadc:subject/text()">
				<IndexField IFname="ead.controlaccess.subject" index="TOKENIZED" store="YES" termVector="NO">
					<xsl:value-of select="./eadc:subject/text()"/>
				</IndexField>
			</xsl:if>
		</xsl:for-each>
		
		<!-- odd -->
		<xsl:if test="eadc:odd/text()">
			<IndexField IFname="ead.odd" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:odd/text()"/>
			</IndexField>
		</xsl:if>
		
		

		<!-- prefercite -->
		<xsl:if test="eadc:prefercite/eadc:p/text()">
			<IndexField IFname="ead.prefercite" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:prefercite/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

	</xsl:template>

</xsl:stylesheet>
