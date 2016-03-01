/*
 * An XML document type.
 * Localname: linkingObjectIdentifierValue
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingObjectIdentifierValueDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one linkingObjectIdentifierValue(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LinkingObjectIdentifierValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingObjectIdentifierValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public LinkingObjectIdentifierValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGOBJECTIDENTIFIERVALUE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingObjectIdentifierValue");
    
    
    /**
     * Gets the "linkingObjectIdentifierValue" element
     */
    public java.lang.String getLinkingObjectIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGOBJECTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "linkingObjectIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetLinkingObjectIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGOBJECTIDENTIFIERVALUE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "linkingObjectIdentifierValue" element
     */
    public void setLinkingObjectIdentifierValue(java.lang.String linkingObjectIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGOBJECTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(LINKINGOBJECTIDENTIFIERVALUE$0);
            }
            target.setStringValue(linkingObjectIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "linkingObjectIdentifierValue" element
     */
    public void xsetLinkingObjectIdentifierValue(org.apache.xmlbeans.XmlString linkingObjectIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGOBJECTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(LINKINGOBJECTIDENTIFIERVALUE$0);
            }
            target.set(linkingObjectIdentifierValue);
        }
    }
}
