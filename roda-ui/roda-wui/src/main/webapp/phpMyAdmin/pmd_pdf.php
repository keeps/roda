<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 *
 * @version $Id: pmd_pdf.php 10416 2007-05-30 16:55:18Z lem9 $
 * @package phpMyAdmin-Designer
 */

/**
 *
 */
include_once 'pmd_common.php';
if (! isset($scale)) {
    $no_die_save_pos = 1;
    include_once 'pmd_save_pos.php';
}
require_once './libraries/relation.lib.php';

if (isset($scale)) {
    if (empty($pdf_page_number)) {
        die("<script>alert('Pages not found!');history.go(-2);</script>");
    }

    $pmd_table = PMA_backquote($GLOBALS['cfgRelation']['db']) . '.' . PMA_backquote($GLOBALS['cfgRelation']['designer_coords']);
    $pma_table = PMA_backquote($GLOBALS['cfgRelation']['db']) . '.' . PMA_backquote($cfgRelation['table_coords']);

    if (isset($exp)) {

        $sql = "REPLACE INTO " . $pma_table . " (db_name, table_name, pdf_page_number, x, y) SELECT db_name, table_name, " . $pdf_page_number . ", ROUND(x/" . $scale . ") , ROUND(y/" . $scale . ") y FROM " . $pmd_table . " WHERE db_name = '" . $db . "'";

        PMA_query_as_cu($sql,TRUE,PMA_DBI_QUERY_STORE);
    }

    if (isset($imp)) {
        PMA_query_as_cu(
        'UPDATE ' . $pma_table . ',' . $pmd_table .
        ' SET ' . $pmd_table . '.`x`= ' . $pma_table . '.`x` * '. $scale . ',
        ' . $pmd_table . '.`y`= ' . $pma_table . '.`y` * '.$scale.'
        WHERE
        ' . $pmd_table . '.`db_name`=' . $pma_table . '.`db_name`
        AND
        ' . $pmd_table . '.`table_name` = ' . $pma_table . '.`table_name`
        AND
        ' . $pmd_table . '.`db_name`=\''.$db.'\'
        AND pdf_page_number = '.$pdf_page_number.';',TRUE,PMA_DBI_QUERY_STORE);     }

    die("<script>alert('$strModifications');history.go(-2);</script>");
}
?>
<html>
<head>
<?php if(0){ ?>
<meta http-equiv="Content-Type" content="text/html; charset=windows-1251" />
<link rel="stylesheet" type="text/css" href="pmd/styles/default/style1.css">
<?php } ?>
<meta http-equiv="Content-Type" content="text/html; charset=<?php echo $charset ?>" />
<link rel="stylesheet" type="text/css" href="pmd/styles/<?php echo $GLOBALS['PMD']['STYLE'] ?>/style1.css">
<title>Designer</title>
</head>
<body>
<br>
<div style="text-align:center; font-weight:bold;">
  <form name="form1" method="post" action="pmd_pdf.php?server=<?php echo $server; ?>&db=<?php echo $db; ?>&token=<?php echo $token; ?>">
    <p><?php echo $strExportImportToScale; ?>:
      <select name="scale">
        <option value="1">1:1</option>
        <option value="2">1:2</option>
    <option value="3" selected>1:3 (<?php echo $strRecommended; ?>)</option>
        <option value="4">1:4</option>
        <option value="5">1:5</option>
        </select>
      </p>
  <p><?php echo $strToFromPage; ?>:

      <select name="pdf_page_number">
      <?php
      $table_info_result = PMA_query_as_cu('SELECT * FROM '.PMA_backquote($GLOBALS['cfgRelation']['db']) . '.' . PMA_backquote($cfgRelation['pdf_pages']).'
                                             WHERE db_name = \''.$db.'\'');
      while($page = PMA_DBI_fetch_assoc($table_info_result))
      {
      ?>
      <option value="<?php echo $page['page_nr'] ?>"><?php echo $page['page_descr'] ?></option>
      <?php
      }
      ?>
      </select>
      <br>
      <br>
  <input type="submit" name="exp" value="<?php echo $strExport; ?>">
  <input type="submit" name="imp" value="<?php echo $strImport; ?>">
        </p>
  </form>
</div>
</body>
</html>

