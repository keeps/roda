<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 *
 * @version $Id: image_jpeg__link.inc.php 10239 2007-04-01 09:51:41Z cybot_tm $
 */

/**
 *
 */
function PMA_transformation_image_jpeg__link($buffer, $options = array(), $meta = '') {
    require_once './libraries/transformations/global.inc.php';

    $transform_options = array ('string' => '<a href="transformation_wrapper.php' . $options['wrapper_link'] . '" alt="[__BUFFER__]">[BLOB]</a>');
    $buffer = PMA_transformation_global_html_replace($buffer, $transform_options);

    return $buffer;
}

?>
