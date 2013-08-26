/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 * general function, usally for data manipulation pages
 *
 * @version $Id: functions.js 10964 2007-12-05 17:01:48Z lem9 $
 */

/**
 * @var sql_box_locked lock for the sqlbox textarea in the querybox/querywindow
 */
var sql_box_locked = false;

/**
 * @var array holds elements which content should only selected once
 */
var only_once_elements = new Array();

/**
 * selects the content of a given object, f.e. a textarea
 *
 * @param   object  element     element of which the content will be selected
 * @param   var     lock        variable which holds the lock for this element
 *                              or true, if no lock exists
 * @param   boolean only_once   if true this is only done once
 *                              f.e. only on first focus
 */
function selectContent( element, lock, only_once ) {
    if ( only_once && only_once_elements[element.name] ) {
        return;
    }

    only_once_elements[element.name] = true;

    if ( lock  ) {
        return;
    }

    element.select();
}

/**
 * Displays an confirmation box before to submit a "DROP DATABASE" query.
 * This function is called while clicking links
 *
 * @param   object   the link
 * @param   object   the sql query to submit
 *
 * @return  boolean  whether to run the query or not
 */
function confirmLinkDropDB(theLink, theSqlQuery)
{
    // Confirmation is not required in the configuration file
    // or browser is Opera (crappy js implementation)
    if (confirmMsg == '' || typeof(window.opera) != 'undefined') {
        return true;
    }

    var is_confirmed = confirm(confirmMsgDropDB + '\n' + confirmMsg + ' :\n' + theSqlQuery);
    if (is_confirmed) {
        theLink.href += '&is_js_confirmed=1';
    }

    return is_confirmed;
} // end of the 'confirmLinkDropDB()' function

/**
 * Displays an confirmation box before to submit a "DROP/DELETE/ALTER" query.
 * This function is called while clicking links
 *
 * @param   object   the link
 * @param   object   the sql query to submit
 *
 * @return  boolean  whether to run the query or not
 */
function confirmLink(theLink, theSqlQuery)
{
    // Confirmation is not required in the configuration file
    // or browser is Opera (crappy js implementation)
    if (confirmMsg == '' || typeof(window.opera) != 'undefined') {
        return true;
    }

    var is_confirmed = confirm(confirmMsg + ' :\n' + theSqlQuery);
    if (is_confirmed) {
        if ( typeof(theLink.href) != 'undefined' ) {
            theLink.href += '&is_js_confirmed=1';
        } else if ( typeof(theLink.form) != 'undefined' ) {
            theLink.form.action += '?is_js_confirmed=1';
        }
    }

    return is_confirmed;
} // end of the 'confirmLink()' function


/**
 * Displays an confirmation box before doing some action
 *
 * @param   object   the message to display
 *
 * @return  boolean  whether to run the query or not
 */
function confirmAction(theMessage)
{
    // TODO: Confirmation is not required in the configuration file
    // or browser is Opera (crappy js implementation)
    if (typeof(window.opera) != 'undefined') {
        return true;
    }

    var is_confirmed = confirm(theMessage);

    return is_confirmed;
} // end of the 'confirmAction()' function


/**
 * Displays an error message if a "DROP DATABASE" statement is submitted
 * while it isn't allowed, else confirms a "DROP/DELETE/ALTER" query before
 * sumitting it if required.
 * This function is called by the 'checkSqlQuery()' js function.
 *
 * @param   object   the form
 * @param   object   the sql query textarea
 *
 * @return  boolean  whether to run the query or not
 *
 * @see     checkSqlQuery()
 */
function confirmQuery(theForm1, sqlQuery1)
{
    // Confirmation is not required in the configuration file
    if (confirmMsg == '') {
        return true;
    }

    // The replace function (js1.2) isn't supported
    else if (typeof(sqlQuery1.value.replace) == 'undefined') {
        return true;
    }

    // js1.2+ -> validation with regular expressions
    else {
        // "DROP DATABASE" statement isn't allowed
        if (noDropDbMsg != '') {
            var drop_re = new RegExp('(^|;)\\s*DROP\\s+(IF EXISTS\\s+)?DATABASE\\s', 'i');
            if (drop_re.test(sqlQuery1.value)) {
                alert(noDropDbMsg);
                theForm1.reset();
                sqlQuery1.focus();
                return false;
            } // end if
        } // end if

        // Confirms a "DROP/DELETE/ALTER" statement
        //
        // TODO: find a way (if possible) to use the parser-analyser
        // for this kind of verification
        // For now, I just added a ^ to check for the statement at
        // beginning of expression

        var do_confirm_re_0 = new RegExp('^\\s*DROP\\s+(IF EXISTS\\s+)?(TABLE|DATABASE|PROCEDURE)\\s', 'i');
        var do_confirm_re_1 = new RegExp('^\\s*ALTER\\s+TABLE\\s+((`[^`]+`)|([A-Za-z0-9_$]+))\\s+DROP\\s', 'i');
        var do_confirm_re_2 = new RegExp('^\\s*DELETE\\s+FROM\\s', 'i');
        if (do_confirm_re_0.test(sqlQuery1.value)
            || do_confirm_re_1.test(sqlQuery1.value)
            || do_confirm_re_2.test(sqlQuery1.value)) {
            var message      = (sqlQuery1.value.length > 100)
                             ? sqlQuery1.value.substr(0, 100) + '\n    ...'
                             : sqlQuery1.value;
            var is_confirmed = confirm(confirmMsg + ' :\n' + message);
            // drop/delete/alter statement is confirmed -> update the
            // "is_js_confirmed" form field so the confirm test won't be
            // run on the server side and allows to submit the form
            if (is_confirmed) {
                theForm1.elements['is_js_confirmed'].value = 1;
                return true;
            }
            // "DROP/DELETE/ALTER" statement is rejected -> do not submit
            // the form
            else {
                window.focus();
                sqlQuery1.focus();
                return false;
            } // end if (handle confirm box result)
        } // end if (display confirm box)
    } // end confirmation stuff

    return true;
} // end of the 'confirmQuery()' function


