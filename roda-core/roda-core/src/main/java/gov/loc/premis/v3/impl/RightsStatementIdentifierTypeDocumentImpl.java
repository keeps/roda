/*
 * An XML document type.
 * Localname: rightsStatementIdentifierType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RightsStatementIdentifierTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one rightsStatementIdentifierType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RightsStatementIdentifierTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RightsStatementIdentifierTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public RightsStatementIdentifierTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RIGHTSSTATEMENTIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "rightsStatementIdentifierType");
    
    
    /**
     * Gets the "rightsStatementIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getRightsStatementIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(RIGHTSSTATEMENTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "rightsStatementIdentifierType" element
     */
    public void setRightsStatementIdentifierType(gov.loc.premis.v3.StringPlusAuthority rightsStatementIdentifierType)
    {
        generatedSetterHelperImpl(rightsStatementIdentifierType, RIGHTSSTATEMENTIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "rightsStatementIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewRightsStatementIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(RIGHTSSTATEMENTIDENTIFIERTYPE$0);
            return target;
        }
    }
}
