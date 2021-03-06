package com.dci.intellij.dbn.database.sqlite.adapter.rs;

import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteMetadataResultSetRow;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteConstraintInfoResultSetStub.SqliteConstraintsLoader.*;

/**
 * DATASET_NAME,
 * CONSTRAINT_NAME,
 * CONSTRAINT_TYPE,
 * FK_CONSTRAINT_OWNER
 * FK_CONSTRAINT_NAME
 * IS_ENABLED
 * CHECK_CONDITION
 */

public abstract class SqliteConstraintsResultSet extends SqliteConstraintInfoResultSetStub<SqliteConstraintsResultSet.Constraint> {


    public SqliteConstraintsResultSet(String ownerName, SqliteDatasetNamesResultSet datasetNames, DBNConnection connection) throws SQLException {
        super(ownerName, datasetNames, connection);
    }

    public SqliteConstraintsResultSet(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        super(ownerName, datasetName, connection);
    }

    @Override
    protected void init(String ownerName, String datasetName) throws SQLException {
        Map<String, List<ConstraintColumnInfo>> constraints = loadConstraintInfo(ownerName, datasetName);

        for (String indexKey : constraints.keySet()) {
            if (indexKey.startsWith("FK")) {
                List<ConstraintColumnInfo> constraintColumnInfos = constraints.get(indexKey);
                String constraintName = getConstraintName(ConstraintType.FK, constraintColumnInfos);
                Constraint constraint = new Constraint();
                constraint.constraintName = constraintName;
                constraint.datasetName = datasetName;
                constraint.constraintType = "FOREIGN KEY";
                constraint.fkConstraintOwner = ownerName;
                constraint.fkConstraintName = constraintName.replace("fk_", "pk_");
                add(constraint);
            } else if (indexKey.startsWith("PK")) {
                List<ConstraintColumnInfo> constraintColumnInfos = constraints.get(indexKey);
                String constraintName = getConstraintName(ConstraintType.PK, constraintColumnInfos);
                Constraint constraint = new Constraint();
                constraint.constraintName = constraintName;
                constraint.datasetName = datasetName;
                constraint.constraintType = "PRIMARY KEY";
                add(constraint);
            } else if (indexKey.startsWith("UQ")) {
                List<ConstraintColumnInfo> constraintColumnInfos = constraints.get(indexKey);
                String constraintName = getConstraintName(ConstraintType.UQ, constraintColumnInfos);
                Constraint constraint = new Constraint();
                constraint.constraintName = constraintName;
                constraint.datasetName = datasetName;
                constraint.constraintType = "UNIQUE";
                add(constraint);
            }
        }
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        Constraint element = current();
        return
            columnLabel.equals("DATASET_NAME") ? element.datasetName :
            columnLabel.equals("CONSTRAINT_NAME") ? element.constraintName :
            columnLabel.equals("CONSTRAINT_TYPE") ? element.constraintType :
            columnLabel.equals("CHECK_CONDITION") ? element.checkCondition :
            columnLabel.equals("FK_CONSTRAINT_OWNER") ? element.fkConstraintOwner :
            columnLabel.equals("FK_CONSTRAINT_NAME") ? element.fkConstraintName :
            columnLabel.equals("IS_ENABLED") ? "Y" : null;
    }

    static class Constraint implements SqliteMetadataResultSetRow<Constraint> {
        private String datasetName;
        private String constraintName;
        private String constraintType;
        private String checkCondition;
        private String fkConstraintOwner;
        private String fkConstraintName;

        @Override
        public String identifier() {
            return datasetName + "." + constraintName;

        }

        @Override
        public String toString() {
            return "[CONSTRAINT] \"" + datasetName + "\".\"" + constraintName + "\"";
        }
    }
}
