/*
 * An XML document type.
 * Localname: rightsStatement
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RightsStatementDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one rightsStatement(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RightsStatementDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RightsStatementDocument
{
    private static final long serialVersionUID = 1L;
    
    public RightsStatementDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RIGHTSSTATEMENT$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "rightsStatement");
    
    
    /**
     * Gets the "rightsStatement" element
     */
    public gov.loc.premis.v3.RightsStatementComplexType getRightsStatement()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsStatementComplexType target = null;
            target = (gov.loc.premis.v3.RightsStatementComplexType)get_store().find_element_user(RIGHTSSTATEMENT$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "rightsStatement" element
     */
    public void setRightsStatement(gov.loc.premis.v3.RightsStatementComplexType rightsStatement)
    {
        generatedSetterHelperImpl(rightsStatement, RIGHTSSTATEMENT$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "rightsStatement" element
     */
    public gov.loc.premis.v3.RightsStatementComplexType addNewRightsStatement()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsStatementComplexType target = null;
            target = (gov.loc.premis.v3.RightsStatementComplexType)get_store().add_element_user(RIGHTSSTATEMENT$0);
            return target;
        }
    }
}