/**
 * Displays an error message if the user submitted the sql query form with no
 * sql query, else checks for "DROP/DELETE/ALTER" statements
 *
 * @param   object   the form
 *
 * @return  boolean  always false
 *
 * @see     confirmQuery()
 */
function checkSqlQuery(theForm)
{
    var sqlQuery = theForm.elements['sql_query'];
    var isEmpty  = 1;

    // The replace function (js1.2) isn't supported -> basic tests
    if (typeof(sqlQuery.value.replace) == 'undefined') {
        isEmpty      = (sqlQuery.value == '') ? 1 : 0;
        if (isEmpty && typeof(theForm.elements['sql_file']) != 'undefined') {
            isEmpty  = (theForm.elements['sql_file'].value == '') ? 1 : 0;
        }
        if (isEmpty && typeof(theForm.elements['sql_localfile']) != 'undefined') {
            isEmpty  = (theForm.elements['sql_localfile'].value == '') ? 1 : 0;
        }
        if (isEmpty && typeof(theForm.elements['id_bookmark']) != 'undefined') {
            isEmpty  = (theForm.elements['id_bookmark'].value == null || theForm.elements['id_bookmark'].value == '');
        }
    }
    // js1.2+ -> validation with regular expressions
    else {
        var space_re = new RegExp('\\s+');
        if (typeof(theForm.elements['sql_file']) != 'undefined' &&
                theForm.elements['sql_file'].value.replace(space_re, '') != '') {
            return true;
        }
        if (typeof(theForm.elements['sql_localfile']) != 'undefined' &&
                theForm.elements['sql_localfile'].value.replace(space_re, '') != '') {
            return true;
        }
        if (isEmpty && typeof(theForm.elements['id_bookmark']) != 'undefined' &&
                (theForm.elements['id_bookmark'].value != null || theForm.elements['id_bookmark'].value != '') &&
                theForm.elements['id_bookmark'].selectedIndex != 0
                ) {
            return true;
        }
        // Checks for "DROP/DELETE/ALTER" statements
        if (sqlQuery.value.replace(space_re, '') != '') {
            if (confirmQuery(theForm, sqlQuery)) {
                return true;
            } else {
                return false;
            }
        }
        theForm.reset();
        isEmpty = 1;
    }

    if (isEmpty) {
        sqlQuery.select();
        alert(errorMsg0);
        sqlQuery.focus();
        return false;
    }

    return true;
} // end of the 'checkSqlQuery()' function


/**
 * Check if a form's element is empty
 * should be
 *
 * @param   object   the form
 * @param   string   the name of the form field to put the focus on
 *
 * @return  boolean  whether the form field is empty or not
 */
function emptyCheckTheField(theForm, theFieldName)
{
    var isEmpty  = 1;
    var theField = theForm.elements[theFieldName];
    // Whether the replace function (js1.2) is supported or not
    var isRegExp = (typeof(theField.value.replace) != 'undefined');

    if (!isRegExp) {
        isEmpty      = (theField.value == '') ? 1 : 0;
    } else {
        var space_re = new RegExp('\\s+');
        isEmpty      = (theField.value.replace(space_re, '') == '') ? 1 : 0;
    }

    return isEmpty;
} // end of the 'emptyCheckTheField()' function


/**
 * Displays an error message if an element of a form hasn't been completed and
 * should be
 *
 * @param   object   the form
 * @param   string   the name of the form field to put the focus on
 *
 * @return  boolean  whether the form field is empty or not
 */
function emptyFormElements(theForm, theFieldName)
{
    var theField = theForm.elements[theFieldName];
    var isEmpty = emptyCheckTheField(theForm, theFieldName);

    if (isEmpty) {
        theForm.reset();
        theField.select();
        alert(errorMsg0);
        theField.focus();
        return false;
    }

    return true;
} // end of the 'emptyFormElements()' function


