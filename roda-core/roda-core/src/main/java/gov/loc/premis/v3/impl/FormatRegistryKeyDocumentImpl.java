/*
 * An XML document type.
 * Localname: formatRegistryKey
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.FormatRegistryKeyDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one formatRegistryKey(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class FormatRegistryKeyDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.FormatRegistryKeyDocument
{
    private static final long serialVersionUID = 1L;
    
    public FormatRegistryKeyDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName FORMATREGISTRYKEY$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "formatRegistryKey");
    
    
    /**
     * Gets the "formatRegistryKey" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getFormatRegistryKey()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(FORMATREGISTRYKEY$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "formatRegistryKey" element
     */
    public void setFormatRegistryKey(gov.loc.premis.v3.StringPlusAuthority formatRegistryKey)
    {
        generatedSetterHelperImpl(formatRegistryKey, FORMATREGISTRYKEY$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "formatRegistryKey" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewFormatRegistryKey()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(FORMATREGISTRYKEY$0);
            return target;
        }
    }
}
