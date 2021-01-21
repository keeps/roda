# Disposal Policies

## Disposal Schedule

Disposal schedules set the minimum requirements for the maintenance, retention or destruction actions to be taken in the existing or future intellectual entities in this repository. A intellectual entity may only be destroyed as part of a disposal process governed by the disposal schedule assigned to that entity. It is the intellectual entity’s disposal schedule that determines how long a record is retained and how it is subsequently disposed of at the end of its retention period.

| *Field* | *Description* | *Mandatory* |
| --------- |---------- | ------------- |
| Title | The identifying name or title of the disposal schedule | True |
| Description | Description of the disposal schedule | False |
| Mandate | Textual reference to a legal or other instrument that provides the authority for a disposal schedule | False |
| Scope Notes | Guidance to authorised users indicating how best to apply a particular entity and stating any organisational policies or constraints on its use | False |
| Disposal Action | Code describing the action to be taken on disposal of the record (Possible values: Retain permanently, Review, Destroy) | True |
| Retention trigger element identifier | The descriptive metadata field used to calculate the retention period * | True (If Disposal action code is different from Retain permanently) |
| Retention period | Number of days, weeks, months or years specified for retaining a record after the retention period is triggered | True (If Disposal action code is different from Retain permanently) |









## Disposal Rules

## Disposal Holds