/**
 * Ensures a value submitted in a form is numeric and is in a range
 *
 * @param   object   the form
 * @param   string   the name of the form field to check
 * @param   integer  the minimum authorized value
 * @param   integer  the maximum authorized value
 *
 * @return  boolean  whether a valid number has been submitted or not
 */
function checkFormElementInRange(theForm, theFieldName, message, min, max)
{
    var theField         = theForm.elements[theFieldName];
    var val              = parseInt(theField.value);

    if (typeof(min) == 'undefined') {
        min = 0;
    }
    if (typeof(max) == 'undefined') {
        max = Number.MAX_VALUE;
    }

    // It's not a number
    if (isNaN(val)) {
        theField.select();
        alert(errorMsg1);
        theField.focus();
        return false;
    }
    // It's a number but it is not between min and max
    else if (val < min || val > max) {
        theField.select();
        alert(message.replace('%d', val));
        theField.focus();
        return false;
    }
    // It's a valid number
    else {
        theField.value = val;
    }
    return true;

} // end of the 'checkFormElementInRange()' function


function checkTableEditForm(theForm, fieldsCnt)
{
    // TODO: avoid sending a message if user just wants to add a line
    // on the form but has not completed at least one field name

    var atLeastOneField = 0;
    var i, elm, elm2, elm3, val, id;

    for (i=0; i<fieldsCnt; i++)
    {
        id = "field_" + i + "_2";
        elm = getElement(id);
        if (elm.value == 'VARCHAR' || elm.value == 'CHAR' || elm.value == 'BIT') {
            elm2 = getElement("field_" + i + "_3");
            val = parseInt(elm2.value);
            elm3 = getElement("field_" + i + "_1");
            if (isNaN(val) && elm3.value != "") {
                elm2.select();
                alert(errorMsg1);
                elm2.focus();
                return false;
            }
        }

        if (atLeastOneField == 0) {
            id = "field_" + i + "_1";
            if (!emptyCheckTheField(theForm, id)) {
                atLeastOneField = 1;
            }
        }
    }
    if (atLeastOneField == 0) {
        var theField = theForm.elements["field_0_1"];
        alert(errorMsg0);
        theField.focus();
        return false;
    }

    return true;
} // enf of the 'checkTableEditForm()' function


/**
 * Ensures the choice between 'transmit', 'zipped', 'gzipped' and 'bzipped'
 * checkboxes is consistant
 *
 * @param   object   the form
 * @param   string   a code for the action that causes this function to be run
 *
 * @return  boolean  always true
 */
function checkTransmitDump(theForm, theAction)
{
    var formElts = theForm.elements;

    // 'zipped' option has been checked
    if (theAction == 'zip' && formElts['zip'].checked) {
        if (!formElts['asfile'].checked) {
            theForm.elements['asfile'].checked = true;
        }
        if (typeof(formElts['gzip']) != 'undefined' && formElts['gzip'].checked) {
            theForm.elements['gzip'].checked = false;
        }
        if (typeof(formElts['bzip']) != 'undefined' && formElts['bzip'].checked) {
            theForm.elements['bzip'].checked = false;
        }
    }
    // 'gzipped' option has been checked
    else if (theAction == 'gzip' && formElts['gzip'].checked) {
        if (!formElts['asfile'].checked) {
            theForm.elements['asfile'].checked = true;
        }
        if (typeof(formElts['zip']) != 'undefined' && formElts['zip'].checked) {
            theForm.elements['zip'].checked = false;
        }
        if (typeof(formElts['bzip']) != 'undefined' && formElts['bzip'].checked) {
            theForm.elements['bzip'].checked = false;
        }
    }
    // 'bzipped' option has been checked
    else if (theAction == 'bzip' && formElts['bzip'].checked) {
        if (!formElts['asfile'].checked) {
            theForm.elements['asfile'].checked = true;
        }
        if (typeof(formElts['zip']) != 'undefined' && formElts['zip'].checked) {
            theForm.elements['zip'].checked = false;
        }
        if (typeof(formElts['gzip']) != 'undefined' && formElts['gzip'].checked) {
            theForm.elements['gzip'].checked = false;
        }
    }
    // 'transmit' option has been unchecked
    else if (theAction == 'transmit' && !formElts['asfile'].checked) {
        if (typeof(formElts['zip']) != 'undefined' && formElts['zip'].checked) {
            theForm.elements['zip'].checked = false;
        }
        if ((typeof(formElts['gzip']) != 'undefined' && formElts['gzip'].checked)) {
            theForm.elements['gzip'].checked = false;
        }
        if ((typeof(formElts['bzip']) != 'undefined' && formElts['bzip'].checked)) {
            theForm.elements['bzip'].checked = false;
        }
    }

    return true;
} // end of the 'checkTransmitDump()' function


