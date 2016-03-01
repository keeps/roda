/*
 * XML Type:  linkingRightsStatementIdentifierComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML linkingRightsStatementIdentifierComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class LinkingRightsStatementIdentifierComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType
{
    private static final long serialVersionUID = 1L;
    
    public LinkingRightsStatementIdentifierComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGRIGHTSSTATEMENTIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingRightsStatementIdentifierType");
    private static final javax.xml.namespace.QName LINKINGRIGHTSSTATEMENTIDENTIFIERVALUE$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingRightsStatementIdentifierValue");
    private static final javax.xml.namespace.QName LINKPERMISSIONSTATEMENTXMLID$4 = 
        new javax.xml.namespace.QName("", "LinkPermissionStatementXmlID");
    private static final javax.xml.namespace.QName SIMPLELINK$6 = 
        new javax.xml.namespace.QName("", "simpleLink");
    
    
    /**
     * Gets the "linkingRightsStatementIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getLinkingRightsStatementIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "linkingRightsStatementIdentifierType" element
     */
    public void setLinkingRightsStatementIdentifierType(gov.loc.premis.v3.StringPlusAuthority linkingRightsStatementIdentifierType)
    {
        generatedSetterHelperImpl(linkingRightsStatementIdentifierType, LINKINGRIGHTSSTATEMENTIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "linkingRightsStatementIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewLinkingRightsStatementIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIERTYPE$0);
            return target;
        }
    }
    
    /**
     * Gets the "linkingRightsStatementIdentifierValue" element
     */
    public java.lang.String getLinkingRightsStatementIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "linkingRightsStatementIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetLinkingRightsStatementIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIERVALUE$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "linkingRightsStatementIdentifierValue" element
     */
    public void setLinkingRightsStatementIdentifierValue(java.lang.String linkingRightsStatementIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIERVALUE$2);
            }
            target.setStringValue(linkingRightsStatementIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "linkingRightsStatementIdentifierValue" element
     */
    public void xsetLinkingRightsStatementIdentifierValue(org.apache.xmlbeans.XmlString linkingRightsStatementIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIERVALUE$2);
            }
            target.set(linkingRightsStatementIdentifierValue);
        }
    }
    
    /**
     * Gets the "LinkPermissionStatementXmlID" attribute
     */
    public java.lang.String getLinkPermissionStatementXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(LINKPERMISSIONSTATEMENTXMLID$4);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "LinkPermissionStatementXmlID" attribute
     */
    public org.apache.xmlbeans.XmlIDREF xgetLinkPermissionStatementXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlIDREF target = null;
            target = (org.apache.xmlbeans.XmlIDREF)get_store().find_attribute_user(LINKPERMISSIONSTATEMENTXMLID$4);
            return target;
        }
    }
    
    /**
     * True if has "LinkPermissionStatementXmlID" attribute
     */
    public boolean isSetLinkPermissionStatementXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(LINKPERMISSIONSTATEMENTXMLID$4) != null;
        }
    }
    
    /**
     * Sets the "LinkPermissionStatementXmlID" attribute
     */
    public void setLinkPermissionStatementXmlID(java.lang.String linkPermissionStatementXmlID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(LINKPERMISSIONSTATEMENTXMLID$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(LINKPERMISSIONSTATEMENTXMLID$4);
            }
            target.setStringValue(linkPermissionStatementXmlID);
        }
    }
    
    /**
     * Sets (as xml) the "LinkPermissionStatementXmlID" attribute
     */
    public void xsetLinkPermissionStatementXmlID(org.apache.xmlbeans.XmlIDREF linkPermissionStatementXmlID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlIDREF target = null;
            target = (org.apache.xmlbeans.XmlIDREF)get_store().find_attribute_user(LINKPERMISSIONSTATEMENTXMLID$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlIDREF)get_store().add_attribute_user(LINKPERMISSIONSTATEMENTXMLID$4);
            }
            target.set(linkPermissionStatementXmlID);
        }
    }
    
    /**
     * Unsets the "LinkPermissionStatementXmlID" attribute
     */
    public void unsetLinkPermissionStatementXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(LINKPERMISSIONSTATEMENTXMLID$4);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SIMPLELINK$6);
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
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SIMPLELINK$6);
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
            return get_store().find_attribute_user(SIMPLELINK$6) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SIMPLELINK$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(SIMPLELINK$6);
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
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SIMPLELINK$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(SIMPLELINK$6);
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
            get_store().remove_attribute(SIMPLELINK$6);
        }
    }
}
