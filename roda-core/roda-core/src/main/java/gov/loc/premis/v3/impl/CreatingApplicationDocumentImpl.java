/*
 * An XML document type.
 * Localname: creatingApplication
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CreatingApplicationDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one creatingApplication(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class CreatingApplicationDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.CreatingApplicationDocument
{
    private static final long serialVersionUID = 1L;
    
    public CreatingApplicationDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName CREATINGAPPLICATION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "creatingApplication");
    
    
    /**
     * Gets the "creatingApplication" element
     */
    public gov.loc.premis.v3.CreatingApplicationComplexType getCreatingApplication()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CreatingApplicationComplexType target = null;
            target = (gov.loc.premis.v3.CreatingApplicationComplexType)get_store().find_element_user(CREATINGAPPLICATION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "creatingApplication" element
     */
    public void setCreatingApplication(gov.loc.premis.v3.CreatingApplicationComplexType creatingApplication)
    {
        generatedSetterHelperImpl(creatingApplication, CREATINGAPPLICATION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "creatingApplication" element
     */
    public gov.loc.premis.v3.CreatingApplicationComplexType addNewCreatingApplication()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CreatingApplicationComplexType target = null;
            target = (gov.loc.premis.v3.CreatingApplicationComplexType)get_store().add_element_user(CREATINGAPPLICATION$0);
            return target;
        }
    }
}
