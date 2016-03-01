/*
 * An XML document type.
 * Localname: contentLocation
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.ContentLocationDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one contentLocation(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class ContentLocationDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.ContentLocationDocument
{
    private static final long serialVersionUID = 1L;
    
    public ContentLocationDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName CONTENTLOCATION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "contentLocation");
    
    
    /**
     * Gets the "contentLocation" element
     */
    public gov.loc.premis.v3.ContentLocationComplexType getContentLocation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ContentLocationComplexType target = null;
            target = (gov.loc.premis.v3.ContentLocationComplexType)get_store().find_element_user(CONTENTLOCATION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "contentLocation" element
     */
    public void setContentLocation(gov.loc.premis.v3.ContentLocationComplexType contentLocation)
    {
        generatedSetterHelperImpl(contentLocation, CONTENTLOCATION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "contentLocation" element
     */
    public gov.loc.premis.v3.ContentLocationComplexType addNewContentLocation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ContentLocationComplexType target = null;
            target = (gov.loc.premis.v3.ContentLocationComplexType)get_store().add_element_user(CONTENTLOCATION$0);
            return target;
        }
    }
}
