/*
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   SmartCity Jena - initial
*   Stefan Bischof (bipolis.org) - initial
*/
package org.eclipse.daanse.jdbc.loader.csv.api;

/**
 *
 */
public class Constants {
    private Constants() {
    }

    public static final String PID_LOADER_FILEWATCHER = "org.eclipse.daanse.jdbc.loader.csv.FileWatchingLoader";

    public static final String PROPERETY_CSV_NULL_VALUE = "nullValue";
    public static final String PROPERETY_CSV_QUOTE_CHARACHTER = "quoteCharacter";
    public static final String PROPERETY_CSV_FIELD_SEPARATOR = "fieldSeparator";
    public static final String PROPERETY_CSV_ENCODING = "encoding";
    public static final String PROPERETY_CSV_SKIP_EMPTY_LINES = "skipEmptyLines";
    public static final String PROPERETY_CSV_COMMENT_CHARACHTER = "commentCharacter";
    public static final String PROPERETY_CSV_IGNORE_DIFFERENT_FIELD_COUNT = "ignoreDifferentFieldCount";

    public static final String PROPERETY_JDBC_BATCH = "batchSize";

}
