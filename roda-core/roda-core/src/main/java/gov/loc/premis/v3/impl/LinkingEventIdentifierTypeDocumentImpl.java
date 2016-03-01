/*
 * An XML document type.
 * Localname: linkingEventIdentifierType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingEventIdentifierTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one linkingEventIdentifierType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LinkingEventIdentifierTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingEventIdentifierTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public LinkingEventIdentifierTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGEVENTIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingEventIdentifierType");
    
    
    /**
     * Gets the "linkingEventIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getLinkingEventIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LINKINGEVENTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "linkingEventIdentifierType" element
     */
    public void setLinkingEventIdentifierType(gov.loc.premis.v3.StringPlusAuthority linkingEventIdentifierType)
    {
        generatedSetterHelperImpl(linkingEventIdentifierType, LINKINGEVENTIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "linkingEventIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewLinkingEventIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(LINKINGEVENTIDENTIFIERTYPE$0);
            return target;
        }
    }
}
