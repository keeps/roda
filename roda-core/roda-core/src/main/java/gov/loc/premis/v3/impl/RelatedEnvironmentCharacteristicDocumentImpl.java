/*
 * An XML document type.
 * Localname: relatedEnvironmentCharacteristic
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RelatedEnvironmentCharacteristicDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one relatedEnvironmentCharacteristic(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RelatedEnvironmentCharacteristicDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RelatedEnvironmentCharacteristicDocument
{
    private static final long serialVersionUID = 1L;
    
    public RelatedEnvironmentCharacteristicDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RELATEDENVIRONMENTCHARACTERISTIC$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relatedEnvironmentCharacteristic");
    
    
    /**
     * Gets the "relatedEnvironmentCharacteristic" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getRelatedEnvironmentCharacteristic()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(RELATEDENVIRONMENTCHARACTERISTIC$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "relatedEnvironmentCharacteristic" element
     */
    public void setRelatedEnvironmentCharacteristic(gov.loc.premis.v3.StringPlusAuthority relatedEnvironmentCharacteristic)
    {
        generatedSetterHelperImpl(relatedEnvironmentCharacteristic, RELATEDENVIRONMENTCHARACTERISTIC$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
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
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(RELATEDENVIRONMENTCHARACTERISTIC$0);
            return target;
        }
    }
}
