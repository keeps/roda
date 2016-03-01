/*
 * XML Type:  rightsStatementIdentifierComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RightsStatementIdentifierComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML rightsStatementIdentifierComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class RightsStatementIdentifierComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RightsStatementIdentifierComplexType
{
    private static final long serialVersionUID = 1L;
    
    public RightsStatementIdentifierComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RIGHTSSTATEMENTIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "rightsStatementIdentifierType");
    private static final javax.xml.namespace.QName RIGHTSSTATEMENTIDENTIFIERVALUE$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "rightsStatementIdentifierValue");
    private static final javax.xml.namespace.QName SIMPLELINK$4 = 
        new javax.xml.namespace.QName("", "simpleLink");
    
    
    /**
     * Gets the "rightsStatementIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getRightsStatementIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(RIGHTSSTATEMENTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "rightsStatementIdentifierType" element
     */
    public void setRightsStatementIdentifierType(gov.loc.premis.v3.StringPlusAuthority rightsStatementIdentifierType)
    {
        generatedSetterHelperImpl(rightsStatementIdentifierType, RIGHTSSTATEMENTIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "rightsStatementIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewRightsStatementIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(RIGHTSSTATEMENTIDENTIFIERTYPE$0);
            return target;
        }
    }
    
    /**
     * Gets the "rightsStatementIdentifierValue" element
     */
    public java.lang.String getRightsStatementIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RIGHTSSTATEMENTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "rightsStatementIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetRightsStatementIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(RIGHTSSTATEMENTIDENTIFIERVALUE$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "rightsStatementIdentifierValue" element
     */
    public void setRightsStatementIdentifierValue(java.lang.String rightsStatementIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RIGHTSSTATEMENTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(RIGHTSSTATEMENTIDENTIFIERVALUE$2);
            }
            target.setStringValue(rightsStatementIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "rightsStatementIdentifierValue" element
     */
    public void xsetRightsStatementIdentifierValue(org.apache.xmlbeans.XmlString rightsStatementIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(RIGHTSSTATEMENTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(RIGHTSSTATEMENTIDENTIFIERVALUE$2);
            }
            target.set(rightsStatementIdentifierValue);
        }
    }
    
    /**
     * Gets the "simpleLink" attribute
     */
    public java.lang.String getSimpleLink()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SIMPLELINK$4);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "simpleLink" attribute
     */
    public org.apache.xmlbeans.XmlAnyURI xgetSimpleLink()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SIMPLELINK$4);
            return target;
        }
    }
    
    /**
     * True if has "simpleLink" attribute
     */
    public boolean isSetSimpleLink()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(SIMPLELINK$4) != null;
        }
    }
    
    /**
     * Sets the "simpleLink" attribute
     */
    public void setSimpleLink(java.lang.String simpleLink)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SIMPLELINK$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(SIMPLELINK$4);
            }
            target.setStringValue(simpleLink);
        }
    }
    
    /**
     * Sets (as xml) the "simpleLink" attribute
     */
    public void xsetSimpleLink(org.apache.xmlbeans.XmlAnyURI simpleLink)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SIMPLELINK$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(SIMPLELINK$4);
            }
            target.set(simpleLink);
        }
    }
    
    /**
     * Unsets the "simpleLink" attribute
     */
    public void unsetSimpleLink()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(SIMPLELINK$4);
        }
    }
}
