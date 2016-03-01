/*
 * An XML document type.
 * Localname: significantProperties
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SignificantPropertiesDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one significantProperties(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class SignificantPropertiesDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.SignificantPropertiesDocument
{
    private static final long serialVersionUID = 1L;
    
    public SignificantPropertiesDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNIFICANTPROPERTIES$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "significantProperties");
    
    
    /**
     * Gets the "significantProperties" element
     */
    public gov.loc.premis.v3.SignificantPropertiesComplexType getSignificantProperties()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.SignificantPropertiesComplexType target = null;
            target = (gov.loc.premis.v3.SignificantPropertiesComplexType)get_store().find_element_user(SIGNIFICANTPROPERTIES$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "significantProperties" element
     */
    public void setSignificantProperties(gov.loc.premis.v3.SignificantPropertiesComplexType significantProperties)
    {
        generatedSetterHelperImpl(significantProperties, SIGNIFICANTPROPERTIES$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "significantProperties" element
     */
    public gov.loc.premis.v3.SignificantPropertiesComplexType addNewSignificantProperties()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.SignificantPropertiesComplexType target = null;
            target = (gov.loc.premis.v3.SignificantPropertiesComplexType)get_store().add_element_user(SIGNIFICANTPROPERTIES$0);
            return target;
        }
    }
}
