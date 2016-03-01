/*
 * An XML document type.
 * Localname: format
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.FormatDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one format(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class FormatDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.FormatDocument
{
    private static final long serialVersionUID = 1L;
    
    public FormatDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName FORMAT$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "format");
    
    
    /**
     * Gets the "format" element
     */
    public gov.loc.premis.v3.FormatComplexType getFormat()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.FormatComplexType target = null;
            target = (gov.loc.premis.v3.FormatComplexType)get_store().find_element_user(FORMAT$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "format" element
     */
    public void setFormat(gov.loc.premis.v3.FormatComplexType format)
    {
        generatedSetterHelperImpl(format, FORMAT$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "format" element
     */
    public gov.loc.premis.v3.FormatComplexType addNewFormat()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.FormatComplexType target = null;
            target = (gov.loc.premis.v3.FormatComplexType)get_store().add_element_user(FORMAT$0);
            return target;
        }
    }
}
