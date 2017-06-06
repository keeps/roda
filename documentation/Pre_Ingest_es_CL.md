# Instrucciones para la pre-ingesta

El proceso de pre-ingesta representa la capacidad de un Productor de paquetes Crear un (SIP) que contienen datos y metadata (en una estructura bien definida) con el fin de someterlos al repositorio para la ingesta. Para los SIP creados se espera que cumpla con el las politicas establecidas por (o negociados con) el repositorio. El proceso de pre-ingesta generalmente comprende algunos o todas las siguientes actividades:

## Negociación

Esta actividad consiste en la definición de los términos, pre condiciones y requisitos de contenido, y infromación de acompañamiento (por ejemplo, metadatos, documentación, contratos, etc.), que serán enviados al repositorio por el productor. Parte de esta actividad consiste de la creación de un esquema de clasificación de base (o lista de colecciones base) en que el productor puede depositar nuevos items de información.

## Presentación del acuerdo

Esta actividad consiste en la firma de un acuerdo escrito entre el productor y el repositorio que especifica el tipo de contenido y todos los requisitos legales y técnicos que ambas partes esperan cumplir.

[Download classification scheme](/api/v1/classification_plans) (nota: la descarga del esquema de clasificación requiere una instancia de RODA)

## Paquetes Presentación de información (SIP)

Esta actividad consiste en la preparación de uno o más paquetes de envío de información (SIP) de acuerdo con la técnica y requisitos técnicos definidos por el Acuerdo de Presentación. Para facilitar la creación del SIP, los productores puedan beneficiarse de la herramienta RODA-in. La herramienta y su documentación están disponibles en [http://rodain.roda-community.org](http://rodain.roda-community.org "rodain").

[Descargar RODA-in](http://rodain.roda-community.org)

## Transferencia de materiales

Esta actividad consiste en la transferencia electrónica de Submission Paquetes Information (SIP) desde el productor hasta el repositorio. SIPs son almacenado temporalmente en un área de cuarentena a la espera de ser procesados por el repositorio. Si se han realizado cambios a la esquema de clasificación externamente, por ejemplo mediante el uso de una herramienta como RODA-in, el nuevo esquema de clasificación, deben cargarse en el repositorio anterior a cualquier actividad de ingesta.

Hay varias maneras en que los productores pueden enviar paquetes al repositorio. Estos incluyen, pero no se limitan a los siguientes opciones:

### Transferencia FTP

1. Conectarse a [ftp://address] y utilice el credentials entregadas por el Archivo con el fin de iniciar la sesión.
2. Crear una carpeta para guardar los paquetes para ser parte del lote de ingesta individual (opcional).
3. Copiar todos los SIP creados en la nueva carpeta.
4. Informar al Archivo que el contenido está listo para ser ingerido.

### Transferencia con medio externo

1. Guardar SIPs en un soporte externo (por ejemplo, CD, disco USB, etc.)
2. Se debe entregar en la siguiente dirección: [Repository address]

## Proceso de ingesta

Después de la transferencia, se seleccionarán los SIP para la ingesta por el Archivo. El proceso de ingesta proporciona servicios y funciones para aceptar los paquetes de información submission (SIP) enviados por los productores de manera de preparar el contenido para el almacenamiento y la gestión dentro del archivo.

Las funciones de ingesta incluyen la recepción de los SIP, realizando aseguramiento de calidad en SIP, generando un paquete de información de archivo (AIP) con el que cumple con el formato y documentation de los estandares definidos por el Archivo, la extracción de información descriptiva de la AIP para la inclusión en el catálogo de archivos y actualizaciones de coordinación para el almacenamiento en el archivo y gestión de datos.
