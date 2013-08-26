<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 *
 * @version $Id: text_plain__sql.inc.php 10142 2007-03-20 10:32:13Z cybot_tm $
 */

/**
 *
 */
function PMA_transformation_text_plain__sql($buffer, $options = array(), $meta = '') {
    $result = PMA_SQP_formatHtml(PMA_SQP_parse($buffer));
    // Need to clear error state not to break subsequent queries display.
    PMA_SQP_resetError();
    return $result;
}

?>
