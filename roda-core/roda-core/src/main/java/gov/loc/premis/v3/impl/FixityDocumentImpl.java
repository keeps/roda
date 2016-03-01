/*
 * An XML document type.
 * Localname: fixity
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.FixityDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one fixity(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class FixityDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.FixityDocument
{
    private static final long serialVersionUID = 1L;
    
    public FixityDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName FIXITY$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "fixity");
    
    
    /**
     * Gets the "fixity" element
     */
    public gov.loc.premis.v3.FixityComplexType getFixity()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.FixityComplexType target = null;
            target = (gov.loc.premis.v3.FixityComplexType)get_store().find_element_user(FIXITY$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "fixity" element
     */
    public void setFixity(gov.loc.premis.v3.FixityComplexType fixity)
    {
        generatedSetterHelperImpl(fixity, FIXITY$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "fixity" element
     */
    public gov.loc.premis.v3.FixityComplexType addNewFixity()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.FixityComplexType target = null;
            target = (gov.loc.premis.v3.FixityComplexType)get_store().add_element_user(FIXITY$0);
            return target;
        }
    }
}
