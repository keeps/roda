<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 * Simple script to set correct charset for the license
 *
 * Note: please do not fold this script into a general script
 * that would read any file using a GET parameter, it would open a hole
 *
 * @version $Id: license.php 10142 2007-03-20 10:32:13Z cybot_tm $
 */

/**
 *
 */
header('Content-type: text/plain; charset=iso-8859-1');
readfile('LICENSE');
?>
