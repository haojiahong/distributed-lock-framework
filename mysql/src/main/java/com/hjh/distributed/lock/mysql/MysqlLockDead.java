package com.hjh.distributed.lock.mysql;

import java.sql.*;

/**
 * 这个有死锁问题，倒是可以研究死锁用来用。
 *
 * @author haojiahong created on 2019/12/11
 */
public class MysqlLockDead {
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/my_lock?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";


    private String resource; //需要锁定的资源
    private Connection connection;


    public MysqlLockDead(String resource) {
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
            connection.setAutoCommit(false);
            String querySql = "select * from lock_table where resource = ? for update";
            PreparedStatement queryStatement = connection.prepareStatement(querySql);
            queryStatement.setString(1, resource);
            ResultSet resultSet = queryStatement.executeQuery();
            resultSet.last();
            if (resultSet.getRow() == 0) {
                String insertSql = "insert into lock_table (resource,node) values(?,?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertSql);
                insertStatement.setString(1, resource);
                insertStatement.setString(2, Thread.currentThread().getName());
                insertStatement.execute();
                connection.commit();
                return true;
            } else {
                connection.commit();
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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
