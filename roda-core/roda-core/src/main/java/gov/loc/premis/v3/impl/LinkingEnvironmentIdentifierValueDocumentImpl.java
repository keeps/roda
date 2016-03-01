/*
 * An XML document type.
 * Localname: linkingEnvironmentIdentifierValue
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingEnvironmentIdentifierValueDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one linkingEnvironmentIdentifierValue(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LinkingEnvironmentIdentifierValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingEnvironmentIdentifierValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public LinkingEnvironmentIdentifierValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGENVIRONMENTIDENTIFIERVALUE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingEnvironmentIdentifierValue");
    
    
    /**
     * Gets the "linkingEnvironmentIdentifierValue" element
     */
    public java.lang.String getLinkingEnvironmentIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGENVIRONMENTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "linkingEnvironmentIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetLinkingEnvironmentIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGENVIRONMENTIDENTIFIERVALUE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "linkingEnvironmentIdentifierValue" element
     */
    public void setLinkingEnvironmentIdentifierValue(java.lang.String linkingEnvironmentIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGENVIRONMENTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(LINKINGENVIRONMENTIDENTIFIERVALUE$0);
            }
            target.setStringValue(linkingEnvironmentIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "linkingEnvironmentIdentifierValue" element
     */
    public void xsetLinkingEnvironmentIdentifierValue(org.apache.xmlbeans.XmlString linkingEnvironmentIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGENVIRONMENTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(LINKINGENVIRONMENTIDENTIFIERVALUE$0);
            }
            target.set(linkingEnvironmentIdentifierValue);
        }
    }
}
