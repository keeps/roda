/*
 * An XML document type.
 * Localname: linkingRightsStatementIdentifierValue
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingRightsStatementIdentifierValueDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one linkingRightsStatementIdentifierValue(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LinkingRightsStatementIdentifierValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingRightsStatementIdentifierValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public LinkingRightsStatementIdentifierValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGRIGHTSSTATEMENTIDENTIFIERVALUE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingRightsStatementIdentifierValue");
    
    
    /**
     * Gets the "linkingRightsStatementIdentifierValue" element
     */
    public java.lang.String getLinkingRightsStatementIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "linkingRightsStatementIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetLinkingRightsStatementIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIERVALUE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "linkingRightsStatementIdentifierValue" element
     */
    public void setLinkingRightsStatementIdentifierValue(java.lang.String linkingRightsStatementIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIERVALUE$0);
            }
            target.setStringValue(linkingRightsStatementIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "linkingRightsStatementIdentifierValue" element
     */
    public void xsetLinkingRightsStatementIdentifierValue(org.apache.xmlbeans.XmlString linkingRightsStatementIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIERVALUE$0);
            }
            target.set(linkingRightsStatementIdentifierValue);
        }
    }
}
