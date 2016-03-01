/*
 * An XML document type.
 * Localname: environmentRegistry
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EnvironmentRegistryDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one environmentRegistry(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EnvironmentRegistryDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EnvironmentRegistryDocument
{
    private static final long serialVersionUID = 1L;
    
    public EnvironmentRegistryDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENVIRONMENTREGISTRY$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentRegistry");
    
    
    /**
     * Gets the "environmentRegistry" element
     */
    public gov.loc.premis.v3.EnvironmentRegistryComplexType getEnvironmentRegistry()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EnvironmentRegistryComplexType target = null;
            target = (gov.loc.premis.v3.EnvironmentRegistryComplexType)get_store().find_element_user(ENVIRONMENTREGISTRY$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "environmentRegistry" element
     */
    public void setEnvironmentRegistry(gov.loc.premis.v3.EnvironmentRegistryComplexType environmentRegistry)
    {
        generatedSetterHelperImpl(environmentRegistry, ENVIRONMENTREGISTRY$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "environmentRegistry" element
     */
    public gov.loc.premis.v3.EnvironmentRegistryComplexType addNewEnvironmentRegistry()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EnvironmentRegistryComplexType target = null;
            target = (gov.loc.premis.v3.EnvironmentRegistryComplexType)get_store().add_element_user(ENVIRONMENTREGISTRY$0);
            return target;
        }
    }
}