/**
 * This array is used to remember mark status of rows in browse mode
 */
var marked_row = new Array;

/**
 * enables highlight and marking of rows in data tables
 *
 */
function PMA_markRowsInit() {
    // for every table row ...
    var rows = document.getElementsByTagName('tr');
    for ( var i = 0; i < rows.length; i++ ) {
        // ... with the class 'odd' or 'even' ...
        if ( 'odd' != rows[i].className.substr(0,3) && 'even' != rows[i].className.substr(0,4) ) {
            continue;
        }
        // ... add event listeners ...
        // ... to highlight the row on mouseover ...
        if ( navigator.appName == 'Microsoft Internet Explorer' ) {
            // but only for IE, other browsers are handled by :hover in css
            rows[i].onmouseover = function() {
                this.className += ' hover';
            }
            rows[i].onmouseout = function() {
                this.className = this.className.replace( ' hover', '' );
            }
        }
        // Do not set click events if not wanted
        if (rows[i].className.search(/noclick/) != -1) {
            continue;
        }
        // ... and to mark the row on click ...
        rows[i].onmousedown = function() {
            var unique_id;
            var checkbox;

            checkbox = this.getElementsByTagName( 'input' )[0];
            if ( checkbox && checkbox.type == 'checkbox' ) {
                unique_id = checkbox.name + checkbox.value;
            } else if ( this.id.length > 0 ) {
                unique_id = this.id;
            } else {
                return;
            }

            if ( typeof(marked_row[unique_id]) == 'undefined' || !marked_row[unique_id] ) {
                marked_row[unique_id] = true;
            } else {
                marked_row[unique_id] = false;
            }

            if ( marked_row[unique_id] ) {
                this.className += ' marked';
            } else {
                this.className = this.className.replace(' marked', '');
            }

            if ( checkbox && checkbox.disabled == false ) {
                checkbox.checked = marked_row[unique_id];
            }
        }

        // ... and disable label ...
        var labeltag = rows[i].getElementsByTagName('label')[0];
        if ( labeltag ) {
            labeltag.onclick = function() {
                return false;
            }
        }
        // .. and checkbox clicks
        var checkbox = rows[i].getElementsByTagName('input')[0];
        if ( checkbox ) {
            checkbox.onclick = function() {
                // opera does not recognize return false;
                this.checked = ! this.checked;
            }
        }
    }
}
window.onload=PMA_markRowsInit;

/**
 * marks all rows and selects its first checkbox inside the given element
 * the given element is usaly a table or a div containing the table or tables
 *
 * @param    container    DOM element
 */
function markAllRows( container_id ) {
    var rows = document.getElementById(container_id).getElementsByTagName('tr');
    var unique_id;
    var checkbox;

    for ( var i = 0; i < rows.length; i++ ) {

        checkbox = rows[i].getElementsByTagName( 'input' )[0];

        if ( checkbox && checkbox.type == 'checkbox' ) {
            unique_id = checkbox.name + checkbox.value;
            if ( checkbox.disabled == false ) {
                checkbox.checked = true;
                if ( typeof(marked_row[unique_id]) == 'undefined' || !marked_row[unique_id] ) {
                    rows[i].className += ' marked';
                    marked_row[unique_id] = true;
                }
            }
        }
    }

    return true;
}

/**
 * marks all rows and selects its first checkbox inside the given element
 * the given element is usaly a table or a div containing the table or tables
 *
 * @param    container    DOM element
 */
function unMarkAllRows( container_id ) {
    var rows = document.getElementById(container_id).getElementsByTagName('tr');
    var unique_id;
    var checkbox;

    for ( var i = 0; i < rows.length; i++ ) {

        checkbox = rows[i].getElementsByTagName( 'input' )[0];

        if ( checkbox && checkbox.type == 'checkbox' ) {
            unique_id = checkbox.name + checkbox.value;
            checkbox.checked = false;
            rows[i].className = rows[i].className.replace(' marked', '');
            marked_row[unique_id] = false;
        }
    }

    return true;
}

/**
 * Sets/unsets the pointer and marker in browse mode
 *
 * @param   object    the table row
 * @param   integer  the row number
 * @param   string    the action calling this script (over, out or click)
 * @param   string    the default background color
 * @param   string    the color to use for mouseover
 * @param   string    the color to use for marking a row
 *
 * @return  boolean  whether pointer is set or not
 */
