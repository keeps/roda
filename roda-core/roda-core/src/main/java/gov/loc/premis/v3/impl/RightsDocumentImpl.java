/*
 * An XML document type.
 * Localname: rights
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RightsDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one rights(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RightsDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RightsDocument
{
    private static final long serialVersionUID = 1L;
    
    public RightsDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RIGHTS$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "rights");
    
    
    /**
     * Gets the "rights" element
     */
    public gov.loc.premis.v3.RightsComplexType getRights()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsComplexType target = null;
            target = (gov.loc.premis.v3.RightsComplexType)get_store().find_element_user(RIGHTS$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "rights" element
     */
    public void setRights(gov.loc.premis.v3.RightsComplexType rights)
    {
        generatedSetterHelperImpl(rights, RIGHTS$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "rights" element
     */
    public gov.loc.premis.v3.RightsComplexType addNewRights()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsComplexType target = null;
            target = (gov.loc.premis.v3.RightsComplexType)get_store().add_element_user(RIGHTS$0);
            return target;
        }
    }
}
