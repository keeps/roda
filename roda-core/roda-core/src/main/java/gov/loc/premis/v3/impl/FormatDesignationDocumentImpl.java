/*
 * An XML document type.
 * Localname: formatDesignation
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.FormatDesignationDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one formatDesignation(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class FormatDesignationDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.FormatDesignationDocument
{
    private static final long serialVersionUID = 1L;
    
    public FormatDesignationDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName FORMATDESIGNATION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "formatDesignation");
    
    
    /**
     * Gets the "formatDesignation" element
     */
    public gov.loc.premis.v3.FormatDesignationComplexType getFormatDesignation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.FormatDesignationComplexType target = null;
            target = (gov.loc.premis.v3.FormatDesignationComplexType)get_store().find_element_user(FORMATDESIGNATION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "formatDesignation" element
     */
    public void setFormatDesignation(gov.loc.premis.v3.FormatDesignationComplexType formatDesignation)
    {
        generatedSetterHelperImpl(formatDesignation, FORMATDESIGNATION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "formatDesignation" element
     */
    public gov.loc.premis.v3.FormatDesignationComplexType addNewFormatDesignation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.FormatDesignationComplexType target = null;
            target = (gov.loc.premis.v3.FormatDesignationComplexType)get_store().add_element_user(FORMATDESIGNATION$0);
            return target;
        }
    }
}