function setPointer(theRow, theRowNum, theAction, theDefaultColor, thePointerColor, theMarkColor)
{
    var theCells = null;

    // 1. Pointer and mark feature are disabled or the browser can't get the
    //    row -> exits
    if ((thePointerColor == '' && theMarkColor == '')
        || typeof(theRow.style) == 'undefined') {
        return false;
    }

    // 1.1 Sets the mouse pointer to pointer on mouseover and back to normal otherwise.
    if (theAction == "over" || theAction == "click") {
        theRow.style.cursor='pointer';
    } else {
        theRow.style.cursor='default';
    }

    // 2. Gets the current row and exits if the browser can't get it
    if (typeof(document.getElementsByTagName) != 'undefined') {
        theCells = theRow.getElementsByTagName('td');
    }
    else if (typeof(theRow.cells) != 'undefined') {
        theCells = theRow.cells;
    }
    else {
        return false;
    }

    // 3. Gets the current color...
    var rowCellsCnt  = theCells.length;
    var domDetect    = null;
    var currentColor = null;
    var newColor     = null;
    // 3.1 ... with DOM compatible browsers except Opera that does not return
    //         valid values with "getAttribute"
    if (typeof(window.opera) == 'undefined'
        && typeof(theCells[0].getAttribute) != 'undefined') {
        currentColor = theCells[0].getAttribute('bgcolor');
        domDetect    = true;
    }
    // 3.2 ... with other browsers
    else {
        currentColor = theCells[0].style.backgroundColor;
        domDetect    = false;
    } // end 3

    // 3.3 ... Opera changes colors set via HTML to rgb(r,g,b) format so fix it
    if (currentColor.indexOf("rgb") >= 0)
    {
        var rgbStr = currentColor.slice(currentColor.indexOf('(') + 1,
                                     currentColor.indexOf(')'));
        var rgbValues = rgbStr.split(",");
        currentColor = "#";
        var hexChars = "0123456789ABCDEF";
        for (var i = 0; i < 3; i++)
        {
            var v = rgbValues[i].valueOf();
            currentColor += hexChars.charAt(v/16) + hexChars.charAt(v%16);
        }
    }

    // 4. Defines the new color
    // 4.1 Current color is the default one
    if (currentColor == ''
        || currentColor.toLowerCase() == theDefaultColor.toLowerCase()) {
        if (theAction == 'over' && thePointerColor != '') {
            newColor              = thePointerColor;
        }
        else if (theAction == 'click' && theMarkColor != '') {
            newColor              = theMarkColor;
            marked_row[theRowNum] = true;
            // Garvin: deactivated onclick marking of the checkbox because it's also executed
            // when an action (like edit/delete) on a single item is performed. Then the checkbox
            // would get deactived, even though we need it activated. Maybe there is a way
            // to detect if the row was clicked, and not an item therein...
            // document.getElementById('id_rows_to_delete' + theRowNum).checked = true;
        }
    }
    // 4.1.2 Current color is the pointer one
    else if (currentColor.toLowerCase() == thePointerColor.toLowerCase()
             && (typeof(marked_row[theRowNum]) == 'undefined' || !marked_row[theRowNum])) {
        if (theAction == 'out') {
            newColor              = theDefaultColor;
        }
        else if (theAction == 'click' && theMarkColor != '') {
            newColor              = theMarkColor;
            marked_row[theRowNum] = true;
            // document.getElementById('id_rows_to_delete' + theRowNum).checked = true;
        }
    }
    // 4.1.3 Current color is the marker one
    else if (currentColor.toLowerCase() == theMarkColor.toLowerCase()) {
        if (theAction == 'click') {
            newColor              = (thePointerColor != '')
                                  ? thePointerColor
                                  : theDefaultColor;
            marked_row[theRowNum] = (typeof(marked_row[theRowNum]) == 'undefined' || !marked_row[theRowNum])
                                  ? true
                                  : null;
            // document.getElementById('id_rows_to_delete' + theRowNum).checked = false;
        }
    } // end 4

    // 5. Sets the new color...
    if (newColor) {
        var c = null;
        // 5.1 ... with DOM compatible browsers except Opera
        if (domDetect) {
            for (c = 0; c < rowCellsCnt; c++) {
                theCells[c].setAttribute('bgcolor', newColor, 0);
            } // end for
        }
        // 5.2 ... with other browsers
        else {
            for (c = 0; c < rowCellsCnt; c++) {
                theCells[c].style.backgroundColor = newColor;
            }
        }
    } // end 5

    return true;
} // end of the 'setPointer()' function

/*
 * Sets/unsets the pointer and marker in vertical browse mode
 *
 * @param   object    the table row
 * @param   integer   the column number
 * @param   string    the action calling this script (over, out or click)
 * @param   string    the default background Class
 * @param   string    the Class to use for mouseover
 * @param   string    the Class to use for marking a row
 *
 * @return  boolean  whether pointer is set or not
 *
 * @author Garvin Hicking <me@supergarv.de> (rewrite of setPointer.)
 */
