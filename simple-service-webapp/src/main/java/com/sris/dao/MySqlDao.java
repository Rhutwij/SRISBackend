package com.sris.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDriver;
import org.apache.commons.dbcp2.Utils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/*
 * @author  Rhutwij tulankar
 * @version 1.0
 * @since   2015-03-31
 * class that contains mysql connections setup and dao
 */
public class MySqlDao {

	private static Connection MysqlDataSource = null;
	private static String username = "root";
	private static String password = "rhutwij";
	private static String dbname = "sris_system";
	public static ConnectionFactory connectionFactory = null;
	public static PoolableConnectionFactory poolfactory = null;
	public static ObjectPool connectionPool = null;
	public static PoolingDriver dbcpDriver = null;
	public static GenericObjectPoolConfig config = null;

	private static String getUsername() {
		return username;
	}

	private static String getPassword() {
		return password;
	}

	private static String getDbname() {
		return dbname;
	}

	/**
	 * Check if jdbc driver exists
	 * 
	 * @return boolean true or false
	 */
	private static boolean init() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			return true;
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Get Mysql DataSource object
	 * 
	 * @return MsqlDataSource
	 */
	public static Connection getConnection() {
		if (init()) {
			if (MysqlDataSource != null) {
				System.out.println("return existing conn needed");
				return MysqlDataSource;
			}
			try {
				MysqlDataSource = DriverManager.getConnection(
						"jdbc:mysql://localhost/" + getDbname(), getUsername(),
						getPassword());
				System.out.println("return conn");

			} catch (SQLException e) {
				System.out.println("Connection Failed! Check output console");
				e.printStackTrace();

			}
			return MysqlDataSource;
		} else {
			return MysqlDataSource;
		}

	}

	/**
	 * Initialize ConnectionPool
	 * 
	 * @return boolean true or false if connection pool established
	 */
	private static boolean initConnectionPool() {
		Connection connJCG = null;
		try {
			if (null == connectionFactory) {
				// 1. Register the Driver to the jbdc.driver java property
				PoolConnectionFactory
						.registerJDBCDriver(PoolConnectionFactory.MYSQL_DRIVER);

				// 2. Create the Connection Factory
				// (DriverManagerConnectionFactory)
				connectionFactory = PoolConnectionFactory.getConnFactory(
						"jdbc:mysql://localhost/" + getDbname(), getUsername(),
						getPassword());

				// 3. Instantiate the Factory of Pooled Objects
				poolfactory = new PoolableConnectionFactory(connectionFactory,
						null);

				// 4. Create the Pool with the PoolableConnection objects
				config = new GenericObjectPoolConfig();
				config.setMaxIdle(4);
				config.setMaxTotal(70);
				config.setMaxWaitMillis(100);
				config.setBlockWhenExhausted(false);
				config.setMaxWaitMillis(300);
				config.setMinIdle(1);

				connectionPool = new GenericObjectPool(poolfactory, config);

				// 5. Set the objectPool to enforces the association (prevent
				// bugs)
				poolfactory.setPool(connectionPool);

				// 6. Get the Driver of the pool and register them
				dbcpDriver = PoolConnectionFactory.getDBCPDriver();
				dbcpDriver.registerPool("dbcp-com-sris-system", connectionPool);
			}

			return true;

		} catch (Exception e) {
			System.out.println("ERROR" + e.getStackTrace());
			return false;
		}
	}

	/**
	 * Get connection pool object DriverManagerConnectionFactory
	 * 
	 * @return Connection
	 */
	protected Connection getConnectionWithPooling() {
		Connection connJCG = null;
		if (initConnectionPool()) {
			try {
				connJCG = DriverManager
						.getConnection("jdbc:apache:commons:dbcp:dbcp-com-sris-system");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return connJCG;
		} else {
			return connJCG;
		}
	}
}
