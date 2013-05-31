<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output encoding="UTF-8" method="xml" indent="yes"
		standalone="yes" />

	<xsl:param name="contentModel" />
	<xsl:param name="label" />
	<xsl:param name="state" />

	<xsl:template match="/">
		<foxml:digitalObject
			fedoraxsi:schemaLocation="info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-0.xsd"
			xmlns:audit="info:fedora/fedora-system:def/audit#"
			xmlns:fedoraxsi="http://www.w3.org/2001/XMLSchema-instance"
			xmlns:foxml="info:fedora/fedora-system:def/foxml#">
			<foxml:objectProperties>
				<foxml:property
					NAME="http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
					VALUE="FedoraObject" />
				<foxml:property
					NAME="info:fedora/fedora-system:def/model#state" VALUE="{$state}" />
				<foxml:property
					NAME="info:fedora/fedora-system:def/model#label" VALUE="{$label}" />
				<foxml:property
					NAME="info:fedora/fedora-system:def/model#contentModel"
					VALUE="{$contentModel}" />
			</foxml:objectProperties>
		</foxml:digitalObject>
	</xsl:template>

</xsl:stylesheet>
