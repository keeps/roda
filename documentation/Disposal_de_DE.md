# Entsorgung

## Entsorgungsplan

Weitere Informationen zu den Entsorgungsplänen finden Sie unter *Hilfe* > *Nutzung* > *Entsorgungsrichtlinien*.

### 1. Konfigurieren Sie RODA, um Felder im Elementidentifikator des Aufbewahrungsauslösers anzuzeigen

Der Elementidentifikator des Aufbewahrungsauslösers wird über die Felder der erweiterten Suche eingegeben. Es werden Felder mit dem Typ `Datum_Intervall` ausgewählt und für die Berechnung der Aufbewahrungsfrist verwendet.
Wie ein neues Feld in die erweiterte Suche hinzugefügt werden kann, ist unter *Hilfe* > *Benutzung* > *Erweiterte Suche* zu finden.

## Kassationsregel

Weitere Informationen zu den Entsorgungsrichtlinien finden Sie unter *Hilfe* > *Nutzung* > *Entsorgungsrichtlinien*.

### 1. Konfigurieren Sie RODA, um Felder in der Auswahlmethode "Metadatenfeld" anzuzeigen

Das Metadatenfeld wird mit den Feldern der erweiterten Suche ausgefüllt. Aus diesen Feldern werden diejenigen ausgewählt, die den Typ `Text` haben. RODA kann so konfiguriert werden, dass einige dieser Felder ignoriert werden. Ändern Sie dazu Ihre `roda-wui.properties` und fügen Sie neue Blacklist-Metadaten hinzu. Standardmäßig zeigt RODA alle beschreibenden Metadaten des Typs `Text` an.

```javaproperties
ui.disposal.rule.blacklist.condition = description
```

Weitere Informationen zu Hinzufügen vom neuen erweiterten Suchfeldelement finden Sie unter *Hilfe* > *Benutzung* > *Erweiterte Suche*.

Weitere Informationen zur Konfiguration von beschreibenden Metadaten auf RODA finden Sie unter *Hilfe* > *Konfiguration* > *Metadatenformate*.
