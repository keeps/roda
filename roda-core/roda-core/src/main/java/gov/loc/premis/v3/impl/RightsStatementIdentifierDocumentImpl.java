/*
 * An XML document type.
 * Localname: rightsStatementIdentifier
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RightsStatementIdentifierDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one rightsStatementIdentifier(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RightsStatementIdentifierDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RightsStatementIdentifierDocument
{
    private static final long serialVersionUID = 1L;
    
    public RightsStatementIdentifierDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RIGHTSSTATEMENTIDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "rightsStatementIdentifier");
    
    
    /**
     * Gets the "rightsStatementIdentifier" element
     */
    public gov.loc.premis.v3.RightsStatementIdentifierComplexType getRightsStatementIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsStatementIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.RightsStatementIdentifierComplexType)get_store().find_element_user(RIGHTSSTATEMENTIDENTIFIER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "rightsStatementIdentifier" element
     */
    public void setRightsStatementIdentifier(gov.loc.premis.v3.RightsStatementIdentifierComplexType rightsStatementIdentifier)
    {
        generatedSetterHelperImpl(rightsStatementIdentifier, RIGHTSSTATEMENTIDENTIFIER$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "rightsStatementIdentifier" element
     */
    public gov.loc.premis.v3.RightsStatementIdentifierComplexType addNewRightsStatementIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsStatementIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.RightsStatementIdentifierComplexType)get_store().add_element_user(RIGHTSSTATEMENTIDENTIFIER$0);
            return target;
        }
    }
}
