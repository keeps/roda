/*
 * An XML document type.
 * Localname: significantPropertiesType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SignificantPropertiesTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one significantPropertiesType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class SignificantPropertiesTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.SignificantPropertiesTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public SignificantPropertiesTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNIFICANTPROPERTIESTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "significantPropertiesType");
    
    
    /**
     * Gets the "significantPropertiesType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getSignificantPropertiesType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(SIGNIFICANTPROPERTIESTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "significantPropertiesType" element
     */
    public void setSignificantPropertiesType(gov.loc.premis.v3.StringPlusAuthority significantPropertiesType)
    {
        generatedSetterHelperImpl(significantPropertiesType, SIGNIFICANTPROPERTIESTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "significantPropertiesType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewSignificantPropertiesType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(SIGNIFICANTPROPERTIESTYPE$0);
            return target;
        }
    }
}
