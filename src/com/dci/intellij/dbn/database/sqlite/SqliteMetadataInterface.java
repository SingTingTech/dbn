package com.dci.intellij.dbn.database.sqlite;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.common.DatabaseMetadataInterfaceImpl;
import com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteColumnConstraintsResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteColumnIndexesResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteColumnsResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteConstraintsResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteDatasetNamesResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteIndexesResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteSchemasResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteTriggerSourceResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteTriggersResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteViewSourceResultSet;


public class SqliteMetadataInterface extends DatabaseMetadataInterfaceImpl {

    public SqliteMetadataInterface(DatabaseInterfaceProvider provider) {
        super("sqlite_metadata_interface.xml", provider);
    }

    public ResultSet loadCompileObjectErrors(String ownerName, String objectName, Connection connection) throws SQLException {
        return null;
    }

    @Override
    public ResultSet loadSchemas(Connection connection) throws SQLException {
        return new SqliteSchemasResultSet(executeQuery(connection, "schemas"));
    }

    @Override
    public ResultSet loadColumns(final String ownerName, String datasetName, final Connection connection) throws SQLException {
        return new SqliteColumnsResultSet(ownerName, datasetName, connection) {
            @Override
            protected ResultSet loadTableInfo(String datasetName) throws SQLException {
                return SqliteMetadataInterface.this.loadTableInfo(ownerName, datasetName, connection);
            }

            @Override
            protected ResultSet loadForeignKeyInfo(String datasetName) throws SQLException {
                return SqliteMetadataInterface.this.loadForeignKeyInfo(ownerName, datasetName, connection);
            }
        };
    }

    @Override
    public ResultSet loadAllColumns(final String ownerName, final Connection connection) throws SQLException {
        return new SqliteColumnsResultSet(ownerName, getDatasetNames(ownerName, connection), connection) {
            @Override
            protected ResultSet loadTableInfo(String datasetName) throws SQLException {
                return SqliteMetadataInterface.this.loadTableInfo(ownerName, datasetName, connection);
            }

            @Override
            protected ResultSet loadForeignKeyInfo(String datasetName) throws SQLException {
                return SqliteMetadataInterface.this.loadForeignKeyInfo(ownerName, datasetName, connection);
            }
        };
    }

    @Override
    public ResultSet loadIndexes(final String ownerName, String tableName, final Connection connection) throws SQLException {
        return new SqliteIndexesResultSet(ownerName, tableName, connection) {
            @Override
            protected ResultSet loadIndexInfo(String tableName) throws SQLException {
                return SqliteMetadataInterface.this.loadIndexInfo(ownerName, tableName, connection);
            }
        };
    }

    @Override
    public ResultSet loadAllIndexes(final String ownerName, final Connection connection) throws SQLException {
        return new SqliteIndexesResultSet(ownerName, getDatasetNames(ownerName, connection), connection) {
            @Override
            protected ResultSet loadIndexInfo(String tableName) throws SQLException {
                return SqliteMetadataInterface.this.loadIndexInfo(ownerName, tableName, connection);
            }
        };
    }

    public ResultSet loadDatasetTriggers(String ownerName, String datasetName, Connection connection) throws SQLException {
        ResultSet resultSet = executeQuery(connection, "dataset-triggers", ownerName, datasetName);
        return new SqliteTriggersResultSet(resultSet);
    }

    public ResultSet loadAllDatasetTriggers(String ownerName, Connection connection) throws SQLException {
        ResultSet resultSet = executeQuery(connection, "all-dataset-triggers", ownerName);
        return new SqliteTriggersResultSet(resultSet);
    }

    @Override
    public ResultSet loadIndexRelations(final String ownerName, String tableName, final Connection connection) throws SQLException {
        return new SqliteColumnIndexesResultSet(ownerName, tableName, connection) {
            @Override
            protected ResultSet loadIndexInfo(String tableName) throws SQLException {
                return SqliteMetadataInterface.this.loadIndexInfo(ownerName, tableName, connection);
            }

            @Override
            protected ResultSet loadIndexDetailInfo(String indexName) throws SQLException {
                return SqliteMetadataInterface.this.loadIndexDetailInfo(ownerName, indexName, connection);
            }
        };
    }

    @Override
    public ResultSet loadAllIndexRelations(final String ownerName, final Connection connection) throws SQLException {
        return new SqliteColumnIndexesResultSet(ownerName, getDatasetNames(ownerName, connection), connection) {
            @Override
            protected ResultSet loadIndexInfo(String tableName) throws SQLException {
                return SqliteMetadataInterface.this.loadIndexInfo(ownerName, tableName, connection);
            }

            @Override
            protected ResultSet loadIndexDetailInfo(String indexName) throws SQLException {
                return SqliteMetadataInterface.this.loadIndexDetailInfo(ownerName, indexName, connection);
            }
        };
    }

    @Override
    public ResultSet loadConstraints(String ownerName, String datasetName, Connection connection) throws SQLException {
        return new ConstraintsResultSet(ownerName, datasetName, connection);
    }

    @Override
    public ResultSet loadAllConstraints(String ownerName, Connection connection) throws SQLException {
        return new ConstraintsResultSet(ownerName, getDatasetNames(ownerName, connection), connection);
    }

