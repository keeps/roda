/*
 * An XML document type.
 * Localname: linkingObjectIdentifierType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingObjectIdentifierTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one linkingObjectIdentifierType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LinkingObjectIdentifierTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingObjectIdentifierTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public LinkingObjectIdentifierTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGOBJECTIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingObjectIdentifierType");
    
    
    /**
     * Gets the "linkingObjectIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getLinkingObjectIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LINKINGOBJECTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "linkingObjectIdentifierType" element
     */
    public void setLinkingObjectIdentifierType(gov.loc.premis.v3.StringPlusAuthority linkingObjectIdentifierType)
    {
        generatedSetterHelperImpl(linkingObjectIdentifierType, LINKINGOBJECTIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "linkingObjectIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewLinkingObjectIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(LINKINGOBJECTIDENTIFIERTYPE$0);
            return target;
        }
    }
}
