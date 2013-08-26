<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 * @version $Id: mrg_myisam.lib.php 10137 2007-03-19 17:55:39Z cybot_tm $
 */

/**
 *
 */
include_once './libraries/engines/merge.lib.php';

/**
 *
 */
class PMA_StorageEngine_mrg_myisam extends PMA_StorageEngine_merge
{
    /**
     * returns string with filename for the MySQL helppage
     * about this storage engne
     *
     * @return  string  mysql helppage filename
     */
    function getMysqlHelpPage()
    {
        return 'merge';
    }
}

?>
