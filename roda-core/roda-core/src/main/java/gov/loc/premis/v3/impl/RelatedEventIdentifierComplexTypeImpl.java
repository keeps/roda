/*
 * XML Type:  relatedEventIdentifierComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RelatedEventIdentifierComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML relatedEventIdentifierComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class RelatedEventIdentifierComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RelatedEventIdentifierComplexType
{
    private static final long serialVersionUID = 1L;
    
    public RelatedEventIdentifierComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RELATEDEVENTIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relatedEventIdentifierType");
    private static final javax.xml.namespace.QName RELATEDEVENTIDENTIFIERVALUE$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relatedEventIdentifierValue");
    private static final javax.xml.namespace.QName RELATEDEVENTSEQUENCE$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relatedEventSequence");
    private static final javax.xml.namespace.QName RELEVENTXMLID$6 = 
        new javax.xml.namespace.QName("", "RelEventXmlID");
    private static final javax.xml.namespace.QName SIMPLELINK$8 = 
        new javax.xml.namespace.QName("", "simpleLink");
    
    
    /**
     * Gets the "relatedEventIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getRelatedEventIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(RELATEDEVENTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "relatedEventIdentifierType" element
     */
    public void setRelatedEventIdentifierType(gov.loc.premis.v3.StringPlusAuthority relatedEventIdentifierType)
    {
        generatedSetterHelperImpl(relatedEventIdentifierType, RELATEDEVENTIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "relatedEventIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewRelatedEventIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(RELATEDEVENTIDENTIFIERTYPE$0);
            return target;
        }
    }
    
    /**
     * Gets the "relatedEventIdentifierValue" element
     */
    public java.lang.String getRelatedEventIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RELATEDEVENTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "relatedEventIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetRelatedEventIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(RELATEDEVENTIDENTIFIERVALUE$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "relatedEventIdentifierValue" element
     */
    public void setRelatedEventIdentifierValue(java.lang.String relatedEventIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RELATEDEVENTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(RELATEDEVENTIDENTIFIERVALUE$2);
            }
            target.setStringValue(relatedEventIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "relatedEventIdentifierValue" element
     */
    public void xsetRelatedEventIdentifierValue(org.apache.xmlbeans.XmlString relatedEventIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(RELATEDEVENTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(RELATEDEVENTIDENTIFIERVALUE$2);
            }
            target.set(relatedEventIdentifierValue);
        }
    }
    
    /**
     * Gets the "relatedEventSequence" element
     */
    public java.math.BigInteger getRelatedEventSequence()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RELATEDEVENTSEQUENCE$4, 0);
            if (target == null)
            {
                return null;
            }
            return target.getBigIntegerValue();
        }
    }
    
    /**
     * Gets (as xml) the "relatedEventSequence" element
     */
    public org.apache.xmlbeans.XmlNonNegativeInteger xgetRelatedEventSequence()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlNonNegativeInteger target = null;
            target = (org.apache.xmlbeans.XmlNonNegativeInteger)get_store().find_element_user(RELATEDEVENTSEQUENCE$4, 0);
            return target;
        }
    }
    
    /**
     * True if has "relatedEventSequence" element
     */
    public boolean isSetRelatedEventSequence()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(RELATEDEVENTSEQUENCE$4) != 0;
        }
    }
    
    /**
     * Sets the "relatedEventSequence" element
     */
    public void setRelatedEventSequence(java.math.BigInteger relatedEventSequence)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RELATEDEVENTSEQUENCE$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(RELATEDEVENTSEQUENCE$4);
            }
            target.setBigIntegerValue(relatedEventSequence);
        }
    }
    
    /**
     * Sets (as xml) the "relatedEventSequence" element
     */
    public void xsetRelatedEventSequence(org.apache.xmlbeans.XmlNonNegativeInteger relatedEventSequence)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlNonNegativeInteger target = null;
            target = (org.apache.xmlbeans.XmlNonNegativeInteger)get_store().find_element_user(RELATEDEVENTSEQUENCE$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlNonNegativeInteger)get_store().add_element_user(RELATEDEVENTSEQUENCE$4);
            }
            target.set(relatedEventSequence);
        }
    }
    
    /**
     * Unsets the "relatedEventSequence" element
     */
    public void unsetRelatedEventSequence()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(RELATEDEVENTSEQUENCE$4, 0);
        }
    }
    
    /**
     * Gets the "RelEventXmlID" attribute
     */
    public java.lang.String getRelEventXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(RELEVENTXMLID$6);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "RelEventXmlID" attribute
     */
    public org.apache.xmlbeans.XmlIDREF xgetRelEventXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlIDREF target = null;
            target = (org.apache.xmlbeans.XmlIDREF)get_store().find_attribute_user(RELEVENTXMLID$6);
            return target;
        }
    }
    
    /**
     * True if has "RelEventXmlID" attribute
     */
    public boolean isSetRelEventXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(RELEVENTXMLID$6) != null;
        }
    }
    
    /**
     * Sets the "RelEventXmlID" attribute
     */
    public void setRelEventXmlID(java.lang.String relEventXmlID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(RELEVENTXMLID$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(RELEVENTXMLID$6);
            }
            target.setStringValue(relEventXmlID);
        }
    }
    
    /**
     * Sets (as xml) the "RelEventXmlID" attribute
     */
    public void xsetRelEventXmlID(org.apache.xmlbeans.XmlIDREF relEventXmlID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlIDREF target = null;
            target = (org.apache.xmlbeans.XmlIDREF)get_store().find_attribute_user(RELEVENTXMLID$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlIDREF)get_store().add_attribute_user(RELEVENTXMLID$6);
            }
            target.set(relEventXmlID);
        }
    }
    
    /**
     * Unsets the "RelEventXmlID" attribute
     */
    public void unsetRelEventXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(RELEVENTXMLID$6);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SIMPLELINK$8);
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
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SIMPLELINK$8);
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
            return get_store().find_attribute_user(SIMPLELINK$8) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SIMPLELINK$8);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(SIMPLELINK$8);
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
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SIMPLELINK$8);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(SIMPLELINK$8);
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
            get_store().remove_attribute(SIMPLELINK$8);
        }
    }
}
