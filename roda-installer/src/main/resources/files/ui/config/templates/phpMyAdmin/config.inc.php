<?php
/**
 * Server(s) configuration
 */
$i = 0;
// The $cfg['Servers'] array starts with $cfg['Servers'][1].  Do not use $cfg['Servers'][0].
// You can disable a server config entry by setting host to ''.
$i++;

/* Authentication type */
$cfg['Servers'][$i]['auth_type'] = 'config';
$cfg['Servers'][$i]['user'] = 'guest';
$cfg['Servers'][$i]['password'] = ''; 
$cfg['blowfish_secret'] = 'af9fecc645acd5cb';
/* Server parameters */
$cfg['Servers'][$i]['host'] = 'localhost';
$cfg['Servers'][$i]['connect_type'] = 'tcp';
$cfg['Servers'][$i]['compress'] = false;
/* Select mysqli if your server has it */
$cfg['Servers'][$i]['extension'] = 'mysqli';
/* Optional: User for advanced features */
$cfg['Servers'][$i]['controluser'] = 'RODAWUI_MYSQL_USER';
$cfg['Servers'][$i]['controlpass'] = 'RODAWUI_MYSQL_USER_PASSWD';
/* Optional: Advanced phpMyAdmin features */
$cfg['Servers'][$i]['pmadb'] = 'phpmyadmin';
$cfg['Servers'][$i]['bookmarktable'] = 'pma_bookmark';
$cfg['Servers'][$i]['relation'] = 'pma_relation';
$cfg['Servers'][$i]['table_info'] = 'pma_table_info';
$cfg['Servers'][$i]['table_coords'] = 'pma_table_coords';
$cfg['Servers'][$i]['pdf_pages'] = 'pma_pdf_pages';
$cfg['Servers'][$i]['column_info'] = 'pma_column_info';
$cfg['Servers'][$i]['history'] = 'pma_history';
$cfg['Servers'][$i]['designer_coords'] = 'pma_designer_coords';

$cfg['PmaAbsoluteUri'] = 'http://localhost/phpMyAdmin/';
$cfg['Servers'][$i]['hide_db'] = '(mysql|information_schema|phpmyadmin)';
//$cfg['Servers'][$i]['hide_db'] = '.*';

/*
 * End of servers configuration
 */

/*
 * Directories for saving/loading files from server
 */
$cfg['UploadDir'] = '';
$cfg['SaveDir'] = '';

/*
 * Layout configurations
 */

$cfg['AllowUserDropDatabase'] = false;
$cfg['LeftDisplayLogo'] = false;
$cfg['ShowPhpInfo'] = false;
$cfg['ShowChgPassword'] = false;
$cfg['ShowCreateDb'] = false;

$cfg['ThemeManager'] = true;
$cfg['ThemeDefault'] = 'xampp';

