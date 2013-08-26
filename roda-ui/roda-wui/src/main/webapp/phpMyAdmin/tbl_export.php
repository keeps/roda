<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 *
 * @version $Id: tbl_export.php 11061 2008-01-18 18:29:17Z lem9 $
 */

/**
 *
 */
require_once './libraries/common.inc.php';

/**
 * Gets tables informations and displays top links
 */
require_once './libraries/tbl_common.php';
$url_query .= '&amp;goto=tbl_export.php&amp;back=tbl_export.php';
require_once './libraries/tbl_info.inc.php';

// Dump of a table

$export_page_title = $strViewDump;

// When we have some query, we need to remove LIMIT from that and possibly
// generate WHERE clause (if we are asked to export specific rows)

if (! empty($sql_query)) {
    // Parse query so we can work with tokens
    $parsed_sql = PMA_SQP_parse($sql_query);
    $analyzed_sql = PMA_SQP_analyze($parsed_sql);

    // Need to generate WHERE clause?
    if (isset($primary_key)) {
        // Yes => rebuild query from scracts, this doesn't work with nested
        // selects :-(
        $sql_query = 'SELECT ';

        if (isset($analyzed_sql[0]['queryflags']['distinct'])) {
            $sql_query .= ' DISTINCT ';
        }

        $sql_query .= $analyzed_sql[0]['select_expr_clause'];

        if (!empty($analyzed_sql[0]['from_clause'])) {
            $sql_query .= ' FROM ' . $analyzed_sql[0]['from_clause'];
        }

        $wheres = array();

        if (isset($primary_key) && is_array($primary_key)
         && count($primary_key) > 0) {
            $wheres[] = '(' . implode(') OR (',$primary_key) . ')';
        }

        if (!empty($analyzed_sql[0]['where_clause']))  {
            $wheres[] = $analyzed_sql[0]['where_clause'];
        }

        if (count($wheres) > 0) {
            $sql_query .= ' WHERE (' . implode(') AND (', $wheres) . ')';
        }

        if (!empty($analyzed_sql[0]['group_by_clause'])) {
            $sql_query .= ' GROUP BY ' . $analyzed_sql[0]['group_by_clause'];
        }
        if (!empty($analyzed_sql[0]['having_clause'])) {
            $sql_query .= ' HAVING ' . $analyzed_sql[0]['having_clause'];
        }
        if (!empty($analyzed_sql[0]['order_by_clause'])) {
            $sql_query .= ' ORDER BY ' . $analyzed_sql[0]['order_by_clause'];
        }
    } else {
        // Just crop LIMIT clause
        $sql_query = $analyzed_sql[0]['section_before_limit'] . $analyzed_sql[0]['section_after_limit'];
    }
    $message = $GLOBALS['strSuccess'];
}

/**
 * Displays top menu links
 */
require './libraries/tbl_links.inc.php';

$export_type = 'table';
require_once './libraries/display_export.lib.php';


/**
 * Displays the footer
 */
require_once './libraries/footer.inc.php';
?>