    @Override
    public ResultSet loadConstraintRelations(String ownerName, String datasetName, Connection connection) throws SQLException {
        return new ColumnConstraintsResultSet(ownerName, datasetName, connection);
    }

    @Override
    public ResultSet loadAllConstraintRelations(String ownerName, Connection connection) throws SQLException {
        return new ColumnConstraintsResultSet(ownerName, getDatasetNames(ownerName, connection), connection);
    }

    private class ConstraintsResultSet extends SqliteConstraintsResultSet {

        public ConstraintsResultSet(String ownerName, SqliteDatasetNamesResultSet datasetNames, Connection connection) throws SQLException {
            super(ownerName, datasetNames, connection);
        }

        public ConstraintsResultSet(String ownerName, String datasetName, Connection connection) throws SQLException {
            super(ownerName, datasetName, connection);
        }

        @Override
        protected ResultSet loadTableInfo(String ownerName, String datasetName) throws SQLException {
            return SqliteMetadataInterface.this.loadTableInfo(ownerName, datasetName, getConnection());
        }

        @Override
        protected ResultSet loadForeignKeyInfo(String ownerName, String datasetName) throws SQLException {
            return SqliteMetadataInterface.this.loadForeignKeyInfo(ownerName, datasetName, getConnection());
        }

        @Override
        protected ResultSet loadIndexInfo(String ownerName, String tableName) throws SQLException {
            return SqliteMetadataInterface.this.loadIndexInfo(ownerName, tableName, getConnection());
        }

        @Override
        protected ResultSet loadIndexDetailInfo(String ownerName, String indexName) throws SQLException {
            return SqliteMetadataInterface.this.loadIndexDetailInfo(ownerName, indexName, getConnection());
        }
    }

    private class ColumnConstraintsResultSet extends SqliteColumnConstraintsResultSet {

        public ColumnConstraintsResultSet(String ownerName, SqliteDatasetNamesResultSet datasetNames, Connection connection) throws SQLException {
            super(ownerName, datasetNames, connection);
        }

        public ColumnConstraintsResultSet(String ownerName, String datasetName, Connection connection) throws SQLException {
            super(ownerName, datasetName, connection);
        }

        @Override
        protected ResultSet loadTableInfo(String ownerName, String datasetName) throws SQLException {
            return SqliteMetadataInterface.this.loadTableInfo(ownerName, datasetName, getConnection());
        }

        @Override
        protected ResultSet loadForeignKeyInfo(String ownerName, String datasetName) throws SQLException {
            return SqliteMetadataInterface.this.loadForeignKeyInfo(ownerName, datasetName, getConnection());
        }

        @Override
        protected ResultSet loadIndexInfo(String ownerName, String tableName) throws SQLException {
            return SqliteMetadataInterface.this.loadIndexInfo(ownerName, tableName, getConnection());
        }

        @Override
        protected ResultSet loadIndexDetailInfo(String ownerName, String indexName) throws SQLException {
            return SqliteMetadataInterface.this.loadIndexDetailInfo(ownerName, indexName, getConnection());
        }
    }

    private ResultSet loadTableInfo(String ownerName, String datasetName, Connection connection) throws SQLException {
        return executeQuery(connection, "dataset-columns", ownerName, datasetName);
    }

    private ResultSet loadForeignKeyInfo(String ownerName, String datasetName, Connection connection) throws SQLException {
        return executeQuery(connection, "foreign-key-constraints", ownerName, datasetName);
    }

    private ResultSet loadIndexInfo(String ownerName, String tableName, Connection connection) throws SQLException {
        return executeQuery(connection, "indexes", ownerName, tableName);
    }

    private ResultSet loadIndexDetailInfo(String ownerName, String indexName, Connection connection) throws SQLException {
        return executeQuery(connection, "index-info", ownerName, indexName);
    }

    @NotNull
    SqliteDatasetNamesResultSet getDatasetNames(final String ownerName, final Connection connection) throws SQLException {
        return new SqliteDatasetNamesResultSet(ownerName) {
            @Override
            protected ResultSet loadTableNames() throws SQLException {
                return executeQuery(connection, "dataset-names", ownerName);
            }
        };
    }

    @Override
    public ResultSet loadViewSourceCode(String ownerName, String viewName, Connection connection) throws SQLException {
        ResultSet resultSet = executeQuery(connection, "view-source-code", ownerName, viewName);
        return new SqliteViewSourceResultSet(resultSet);
    }

    public ResultSet loadDatasetTriggerSourceCode(String tableOwner, String tableName, String ownerName, String triggerName, Connection connection) throws SQLException {
        ResultSet resultSet = executeQuery(connection, "dataset-trigger-source-code", ownerName, tableName, triggerName);
        return new SqliteTriggerSourceResultSet(resultSet);
    }

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public String createDateString(Date date) {
        String dateString = DATE_FORMAT.format(date);
        return "str_to_date('" + dateString + "', '%Y-%m-%d %T')";
    }

    @Override
    public void killSession(Object sessionId, Object serialNumber, boolean immediate, Connection connection) throws SQLException {
        executeStatement(connection, "kill-session", sessionId);
    }
}