function setVerticalPointer(theRow, theColNum, theAction, theDefaultClass1, theDefaultClass2, thePointerClass, theMarkClass) {
    // 1. Pointer and mark feature are disabled or the browser can't get the
    //    row -> exits
    if ((thePointerClass == '' && theMarkClass == '')
        || typeof(theRow.style) == 'undefined') {
        return false;
    }

    var tagSwitch = null;

    // 2. Gets the current row and exits if the browser can't get it
    if (typeof(document.getElementsByTagName) != 'undefined') {
        tagSwitch = 'tag';
    } else if (typeof(document.getElementById('table_results')) != 'undefined') {
        tagSwitch = 'cells';
    } else {
        return false;
    }

    var theCells = null;

    if (tagSwitch == 'tag') {
        theRows     = document.getElementById('table_results').getElementsByTagName('tr');
        theCells    = theRows[1].getElementsByTagName('td');
    } else if (tagSwitch == 'cells') {
        theRows     = document.getElementById('table_results').rows;
        theCells    = theRows[1].cells;
    }

    // 3. Gets the current Class...
    var currentClass   = null;
    var newClass       = null;

    // 3.1 ... with DOM compatible browsers except Opera that does not return
    //         valid values with "getAttribute"
    if (typeof(window.opera) == 'undefined'
        && typeof(theCells[theColNum].getAttribute) != 'undefined') {
        currentClass = theCells[theColNum].className;
    } // end 3

    // 4. Defines the new Class
    // 4.1 Current Class is the default one
    if (currentClass == ''
        || currentClass.toLowerCase() == theDefaultClass1.toLowerCase()
        || currentClass.toLowerCase() == theDefaultClass2.toLowerCase()) {
        if (theAction == 'over' && thePointerClass != '') {
            newClass              = thePointerClass;
        } else if (theAction == 'click' && theMarkClass != '') {
            newClass              = theMarkClass;
            marked_row[theColNum] = true;
        }
    }
    // 4.1.2 Current Class is the pointer one
    else if (currentClass.toLowerCase() == thePointerClass.toLowerCase() &&
             (typeof(marked_row[theColNum]) == 'undefined' || !marked_row[theColNum]) || marked_row[theColNum] == false) {
            if (theAction == 'out') {
                if (theColNum % 2) {
                    newClass              = theDefaultClass1;
                } else {
                    newClass              = theDefaultClass2;
                }
            }
            else if (theAction == 'click' && theMarkClass != '') {
                newClass              = theMarkClass;
                marked_row[theColNum] = true;
            }
    }
    // 4.1.3 Current Class is the marker one
    else if (currentClass.toLowerCase() == theMarkClass.toLowerCase()) {
        if (theAction == 'click') {
            newClass              = (thePointerClass != '')
                                  ? thePointerClass
                                  : ((theColNum % 2) ? theDefaultClass2 : theDefaultClass1);
            marked_row[theColNum] = false;
        }
    } // end 4

    // 5 ... with DOM compatible browsers except Opera

    if (newClass) {
        var c = null;
        var rowCnt = theRows.length;
        for (c = 0; c < rowCnt; c++) {
            if (tagSwitch == 'tag') {
                Cells = theRows[c].getElementsByTagName('td');
            } else if (tagSwitch == 'cells') {
                Cells = theRows[c].cells;
            }

            Cell  = Cells[theColNum];

            // 5.1 Sets the new Class...
            Cell.className = Cell.className.replace(currentClass, newClass);
        } // end for
    } // end 5

     return true;
 } // end of the 'setVerticalPointer()' function

/**
 * Checks/unchecks all checkbox in given conainer (f.e. a form, fieldset or div)
 *
 * @param   string   container_id  the container id
 * @param   boolean  state         new value for checkbox (true or false)
 * @return  boolean  always true
 */
function setCheckboxes( container_id, state ) {
    var checkboxes = document.getElementById(container_id).getElementsByTagName('input');

    for ( var i = 0; i < checkboxes.length; i++ ) {
        if ( checkboxes[i].type == 'checkbox' ) {
            checkboxes[i].checked = state;
        }
    }

    return true;
} // end of the 'setCheckboxes()' function


// added 2004-05-08 by Michael Keck <mail_at_michaelkeck_dot_de>
//   copy the checked from left to right or from right to left
//   so it's easier for users to see, if $cfg['ModifyAtRight']=true, what they've checked ;)
function copyCheckboxesRange(the_form, the_name, the_clicked)
{
    if (typeof(document.forms[the_form].elements[the_name]) != 'undefined' && typeof(document.forms[the_form].elements[the_name + 'r']) != 'undefined') {
        if (the_clicked !== 'r') {
            if (document.forms[the_form].elements[the_name].checked == true) {
                document.forms[the_form].elements[the_name + 'r'].checked = true;
            }else {
                document.forms[the_form].elements[the_name + 'r'].checked = false;
            }
        } else if (the_clicked == 'r') {
            if (document.forms[the_form].elements[the_name + 'r'].checked == true) {
                document.forms[the_form].elements[the_name].checked = true;
            }else {
                document.forms[the_form].elements[the_name].checked = false;
            }
       }
    }
}


