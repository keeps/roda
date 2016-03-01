/*
 * An XML document type.
 * Localname: linkingEnvironmentIdentifierType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingEnvironmentIdentifierTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one linkingEnvironmentIdentifierType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LinkingEnvironmentIdentifierTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingEnvironmentIdentifierTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public LinkingEnvironmentIdentifierTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGENVIRONMENTIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingEnvironmentIdentifierType");
    
    
    /**
     * Gets the "linkingEnvironmentIdentifierType" element
     */
    public java.lang.String getLinkingEnvironmentIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGENVIRONMENTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "linkingEnvironmentIdentifierType" element
     */
    public org.apache.xmlbeans.XmlString xgetLinkingEnvironmentIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGENVIRONMENTIDENTIFIERTYPE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "linkingEnvironmentIdentifierType" element
     */
    public void setLinkingEnvironmentIdentifierType(java.lang.String linkingEnvironmentIdentifierType)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGENVIRONMENTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(LINKINGENVIRONMENTIDENTIFIERTYPE$0);
            }
            target.setStringValue(linkingEnvironmentIdentifierType);
        }
    }
    
    /**
     * Sets (as xml) the "linkingEnvironmentIdentifierType" element
     */
    public void xsetLinkingEnvironmentIdentifierType(org.apache.xmlbeans.XmlString linkingEnvironmentIdentifierType)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGENVIRONMENTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(LINKINGENVIRONMENTIDENTIFIERTYPE$0);
            }
            target.set(linkingEnvironmentIdentifierType);
        }
    }
}
