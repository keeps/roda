<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 * Displays form for creating database (if user has privileges for that)
 *
 * @version $Id: display_create_database.lib.php 10239 2007-04-01 09:51:41Z cybot_tm $
 */

/**
 *
 */
require_once './libraries/check_user_privileges.lib.php';

if ($is_create_db_priv) {
    // The user is allowed to create a db
    ?>
        <form method="post" action="db_create.php"><b>
            <?php echo '<label for="text_create_db">' . $strCreateNewDatabase . '</label>&nbsp;' . PMA_showMySQLDocu('SQL-Syntax', 'CREATE_DATABASE'); ?></b><br />
            <?php echo PMA_generate_common_hidden_inputs('', '', 5); ?>
            <input type="hidden" name="reload" value="1" />
            <input type="text" name="db" value="<?php echo $db_to_create; ?>" maxlength="64" class="textfield" id="text_create_db"/>
            <?php
    if (PMA_MYSQL_INT_VERSION >= 40101) {
        require_once './libraries/mysql_charsets.lib.php';
        echo PMA_generateCharsetDropdownBox(PMA_CSDROPDOWN_COLLATION, 'db_collation', null, null, TRUE, 5);
    }
            ?>
            <input type="submit" value="<?php echo $strCreate; ?>" id="buttonGo" />
        </form>
    <?php
} else {
    ?>
    <!-- db creation no privileges message -->
        <b><?php echo $strCreateNewDatabase . ':&nbsp;' . PMA_showMySQLDocu('SQL-Syntax', 'CREATE_DATABASE'); ?></b><br />
        <?php
              echo '<span class="noPrivileges">'
                 . ($cfg['ErrorIconic'] ? '<img src="' . $pmaThemeImage . 's_error2.png" width="11" height="11" hspace="2" border="0" align="middle" />' : '')
                 . '' . $strNoPrivileges .'</span>';
} // end create db form or message
?>