// added 2004-05-08 by Michael Keck <mail_at_michaelkeck_dot_de>
//  - this was directly written to each td, so why not a function ;)
//  setCheckboxColumn(\'id_rows_to_delete' . $row_no . ''\');
function setCheckboxColumn(theCheckbox){
    if (document.getElementById(theCheckbox)) {
        document.getElementById(theCheckbox).checked = (document.getElementById(theCheckbox).checked ? false : true);
        if (document.getElementById(theCheckbox + 'r')) {
            document.getElementById(theCheckbox + 'r').checked = document.getElementById(theCheckbox).checked;
        }
    } else {
        if (document.getElementById(theCheckbox + 'r')) {
            document.getElementById(theCheckbox + 'r').checked = (document.getElementById(theCheckbox +'r').checked ? false : true);
            if (document.getElementById(theCheckbox)) {
                document.getElementById(theCheckbox).checked = document.getElementById(theCheckbox + 'r').checked;
            }
        }
    }
}


/**
  * Checks/unchecks all options of a <select> element
  *
  * @param   string   the form name
  * @param   string   the element name
  * @param   boolean  whether to check or to uncheck the element
  *
  * @return  boolean  always true
  */
function setSelectOptions(the_form, the_select, do_check)
{
    var selectObject = document.forms[the_form].elements[the_select];
    var selectCount  = selectObject.length;

    for (var i = 0; i < selectCount; i++) {
        selectObject.options[i].selected = do_check;
    } // end for

    return true;
} // end of the 'setSelectOptions()' function

/**
  * Inserts multiple fields.
  *
  */
function insertValueQuery() {
    var myQuery = document.sqlform.sql_query;
    var myListBox = document.sqlform.dummy;

    if(myListBox.options.length > 0) {
        sql_box_locked = true;
        var chaineAj = "";
        var NbSelect = 0;
        for(var i=0; i<myListBox.options.length; i++) {
            if (myListBox.options[i].selected){
                NbSelect++;
                if (NbSelect > 1)
                    chaineAj += ", ";
                chaineAj += myListBox.options[i].value;
            }
        }

        //IE support
        if (document.selection) {
            myQuery.focus();
            sel = document.selection.createRange();
            sel.text = chaineAj;
            document.sqlform.insert.focus();
        }
        //MOZILLA/NETSCAPE support
        else if (document.sqlform.sql_query.selectionStart || document.sqlform.sql_query.selectionStart == "0") {
            var startPos = document.sqlform.sql_query.selectionStart;
            var endPos = document.sqlform.sql_query.selectionEnd;
            var chaineSql = document.sqlform.sql_query.value;

            myQuery.value = chaineSql.substring(0, startPos) + chaineAj + chaineSql.substring(endPos, chaineSql.length);
        } else {
            myQuery.value += chaineAj;
        }
        sql_box_locked = false;
    }
}

/**
  * listbox redirection
  */
function goToUrl(selObj, goToLocation) {
    eval("document.location.href = '" + goToLocation + "pos=" + selObj.options[selObj.selectedIndex].value + "'");
}

/**
 * getElement
 */
function getElement(e,f){
    if(document.layers){
        f=(f)?f:self;
        if(f.document.layers[e]) {
            return f.document.layers[e];
        }
        for(W=0;W<f.document.layers.length;W++) {
            return(getElement(e,f.document.layers[W]));
        }
    }
    if(document.all) {
        return document.all[e];
    }
    return document.getElementById(e);
}

/**
  * Refresh the WYSIWYG-PDF scratchboard after changes have been made
  */
function refreshDragOption(e) {
    myid = getElement(e);
    if (myid.style.visibility == 'visible') {
        refreshLayout();
    }
}

/**
  * Refresh/resize the WYSIWYG-PDF scratchboard
  */
function refreshLayout() {
        myid = getElement('pdflayout');

        if (document.pdfoptions.orientation.value == 'P') {
            posa = 'x';
            posb = 'y';
        } else {
            posa = 'y';
            posb = 'x';
        }

        myid.style.width = pdfPaperSize(document.pdfoptions.paper.value, posa) + 'px';
        myid.style.height = pdfPaperSize(document.pdfoptions.paper.value, posb) + 'px';
}

/**
  * Show/hide the WYSIWYG-PDF scratchboard
  */
function ToggleDragDrop(e) {
    myid = getElement(e);

    if (myid.style.visibility == 'hidden') {
        init();
        myid.style.visibility = 'visible';
        myid.style.display = 'block';
        document.edcoord.showwysiwyg.value = '1';
    } else {
        myid.style.visibility = 'hidden';
        myid.style.display = 'none';
        document.edcoord.showwysiwyg.value = '0';
    }
}

/**
  * PDF scratchboard: When a position is entered manually, update
  * the fields inside the scratchboard.
  */
function dragPlace(no, axis, value) {
    if (axis == 'x') {
        getElement("table_" + no).style.left = value + 'px';
    } else {
        getElement("table_" + no).style.top  = value + 'px';
    }
}

