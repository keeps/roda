/*
 * XML Type:  statuteDocumentationIdentifierComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML statuteDocumentationIdentifierComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class StatuteDocumentationIdentifierComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType
{
    private static final long serialVersionUID = 1L;
    
    public StatuteDocumentationIdentifierComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName STATUTEDOCUMENTATIONIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "statuteDocumentationIdentifierType");
    private static final javax.xml.namespace.QName STATUTEDOCUMENTATIONIDENTIFIERVALUE$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "statuteDocumentationIdentifierValue");
    private static final javax.xml.namespace.QName STATUTEDOCUMENTATIONROLE$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "statuteDocumentationRole");
    
    
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
    
    /**
     * Gets the "statuteDocumentationIdentifierValue" element
     */
    public java.lang.String getStatuteDocumentationIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STATUTEDOCUMENTATIONIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "statuteDocumentationIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetStatuteDocumentationIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(STATUTEDOCUMENTATIONIDENTIFIERVALUE$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "statuteDocumentationIdentifierValue" element
     */
    public void setStatuteDocumentationIdentifierValue(java.lang.String statuteDocumentationIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STATUTEDOCUMENTATIONIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(STATUTEDOCUMENTATIONIDENTIFIERVALUE$2);
            }
            target.setStringValue(statuteDocumentationIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "statuteDocumentationIdentifierValue" element
     */
    public void xsetStatuteDocumentationIdentifierValue(org.apache.xmlbeans.XmlString statuteDocumentationIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(STATUTEDOCUMENTATIONIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(STATUTEDOCUMENTATIONIDENTIFIERVALUE$2);
            }
            target.set(statuteDocumentationIdentifierValue);
        }
    }
    
    /**
     * Gets the "statuteDocumentationRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getStatuteDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(STATUTEDOCUMENTATIONROLE$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "statuteDocumentationRole" element
     */
    public boolean isSetStatuteDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(STATUTEDOCUMENTATIONROLE$4) != 0;
        }
    }
    
    /**
     * Sets the "statuteDocumentationRole" element
     */
    public void setStatuteDocumentationRole(gov.loc.premis.v3.StringPlusAuthority statuteDocumentationRole)
    {
        generatedSetterHelperImpl(statuteDocumentationRole, STATUTEDOCUMENTATIONROLE$4, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "statuteDocumentationRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewStatuteDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(STATUTEDOCUMENTATIONROLE$4);
            return target;
        }
    }
    
    /**
     * Unsets the "statuteDocumentationRole" element
     */
    public void unsetStatuteDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(STATUTEDOCUMENTATIONROLE$4, 0);
        }
    }
}
