<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 *
 * @version $Id: chk_rel.php 10239 2007-04-01 09:51:41Z cybot_tm $
 */

/**
 * Gets some core libraries
 */
require_once './libraries/common.inc.php';
require_once './libraries/db_common.inc.php';
require_once './libraries/relation.lib.php';


/**
 * Gets the relation settings
 */
$cfgRelation = PMA_getRelationsParam(TRUE);


/**
 * Displays the footer
 */
require_once './libraries/footer.inc.php';
?>