/**
 * Returns paper sizes for a given format
 */
function pdfPaperSize(format, axis) {
    switch (format.toUpperCase()) {
        case '4A0':
            if (axis == 'x') return 4767.87; else return 6740.79;
            break;
        case '2A0':
            if (axis == 'x') return 3370.39; else return 4767.87;
            break;
        case 'A0':
            if (axis == 'x') return 2383.94; else return 3370.39;
            break;
        case 'A1':
            if (axis == 'x') return 1683.78; else return 2383.94;
            break;
        case 'A2':
            if (axis == 'x') return 1190.55; else return 1683.78;
            break;
        case 'A3':
            if (axis == 'x') return 841.89; else return 1190.55;
            break;
        case 'A4':
            if (axis == 'x') return 595.28; else return 841.89;
            break;
        case 'A5':
            if (axis == 'x') return 419.53; else return 595.28;
            break;
        case 'A6':
            if (axis == 'x') return 297.64; else return 419.53;
            break;
        case 'A7':
            if (axis == 'x') return 209.76; else return 297.64;
            break;
        case 'A8':
            if (axis == 'x') return 147.40; else return 209.76;
            break;
        case 'A9':
            if (axis == 'x') return 104.88; else return 147.40;
            break;
        case 'A10':
            if (axis == 'x') return 73.70; else return 104.88;
            break;
        case 'B0':
            if (axis == 'x') return 2834.65; else return 4008.19;
            break;
        case 'B1':
            if (axis == 'x') return 2004.09; else return 2834.65;
            break;
        case 'B2':
            if (axis == 'x') return 1417.32; else return 2004.09;
            break;
        case 'B3':
            if (axis == 'x') return 1000.63; else return 1417.32;
            break;
        case 'B4':
            if (axis == 'x') return 708.66; else return 1000.63;
            break;
        case 'B5':
            if (axis == 'x') return 498.90; else return 708.66;
            break;
        case 'B6':
            if (axis == 'x') return 354.33; else return 498.90;
            break;
        case 'B7':
            if (axis == 'x') return 249.45; else return 354.33;
            break;
        case 'B8':
            if (axis == 'x') return 175.75; else return 249.45;
            break;
        case 'B9':
            if (axis == 'x') return 124.72; else return 175.75;
            break;
        case 'B10':
            if (axis == 'x') return 87.87; else return 124.72;
            break;
        case 'C0':
            if (axis == 'x') return 2599.37; else return 3676.54;
            break;
        case 'C1':
            if (axis == 'x') return 1836.85; else return 2599.37;
            break;
        case 'C2':
            if (axis == 'x') return 1298.27; else return 1836.85;
            break;
        case 'C3':
            if (axis == 'x') return 918.43; else return 1298.27;
            break;
        case 'C4':
            if (axis == 'x') return 649.13; else return 918.43;
            break;
        case 'C5':
            if (axis == 'x') return 459.21; else return 649.13;
            break;
        case 'C6':
            if (axis == 'x') return 323.15; else return 459.21;
            break;
        case 'C7':
            if (axis == 'x') return 229.61; else return 323.15;
            break;
        case 'C8':
            if (axis == 'x') return 161.57; else return 229.61;
            break;
        case 'C9':
            if (axis == 'x') return 113.39; else return 161.57;
            break;
        case 'C10':
            if (axis == 'x') return 79.37; else return 113.39;
            break;
        case 'RA0':
            if (axis == 'x') return 2437.80; else return 3458.27;
            break;
        case 'RA1':
            if (axis == 'x') return 1729.13; else return 2437.80;
            break;
        case 'RA2':
            if (axis == 'x') return 1218.90; else return 1729.13;
            break;
        case 'RA3':
            if (axis == 'x') return 864.57; else return 1218.90;
            break;
        case 'RA4':
            if (axis == 'x') return 609.45; else return 864.57;
            break;
        case 'SRA0':
            if (axis == 'x') return 2551.18; else return 3628.35;
            break;
        case 'SRA1':
            if (axis == 'x') return 1814.17; else return 2551.18;
            break;
        case 'SRA2':
            if (axis == 'x') return 1275.59; else return 1814.17;
            break;
        case 'SRA3':
            if (axis == 'x') return 907.09; else return 1275.59;
            break;
        case 'SRA4':
            if (axis == 'x') return 637.80; else return 907.09;
            break;
        case 'LETTER':
            if (axis == 'x') return 612.00; else return 792.00;
            break;
        case 'LEGAL':
            if (axis == 'x') return 612.00; else return 1008.00;
            break;
        case 'EXECUTIVE':
            if (axis == 'x') return 521.86; else return 756.00;
            break;
        case 'FOLIO':
            if (axis == 'x') return 612.00; else return 936.00;
            break;
    } // end switch

    return 0;
}
