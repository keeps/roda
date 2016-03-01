/*
 * An XML document type.
 * Localname: creatingApplicationExtension
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CreatingApplicationExtensionDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one creatingApplicationExtension(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class CreatingApplicationExtensionDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.CreatingApplicationExtensionDocument
{
    private static final long serialVersionUID = 1L;
    
    public CreatingApplicationExtensionDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName CREATINGAPPLICATIONEXTENSION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "creatingApplicationExtension");
    
    
    /**
     * Gets the "creatingApplicationExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType getCreatingApplicationExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(CREATINGAPPLICATIONEXTENSION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "creatingApplicationExtension" element
     */
    public void setCreatingApplicationExtension(gov.loc.premis.v3.ExtensionComplexType creatingApplicationExtension)
    {
        generatedSetterHelperImpl(creatingApplicationExtension, CREATINGAPPLICATIONEXTENSION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "creatingApplicationExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType addNewCreatingApplicationExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().add_element_user(CREATINGAPPLICATIONEXTENSION$0);
            return target;
        }
    }
}
