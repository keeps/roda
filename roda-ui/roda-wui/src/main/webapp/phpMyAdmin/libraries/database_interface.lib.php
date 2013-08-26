<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 * Common Option Constants For DBI Functions
 *
 * @version $Id: database_interface.lib.php 11196 2008-04-16 18:54:42Z lem9 $
 */

/**
 *
 */
// PMA_DBI_try_query()
define('PMA_DBI_QUERY_STORE',       1);  // Force STORE_RESULT method, ignored by classic MySQL.
define('PMA_DBI_QUERY_UNBUFFERED',  2);  // Do not read whole query
// PMA_DBI_get_variable()
define('PMA_DBI_GETVAR_SESSION', 1);
define('PMA_DBI_GETVAR_GLOBAL', 2);

/**
 * Loads the mysql extensions if it is not loaded yet
 *
 * @param   string  $extension  mysql extension to load
 */
function PMA_DBI_checkAndLoadMysqlExtension($extension = 'mysql') {
    if (! function_exists($extension . '_connect')) {
        PMA_dl($extension);
        // check whether mysql is available
        if (! function_exists($extension . '_connect')) {
            return false;
        }
    }

    return true;
}


/**
 * check for requested extension
 */
if (! PMA_DBI_checkAndLoadMysqlExtension($GLOBALS['cfg']['Server']['extension'])) {

    // if it fails try alternative extension ...
    // and display an error ...

    /**
     * @todo 2.7.1: add different messages for alternativ extension
     * and complete fail (no alternativ extension too)
     */
    $GLOBALS['PMA_errors'][] =
        sprintf(PMA_sanitize($GLOBALS['strCantLoad']),
            $GLOBALS['cfg']['Server']['extension'])
        .' - <a href="./Documentation.html#faqmysql" target="documentation">'
        .$GLOBALS['strDocu'] . '</a>';

    if ($GLOBALS['cfg']['Server']['extension'] === 'mysql') {
        $alternativ_extension = 'mysqli';
    } else {
        $alternativ_extension = 'mysql';
    }

    if (! PMA_DBI_checkAndLoadMysqlExtension($alternativ_extension)) {
        // if alternativ fails too ...
        PMA_fatalError(
            sprintf($GLOBALS['strCantLoad'],
                $GLOBALS['cfg']['Server']['extension'])
            . ' - [a@./Documentation.html#faqmysql@documentation]'
            . $GLOBALS['strDocu'] . '[/a]');
    }

    $GLOBALS['cfg']['Server']['extension'] = $alternativ_extension;
    unset($alternativ_extension);
}

/**
 * Including The DBI Plugin
 */
require_once './libraries/dbi/' . $GLOBALS['cfg']['Server']['extension'] . '.dbi.lib.php';

/**
 * Common Functions
 */
function PMA_DBI_query($query, $link = null, $options = 0) {
    $res = PMA_DBI_try_query($query, $link, $options)
        or PMA_mysqlDie(PMA_DBI_getError($link), $query);
    return $res;
}

/**
 * converts charset of a mysql message, usally coming from mysql_error(),
 * into PMA charset, usally UTF-8
 * uses language to charset mapping from mysql/share/errmsg.txt
 * and charset names to ISO charset from information_schema.CHARACTER_SETS
 *
 * @uses    $GLOBALS['cfg']['IconvExtraParams']
 * @uses    $GLOBALS['charset']     as target charset
 * @uses    PMA_DBI_fetch_value()   to get server_language
 * @uses    preg_match()            to filter server_language
 * @uses    in_array()
 * @uses    function_exists()       to check for a convert function
 * @uses    iconv()                 to convert message
 * @uses    libiconv()              to convert message
 * @uses    recode_string()         to convert message
 * @uses    mb_convert_encoding()   to convert message
 * @param   string  $message
 * @return  string  $message
 */
function PMA_DBI_convert_message($message) {
    // latin always last!
    $encodings = array(
        'japanese'      => 'EUC-JP', //'ujis',
        'japanese-sjis' => 'Shift-JIS', //'sjis',
        'korean'        => 'EUC-KR', //'euckr',
        'russian'       => 'KOI8-R', //'koi8r',
        'ukrainian'     => 'KOI8-U', //'koi8u',
        'greek'         => 'ISO-8859-7', //'greek',
        'serbian'       => 'CP1250', //'cp1250',
        'estonian'      => 'ISO-8859-13', //'latin7',
        'slovak'        => 'ISO-8859-2', //'latin2',
        'czech'         => 'ISO-8859-2', //'latin2',
        'hungarian'     => 'ISO-8859-2', //'latin2',
        'polish'        => 'ISO-8859-2', //'latin2',
        'romanian'      => 'ISO-8859-2', //'latin2',
        'spanish'       => 'CP1252', //'latin1',
        'swedish'       => 'CP1252', //'latin1',
        'italian'       => 'CP1252', //'latin1',
        'norwegian-ny'  => 'CP1252', //'latin1',
        'norwegian'     => 'CP1252', //'latin1',
        'portuguese'    => 'CP1252', //'latin1',
        'danish'        => 'CP1252', //'latin1',
        'dutch'         => 'CP1252', //'latin1',
        'english'       => 'CP1252', //'latin1',
        'french'        => 'CP1252', //'latin1',
        'german'        => 'CP1252', //'latin1',
    );

    if ($server_language = PMA_DBI_fetch_value('SHOW VARIABLES LIKE \'language\';', 0, 1)) {
        $found = array();
        if (preg_match('&(?:\\\|\\/)([^\\\\\/]*)(?:\\\|\\/)$&i', $server_language, $found)) {
            $server_language = $found[1];
        }
    }

    if (! empty($server_language) && isset($encodings[$server_language])) {
        if (function_exists('iconv')) {
            if ((@stristr(PHP_OS, 'AIX')) && (@strcasecmp(ICONV_IMPL, 'unknown') == 0) && (@strcasecmp(ICONV_VERSION, 'unknown') == 0)) {
                require_once './libraries/iconv_wrapper.lib.php';
                $message = PMA_aix_iconv_wrapper($encodings[$server_language],
                    $GLOBALS['charset'] . $GLOBALS['cfg']['IconvExtraParams'], $message);
            } else {
                $message = iconv($encodings[$server_language],
                    $GLOBALS['charset'] . $GLOBALS['cfg']['IconvExtraParams'], $message);
            }
        } elseif (function_exists('recode_string')) {
            $message = recode_string($encodings[$server_language] . '..'  . $GLOBALS['charset'],
                $message);
        } elseif (function_exists('libiconv')) {
            $message = libiconv($encodings[$server_language], $GLOBALS['charset'], $message);
        } elseif (function_exists('mb_convert_encoding')) {
            // do not try unsupported charsets
            if (! in_array($server_language, array('ukrainian', 'greek', 'serbian'))) {
                $message = mb_convert_encoding($message, $GLOBALS['charset'],
                    $encodings[$server_language]);
            }
        }
    } else {
        /**
         * @todo lang not found, try all, what TODO ?
         */
    }

    return $message;
}

