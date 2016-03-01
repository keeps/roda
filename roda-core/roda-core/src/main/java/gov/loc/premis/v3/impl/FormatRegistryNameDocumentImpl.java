/*
 * An XML document type.
 * Localname: formatRegistryName
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.FormatRegistryNameDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one formatRegistryName(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class FormatRegistryNameDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.FormatRegistryNameDocument
{
    private static final long serialVersionUID = 1L;
    
    public FormatRegistryNameDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName FORMATREGISTRYNAME$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "formatRegistryName");
    
    
    /**
     * Gets the "formatRegistryName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getFormatRegistryName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(FORMATREGISTRYNAME$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "formatRegistryName" element
     */
    public void setFormatRegistryName(gov.loc.premis.v3.StringPlusAuthority formatRegistryName)
    {
        generatedSetterHelperImpl(formatRegistryName, FORMATREGISTRYNAME$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "formatRegistryName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewFormatRegistryName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(FORMATREGISTRYNAME$0);
            return target;
        }
    }
}
