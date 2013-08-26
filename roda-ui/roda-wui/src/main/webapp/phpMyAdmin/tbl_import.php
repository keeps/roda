<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 *
 * @version $Id: tbl_import.php 10239 2007-04-01 09:51:41Z cybot_tm $
 */

/**
 *
 */
require_once './libraries/common.inc.php';

/**
 * Gets tables informations and displays top links
 */
require_once './libraries/tbl_common.php';
$url_query .= '&amp;goto=tbl_import.php&amp;back=tbl_import.php';

require_once './libraries/tbl_info.inc.php';
/**
 * Displays top menu links
 */
require_once './libraries/tbl_links.inc.php';

$import_type = 'table';
require_once './libraries/display_import.lib.php';

/**
 * Displays the footer
 */
require_once './libraries/footer.inc.php';
?>

