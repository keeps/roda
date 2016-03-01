/*
 * An XML document type.
 * Localname: object
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.ObjectDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one object(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class ObjectDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.ObjectDocument
{
    private static final long serialVersionUID = 1L;
    
    public ObjectDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName OBJECT$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "object");
    
    
    /**
     * Gets the "object" element
     */
    public gov.loc.premis.v3.ObjectComplexType getObject()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ObjectComplexType target = null;
            target = (gov.loc.premis.v3.ObjectComplexType)get_store().find_element_user(OBJECT$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "object" element
     */
    public void setObject(gov.loc.premis.v3.ObjectComplexType object)
    {
        generatedSetterHelperImpl(object, OBJECT$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "object" element
     */
    public gov.loc.premis.v3.ObjectComplexType addNewObject()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ObjectComplexType target = null;
            target = (gov.loc.premis.v3.ObjectComplexType)get_store().add_element_user(OBJECT$0);
            return target;
        }
    }
}
