/*
 * An XML document type.
 * Localname: rightsGranted
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RightsGrantedDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one rightsGranted(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RightsGrantedDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RightsGrantedDocument
{
    private static final long serialVersionUID = 1L;
    
    public RightsGrantedDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RIGHTSGRANTED$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "rightsGranted");
    
    
    /**
     * Gets the "rightsGranted" element
     */
    public gov.loc.premis.v3.RightsGrantedComplexType getRightsGranted()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsGrantedComplexType target = null;
            target = (gov.loc.premis.v3.RightsGrantedComplexType)get_store().find_element_user(RIGHTSGRANTED$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "rightsGranted" element
     */
    public void setRightsGranted(gov.loc.premis.v3.RightsGrantedComplexType rightsGranted)
    {
        generatedSetterHelperImpl(rightsGranted, RIGHTSGRANTED$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "rightsGranted" element
     */
    public gov.loc.premis.v3.RightsGrantedComplexType addNewRightsGranted()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsGrantedComplexType target = null;
            target = (gov.loc.premis.v3.RightsGrantedComplexType)get_store().add_element_user(RIGHTSGRANTED$0);
            return target;
        }
    }
}
