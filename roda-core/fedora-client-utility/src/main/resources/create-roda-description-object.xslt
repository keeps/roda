<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output encoding="UTF-8" method="xml" indent="yes" omit-xml-declaration="yes"/>
	
	<xsl:param name="PID"/>
	<xsl:param name="contentModel"/>
	<xsl:param name="label"/>
	<xsl:param name="state"/>
	
	<xsl:template match="/">
		<foxml:digitalObject
			fedoraxsi:schemaLocation="info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-0.xsd"
			xmlns:audit="info:fedora/fedora-system:def/audit#"
			xmlns:fedoraxsi="http://www.w3.org/2001/XMLSchema-instance"
			xmlns:foxml="info:fedora/fedora-system:def/foxml#">
			
			<xsl:attribute name="PID" select="$PID"/>

			<foxml:objectProperties>
				<foxml:property NAME="http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
					VALUE="FedoraObject"/>
				<foxml:property NAME="info:fedora/fedora-system:def/model#state" VALUE="{$state}"/>
				<foxml:property NAME="info:fedora/fedora-system:def/model#label" VALUE="{$label}"/>
				<foxml:property NAME="info:fedora/fedora-system:def/model#contentModel"
					VALUE="{$contentModel}"/>
			</foxml:objectProperties>

			<foxml:datastream CONTROL_GROUP="X" ID="RELS-EXT" STATE="A" VERSIONABLE="true">
				<foxml:datastreamVersion ID="RELS-EXT.0" LABEL="Relationship Metadata"
					MIMETYPE="text/xml">
					<foxml:contentDigest DIGEST="none" TYPE="DISABLED"/>
					<foxml:xmlContent>RDF_XML_PLACEHOLDER</foxml:xmlContent>
				</foxml:datastreamVersion>
			</foxml:datastream>

			<foxml:datastream CONTROL_GROUP="X" ID="POLICY" STATE="A" VERSIONABLE="true">
				<foxml:datastreamVersion ID="POLICY.0" LABEL="XACML Policy" MIMETYPE="text/xml">
					<foxml:contentDigest DIGEST="none" TYPE="DISABLED"/>
					<foxml:xmlContent>POLICY_XML_PLACEHOLDER</foxml:xmlContent>
				</foxml:datastreamVersion>
			</foxml:datastream>

			<foxml:datastream CONTROL_GROUP="X" ID="EAD-C" STATE="A" VERSIONABLE="true">
				<foxml:datastreamVersion ID="EAD-C.0" LABEL="Encoded Archival Description Component"
					MIMETYPE="text/xml">
					<foxml:contentDigest DIGEST="none" TYPE="DISABLED"/>
					<foxml:xmlContent>EADC_XML_PLACEHOLDER</foxml:xmlContent>
				</foxml:datastreamVersion>
			</foxml:datastream>

		</foxml:digitalObject>
	</xsl:template>

</xsl:stylesheet>
