<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 *
 * @version $Id: text_plain__imagelink.inc.php 10239 2007-04-01 09:51:41Z cybot_tm $
 */

/**
 *
 */
function PMA_transformation_text_plain__imagelink($buffer, $options = array(), $meta = '') {
    require_once './libraries/transformations/global.inc.php';

    $transform_options = array ('string' => '<a href="' . (isset($options[0]) ? $options[0] : '') . $buffer . '" target="_blank"><img src="' . (isset($options[0]) ? $options[0] : '') . $buffer . '" border="0" width="' . (isset($options[1]) ? $options[1] : 100) . '" height="' . (isset($options[2]) ? $options[2] : 50) . '" />' . $buffer . '</a>');
    $buffer = PMA_transformation_global_html_replace($buffer, $transform_options);
    return $buffer;
}

?>
