***注：本页信息已过期。如需了解更多信息，请联系项目管理员。***

# 格式规范策略

RODA支持任何文件格式，但转换工具只能自动转换成有限的文件格式和对象类做为保存格式（规范）。

本页你将看到格式和对象类的默认映射，以及规范策略建议表。

## 摄取格式与对象类的默认映射

| *类* | *格式* | *Identification (MIME, PRONOM, Extensions)* |
| --------- |---------- | ------------- |
| 文本 | PDF | application/pdf<br>fmt/14, fmt/15, fmt/16, fmt/17, fmt/18, fmt/19, fmt/20, fmt/276, fmt/95, fmt/354, fmt/476, fmt/477, fmt/478, fmt/479, fmt/480, fmt/481, fmt/493, fmt/144, fmt/145, fmt/146, fmt/147, fmt/148, fmt/157, fmt/488, fmt/489, fmt/490, fmt/491<br>.pdf |
| 文本 | Microsoft Word | application/msword<br>fmt/40, fmt/609, fmt/39, x-fmt/2, x-fmt/129, x-fmt/273, x-fmt/274, x-fmt/275, x-fmt/276, fmt/37, fmt/38<br>.doc |
| 文本 | Microsoft Word Open XML document | application/vnd.openxmlformats-officedocument.wordprocessingml.document<br>fmt/412<br>.docx |
| 文本 | Open Office Text | application/vnd.oasis.opendocument.text<br>x-fmt/3, fmt/136, fmt/290, fmt/291 <br>.odt |
| 文本 | 富文本格式 | application/rtf<br>fmt/355, fmt/45, fmt/50, fmt/52 ,fmt/53 <br>.rtf |
| 文本 | 纯文本 | text/plain<br>x-fmt/111 <br>.txt |
| 演示 | Microsoft Powerpoint | application/vnd.ms-powerpoint<br>x-fmt/88, fmt/125, fmt/126, fmt/181 <br>.ppt |
| 演示 | Microsoft Powerpoint Open XML document | application/vnd.openxmlformats-officedocument.presentationml.presentation<br/>fmt/215 <br>.pptx |
| 演示 | Open Office Presentation | application/vnd.oasis.opendocument.presentation<br> fmt/138, fmt/292, fmt/293 <br>.odp |
| 电子表格 | Microsoft Excel | application/vnd.ms-excel<br>fmt/55, fmt/56, fmt/57, fmt/59, fmt/61, fmt/62 <br>.xls |
| 电子表格 | Microsoft Excel Open XML document | application/vnd.openxmlformats-officedocument.spreadsheetml<br>fmt/214 <br>.xlsx |
| 电子表格 | Open Office Spreadsheet | application/ vnd.oasis.opendocument.spreadsheet<br>fmt/137, fmt/294, fmt/295 <br>.ods |
| 图像 | TIFF | image/tiff<br>fmt/152, x-fmt/399, x-fmt/388, x-fmt/387, fmt/155, fmt/353, fmt/154, fmt/153, fmt/156 <br>.tiff .tif |
| 图像 | JPEG | image/jpeg<br>fmt/41, fmt/42, x-fmt/398, x-fmt/390, x-fmt/391, fmt/43, fmt/44, fmt/112 <br>.jpeg .jpg |
| 图像 | PNG | image/png<br>fmt/11,fmt/12,fmt/13 <br>.png |
| 图像 | BMP | image/bmp<br>x-fmt/270,fmt/115,fmt/118,fmt/119,fmt/114,fmt/116,fmt/117 <br>.bmp |
| 图像 | GIF | image/gif<br>fmt/3, fmt/4 <br>.gif |
| 图像 | ICO | image/ico<br>x-fmt/418 <br>.ico |
| 图像 | XPM | image/xpm<br>x-fmt/208 <br>.xpm |
| 图像 | TGA | image/tga<br>fmt/402,x-fmt/367 <br>.tga |
| 数据库<sup>1</sup> | DBML | application/dbml <br>.xml |
| 数据库<sup>1</sup> | DBML+FILES | application/dbml+octet-stream <br>.xml .bin |
| 音频 | Wave | audio/wav <br>.wav |
| 音频 | MP3 | audio/mpeg <br>.mp3 |
| 音频 | MP4 | audio/mp4 <br>.mp4 |
| 音频 | Flac | audio/flac <br>.flac |
| 音频 | AIFF | audio/aiff <br>.aif .aiff |
| 音频 | Ogg Vorbis | audio/ogg <br>.ogg |
| 音频 | Windows Media Audio | audio/x-ms-wma <br>.wma |
| 视频 | Mpeg 1 | video/mpeg <br>.mpg .mpeg |
| 视频 | Mpeg 2 | video/mpeg2 <br>.vob .mpv2 mp2v |
| 视频 | Mpeg 4 | video/mp4 <br>.mp4 |
| 视频 | Audio Video Interlave | video/avi <br>.avi |
| 视频 | Windows Media Video | video/x-ms-wmv <br>.wmv |
| 视频 | Quicktime | video/quicktime <br>.mov .qt |
| 矢量图形 | Adobe Illustrator | application/illustrator<br>x-fmt/20, fmt/419, fmt/420, fmt/422, fmt/423, fmt/557, fmt/558, fmt/559, fmt/560, fmt/561, fmt/562, fmt/563, fmt/564, fmt/565 <br>.ai |
| 矢量图形 | CorelDraw | application/coreldraw<br>fmt/467, fmt/466, fmt/465, fmt/464, fmt/427, fmt/428, fmt/429, fmt/430, x-fmt/291, x-fmt/292, x-fmt/374, x-fmt/375, x-fmt/378, x-fmt/379, x-fmt/29 <br> .cdr |
| 矢量图形 | Autocad image | image/vnd.dwg<br>fmt/30, fmt/31, fmt/32, fmt/33, fmt/34, fmt/35, fmt/36, fmt/434, x-fmt/455, fmt/21, fmt/22, fmt/23, fmt/24, fmt/25, fmt/26, fmt/27, fmt/28, fmt/29, fmt/531 <br>.dwg |  
| 电子邮件 | EML | message/rfc822<br>fmt/278 <br>.eml |
| 电子邮件 | Microsoft Outlook email | application/vnd.ms-outlook<br>x-fmt/430, x-fmt/431 <br>.msg |

## 每个对象类的建议保存格式

| *类* | *格式* | *媒体类型* | *扩展名* | *著录* |
|---------|----------|-------------|--------------|---------------|
| 文本    | PDF/A | application/pdf or text/plain<sup>2</sup> | .pdf | 用于归档的PDF|
| 演示 | PDF/A | application/pdf | .pdf | 用于归档的PDF|
| 电子表格 | PDF/A | application/pdf | .pdf | 用于归档的PDF|
| 图像 | METS+TIFF | image/mets+tiff | .xml .tiff | 包含结构和未压缩TIFF图像的METS XML文件 |
| 音频 | Wave | audio/wav | .wav | Wave音频格式 |
| 视频 | MPEG-2 | video/mpeg2 | .mpeg .mpg |MPEG 2视频格式，采用DVD内部结构 |
| 数据库 | SIARD 2 |  | .siard | 用于归档关系数据库的开放格式 |
