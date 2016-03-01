/*
 * An XML document type.
 * Localname: formatRegistry
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.FormatRegistryDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one formatRegistry(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class FormatRegistryDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.FormatRegistryDocument
{
    private static final long serialVersionUID = 1L;
    
    public FormatRegistryDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName FORMATREGISTRY$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "formatRegistry");
    
    
    /**
     * Gets the "formatRegistry" element
     */
    public gov.loc.premis.v3.FormatRegistryComplexType getFormatRegistry()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.FormatRegistryComplexType target = null;
            target = (gov.loc.premis.v3.FormatRegistryComplexType)get_store().find_element_user(FORMATREGISTRY$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "formatRegistry" element
     */
    public void setFormatRegistry(gov.loc.premis.v3.FormatRegistryComplexType formatRegistry)
    {
        generatedSetterHelperImpl(formatRegistry, FORMATREGISTRY$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "formatRegistry" element
     */
    public gov.loc.premis.v3.FormatRegistryComplexType addNewFormatRegistry()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.FormatRegistryComplexType target = null;
            target = (gov.loc.premis.v3.FormatRegistryComplexType)get_store().add_element_user(FORMATREGISTRY$0);
            return target;
        }
    }
}
