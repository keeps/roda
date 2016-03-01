/*
 * An XML document type.
 * Localname: environmentExtension
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EnvironmentExtensionDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one environmentExtension(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EnvironmentExtensionDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EnvironmentExtensionDocument
{
    private static final long serialVersionUID = 1L;
    
    public EnvironmentExtensionDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENVIRONMENTEXTENSION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentExtension");
    
    
    /**
     * Gets the "environmentExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType getEnvironmentExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(ENVIRONMENTEXTENSION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "environmentExtension" element
     */
    public void setEnvironmentExtension(gov.loc.premis.v3.ExtensionComplexType environmentExtension)
    {
        generatedSetterHelperImpl(environmentExtension, ENVIRONMENTEXTENSION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "environmentExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType addNewEnvironmentExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().add_element_user(ENVIRONMENTEXTENSION$0);
            return target;
        }
    }
}
