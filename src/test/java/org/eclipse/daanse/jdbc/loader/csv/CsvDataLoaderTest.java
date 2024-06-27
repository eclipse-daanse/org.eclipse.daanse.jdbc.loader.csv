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
 *   SmartCity Jena, Stefan Bischof - initial
 *
 */
package org.eclipse.daanse.jdbc.loader.csv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.osgi.test.common.dictionary.Dictionaries.dictionaryOf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.eclipse.daanse.io.fs.watcher.api.FileSystemWatcherWhiteboardConstants;
import org.eclipse.daanse.jdbc.db.api.DatabaseService;
import org.eclipse.daanse.jdbc.db.api.meta.TableDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.TableReference;
import org.eclipse.daanse.jdbc.db.record.schema.SchemaReferenceR;
import org.eclipse.daanse.jdbc.db.record.schema.TableReferenceR;
import org.eclipse.daanse.jdbc.loader.csv.api.Constants;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import aQute.bnd.annotation.spi.ServiceProvider;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(MockitoExtension.class)
@RequireConfigurationAdmin
@ServiceProvider(value = DataSource.class)
class CscDataLoaderTest {
    @TempDir(cleanup = CleanupMode.ON_SUCCESS)
    Path path;

    @InjectBundleContext
    BundleContext bc;

    @InjectService
    ConfigurationAdmin ca;

    @InjectService
    DatabaseService databaseService;

    private Configuration conf;
    private Connection connection = null;
    private DatabaseMetaData metaData;

    @BeforeEach
    void beforeEach() throws SQLException, IOException {

        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:memFS:" + UUID.randomUUID().toString());
        connection = dataSource.getConnection();
        metaData = connection.getMetaData();
        bc.registerService(DataSource.class, dataSource, dictionaryOf("ds", "1"));
    }

    private Path copy(String file) throws IOException {

        Path target = path.resolve(file);
        InputStream is = bc.getBundle().getResource(file).openConnection().getInputStream();
        byte[] bytes = is.readAllBytes();
        Files.write(target, bytes);
        return target;

    }

    @AfterEach
    void afterEach() throws IOException {
        if (conf != null) {
            conf.delete();
        }
    }

    private void setupCsvDataLoadServiceImpl(String nullValue, Character quote, Character fieldSeparator,
            String encoding, String stringPath) throws IOException {
        conf = ca.getFactoryConfiguration(Constants.PID_LOADER_FILEWATCHER, "1", "?");
        Dictionary<String, Object> dict = new Hashtable<>();
        if (nullValue != null) {

            dict.put(Constants.PROPERETY_CSV_NULL_VALUE, nullValue);
        }
        if (quote != null) {
            dict.put(Constants.PROPERETY_CSV_QUOTE_CHARACHTER, quote);
        }
        if (fieldSeparator != null) {
            dict.put(Constants.PROPERETY_CSV_FIELD_SEPARATOR, fieldSeparator);
        }
        if (encoding != null) {
            dict.put(Constants.PROPERETY_CSV_ENCODING, encoding);
        }

        dict.put(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATH,
                stringPath != null ? path.resolve(stringPath).toAbsolutePath().toString()
                        : path.toAbsolutePath().toString());
        dict.put(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_RECURSIVE, true);
        dict.put(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_KINDS, new String[] { "ENTRY_CREATE" });
        conf.update(dict);
    }

    @Test
    void testinsertParamStatement() throws IOException, URISyntaxException, SQLException, InterruptedException {

        Path p = path.resolve("csv");
        Files.createDirectories(p);
        Thread.sleep(200);

        setupCsvDataLoadServiceImpl("NULL", '\"', ',', "UTF-8", "csv");

        Thread.sleep(500);
        copy("csv/test.csv");
        Thread.sleep(1000);

        TableReference table = new TableReferenceR("test");

        List<TableDefinition> tableDefinitions = databaseService.getTableDefinitions(metaData, table);
        assertThat(tableDefinitions).hasSize(1);

        List<ColumnDefinition> columnDefinitions = databaseService.getColumnDefinitions(metaData, table);
        assertThat(columnDefinitions).hasSize(10);
    }

    @Test
    void testSubDir() throws IOException, URISyntaxException, SQLException, InterruptedException {
        Path p = path.resolve("csv/schema1");
        Files.createDirectories(p);

        Thread.sleep(200);
        setupCsvDataLoadServiceImpl("NULL", '\"', ',', "UTF-8", "csv");

        Thread.sleep(500);
        copy("csv/schema1/test1.csv");
        Thread.sleep(1000);

        TableReference table = new TableReferenceR(Optional.of(new SchemaReferenceR("schema1")), "test1");
        List<TableDefinition> tableDefinitions = databaseService.getTableDefinitions(metaData, table);
        assertThat(tableDefinitions).hasSize(1);

        List<ColumnDefinition> columnDefinitions = databaseService.getColumnDefinitions(metaData, table);
        assertThat(columnDefinitions).hasSize(10);

    }

}
