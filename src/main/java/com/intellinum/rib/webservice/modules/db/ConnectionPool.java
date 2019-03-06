package com.intellinum.rib.webservice.modules.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Otniel on 5/15/2015.
 */
public class ConnectionPool {
    private Connection connection;
    private String dbUrl;
    private String dbDriver;
    private HikariDataSource ds;

    static final String HOST_NAME   = "localhost";
    static final String DB_NAME     = "rib";
    static final String USERNAME    = "root";
    static final String PASSWORD    = "";

    static final int PORT_NUMBER    = 8080;
    static final int MAX_POOL_SIZE  = 20;
    static final int MIN_IDLE       = 2;
    static final long MAX_LIFE_TIME = 28800;

    public enum DriverCollection{
        ORACLE,
        MYSQL,
        DB2,
        SYBASE
    }

    public ConnectionPool(){
        setDatabaseDriver(DriverCollection.MYSQL);

        HikariConfig config = new HikariConfig();

        config.setMaximumPoolSize(MAX_POOL_SIZE);
        config.setMinimumIdle(MIN_IDLE);
        config.setMaxLifetime(MAX_LIFE_TIME);
        config.setDriverClassName(this.getDbDriver());
        config.setJdbcUrl(this.getDbUrl());
        config.addDataSourceProperty("user", USERNAME);
        config.addDataSourceProperty("password", PASSWORD);
        config.setConnectionTestQuery("SELECT 1 FROM DUAL");

        ds = new HikariDataSource(config);
    }

    public static ConnectionPool getInstance(){
        return new ConnectionPool();
    }

    public void setDatabaseDriver(DriverCollection collection){
        switch (collection){
            case ORACLE:
                dbDriver    = "oracle.jdbc.driver.OracleDriver";
                dbUrl       = "jdbc:oracle:thin:@"+ HOST_NAME +":"+ PORT_NUMBER +":"+ DB_NAME;
                break;
            case MYSQL:
                dbDriver    = "com.mysql.jdbc.Driver";
                dbUrl       = "jdbc:mysql://"+ HOST_NAME +"/"+ DB_NAME;
                break;
            case DB2:
                dbDriver    = "COM.ibm.db2.jdbc.net.DB2Driver";
                dbUrl       = "jdbc:db2:"+ HOST_NAME +":"+ PORT_NUMBER +"/"+ DB_NAME;
                break;
            case SYBASE:
                dbDriver    = "com.sybase.jdbc.SybDriver";
                dbUrl       = "jdbc:sybase:Tds:"+ HOST_NAME +":"+ PORT_NUMBER +"/"+ DB_NAME;
                break;
        }
    }

    public String getDbUrl(){
        return dbUrl;
    }

    public String getDbDriver(){
        return dbDriver;
    }

    public void setDbUrl(String dbUrl){
        this.dbUrl = dbUrl;
    }

    public void setDbDriver(String dbDriver){
        this.dbDriver = dbDriver;
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}

