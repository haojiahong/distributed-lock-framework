package com.hjh.distributed.lock.mysql;


import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 这个有死锁问题，倒是可以研究死锁用来用。
 *
 * @author haojiahong created on 2019/12/11
 */
public class MysqlLock {
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/my_lock?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";


    private String resource; //需要锁定的资源
    private Connection connection;


    public MysqlLock(String resource) {
        this.resource = resource;
    }

    public void lock() {
        while (!tryLock()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(Thread.currentThread().getName() + "加锁。。。");
    }


    public boolean tryLock() {
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            String insertSql = "insert into lock_table (resource,node) values(?,?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertSql);
            insertStatement.setString(1, resource);
            insertStatement.setString(2, Thread.currentThread().getName());
            insertStatement.execute();
            return true;
        } catch (Exception e) {
            if (e instanceof MySQLIntegrityConstraintViolationException) {
                System.out.println(e.getMessage());
            }
            return false;
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void release() {
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            String sql = "delete from lock_table where resource = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, resource);
            preparedStatement.execute();
            System.out.println(Thread.currentThread().getName() + "解锁。。。");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