/**
 * returns array with table names for given db
 *
 * @param   string  $database   name of database
 * @param   mixed   $link       mysql link resource|object
 * @return  array   tables names
 */
function PMA_DBI_get_tables($database, $link = null)
{
    return PMA_DBI_fetch_result('SHOW TABLES FROM ' . PMA_backquote($database) . ';',
        null, 0, $link, PMA_DBI_QUERY_STORE);
}

/**
 * returns array of all tables in given db or dbs
 * this function expects unquoted names:
 * RIGHT: my_database
 * WRONG: `my_database`
 * WRONG: my\_database
 * if $tbl_is_group is true, $table is used as filter for table names
 * if $tbl_is_group is 'comment, $table is used as filter for table comments
 *
 * <code>
 * PMA_DBI_get_tables_full('my_database');
 * PMA_DBI_get_tables_full('my_database', 'my_table'));
 * PMA_DBI_get_tables_full('my_database', 'my_tables_', true));
 * PMA_DBI_get_tables_full('my_database', 'my_tables_', 'comment'));
 * </code>
 *
 * @uses    PMA_MYSQL_INT_VERSION
 * @uses    PMA_DBI_fetch_result()
 * @uses    PMA_escape_mysql_wildcards()
 * @uses    PMA_backquote()
 * @uses    is_array()
 * @uses    addslashes()
 * @uses    strpos()
 * @uses    strtoupper()
 * @param   string          $databases      database
 * @param   string          $table          table
 * @param   boolean|string  $tbl_is_group   $table is a table group
 * @param   resource        $link           mysql link
 * @param   integer         $limit_offset   zero-based offset for the count 
 * @param   boolean|integer $limit_count    number of tables to return 
 * @return  array           list of tables in given db(s)
 */
