package com.alibaba.otter.node.etl.common.datasource;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.common.model.config.data.DataMediaType;
import com.alibaba.otter.shared.common.utils.meta.DdlUtils;

/**
 * 类TestAbstractDbDialect.java的实现描述：TODO 类实现描述
 * 
 * @author xiaoqing.zhouxq 2011-12-9 下午3:03:55
 */
public class AbstractDbDialectTest {

    @Test
    public void testFindTable() throws Exception {
        DataSource dataSource = createDataSource("jdbc:oracle:thin:@10.20.144.25:1521:OINTEST", "otter1", "jonathan",
                                                 "oracle.jdbc.OracleDriver", DataMediaType.ORACLE, "utf-8");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        Table table = DdlUtils.findTable(jdbcTemplate, "otter1".toUpperCase(), "otter1".toUpperCase(),
                                         "wytable3".toUpperCase());
        System.out.println("the tablename = " + table.getSchema() + "." + table.getName());
        Column[] columns = table.getColumns();
        for (Column column : columns) {
            System.out.println("columnName = " + column.getName() + ",columnType = " + column.getTypeCode()
                               + ",isPrimary = " + column.isPrimaryKey() + ",nullable = " + column.isRequired());
        }

    }

    private DataSource createDataSource(String url, String userName, String password, String driverClassName,
                                        DataMediaType dataMediaType, String encoding) {
        BasicDataSource dbcpDs = new BasicDataSource();

        dbcpDs.setRemoveAbandoned(true);
        dbcpDs.setLogAbandoned(true);
        dbcpDs.setTestOnBorrow(true);
        dbcpDs.setTestWhileIdle(true);

        // 动态的参数
        dbcpDs.setDriverClassName(driverClassName);
        dbcpDs.setUrl(url);
        dbcpDs.setUsername(userName);
        dbcpDs.setPassword(password);

        if (dataMediaType.isOracle()) {
            dbcpDs.addConnectionProperty("restrictGetTables", "true");
            dbcpDs.setValidationQuery("select 1 from dual");
        } else if (dataMediaType.isMysql()) {
            // open the batch mode for mysql since 5.1.8
            dbcpDs.addConnectionProperty("useServerPrepStmts", "true");
            dbcpDs.addConnectionProperty("rewriteBatchedStatements", "true");
            if (StringUtils.isNotEmpty(encoding)) {
                dbcpDs.addConnectionProperty("characterEncoding", encoding);
            }
            dbcpDs.setValidationQuery("select 1");
        }

        return dbcpDs;
    }
}
