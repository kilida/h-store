/* This file is part of VoltDB.
 * Copyright (C) 2008 Vertica Systems Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */



package org.hsqldb;

/**
 * Class provides static methods that return embed-able DTDs
 * for the XML generated by HZSQLInterface. These DTDs don't
 * really add any functionality, but the can be very helpful
 * in testing.
 *
 */
public abstract class DTDSource {

    private static String getExpressionElementList() {
        return "(value|columnref|bool|asterisk|operation|function)*";
    }

    private static String getExpressionDTDFragment() {
        StringBuilder sb = new StringBuilder();

        sb.append("  <!ELEMENT value ANY>\n");
        sb.append("  <!ATTLIST value\n");
        sb.append("    id CDATA #REQUIRED\n");
        sb.append("    type CDATA #REQUIRED\n");
        sb.append("    value CDATA #IMPLIED\n");
        sb.append("    isparam CDATA #IMPLIED>\n");

        sb.append("  <!ELEMENT columnref ANY>\n");
        sb.append("  <!ATTLIST columnref\n");
        sb.append("    id CDATA #REQUIRED\n");
        sb.append("    table CDATA #REQUIRED\n");
        sb.append("    column CDATA #REQUIRED\n");
        sb.append("    alias CDATA #IMPLIED\n");
        sb.append("    dir CDATA #IMPLIED>\n");

        sb.append("  <!ELEMENT bool ANY>\n");
        sb.append("  <!ATTLIST bool\n");
        sb.append("    id CDATA #REQUIRED\n");
        sb.append("    value CDATA #REQUIRED>\n");

        sb.append("  <!ELEMENT asterisk ANY>\n");

        sb.append("  <!ELEMENT groupcolumns (").append(getExpressionElementList()).append(")>\n");
        sb.append("  <!ELEMENT ordercolumns (").append(getExpressionElementList()).append(")>\n");
        sb.append("  <!ELEMENT operation (").append(getExpressionElementList()).append(")>\n");
        sb.append("  <!ATTLIST operation\n");
        sb.append("    id CDATA #REQUIRED\n");
        sb.append("    type CDATA #REQUIRED\n");
        sb.append("    alias CDATA #IMPLIED\n");
        sb.append("    distinct CDATA #IMPLIED\n");
        sb.append("    desc CDATA #IMPLIED>\n");


        sb.append("  <!ELEMENT function (").append(getExpressionElementList()).append(")>\n");
        sb.append("  <!ATTLIST function\n");
        sb.append("    id CDATA #IMPLIED\n");
        sb.append("    name CDATA #REQUIRED\n");
        sb.append("    type CDATA #REQUIRED>\n");

        return sb.toString();
    }

