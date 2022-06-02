# Disposal 

## Disposal Schedule

Please refer to *Help* > *Usage* > *Disposal policies* for more information about disposal schedules. 

### 1. Configure RODA to show fields in retention trigger element identifier

The retention trigger element identifier is populated using the advanced search field items. From those fields the ones that have `date_interval` type will be selected and used in the calculation of retention period.
Please refer to *Help* > *Usage* > *Advanced search* for more information about add a new advanced search field item. 

## Disposal rule

Please refer to *Help* > *Usage* > *Disposal policies* for more information about disposal rules. 

### 1. Configure RODA to show fields in selection method 'metadata field'

The metadata field is populated using the advanced search field items. From those fields the ones that have `text` type will be selected. RODA can be configured to ignore some of these fields. In order to do that, change your `roda-wui.properties` to add a new blacklist metadata. By default, RODA shows all `text` type descriptive metadata.

```javaproperties
ui.disposal.rule.blacklist.condition = description
```

Please refer to *Help* > *Usage* > *Advanced search* for more information about add a new advanced search field item.

Please refer to *Help* > *Configuration* > *Metadata formats* for more information about descriptive metadata configuration on RODA.
