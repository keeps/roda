/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 * function used in server privilege pages
 *
 * @version $Id: server_privileges.js 10929 2007-11-15 17:51:46Z lem9 $
 */

/**
 * Validates the password field in a form
 *
 * @param   object   the form
 *
 * @return  boolean  whether the field value is valid or not
 */
function checkPassword(the_form)
{
    // Did the user select 'no password'?
    if (typeof(the_form.elements['nopass']) != 'undefined' && the_form.elements['nopass'][0].checked) {
        return true;
    } else if (typeof(the_form.elements['pred_password']) != 'undefined' && (the_form.elements['pred_password'].value == 'none' || the_form.elements['pred_password'].value == 'keep')) {
        return true;
    }

    // Validates
    if (the_form.elements['pma_pw'].value == '') {
        alert(jsPasswordEmpty);
        the_form.elements['pma_pw2'].value = '';
        the_form.elements['pma_pw'].focus();
        return false;
    } else if (the_form.elements['pma_pw'].value != the_form.elements['pma_pw2'].value) {
        alert(jsPasswordNotSame);
        the_form.elements['pma_pw'].value  = '';
        the_form.elements['pma_pw2'].value = '';
        the_form.elements['pma_pw'].focus();
        return false;
    } // end if...else if

    return true;
} // end of the 'checkPassword()' function


/**
 * Validates the "add a user" form
 *
 * @return  boolean  whether the form is validated or not
 */
function checkAddUser(the_form)
{
    if (the_form.elements['pred_hostname'].value == 'userdefined' && the_form.elements['hostname'].value == '') {
        alert(jsHostEmpty);
        the_form.elements['hostname'].focus();
        return false;
    }

    if (the_form.elements['pred_username'].value == 'userdefined' && the_form.elements['username'].value == '') {
        alert(jsUserEmpty);
        the_form.elements['username'].focus();
        return false;
    }

    return checkPassword(the_form);
} // end of the 'checkAddUser()' function


/**
 * Generate a new password, which may then be copied to the form
 * with suggestPasswordCopy().
 *
 * @param   string   the form name
 *
 * @return  boolean  always true
 */
function suggestPassword() {
    // restrict the password to just letters and numbers to avoid problems:
    // "editors and viewers regard the password as multiple words and
    // things like double click no longer work"
    var pwchars = "abcdefhjmnpqrstuvwxyz23456789ABCDEFGHJKLMNPQRSTUVWYXZ";
    var passwordlength = 16;    // do we want that to be dynamic?  no, keep it simple :)
    var passwd = document.getElementById('generated_pw');
    passwd.value = '';

    for ( i = 0; i < passwordlength; i++ ) {
        passwd.value += pwchars.charAt( Math.floor( Math.random() * pwchars.length ) )
    }
    return passwd.value;
}


/**
 * Copy the generated password (or anything in the field) to the form
 *
 * @param   string   the form name
 *
 * @return  boolean  always true
 */
function suggestPasswordCopy() {
    document.getElementById('text_pma_pw').value = document.getElementById('generated_pw').value;
    document.getElementById('text_pma_pw2').value = document.getElementById('generated_pw').value;
    return true;
}
