<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 * Displays form for password change 
 *
 * @version $Id: display_change_password.lib.php 10796 2007-10-16 07:09:50Z cybot_tm $
 */

// loic1: autocomplete feature of IE kills the "onchange" event handler and it
//        must be replaced by the "onpropertychange" one in this case
$chg_evt_handler = (PMA_USR_BROWSER_AGENT == 'IE' && PMA_USR_BROWSER_VER >= 5)
                 ? 'onpropertychange'
                 : 'onchange';

// Displays the form
?>
<form method="post" action="<?php echo $GLOBALS['PMA_PHP_SELF']; ?>" name="chgPassword" onsubmit="return checkPassword(this)">
    <?php   echo PMA_generate_common_hidden_inputs();
            if (strpos($GLOBALS['PMA_PHP_SELF'], 'server_privileges') !== false) {
                echo '<input type="hidden" name="username" value="' . htmlspecialchars($username) . '" />' . "\n"
                   . '<input type="hidden" name="hostname" value="' . htmlspecialchars($hostname) . '" />' . "\n";
            }?>
    <fieldset id="fieldset_change_password">
        <legend><?php echo $GLOBALS['strChangePassword']; ?></legend>
            <table class="data">
            <tr class="odd noclick">
                <td colspan="2">
                    <input type="radio" name="nopass" value="1" onclick="pma_pw.value = ''; pma_pw2.value = ''; this.checked = true" />
            <?php echo $GLOBALS['strNoPassword'] . "\n"; ?>
                </td>
            </tr>
            <tr class="even noclick">
                <td>
                    <input type="radio" name="nopass" value="0" onclick="document.getElementById('pw_pma_pw').focus();" checked="checked " />
            <?php echo $GLOBALS['strPassword']; ?>:&nbsp;
                </td>
                <td>
                    <input type="password" name="pma_pw" id="pw_pma_pw" size="10" class="textfield" <?php echo $chg_evt_handler; ?>="nopass[1].checked = true" />
            &nbsp;&nbsp;
            <?php echo $GLOBALS['strReType']; ?>:&nbsp;
                    <input type="password" name="pma_pw2" id="pw_pma_pw2" size="10" class="textfield" <?php echo $chg_evt_handler; ?>="nopass[1].checked = true" />
                </td>
            </tr>
    <?php

if (PMA_MYSQL_INT_VERSION >= 40102) {
    ?>
    <tr>
        <td>
        <?php echo $strPasswordHashing; ?>:
    </td>
    <td>
        <input type="radio" name="pw_hash" id="radio_pw_hash_new" value="new" checked="checked" />
        <label for="radio_pw_hash_new">
            MySQL&nbsp;4.1+
        </label>
    </td>
    </tr>
    <tr>
        <td>&nbsp;</td>
    <td>
        <input type="radio" name="pw_hash" id="radio_pw_hash_old" value="old" />
        <label for="radio_pw_hash_old">
            <?php echo $strCompatibleHashing; ?>
        </label>
    </td>
    </tr>
    <?php
}
    ?>
        </table>
    </fieldset>
    <fieldset id="fieldset_change_password_footer" class="tblFooters">
            <input type="submit" name="change_pw" value="<?php echo($strGo); ?>" />
    </fieldset>
</form>