function PMA_DBI_get_tables_full($database, $table = false,
    $tbl_is_group = false, $link = null, $limit_offset = 0, $limit_count = false)
{
    if (true === $limit_count) {
        $limit_count = $GLOBALS['cfg']['MaxTableList'];
    }
    // prepare and check parameters
    if (! is_array($database)) {
        $databases = array($database);
    } else {
        $databases = $database;
    }

    $tables = array();

    if (PMA_MYSQL_INT_VERSION >= 50002) {
        // get table information from information_schema
        if ($table) {
            if (true === $tbl_is_group) {
                $sql_where_table = 'AND `TABLE_NAME` LIKE \''
                    . PMA_escape_mysql_wildcards(addslashes($table)) . '%\'';
            } elseif ('comment' === $tbl_is_group) {
                $sql_where_table = 'AND `TABLE_COMMENT` LIKE \''
                    . PMA_escape_mysql_wildcards(addslashes($table)) . '%\'';
            } else {
                $sql_where_table = 'AND `TABLE_NAME` = \'' . addslashes($table) . '\'';
            }
        } else {
            $sql_where_table = '';
        }

        // for PMA bc:
        // `SCHEMA_FIELD_NAME` AS `SHOW_TABLE_STATUS_FIELD_NAME`
        //
        // on non-Windows servers,
        // added BINARY in the WHERE clause to force a case sensitive
        // comparison (if we are looking for the db Aa we don't want
        // to find the db aa)
        $this_databases = array_map('PMA_sqlAddslashes', $databases);

        $sql = '
             SELECT *,
                    `TABLE_SCHEMA`       AS `Db`,
                    `TABLE_NAME`         AS `Name`,
                    `ENGINE`             AS `Engine`,
                    `ENGINE`             AS `Type`,
                    `VERSION`            AS `Version`,
                    `ROW_FORMAT`         AS `Row_format`,
                    `TABLE_ROWS`         AS `Rows`,
                    `AVG_ROW_LENGTH`     AS `Avg_row_length`,
                    `DATA_LENGTH`        AS `Data_length`,
                    `MAX_DATA_LENGTH`    AS `Max_data_length`,
                    `INDEX_LENGTH`       AS `Index_length`,
                    `DATA_FREE`          AS `Data_free`,
                    `AUTO_INCREMENT`     AS `Auto_increment`,
                    `CREATE_TIME`        AS `Create_time`,
                    `UPDATE_TIME`        AS `Update_time`,
                    `CHECK_TIME`         AS `Check_time`,
                    `TABLE_COLLATION`    AS `Collation`,
                    `CHECKSUM`           AS `Checksum`,
                    `CREATE_OPTIONS`     AS `Create_options`,
                    `TABLE_COMMENT`      AS `Comment`
               FROM `information_schema`.`TABLES`
              WHERE ' . (PMA_IS_WINDOWS ? '' : 'BINARY') . ' `TABLE_SCHEMA` IN (\'' . implode("', '", $this_databases) . '\')
                ' . $sql_where_table;

        if ($limit_count) {
            $sql .= ' LIMIT ' . $limit_count . ' OFFSET ' . $limit_offset;
        }
        $tables = PMA_DBI_fetch_result($sql, array('TABLE_SCHEMA', 'TABLE_NAME'),
            null, $link);
        unset($sql_where_table, $sql);
    }
    // If permissions are wrong on even one database directory,
    // information_schema does not return any table info for any database
    // this is why we fall back to SHOW TABLE STATUS even for MySQL >= 50002
    if (PMA_MYSQL_INT_VERSION < 50002 || empty($tables)) {
        foreach ($databases as $each_database) {
            if (true === $tbl_is_group) {
                $sql = 'SHOW TABLE STATUS FROM '
                    . PMA_backquote($each_database)
                    .' LIKE \'' . PMA_escape_mysql_wildcards(addslashes($table)) . '%\'';
            } else {
                $sql = 'SHOW TABLE STATUS FROM '
                    . PMA_backquote($each_database) . ';';
            }
            $each_tables = PMA_DBI_fetch_result($sql, 'Name', null, $link);
            if ($limit_count) {
                $each_tables = array_slice($each_tables, $limit_offset, $limit_count);
            }

            foreach ($each_tables as $table_name => $each_table) {
                if ('comment' === $tbl_is_group
                  && 0 === strpos($each_table['Comment'], $table))
                {
                    // remove table from list
                    unset($each_tables[$table_name]);
                    continue;
                }

                if (! isset($each_tables[$table_name]['Type'])
                  && isset($each_tables[$table_name]['Engine'])) {
                    // pma BC, same parts of PMA still uses 'Type'
                    $each_tables[$table_name]['Type']
                        =& $each_tables[$table_name]['Engine'];
                } elseif (! isset($each_tables[$table_name]['Engine'])
                  && isset($each_tables[$table_name]['Type'])) {
                    // old MySQL reports Type, newer MySQL reports Engine
                    $each_tables[$table_name]['Engine']
                        =& $each_tables[$table_name]['Type'];
                }

                // MySQL forward compatibility
                // so pma could use this array as if every server is of version >5.0
                $each_tables[$table_name]['TABLE_SCHEMA']      = $each_database;
                $each_tables[$table_name]['TABLE_NAME']        =& $each_tables[$table_name]['Name'];
                $each_tables[$table_name]['ENGINE']            =& $each_tables[$table_name]['Engine'];
                $each_tables[$table_name]['VERSION']           =& $each_tables[$table_name]['Version'];
                $each_tables[$table_name]['ROW_FORMAT']        =& $each_tables[$table_name]['Row_format'];
                $each_tables[$table_name]['TABLE_ROWS']        =& $each_tables[$table_name]['Rows'];
                $each_tables[$table_name]['AVG_ROW_LENGTH']    =& $each_tables[$table_name]['Avg_row_length'];
                $each_tables[$table_name]['DATA_LENGTH']       =& $each_tables[$table_name]['Data_length'];
                $each_tables[$table_name]['MAX_DATA_LENGTH']   =& $each_tables[$table_name]['Max_data_length'];
                $each_tables[$table_name]['INDEX_LENGTH']      =& $each_tables[$table_name]['Index_length'];
                $each_tables[$table_name]['DATA_FREE']         =& $each_tables[$table_name]['Data_free'];
                $each_tables[$table_name]['AUTO_INCREMENT']    =& $each_tables[$table_name]['Auto_increment'];
                $each_tables[$table_name]['CREATE_TIME']       =& $each_tables[$table_name]['Create_time'];
                $each_tables[$table_name]['UPDATE_TIME']       =& $each_tables[$table_name]['Update_time'];
                $each_tables[$table_name]['CHECK_TIME']        =& $each_tables[$table_name]['Check_time'];
                $each_tables[$table_name]['TABLE_COLLATION']   =& $each_tables[$table_name]['Collation'];
                $each_tables[$table_name]['CHECKSUM']          =& $each_tables[$table_name]['Checksum'];
                $each_tables[$table_name]['CREATE_OPTIONS']    =& $each_tables[$table_name]['Create_options'];
                $each_tables[$table_name]['TABLE_COMMENT']     =& $each_tables[$table_name]['Comment'];

                if (strtoupper($each_tables[$table_name]['Comment']) === 'VIEW') {
                    $each_tables[$table_name]['TABLE_TYPE'] = 'VIEW';
                } else {
                    /**
                     * @todo difference between 'TEMPORARY' and 'BASE TABLE' but how to detect?
                     */
                    $each_tables[$table_name]['TABLE_TYPE'] = 'BASE TABLE';
                }
            }

            $tables[$each_database] = $each_tables;
        }
    }

    if ($GLOBALS['cfg']['NaturalOrder']) {
        foreach ($tables as $key => $val) {
            uksort($tables[$key], 'strnatcasecmp');
        }
    }

    if (! is_array($database)) {
        if (isset($tables[$database])) {
            return $tables[$database];
        } elseif (isset($tables[strtolower($database)])) {
            // on windows with lower_case_table_names = 1
            // MySQL returns
            // with SHOW DATABASES or information_schema.SCHEMATA: `Test`
            // but information_schema.TABLES gives `test`
            // bug #1436171
            // http://sf.net/support/tracker.php?aid=1436171
            return $tables[strtolower($database)];
        } else {
            return $tables;
        }
    } else {
        return $tables;
    }
}

/**
 * returns array with databases containing extended infos about them
 *
 * @todo    move into PMA_List_Database?
 * @param   string      $databases      database
 * @param   boolean     $force_stats    retrieve stats also for MySQL < 5
 * @param   resource    $link           mysql link
 * @param   string      $sort_by        collumn to order by
 * @param   string      $sort_order     ASC or DESC
 * @param   integer     $limit_offset   starting offset for LIMIT
 * @param   bool|int    $limit_count    row count for LIMIT or true for $GLOBALS['cfg']['MaxDbList']
 * @return  array       $databases
 */
function PMA_DBI_get_databases_full($database = null, $force_stats = false,
    $link = null, $sort_by = 'SCHEMA_NAME', $sort_order = 'ASC',
    $limit_offset = 0, $limit_count = false)
{
    $sort_order = strtoupper($sort_order);

    if (true === $limit_count) {
        $limit_count = $GLOBALS['cfg']['MaxDbList'];
    }

    // initialize to avoid errors when there are no databases
    $databases = array();

    $apply_limit_and_order_manual = true;

    if (PMA_MYSQL_INT_VERSION >= 50002) {
        /**
         * if $GLOBALS['cfg']['NaturalOrder'] is enabled, we cannot use LIMIT
         * cause MySQL does not support natural ordering, we have to do it afterward
         */
        if ($GLOBALS['cfg']['NaturalOrder']) {
            $limit = '';
        } else {
            if ($limit_count) {
                $limit = ' LIMIT ' . $limit_count . ' OFFSET ' . $limit_offset;
            }

            $apply_limit_and_order_manual = false;
        }

        // get table information from information_schema
        if ($database) {
            $sql_where_schema = 'WHERE `SCHEMA_NAME` LIKE \''
                . addslashes($database) . '\'';
        } else {
            $sql_where_schema = '';
        }

        // for PMA bc:
        // `SCHEMA_FIELD_NAME` AS `SHOW_TABLE_STATUS_FIELD_NAME`
        $sql = '
             SELECT `information_schema`.`SCHEMATA`.*';
        if ($force_stats) {
            $sql .= ',
                    COUNT(`information_schema`.`TABLES`.`TABLE_SCHEMA`)
                        AS `SCHEMA_TABLES`,
                    SUM(`information_schema`.`TABLES`.`TABLE_ROWS`)
                        AS `SCHEMA_TABLE_ROWS`,
                    SUM(`information_schema`.`TABLES`.`DATA_LENGTH`)
                        AS `SCHEMA_DATA_LENGTH`,
                    SUM(`information_schema`.`TABLES`.`MAX_DATA_LENGTH`)
                        AS `SCHEMA_MAX_DATA_LENGTH`,
                    SUM(`information_schema`.`TABLES`.`INDEX_LENGTH`)
                        AS `SCHEMA_INDEX_LENGTH`,
                    SUM(`information_schema`.`TABLES`.`DATA_LENGTH`
                      + `information_schema`.`TABLES`.`INDEX_LENGTH`)
                        AS `SCHEMA_LENGTH`,
                    SUM(`information_schema`.`TABLES`.`DATA_FREE`)
                        AS `SCHEMA_DATA_FREE`';
        }
        $sql .= '
               FROM `information_schema`.`SCHEMATA`';
        if ($force_stats) {
            $sql .= '
          LEFT JOIN `information_schema`.`TABLES`
                 ON BINARY `information_schema`.`TABLES`.`TABLE_SCHEMA`
                  = BINARY `information_schema`.`SCHEMATA`.`SCHEMA_NAME`';
        }
        $sql .= '
              ' . $sql_where_schema . '
           GROUP BY BINARY `information_schema`.`SCHEMATA`.`SCHEMA_NAME`
           ORDER BY BINARY ' . PMA_backquote($sort_by) . ' ' . $sort_order
           . $limit;
        $databases = PMA_DBI_fetch_result($sql, 'SCHEMA_NAME', null, $link);

        $mysql_error = PMA_DBI_getError($link);
        if (! count($databases) && $GLOBALS['errno']) {
            PMA_mysqlDie($mysql_error, $sql);
        }

        // display only databases also in official database list
        // f.e. to apply hide_db and only_db
        $drops = array_diff(array_keys($databases), $GLOBALS['PMA_List_Database']->items);
        if (count($drops)) {
            foreach ($drops as $drop) {
                unset($databases[$drop]);
            }
            unset($drop);
        }
        unset($sql_where_schema, $sql, $drops);
    } else {
        foreach ($GLOBALS['PMA_List_Database']->items as $database_name) {
            // MySQL forward compatibility
            // so pma could use this array as if every server is of version >5.0
            $databases[$database_name]['SCHEMA_NAME']      = $database_name;

            if ($force_stats) {
                require_once 'mysql_charsets.lib.php';

                $databases[$database_name]['DEFAULT_COLLATION_NAME']
                    = PMA_getDbCollation($database_name);

                // get additonal info about tables
                $databases[$database_name]['SCHEMA_TABLES']          = 0;
                $databases[$database_name]['SCHEMA_TABLE_ROWS']      = 0;
                $databases[$database_name]['SCHEMA_DATA_LENGTH']     = 0;
                $databases[$database_name]['SCHEMA_MAX_DATA_LENGTH'] = 0;
                $databases[$database_name]['SCHEMA_INDEX_LENGTH']    = 0;
                $databases[$database_name]['SCHEMA_LENGTH']          = 0;
                $databases[$database_name]['SCHEMA_DATA_FREE']       = 0;

                $res = PMA_DBI_query('SHOW TABLE STATUS FROM ' . PMA_backquote($database_name) . ';');
                while ($row = PMA_DBI_fetch_assoc($res)) {
                    $databases[$database_name]['SCHEMA_TABLES']++;
                    $databases[$database_name]['SCHEMA_TABLE_ROWS']
                        += $row['Rows'];
                    $databases[$database_name]['SCHEMA_DATA_LENGTH']
                        += $row['Data_length'];
                    $databases[$database_name]['SCHEMA_MAX_DATA_LENGTH']
                        += $row['Max_data_length'];
                    $databases[$database_name]['SCHEMA_INDEX_LENGTH']
                        += $row['Index_length'];
                    $databases[$database_name]['SCHEMA_DATA_FREE']
                        += $row['Data_free'];
                    $databases[$database_name]['SCHEMA_LENGTH']
                        += $row['Data_length'] + $row['Index_length'];
                }
                PMA_DBI_free_result($res);
                unset($res);
            }
        }
    }

    /**
     * apply limit and order manually now
     * (caused by older MySQL < 5 or $GLOBALS['cfg']['NaturalOrder'])
     */
    if ($apply_limit_and_order_manual) {

        /**
         * first apply ordering
         */
        if ($GLOBALS['cfg']['NaturalOrder']) {
            $sorter = 'strnatcasecmp';
        } else {
            $sorter = 'strcasecmp';
        }

        // produces f.e.:
        // return -1 * strnatcasecmp($a["SCHEMA_TABLES"], $b["SCHEMA_TABLES"])
        $sort_function = '
            return ' . ($sort_order == 'ASC' ? 1 : -1) . ' * ' . $sorter . '($a["' . $sort_by . '"], $b["' . $sort_by . '"]);
        ';

        usort($databases, create_function('$a, $b', $sort_function));

        /**
         * now apply limit
         */
        if ($limit_count) {
            $databases = array_slice($databases, $limit_offset, $limit_count);
        }
    }

    return $databases;
}

/**
 * returns detailed array with all columns for given table in database,
 * or all tables/databases
 *
 * @param   string  $database   name of database
 * @param   string  $table      name of table to retrieve columns from
 * @param   string  $column     name of specific column
 * @param   mixed   $link       mysql link resource
 */
function PMA_DBI_get_columns_full($database = null, $table = null,
    $column = null, $link = null)
{
    $columns = array();

    if (PMA_MYSQL_INT_VERSION >= 50002) {
        $sql_wheres = array();
        $array_keys = array();

        // get columns information from information_schema
        if (null !== $database) {
            $sql_wheres[] = '`TABLE_SCHEMA` = \'' . addslashes($database) . '\' ';
        } else {
            $array_keys[] = 'TABLE_SCHEMA';
        }
        if (null !== $table) {
            $sql_wheres[] = '`TABLE_NAME` = \'' . addslashes($table) . '\' ';
        } else {
            $array_keys[] = 'TABLE_NAME';
        }
        if (null !== $column) {
            $sql_wheres[] = '`COLUMN_NAME` = \'' . addslashes($column) . '\' ';
        } else {
            $array_keys[] = 'COLUMN_NAME';
        }

        // for PMA bc:
        // `[SCHEMA_FIELD_NAME]` AS `[SHOW_FULL_COLUMNS_FIELD_NAME]`
        $sql = '
             SELECT *,
                    `COLUMN_NAME`       AS `Field`,
                    `COLUMN_TYPE`       AS `Type`,
                    `COLLATION_NAME`    AS `Collation`,
                    `IS_NULLABLE`       AS `Null`,
                    `COLUMN_KEY`        AS `Key`,
                    `COLUMN_DEFAULT`    AS `Default`,
                    `EXTRA`             AS `Extra`,
                    `PRIVILEGES`        AS `Privileges`,
                    `COLUMN_COMMENT`    AS `Comment`
               FROM `information_schema`.`COLUMNS`';
        if (count($sql_wheres)) {
            $sql .= "\n" . ' WHERE ' . implode(' AND ', $sql_wheres);
        }

        $columns = PMA_DBI_fetch_result($sql, $array_keys, null, $link);
        unset($sql_wheres, $sql);
    } else {
        if (null === $database) {
            foreach ($GLOBALS['PMA_List_Database']->items as $database) {
                $columns[$database] = PMA_DBI_get_columns_full($database, null,
                    null, $link);
            }
            return $columns;
        } elseif (null === $table) {
            $tables = PMA_DBI_get_tables($database);
            foreach ($tables as $table) {
                $columns[$table] = PMA_DBI_get_columns_full(
                    $database, $table, null, $link);
            }
            return $columns;
        }

        $sql = 'SHOW FULL COLUMNS FROM '
            . PMA_backquote($database) . '.' . PMA_backquote($table);
        if (null !== $column) {
            $sql .= " LIKE '" . $column . "'";
        }

        $columns = PMA_DBI_fetch_result($sql, 'Field', null, $link);

        $ordinal_position = 1;
        foreach ($columns as $column_name => $each_column) {

            // MySQL forward compatibility
            // so pma could use this array as if every server is of version >5.0
            $columns[$column_name]['COLUMN_NAME']                 =& $columns[$column_name]['Field'];
            $columns[$column_name]['COLUMN_TYPE']                 =& $columns[$column_name]['Type'];
            $columns[$column_name]['COLLATION_NAME']              =& $columns[$column_name]['Collation'];
            $columns[$column_name]['IS_NULLABLE']                 =& $columns[$column_name]['Null'];
            $columns[$column_name]['COLUMN_KEY']                  =& $columns[$column_name]['Key'];
            $columns[$column_name]['COLUMN_DEFAULT']              =& $columns[$column_name]['Default'];
            $columns[$column_name]['EXTRA']                       =& $columns[$column_name]['Extra'];
            $columns[$column_name]['PRIVILEGES']                  =& $columns[$column_name]['Privileges'];
            $columns[$column_name]['COLUMN_COMMENT']              =& $columns[$column_name]['Comment'];

            $columns[$column_name]['TABLE_CATALOG']               = null;
            $columns[$column_name]['TABLE_SCHEMA']                = $database;
            $columns[$column_name]['TABLE_NAME']                  = $table;
            $columns[$column_name]['ORDINAL_POSITION']            = $ordinal_position;
            $columns[$column_name]['DATA_TYPE']                   =
                substr($columns[$column_name]['COLUMN_TYPE'], 0,
                    strpos($columns[$column_name]['COLUMN_TYPE'], '('));
            /**
             * @todo guess CHARACTER_MAXIMUM_LENGTH from COLUMN_TYPE
             */
            $columns[$column_name]['CHARACTER_MAXIMUM_LENGTH']    = null;
            /**
             * @todo guess CHARACTER_OCTET_LENGTH from CHARACTER_MAXIMUM_LENGTH
             */
            $columns[$column_name]['CHARACTER_OCTET_LENGTH']      = null;
            $columns[$column_name]['NUMERIC_PRECISION']           = null;
            $columns[$column_name]['NUMERIC_SCALE']               = null;
            $columns[$column_name]['CHARACTER_SET_NAME']          =
                substr($columns[$column_name]['COLLATION_NAME'], 0,
                    strpos($columns[$column_name]['COLLATION_NAME'], '_'));

            $ordinal_position++;
        }

        if (null !== $column) {
            reset($columns);
            $columns = current($columns);
        }
    }

    return $columns;
}

/**
 * @todo should only return columns names, for more info use PMA_DBI_get_columns_full()
 *
 * @deprecated by PMA_DBI_get_columns() or PMA_DBI_get_columns_full()
 * @param   string  $database   name of database
 * @param   string  $table      name of table to retrieve columns from
 * @param   mixed   $link       mysql link resource
 * @return  array   column info
 */
function PMA_DBI_get_fields($database, $table, $link = null)
{
    // here we use a try_query because when coming from
    // tbl_create + tbl_properties.inc.php, the table does not exist
    $fields = PMA_DBI_fetch_result(
        'SHOW FULL COLUMNS
        FROM ' . PMA_backquote($database) . '.' . PMA_backquote($table),
        null, null, $link);
    if (! is_array($fields) || count($fields) < 1) {
        return false;
    }
    return $fields;
}

/**
 * array PMA_DBI_get_columns(string $database, string $table, bool $full = false, mysql db link $link = null)
 *
 * @param   string  $database   name of database
 * @param   string  $table      name of table to retrieve columns from
 * @param   boolean $full       wether to return full info or only column names
 * @param   mixed   $link       mysql link resource
 * @return  array   column names
 */
function PMA_DBI_get_columns($database, $table, $full = false, $link = null)
{
    $fields = PMA_DBI_fetch_result(
        'SHOW ' . ($full ? 'FULL' : '') . ' COLUMNS
        FROM ' . PMA_backquote($database) . '.' . PMA_backquote($table),
        'Field', ($full ? null : 'Field'), $link);
    if (! is_array($fields) || count($fields) < 1) {
        return false;
    }
    return $fields;
}

/**
 * returns value of given mysql server variable
 *
 * @param   string  $var    mysql server variable name
 * @param   int     $type   PMA_DBI_GETVAR_SESSION|PMA_DBI_GETVAR_GLOBAL
 * @param   mixed   $link   mysql link resource|object
 * @return  mixed   value for mysql server variable
 */
function PMA_DBI_get_variable($var, $type = PMA_DBI_GETVAR_SESSION, $link = null)
{
    if ($link === null) {
        if (isset($GLOBALS['userlink'])) {
            $link = $GLOBALS['userlink'];
        } else {
            return false;
        }
    }
    if (PMA_MYSQL_INT_VERSION < 40002) {
        $type = 0;
    }
    switch ($type) {
        case PMA_DBI_GETVAR_SESSION:
            $modifier = ' SESSION';
            break;
        case PMA_DBI_GETVAR_GLOBAL:
            $modifier = ' GLOBAL';
            break;
        default:
            $modifier = '';
    }
    return PMA_DBI_fetch_value(
        'SHOW' . $modifier . ' VARIABLES LIKE \'' . $var . '\';', 0, 1, $link);
}

/**
 * @uses    ./libraries/charset_conversion.lib.php
 * @uses    PMA_DBI_QUERY_STORE
 * @uses    PMA_REMOVED_NON_UTF_8
 * @uses    PMA_MYSQL_INT_VERSION
 * @uses    PMA_MYSQL_STR_VERSION
 * @uses    PMA_DBI_GETVAR_SESSION
 * @uses    PMA_DBI_fetch_value()
 * @uses    PMA_DBI_query()
 * @uses    PMA_DBI_get_variable()
 * @uses    $GLOBALS['collation_connection']
 * @uses    $GLOBALS['charset_connection']
 * @uses    $GLOBALS['available_languages']
 * @uses    $GLOBALS['mysql_charset_map']
 * @uses    $GLOBALS['charset']
 * @uses    $GLOBALS['lang']
 * @uses    $GLOBALS['cfg']['Lang']
 * @uses    $GLOBALS['cfg']['ColumnTypes']
 * @uses    defined()
 * @uses    explode()
 * @uses    sprintf()
 * @uses    intval()
 * @uses    define()
 * @uses    defined()
 * @uses    substr()
 * @uses    count()
 * @param   mixed   $link   mysql link resource|object
 * @param   boolean $is_controluser
 */
function PMA_DBI_postConnect($link, $is_controluser = false)
{
    if (!defined('PMA_MYSQL_INT_VERSION')) {
        $mysql_version = PMA_DBI_fetch_value(
            'SELECT VERSION()', 0, 0, $link, PMA_DBI_QUERY_STORE);
        if ($mysql_version) {
            $match = explode('.', $mysql_version);
            define('PMA_MYSQL_INT_VERSION',
                (int) sprintf('%d%02d%02d', $match[0], $match[1],
                        intval($match[2])));
            define('PMA_MYSQL_STR_VERSION', $mysql_version);
            unset($mysql_version, $match);
        } else {
            define('PMA_MYSQL_INT_VERSION', 32332);
            define('PMA_MYSQL_STR_VERSION', '3.23.32');
        }
    }

    if (!defined('PMA_ENGINE_KEYWORD')) {
        if (PMA_MYSQL_INT_VERSION >= 40102) {
            define('PMA_ENGINE_KEYWORD','ENGINE');
        } else {
            define('PMA_ENGINE_KEYWORD','TYPE');
        }
    }

    if (PMA_MYSQL_INT_VERSION >= 40100) {

        // If $lang is defined and we are on MySQL >= 4.1.x,
        // we auto-switch the lang to its UTF-8 version (if it exists and user
        // didn't force language)
        if (!empty($GLOBALS['lang'])
          && (substr($GLOBALS['lang'], -5) != 'utf-8')
          && !isset($GLOBALS['cfg']['Lang'])) {
            $lang_utf_8_version =
                substr($GLOBALS['lang'], 0, strpos($GLOBALS['lang'], '-'))
                . '-utf-8';
            if (!empty($GLOBALS['available_languages'][$lang_utf_8_version])) {
                $GLOBALS['lang'] = $lang_utf_8_version;
                $GLOBALS['charset'] = 'utf-8';
                define('PMA_LANG_RELOAD', 1);
            }
        }

        // and we remove the non-UTF-8 choices to avoid confusion
        // (unless there is a forced language)
        if (!defined('PMA_REMOVED_NON_UTF_8') && ! isset($GLOBALS['cfg']['Lang'])) {
            foreach ($GLOBALS['available_languages'] as $each_lang => $dummy) {
                if (substr($each_lang, -5) != 'utf-8') {
                    unset($GLOBALS['available_languages'][$each_lang]);
                }
            }
            define('PMA_REMOVED_NON_UTF_8', 1);
        }

        $mysql_charset = $GLOBALS['mysql_charset_map'][$GLOBALS['charset']];
        if ($is_controluser
          || empty($GLOBALS['collation_connection'])
          || (strpos($GLOBALS['collation_connection'], '_')
                ? substr($GLOBALS['collation_connection'], 0, strpos($GLOBALS['collation_connection'], '_'))
                : $GLOBALS['collation_connection']) == $mysql_charset) {

            PMA_DBI_query('SET NAMES ' . $mysql_charset . ';', $link,
                PMA_DBI_QUERY_STORE);
        } else {
            PMA_DBI_query('SET CHARACTER SET ' . $mysql_charset . ';', $link,
                PMA_DBI_QUERY_STORE);
        }
        if (!empty($GLOBALS['collation_connection'])) {
            PMA_DBI_query('SET collation_connection = \'' . $GLOBALS['collation_connection'] . '\';',
                $link, PMA_DBI_QUERY_STORE);
        }
        if (!$is_controluser) {
            $GLOBALS['collation_connection'] = PMA_DBI_get_variable('collation_connection',
                PMA_DBI_GETVAR_SESSION, $link);
            $GLOBALS['charset_connection']   = PMA_DBI_get_variable('character_set_connection',
                PMA_DBI_GETVAR_SESSION, $link);
        }

        // Add some field types to the list, this needs to be done once per session!
        if (!in_array('BINARY', $GLOBALS['cfg']['ColumnTypes'])) {
            $GLOBALS['cfg']['ColumnTypes'][] = 'BINARY';
        }
        if (!in_array('VARBINARY', $GLOBALS['cfg']['ColumnTypes'])) {
            $GLOBALS['cfg']['ColumnTypes'][] = 'VARBINARY';
        }
    } else {
        require_once './libraries/charset_conversion.lib.php';
    }
}

/**
 * returns a single value from the given result or query,
 * if the query or the result has more than one row or field
 * the first field of the first row is returned
 *
 * <code>
 * $sql = 'SELECT `name` FROM `user` WHERE `id` = 123';
 * $user_name = PMA_DBI_fetch_value($sql);
 * // produces
 * // $user_name = 'John Doe'
 * </code>
 *
 * @uses    is_string()
 * @uses    is_int()
 * @uses    PMA_DBI_try_query()
 * @uses    PMA_DBI_num_rows()
 * @uses    PMA_DBI_fetch_row()
 * @uses    PMA_DBI_fetch_assoc()
 * @uses    PMA_DBI_free_result()
 * @param   string|mysql_result $result query or mysql result
 * @param   integer             $row_number row to fetch the value from,
 *                                      starting at 0, with 0 beeing default
 * @param   integer|string      $field  field to fetch the value from,
 *                                      starting at 0, with 0 beeing default
 * @param   resource            $link   mysql link
 * @param   mixed               $options
 * @return  mixed               value of first field in first row from result
 *                              or false if not found
 */
function PMA_DBI_fetch_value($result, $row_number = 0, $field = 0, $link = null, $options = 0) {
    $value = false;

    if (is_string($result)) {
        $result = PMA_DBI_try_query($result, $link, $options | PMA_DBI_QUERY_STORE);
    }

    // return false if result is empty or false
    // or requested row is larger than rows in result
    if (PMA_DBI_num_rows($result) < ($row_number + 1)) {
        return $value;
    }

    // if $field is an integer use non associative mysql fetch function
    if (is_int($field)) {
        $fetch_function = 'PMA_DBI_fetch_row';
    } else {
        $fetch_function = 'PMA_DBI_fetch_assoc';
    }

    // get requested row
    for ($i = 0; $i <= $row_number; $i++) {
        $row = $fetch_function($result);
    }
    PMA_DBI_free_result($result);

    // return requested field
    if (isset($row[$field])) {
        $value = $row[$field];
    }
    unset($row);

    return $value;
}

/**
 * returns only the first row from the result
 *
 * <code>
 * $sql = 'SELECT * FROM `user` WHERE `id` = 123';
 * $user = PMA_DBI_fetch_single_row($sql);
 * // produces
 * // $user = array('id' => 123, 'name' => 'John Doe')
 * </code>
 *
 * @uses    is_string()
 * @uses    PMA_DBI_try_query()
 * @uses    PMA_DBI_num_rows()
 * @uses    PMA_DBI_fetch_row()
 * @uses    PMA_DBI_fetch_assoc()
 * @uses    PMA_DBI_fetch_array()
 * @uses    PMA_DBI_free_result()
 * @param   string|mysql_result $result query or mysql result
 * @param   string              $type   NUM|ASSOC|BOTH
 *                                      returned array should either numeric
 *                                      associativ or booth
 * @param   resource            $link   mysql link
 * @param   mixed               $options
 * @return  array|boolean       first row from result
 *                              or false if result is empty
 */
function PMA_DBI_fetch_single_row($result, $type = 'ASSOC', $link = null, $options = 0) {
    if (is_string($result)) {
        $result = PMA_DBI_try_query($result, $link, $options | PMA_DBI_QUERY_STORE);
    }

    // return null if result is empty or false
    if (! PMA_DBI_num_rows($result)) {
        return false;
    }

    switch ($type) {
        case 'NUM' :
            $fetch_function = 'PMA_DBI_fetch_row';
            break;
        case 'ASSOC' :
            $fetch_function = 'PMA_DBI_fetch_assoc';
            break;
        case 'BOTH' :
        default :
            $fetch_function = 'PMA_DBI_fetch_array';
            break;
    }

    $row = $fetch_function($result);
    PMA_DBI_free_result($result);
    return $row;
}

/**
 * returns all rows in the resultset in one array
 *
 * <code>
 * $sql = 'SELECT * FROM `user`';
 * $users = PMA_DBI_fetch_result($sql);
 * // produces
 * // $users[] = array('id' => 123, 'name' => 'John Doe')
 *
 * $sql = 'SELECT `id`, `name` FROM `user`';
 * $users = PMA_DBI_fetch_result($sql, 'id');
 * // produces
 * // $users['123'] = array('id' => 123, 'name' => 'John Doe')
 *
 * $sql = 'SELECT `id`, `name` FROM `user`';
 * $users = PMA_DBI_fetch_result($sql, 0);
 * // produces
 * // $users['123'] = array(0 => 123, 1 => 'John Doe')
 *
 * $sql = 'SELECT `id`, `name` FROM `user`';
 * $users = PMA_DBI_fetch_result($sql, 'id', 'name');
 * // or
 * $users = PMA_DBI_fetch_result($sql, 0, 1);
 * // produces
 * // $users['123'] = 'John Doe'
 *
 * $sql = 'SELECT `name` FROM `user`';
 * $users = PMA_DBI_fetch_result($sql);
 * // produces
 * // $users[] = 'John Doe'
 * </code>
 *
 * @uses    is_string()
 * @uses    is_int()
 * @uses    PMA_DBI_try_query()
 * @uses    PMA_DBI_num_rows()
 * @uses    PMA_DBI_num_fields()
 * @uses    PMA_DBI_fetch_row()
 * @uses    PMA_DBI_fetch_assoc()
 * @uses    PMA_DBI_free_result()
 * @param   string|mysql_result $result query or mysql result
 * @param   string|integer      $key    field-name or offset
 *                                      used as key for array
 * @param   string|integer      $value  value-name or offset
 *                                      used as value for array
 * @param   resource            $link   mysql link
 * @param   mixed               $options
 * @return  array               resultrows or values indexed by $key
 */
function PMA_DBI_fetch_result($result, $key = null, $value = null,
    $link = null, $options = 0)
{
    $resultrows = array();

    if (is_string($result)) {
        $result = PMA_DBI_try_query($result, $link, $options);
    }

    // return empty array if result is empty or false
    if (! $result) {
        return $resultrows;
    }

    $fetch_function = 'PMA_DBI_fetch_assoc';

    // no nested array if only one field is in result
    if (null === $key && 1 === PMA_DBI_num_fields($result)) {
        $value = 0;
        $fetch_function = 'PMA_DBI_fetch_row';
    }

    // if $key is an integer use non associative mysql fetch function
    if (is_int($key)) {
        $fetch_function = 'PMA_DBI_fetch_row';
    }

    if (null === $key && null === $value) {
        while ($row = $fetch_function($result)) {
            $resultrows[] = $row;
        }
    } elseif (null === $key) {
        while ($row = $fetch_function($result)) {
            $resultrows[] = $row[$value];
        }
    } elseif (null === $value) {
        if (is_array($key)) {
            while ($row = $fetch_function($result)) {
                $result_target =& $resultrows;
                foreach ($key as $key_index) {
                    if (! isset($result_target[$row[$key_index]])) {
                        $result_target[$row[$key_index]] = array();
                    }
                    $result_target =& $result_target[$row[$key_index]];
                }
                $result_target = $row;
            }
        } else {
            while ($row = $fetch_function($result)) {
                $resultrows[$row[$key]] = $row;
            }
        }
    } else {
        if (is_array($key)) {
            while ($row = $fetch_function($result)) {
                $result_target =& $resultrows;
                foreach ($key as $key_index) {
                    if (! isset($result_target[$row[$key_index]])) {
                        $result_target[$row[$key_index]] = array();
                    }
                    $result_target =& $result_target[$row[$key_index]];
                }
                $result_target = $row[$value];
            }
        } else {
            while ($row = $fetch_function($result)) {
                $resultrows[$row[$key]] = $row[$value];
            }
        }
    }

    PMA_DBI_free_result($result);
    return $resultrows;
}

/**
 * return default table engine for given database
 *
 * @return  string  default table engine
 */
function PMA_DBI_get_default_engine()
{
    if (PMA_MYSQL_INT_VERSION > 50002) {
        return PMA_DBI_fetch_value('SHOW VARIABLES LIKE \'storage_engine\';', 0, 1);
    } else {
        return PMA_DBI_fetch_value('SHOW VARIABLES LIKE \'table_type\';', 0, 1);
    }
}

/**
 * Get supported SQL compatibility modes
 *
 * @return  array   supported SQL compatibility modes
 */
function PMA_DBI_getCompatibilities()
{
    if (PMA_MYSQL_INT_VERSION < 40100) {
        return array();
    }
    $compats = array('NONE');
    if (PMA_MYSQL_INT_VERSION >= 40101) {
        $compats[] = 'ANSI';
        $compats[] = 'DB2';
        $compats[] = 'MAXDB';
        $compats[] = 'MYSQL323';
        $compats[] = 'MYSQL40';
        $compats[] = 'MSSQL';
        $compats[] = 'ORACLE';
        // removed; in MySQL 5.0.33, this produces exports that
        // can't be read by POSTGRESQL (see our bug #1596328)
        //$compats[] = 'POSTGRESQL';
        if (PMA_MYSQL_INT_VERSION >= 50002) {
            $compats[] = 'TRADITIONAL';
        }
    }
    return $compats;
}

/**
 * returns warnings for last query
 *
 * @uses    $GLOBALS['userlink']
 * @uses    PMA_DBI_fetch_result()
 * @param   resource mysql link  $link   mysql link resource
 * @return  array   warnings
 */
function PMA_DBI_get_warnings($link = null)
{
    if (PMA_MYSQL_INT_VERSION < 40100) {
        return array();
    }

    if (empty($link)) {
        if (isset($GLOBALS['userlink'])) {
            $link = $GLOBALS['userlink'];
        } else {
            return array();
        }
    }

    return PMA_DBI_fetch_result('SHOW WARNINGS', null, null, $link);
}

/**
 * returns true (int > 0) if current user is superuser
 * otherwise 0
 *
 * @return integer  $is_superuser
 */
function PMA_isSuperuser() {
    return PMA_DBI_try_query('SELECT COUNT(*) FROM mysql.user',
        $GLOBALS['userlink'], PMA_DBI_QUERY_STORE);
}


/**
 * returns an array of PROCEDURE or FUNCTION names for a db
 *
 * @uses    PMA_DBI_free_result()
 * @param   string              $db     db name
 * @param   string              $which  PROCEDURE | FUNCTION
 * @param   resource            $link   mysql link
 *
 * @return  array   the procedure names or function names
 */
function PMA_DBI_get_procedures_or_functions($db, $which, $link = null) {

    $shows = PMA_DBI_fetch_result('SHOW ' . $which . ' STATUS;', null, null, $link);
    $result = array();
    foreach ($shows as $one_show) {
        if ($one_show['Db'] == $db && $one_show['Type'] == $which) {
            $result[] = $one_show['Name'];
        }
    }
    return($result);
}

/**
 * returns the definition of a specific PROCEDURE or FUNCTION
 *
 * @uses    PMA_DBI_fetch_value()
 * @param   string              $db     db name
 * @param   string              $which  PROCEDURE | FUNCTION
 * @param   string              $proc_or_function_name  the procedure name or function name
 * @param   resource            $link   mysql link
 *
 * @return  string              the procedure's or function's definition
 */
function PMA_DBI_get_procedure_or_function_def($db, $which, $proc_or_function_name, $link = null) {

    $returned_field = array('PROCEDURE' => 'Create Procedure', 'FUNCTION' => 'Create Function');
    $query = 'SHOW CREATE ' . $which . ' ' . PMA_backquote($db) . '.' . PMA_backquote($proc_or_function_name);
    return(PMA_DBI_fetch_value($query, 0, $returned_field[$which]));
}

/**
 * returns details about the TRIGGERs of a specific table
 *
 * @uses    PMA_DBI_fetch_result()
 * @param   string              $db     db name
 * @param   string              $table  table name
 *
 * @return  array               information about triggers (may be empty)
 */
function PMA_DBI_get_triggers($db, $table) {

    $result = array();

    // available in INFORMATION_SCHEMA since MySQL 5.0.10
    if (PMA_MYSQL_INT_VERSION >= 50010) {
        $triggers = PMA_DBI_fetch_result("SELECT TRIGGER_SCHEMA, TRIGGER_NAME, EVENT_MANIPULATION, ACTION_TIMING, ACTION_STATEMENT, EVENT_OBJECT_SCHEMA, EVENT_OBJECT_TABLE FROM information_schema.TRIGGERS WHERE EVENT_OBJECT_SCHEMA= '" . PMA_sqlAddslashes($db,true) . "' and EVENT_OBJECT_TABLE = '" . PMA_sqlAddslashes($table, true) . "';");

        if ($triggers) {
            $delimiter = '//';
            foreach ($triggers as $trigger) {
                $one_result = array();
                $one_result['name'] = $trigger['TRIGGER_NAME'];
                $one_result['action_timing'] = $trigger['ACTION_TIMING'];
                $one_result['event_manipulation'] = $trigger['EVENT_MANIPULATION'];

                $one_result['full_trigger_name'] = PMA_backquote($trigger['TRIGGER_SCHEMA']) . '.' . PMA_backquote($trigger['TRIGGER_NAME']);
                $one_result['drop'] = 'DROP TRIGGER IF EXISTS ' . $one_result['full_trigger_name'];
                $one_result['create'] = 'CREATE TRIGGER ' . $one_result['full_trigger_name'] . ' ' . $trigger['ACTION_TIMING']. ' ' . $trigger['EVENT_MANIPULATION'] . ' ON ' . PMA_backquote($trigger['EVENT_OBJECT_SCHEMA']) . '.' . PMA_backquote($trigger['EVENT_OBJECT_TABLE']) . "\n" . ' FOR EACH ROW ' . $trigger['ACTION_STATEMENT'] . "\n" . $delimiter . "\n";

                $result[] = $one_result;
            }
        }
    }
    return($result);
}
?>
