/*
 * An XML document type.
 * Localname: originalName
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.OriginalNameDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one originalName(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class OriginalNameDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.OriginalNameDocument
{
    private static final long serialVersionUID = 1L;
    
    public OriginalNameDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ORIGINALNAME$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "originalName");
    
    
    /**
     * Gets the "originalName" element
     */
    public gov.loc.premis.v3.OriginalNameComplexType getOriginalName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.OriginalNameComplexType target = null;
            target = (gov.loc.premis.v3.OriginalNameComplexType)get_store().find_element_user(ORIGINALNAME$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "originalName" element
     */
    public void setOriginalName(gov.loc.premis.v3.OriginalNameComplexType originalName)
    {
        generatedSetterHelperImpl(originalName, ORIGINALNAME$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "originalName" element
     */
    public gov.loc.premis.v3.OriginalNameComplexType addNewOriginalName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.OriginalNameComplexType target = null;
            target = (gov.loc.premis.v3.OriginalNameComplexType)get_store().add_element_user(ORIGINALNAME$0);
            return target;
        }
    }
}
