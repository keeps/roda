/**
 * ConversionResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.migrator.common.data;

public class ConversionResult  implements java.io.Serializable {
    private pt.gov.dgarq.roda.core.data.AgentPreservationObject migrationAgent;

    private pt.gov.dgarq.roda.core.data.EventPreservationObject migrationEvent;

    private pt.gov.dgarq.roda.core.data.RepresentationObject representation;

    public ConversionResult() {
    }

    public ConversionResult(
           pt.gov.dgarq.roda.core.data.AgentPreservationObject migrationAgent,
           pt.gov.dgarq.roda.core.data.EventPreservationObject migrationEvent,
           pt.gov.dgarq.roda.core.data.RepresentationObject representation) {
           this.migrationAgent = migrationAgent;
           this.migrationEvent = migrationEvent;
           this.representation = representation;
    }


    /**
     * Gets the migrationAgent value for this ConversionResult.
     * 
     * @return migrationAgent
     */
    public pt.gov.dgarq.roda.core.data.AgentPreservationObject getMigrationAgent() {
        return migrationAgent;
    }


    /**
     * Sets the migrationAgent value for this ConversionResult.
     * 
     * @param migrationAgent
     */
    public void setMigrationAgent(pt.gov.dgarq.roda.core.data.AgentPreservationObject migrationAgent) {
        this.migrationAgent = migrationAgent;
    }


    /**
     * Gets the migrationEvent value for this ConversionResult.
     * 
     * @return migrationEvent
     */
    public pt.gov.dgarq.roda.core.data.EventPreservationObject getMigrationEvent() {
        return migrationEvent;
    }


    /**
     * Sets the migrationEvent value for this ConversionResult.
     * 
     * @param migrationEvent
     */
    public void setMigrationEvent(pt.gov.dgarq.roda.core.data.EventPreservationObject migrationEvent) {
        this.migrationEvent = migrationEvent;
    }


    /**
     * Gets the representation value for this ConversionResult.
     * 
     * @return representation
     */
    public pt.gov.dgarq.roda.core.data.RepresentationObject getRepresentation() {
        return representation;
    }


    /**
     * Sets the representation value for this ConversionResult.
     * 
     * @param representation
     */
    public void setRepresentation(pt.gov.dgarq.roda.core.data.RepresentationObject representation) {
        this.representation = representation;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ConversionResult)) return false;
        ConversionResult other = (ConversionResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.migrationAgent==null && other.getMigrationAgent()==null) || 
             (this.migrationAgent!=null &&
              this.migrationAgent.equals(other.getMigrationAgent()))) &&
            ((this.migrationEvent==null && other.getMigrationEvent()==null) || 
             (this.migrationEvent!=null &&
              this.migrationEvent.equals(other.getMigrationEvent()))) &&
            ((this.representation==null && other.getRepresentation()==null) || 
             (this.representation!=null &&
              this.representation.equals(other.getRepresentation())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getMigrationAgent() != null) {
            _hashCode += getMigrationAgent().hashCode();
        }
        if (getMigrationEvent() != null) {
            _hashCode += getMigrationEvent().hashCode();
        }
        if (getRepresentation() != null) {
            _hashCode += getRepresentation().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ConversionResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://data.common.migrator.roda.dgarq.gov.pt", "ConversionResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("migrationAgent");
        elemField.setXmlName(new javax.xml.namespace.QName("http://data.common.migrator.roda.dgarq.gov.pt", "migrationAgent"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "AgentPreservationObject"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("migrationEvent");
        elemField.setXmlName(new javax.xml.namespace.QName("http://data.common.migrator.roda.dgarq.gov.pt", "migrationEvent"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "EventPreservationObject"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("representation");
        elemField.setXmlName(new javax.xml.namespace.QName("http://data.common.migrator.roda.dgarq.gov.pt", "representation"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "RepresentationObject"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
