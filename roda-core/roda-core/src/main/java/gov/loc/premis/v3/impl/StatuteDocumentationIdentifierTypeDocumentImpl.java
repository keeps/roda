/*
 * An XML document type.
 * Localname: statuteDocumentationIdentifierType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.StatuteDocumentationIdentifierTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one statuteDocumentationIdentifierType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class StatuteDocumentationIdentifierTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.StatuteDocumentationIdentifierTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public StatuteDocumentationIdentifierTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName STATUTEDOCUMENTATIONIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "statuteDocumentationIdentifierType");
    
    
    /**
     * Gets the "statuteDocumentationIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getStatuteDocumentationIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(STATUTEDOCUMENTATIONIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "statuteDocumentationIdentifierType" element
     */
    public void setStatuteDocumentationIdentifierType(gov.loc.premis.v3.StringPlusAuthority statuteDocumentationIdentifierType)
    {
        generatedSetterHelperImpl(statuteDocumentationIdentifierType, STATUTEDOCUMENTATIONIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "statuteDocumentationIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewStatuteDocumentationIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(STATUTEDOCUMENTATIONIDENTIFIERTYPE$0);
            return target;
        }
    }
}
