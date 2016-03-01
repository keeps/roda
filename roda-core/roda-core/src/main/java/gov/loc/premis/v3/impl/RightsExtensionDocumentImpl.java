/*
 * An XML document type.
 * Localname: rightsExtension
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RightsExtensionDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one rightsExtension(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RightsExtensionDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RightsExtensionDocument
{
    private static final long serialVersionUID = 1L;
    
    public RightsExtensionDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RIGHTSEXTENSION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "rightsExtension");
    
    
    /**
     * Gets the "rightsExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType getRightsExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(RIGHTSEXTENSION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "rightsExtension" element
     */
    public void setRightsExtension(gov.loc.premis.v3.ExtensionComplexType rightsExtension)
    {
        generatedSetterHelperImpl(rightsExtension, RIGHTSEXTENSION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "rightsExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType addNewRightsExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().add_element_user(RIGHTSEXTENSION$0);
            return target;
        }
    }
}