    /**
     * Get an embed-able DTD that will validate the XML output
     * for the Catalog XML output.
     * @return the DTD as a string.
     */
    public static String getCatalogDTD() {
        StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE databaseschema [\n");
        sb.append("  <!ELEMENT databaseschema (table*)>\n");

        sb.append("  <!ELEMENT table (columns,indexes,constraints)>\n");
        sb.append("  <!ATTLIST table\n");
        sb.append("    name CDATA #REQUIRED\n");
        sb.append("    isStream CDATA #IMPLIED\n"); // added by hawk
        sb.append("    query CDATA #IMPLIED>\n");

        sb.append("  <!ELEMENT columns (column*)>\n");
        sb.append("  <!ELEMENT indexes (index*)>\n");
        sb.append("  <!ELEMENT constraints (constraint*)>\n");

        sb.append("  <!ELEMENT column (default?)>\n");
        sb.append("  <!ATTLIST column\n");
        sb.append("    name CDATA #REQUIRED\n");
        sb.append("    type CDATA #REQUIRED\n");
        sb.append("    nullable CDATA #REQUIRED\n");
        sb.append("    size CDATA #REQUIRED>\n");
        sb.append("  <!ELEMENT default (").append(getExpressionElementList()).append(")>\n");

        sb.append(getExpressionDTDFragment());

        sb.append("  <!ELEMENT index ANY>\n");
        sb.append("  <!ATTLIST index\n");
        sb.append("    name CDATA #REQUIRED\n");
        sb.append("    unique CDATA #REQUIRED\n");
        sb.append("    columns CDATA #REQUIRED>\n");

        sb.append("  <!ELEMENT constraint ANY>\n");
        sb.append("  <!ATTLIST constraint\n");
        sb.append("    name CDATA #REQUIRED\n");
        sb.append("    type CDATA #REQUIRED\n");
        sb.append("    index CDATA #IMPLIED\n");
        sb.append("    foreignkeytable CDATA #IMPLIED>\n");

        sb.append("  <!ELEMENT reference ANY>\n");
        sb.append("  <!ATTLIST reference\n");
        sb.append("    from CDATA #REQUIRED\n");
        sb.append("    to CDATA #REQUIRED>\n");

        sb.append("]>\n");

        return sb.toString();
    }

    /**
     * Get an embed-able DTD that will validate the XML output
     * for Compiled Statements.
     * @return the DTD as a string.
     */
    public static String getCompiledStatementDTD() {
        StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE statement [\n");

        sb.append("  <!ELEMENT statement (select|update|insert|delete)>\n");

        sb.append("  <!ELEMENT select (columns,parameters,tablescans,querycondition?,havingcondition?,ordercolumns?)>\n");
        sb.append("  <!ATTLIST select\n");
        sb.append("    limit CDATA #IMPLIED\n");
        sb.append("    limit_paramid CDATA #IMPLIED\n");
        sb.append("    offset CDATA #IMPLIED\n");
        sb.append("    offset_paramid CDATA #IMPLIED\n");
        sb.append("    distinct CDATA #IMPLIED\n");
        sb.append("    grouped CDATA #IMPLIED\n");
        sb.append("    aggregated CDATA #IMPLIED>\n");

        sb.append("  <!ELEMENT values (columnref*|").append(getExpressionElementList()).append(")>\n");
        sb.append("  <!ELEMENT columns (columnref*|column*|").append(getExpressionElementList()).append(")>\n");

        // columnref is declared in the included expression section

        sb.append("  <!ELEMENT parameters (parameter*)>\n");
        sb.append("  <!ELEMENT parameter ANY>\n");
        sb.append("  <!ATTLIST parameter\n");
        sb.append("    index CDATA #REQUIRED\n");
        sb.append("    id CDATA #REQUIRED\n");
        sb.append("    type CDATA #REQUIRED>\n");

        sb.append("  <!ELEMENT tablescans (tablescan*)>\n");
        sb.append("  <!ELEMENT tablescan (searchkey?,startexp?,endexp?,postexp?)>\n");
        sb.append("  <!ATTLIST tablescan\n");
        sb.append("    type CDATA #REQUIRED\n");
        sb.append("    table CDATA #REQUIRED\n");
        sb.append("    index CDATA #IMPLIED\n");
        sb.append("    alias CDATA #IMPLIED>\n");

        sb.append("  <!ELEMENT searchkey (").append(getExpressionElementList()).append(")>\n");
        sb.append("  <!ELEMENT startexp (").append(getExpressionElementList()).append(")>\n");
        sb.append("  <!ELEMENT endexp (").append(getExpressionElementList()).append(")>\n");
        sb.append("  <!ELEMENT postexp (").append(getExpressionElementList()).append(")>\n");

        sb.append("  <!ELEMENT querycondition (").append(getExpressionElementList()).append(")>\n");
        sb.append("  <!ELEMENT havingcondition (").append(getExpressionElementList()).append(")>\n");

        sb.append(getExpressionDTDFragment());

        sb.append("  <!ELEMENT update (columns,parameters,tablescan,condition?)>\n");
        sb.append("  <!ATTLIST update\n");
        sb.append("    table CDATA #REQUIRED>\n");
        sb.append("  <!ELEMENT condition (").append(getExpressionElementList()).append(")>\n");

        sb.append("  <!ELEMENT column (").append(getExpressionElementList()).append(")>\n");
        sb.append("  <!ATTLIST column\n");
        sb.append("    name CDATA #REQUIRED\n");
        sb.append("    table CDATA #REQUIRED>\n");

        sb.append("  <!ELEMENT insert (columns,parameters,tablescan?,condition?)>\n");
        sb.append("  <!ATTLIST insert\n");
        sb.append("    table CDATA #REQUIRED>\n");

        sb.append("  <!ELEMENT delete (parameters,tablescan?,condition?)>\n");
        sb.append("  <!ATTLIST delete\n");
        sb.append("    table CDATA #REQUIRED>\n");

        sb.append("]>\n");

        return sb.toString();
    }
}
