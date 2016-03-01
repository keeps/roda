/*
 * An XML document type.
 * Localname: environmentDesignation
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EnvironmentDesignationDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one environmentDesignation(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EnvironmentDesignationDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EnvironmentDesignationDocument
{
    private static final long serialVersionUID = 1L;
    
    public EnvironmentDesignationDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENVIRONMENTDESIGNATION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentDesignation");
    
    
    /**
     * Gets the "environmentDesignation" element
     */
    public gov.loc.premis.v3.EnvironmentDesignationComplexType getEnvironmentDesignation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EnvironmentDesignationComplexType target = null;
            target = (gov.loc.premis.v3.EnvironmentDesignationComplexType)get_store().find_element_user(ENVIRONMENTDESIGNATION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "environmentDesignation" element
     */
    public void setEnvironmentDesignation(gov.loc.premis.v3.EnvironmentDesignationComplexType environmentDesignation)
    {
        generatedSetterHelperImpl(environmentDesignation, ENVIRONMENTDESIGNATION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "environmentDesignation" element
     */
    public gov.loc.premis.v3.EnvironmentDesignationComplexType addNewEnvironmentDesignation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EnvironmentDesignationComplexType target = null;
            target = (gov.loc.premis.v3.EnvironmentDesignationComplexType)get_store().add_element_user(ENVIRONMENTDESIGNATION$0);
            return target;
        }
    }
}
