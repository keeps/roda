/*
 * An XML document type.
 * Localname: creatingApplicationName
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CreatingApplicationNameDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one creatingApplicationName(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class CreatingApplicationNameDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.CreatingApplicationNameDocument
{
    private static final long serialVersionUID = 1L;
    
    public CreatingApplicationNameDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName CREATINGAPPLICATIONNAME$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "creatingApplicationName");
    
    
    /**
     * Gets the "creatingApplicationName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getCreatingApplicationName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(CREATINGAPPLICATIONNAME$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "creatingApplicationName" element
     */
    public void setCreatingApplicationName(gov.loc.premis.v3.StringPlusAuthority creatingApplicationName)
    {
        generatedSetterHelperImpl(creatingApplicationName, CREATINGAPPLICATIONNAME$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "creatingApplicationName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewCreatingApplicationName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(CREATINGAPPLICATIONNAME$0);
            return target;
        }
    }
}
