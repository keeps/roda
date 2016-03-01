/*
 * An XML document type.
 * Localname: contentLocationType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.ContentLocationTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one contentLocationType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class ContentLocationTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.ContentLocationTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public ContentLocationTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName CONTENTLOCATIONTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "contentLocationType");
    
    
    /**
     * Gets the "contentLocationType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getContentLocationType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(CONTENTLOCATIONTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "contentLocationType" element
     */
    public void setContentLocationType(gov.loc.premis.v3.StringPlusAuthority contentLocationType)
    {
        generatedSetterHelperImpl(contentLocationType, CONTENTLOCATIONTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "contentLocationType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewContentLocationType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(CONTENTLOCATIONTYPE$0);
            return target;
        }
    }
}
