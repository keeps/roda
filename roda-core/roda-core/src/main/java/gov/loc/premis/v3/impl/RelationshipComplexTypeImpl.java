/*
 * XML Type:  relationshipComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RelationshipComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML relationshipComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class RelationshipComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RelationshipComplexType
{
    private static final long serialVersionUID = 1L;
    
    public RelationshipComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RELATIONSHIPTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relationshipType");
    private static final javax.xml.namespace.QName RELATIONSHIPSUBTYPE$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relationshipSubType");
    private static final javax.xml.namespace.QName RELATEDOBJECTIDENTIFIER$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relatedObjectIdentifier");
    private static final javax.xml.namespace.QName RELATEDEVENTIDENTIFIER$6 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relatedEventIdentifier");
    private static final javax.xml.namespace.QName RELATEDENVIRONMENTPURPOSE$8 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relatedEnvironmentPurpose");
    private static final javax.xml.namespace.QName RELATEDENVIRONMENTCHARACTERISTIC$10 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relatedEnvironmentCharacteristic");
    
    
    /**
     * Gets the "relationshipType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getRelationshipType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(RELATIONSHIPTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "relationshipType" element
     */
    public void setRelationshipType(gov.loc.premis.v3.StringPlusAuthority relationshipType)
    {
        generatedSetterHelperImpl(relationshipType, RELATIONSHIPTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "relationshipType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewRelationshipType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(RELATIONSHIPTYPE$0);
            return target;
        }
    }
    
    /**
     * Gets the "relationshipSubType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getRelationshipSubType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(RELATIONSHIPSUBTYPE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "relationshipSubType" element
     */
    public void setRelationshipSubType(gov.loc.premis.v3.StringPlusAuthority relationshipSubType)
    {
        generatedSetterHelperImpl(relationshipSubType, RELATIONSHIPSUBTYPE$2, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "relationshipSubType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewRelationshipSubType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(RELATIONSHIPSUBTYPE$2);
            return target;
        }
    }
    
    /**
     * Gets array of all "relatedObjectIdentifier" elements
     */
    public gov.loc.premis.v3.RelatedObjectIdentifierComplexType[] getRelatedObjectIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(RELATEDOBJECTIDENTIFIER$4, targetList);
            gov.loc.premis.v3.RelatedObjectIdentifierComplexType[] result = new gov.loc.premis.v3.RelatedObjectIdentifierComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "relatedObjectIdentifier" element
     */
    public gov.loc.premis.v3.RelatedObjectIdentifierComplexType getRelatedObjectIdentifierArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RelatedObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.RelatedObjectIdentifierComplexType)get_store().find_element_user(RELATEDOBJECTIDENTIFIER$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "relatedObjectIdentifier" element
     */
    public int sizeOfRelatedObjectIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(RELATEDOBJECTIDENTIFIER$4);
        }
    }
    
    /**
     * Sets array of all "relatedObjectIdentifier" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setRelatedObjectIdentifierArray(gov.loc.premis.v3.RelatedObjectIdentifierComplexType[] relatedObjectIdentifierArray)
    {
        check_orphaned();
        arraySetterHelper(relatedObjectIdentifierArray, RELATEDOBJECTIDENTIFIER$4);
    }
    
    /**
     * Sets ith "relatedObjectIdentifier" element
     */
    public void setRelatedObjectIdentifierArray(int i, gov.loc.premis.v3.RelatedObjectIdentifierComplexType relatedObjectIdentifier)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RelatedObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.RelatedObjectIdentifierComplexType)get_store().find_element_user(RELATEDOBJECTIDENTIFIER$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(relatedObjectIdentifier);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "relatedObjectIdentifier" element
     */
    public gov.loc.premis.v3.RelatedObjectIdentifierComplexType insertNewRelatedObjectIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RelatedObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.RelatedObjectIdentifierComplexType)get_store().insert_element_user(RELATEDOBJECTIDENTIFIER$4, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "relatedObjectIdentifier" element
     */
    public gov.loc.premis.v3.RelatedObjectIdentifierComplexType addNewRelatedObjectIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RelatedObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.RelatedObjectIdentifierComplexType)get_store().add_element_user(RELATEDOBJECTIDENTIFIER$4);
            return target;
        }
    }
    
    /**
     * Removes the ith "relatedObjectIdentifier" element
     */
    public void removeRelatedObjectIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(RELATEDOBJECTIDENTIFIER$4, i);
        }
    }
    
    /**
     * Gets array of all "relatedEventIdentifier" elements
     */
    public gov.loc.premis.v3.RelatedEventIdentifierComplexType[] getRelatedEventIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(RELATEDEVENTIDENTIFIER$6, targetList);
            gov.loc.premis.v3.RelatedEventIdentifierComplexType[] result = new gov.loc.premis.v3.RelatedEventIdentifierComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "relatedEventIdentifier" element
     */
    public gov.loc.premis.v3.RelatedEventIdentifierComplexType getRelatedEventIdentifierArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RelatedEventIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.RelatedEventIdentifierComplexType)get_store().find_element_user(RELATEDEVENTIDENTIFIER$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "relatedEventIdentifier" element
     */
    public int sizeOfRelatedEventIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(RELATEDEVENTIDENTIFIER$6);
        }
    }
    
    /**
     * Sets array of all "relatedEventIdentifier" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setRelatedEventIdentifierArray(gov.loc.premis.v3.RelatedEventIdentifierComplexType[] relatedEventIdentifierArray)
    {
        check_orphaned();
        arraySetterHelper(relatedEventIdentifierArray, RELATEDEVENTIDENTIFIER$6);
    }
    
    /**
     * Sets ith "relatedEventIdentifier" element
     */
    public void setRelatedEventIdentifierArray(int i, gov.loc.premis.v3.RelatedEventIdentifierComplexType relatedEventIdentifier)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RelatedEventIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.RelatedEventIdentifierComplexType)get_store().find_element_user(RELATEDEVENTIDENTIFIER$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(relatedEventIdentifier);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "relatedEventIdentifier" element
     */
    public gov.loc.premis.v3.RelatedEventIdentifierComplexType insertNewRelatedEventIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RelatedEventIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.RelatedEventIdentifierComplexType)get_store().insert_element_user(RELATEDEVENTIDENTIFIER$6, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "relatedEventIdentifier" element
     */
    public gov.loc.premis.v3.RelatedEventIdentifierComplexType addNewRelatedEventIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RelatedEventIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.RelatedEventIdentifierComplexType)get_store().add_element_user(RELATEDEVENTIDENTIFIER$6);
            return target;
        }
    }
    
    /**
     * Removes the ith "relatedEventIdentifier" element
     */
    public void removeRelatedEventIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(RELATEDEVENTIDENTIFIER$6, i);
        }
    }
    
    /**
     * Gets array of all "relatedEnvironmentPurpose" elements
     */
    public gov.loc.premis.v3.StringPlusAuthority[] getRelatedEnvironmentPurposeArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(RELATEDENVIRONMENTPURPOSE$8, targetList);
            gov.loc.premis.v3.StringPlusAuthority[] result = new gov.loc.premis.v3.StringPlusAuthority[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "relatedEnvironmentPurpose" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getRelatedEnvironmentPurposeArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(RELATEDENVIRONMENTPURPOSE$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "relatedEnvironmentPurpose" element
     */
    public int sizeOfRelatedEnvironmentPurposeArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(RELATEDENVIRONMENTPURPOSE$8);
        }
    }
    
    /**
     * Sets array of all "relatedEnvironmentPurpose" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setRelatedEnvironmentPurposeArray(gov.loc.premis.v3.StringPlusAuthority[] relatedEnvironmentPurposeArray)
    {
        check_orphaned();
        arraySetterHelper(relatedEnvironmentPurposeArray, RELATEDENVIRONMENTPURPOSE$8);
    }
    
    /**
     * Sets ith "relatedEnvironmentPurpose" element
     */
    public void setRelatedEnvironmentPurposeArray(int i, gov.loc.premis.v3.StringPlusAuthority relatedEnvironmentPurpose)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(RELATEDENVIRONMENTPURPOSE$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(relatedEnvironmentPurpose);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "relatedEnvironmentPurpose" element
     */
    public gov.loc.premis.v3.StringPlusAuthority insertNewRelatedEnvironmentPurpose(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().insert_element_user(RELATEDENVIRONMENTPURPOSE$8, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "relatedEnvironmentPurpose" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewRelatedEnvironmentPurpose()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(RELATEDENVIRONMENTPURPOSE$8);
            return target;
        }
    }
    
    /**
     * Removes the ith "relatedEnvironmentPurpose" element
     */
    public void removeRelatedEnvironmentPurpose(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(RELATEDENVIRONMENTPURPOSE$8, i);
        }
    }
    
    /**
     * Gets the "relatedEnvironmentCharacteristic" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getRelatedEnvironmentCharacteristic()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(RELATEDENVIRONMENTCHARACTERISTIC$10, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "relatedEnvironmentCharacteristic" element
     */
    public boolean isSetRelatedEnvironmentCharacteristic()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(RELATEDENVIRONMENTCHARACTERISTIC$10) != 0;
        }
    }
    
    /**
     * Sets the "relatedEnvironmentCharacteristic" element
     */
    public void setRelatedEnvironmentCharacteristic(gov.loc.premis.v3.StringPlusAuthority relatedEnvironmentCharacteristic)
    {
        generatedSetterHelperImpl(relatedEnvironmentCharacteristic, RELATEDENVIRONMENTCHARACTERISTIC$10, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "relatedEnvironmentCharacteristic" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewRelatedEnvironmentCharacteristic()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(RELATEDENVIRONMENTCHARACTERISTIC$10);
            return target;
        }
    }
    
    /**
     * Unsets the "relatedEnvironmentCharacteristic" element
     */
    public void unsetRelatedEnvironmentCharacteristic()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(RELATEDENVIRONMENTCHARACTERISTIC$10, 0);
        }
    }
}
