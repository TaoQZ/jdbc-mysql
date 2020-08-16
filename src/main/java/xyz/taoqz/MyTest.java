package xyz.taoqz;

import org.junit.Test;
import xyz.taoqz.domain.School;
import xyz.taoqz.handler.BeanHandler;
import xyz.taoqz.util.JDBCUtil;

import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author :almostTao
 * @date :Created in 2020/8/16 14:12
 */
public class MyTest {

    public static void main(String[] args) throws Exception {

        Connection connection = JDBCUtil.getConnection();
        Statement statement = connection.createStatement();
        String sql = "select * from jdbc_stu.school";
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            // 获取指定名称列的结果
            System.out.println(resultSet.getString("name"));
        }

        // 结果不明确,不推荐使用
        // 查询使用executeQuery
        boolean execute = statement.execute(sql);
        System.out.println(execute);

        // 增删改使用
        String updateSql = "insert into school values (null,'哈工大')";
        statement.executeUpdate(updateSql);

        // 批处理
        String batchSql = "delete from school where id = 1;";
        String batchSql2 = "delete from school where id = 2;";
        statement.addBatch(batchSql);
        statement.addBatch(batchSql2);
        // 需要注意的是,不能执行select语句
        // Can not issue SELECT via executeUpdate() or executeLargeUpdate()
        int[] ints = statement.executeBatch();
        // 返回执行结果 0 失败或为修改 1 成功
        System.out.println(Arrays.toString(ints));
    }

    @Test
    public void demo() throws SQLException, IOException {
        Connection connection = JDBCUtil.getConnection();
        // ? : 占位符
        String sql = "select * from school where id = ?";
        // 创建预编译对象,有效的防止 sql注入问题
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, 2);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            System.out.println(resultSet.getString("id"));
            System.out.println(resultSet.getString("name"));
            // 列定义的索引,从1开始
            System.out.println(resultSet.getString(2));
        }
    }


    @Test
    public void insert() throws SQLException {
        Connection connection = JDBCUtil.getConnection();
        // 设置自动提交为false
        connection.setAutoCommit(false);

        try {
            String sql = "insert into jdbc_stu.school values (null,?)";
            // 获取由数据库自增的id
            PreparedStatement preparedStatement = connection.prepareStatement(sql, com.mysql.jdbc.PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, "北大");
            int i = preparedStatement.executeUpdate();
            System.out.println(i);

            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            resultSet.next();
            System.out.println(resultSet.getInt(1));
//            System.out.println(2 / 0);
            // 手动提交
            connection.commit();
        } catch (SQLException e) {
            // 当我们尝试回滚时,报错
            // com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException: Can't call rollback when autocommit=true
            connection.rollback();
            e.printStackTrace();
        }
    }

    @Test
    public void demoZhuRu() throws SQLException {

        Connection connection = JDBCUtil.getConnection();
        Statement statement = connection.createStatement();
        String param = "''or 1=1";
        String sql = "select * from jdbc_stu.school where name = " + param;
        // 相当于执行 select * from jdbc_stu.school where name = ''or 1=1
        // 直接将其拼接到了sql后
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            System.out.println(resultSet.getString("name"));
        }

        System.out.println("====================================================");

        sql = "select * from jdbc_stu.school where name = ? ";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
//        在底层会使用 instanceof 关键字比较类型,从而调用对应的可以指定类型的方法
//        preparedStatement.setObject(1,param);
        preparedStatement.setString(1, param);
        // 相当于执行 select * from jdbc_stu.school where name = '''or 1=1';
        // 不会把参数中的 和sql语句语法有关的当成sql的一部分执行,有效防止了sql注入
        resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            System.out.println(resultSet.getString("name"));
        }
    }

    @Test
    public void demoResultSet() throws SQLException {
        Connection connection = JDBCUtil.getConnection();
        Statement statement = connection.createStatement();
        String sql = "select * from jdbc_stu.school";
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            // 获取指定名称列的结果
            System.out.println(resultSet.getInt("id"));
            System.out.println(resultSet.getString("name"));
        }

    }

    @Test
    public void callProcedure() throws SQLException {
        Connection connection = JDBCUtil.getConnection();
        String callSql = "{call name_like(?,?)}";
        CallableStatement callableStatement = connection.prepareCall(callSql);
        // 设置参数
        callableStatement.setString(1, "%清%");
        callableStatement.registerOutParameter(2, Types.INTEGER);
        callableStatement.execute();
        // 获取返回值
        String result = callableStatement.getString(2);
        System.out.println(result);
    }

    @Test
    public void beanHandlerTest() throws Exception {
        Connection connection = JDBCUtil.getConnection();
        Statement statement = connection.createStatement();
        String sql = "select * from jdbc_stu.school";
        ResultSet resultSet = statement.executeQuery(sql);

        BeanHandler<School> beanHandler = new BeanHandler<>(School.class);
        List<School> handler = beanHandler.handler(resultSet);
        System.out.println(handler);

    }

    @Test
    public void savepointDemo() throws Exception {
        Connection connection = JDBCUtil.getConnection();

        // 开启事务
        connection.setAutoCommit(false);

        String sql = "insert into school values(null,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1,"天津大学");
        // 提交后,插入数据库
        preparedStatement.execute();
        // 设置保存点
        Savepoint savepoint = connection.setSavepoint();

        // 不会插入
        preparedStatement.setString(1,"清华大学");
        preparedStatement.execute();
        // 回滚至指定的保存点
        connection.rollback(savepoint);
        // 关闭事务,并提交
        connection.setAutoCommit(true);

    }

    @Test
    public void demo1() throws SQLException {
        Connection connection = JDBCUtil.getConnection();
        String data = "/* mysql-connector-java-5.1.47 ( Revision: fe1903b1ecb4a96a917f7ed3190d80c049b1de29 ) */SELECT  @@session.auto_increment_increment AS auto_increment_increment, @@character_set_client AS character_set_client, @@character_set_connection AS character_set_connection, @@character_set_results AS character_set_results, @@character_set_server AS character_set_server, @@collation_server AS collation_server, @@collation_connection AS collation_connection, @@init_connect AS init_connect, @@interactive_timeout AS interactive_timeout, @@license AS license, @@lower_case_table_names AS lower_case_table_names, @@max_allowed_packet AS max_allowed_packet, @@net_buffer_length AS net_buffer_length, @@net_write_timeout AS net_write_timeout, @@query_cache_size AS query_cache_size, @@query_cache_type AS query_cache_type, @@sql_mode AS sql_mode, @@system_time_zone AS system_time_zone, @@time_zone AS time_zone, @@transaction_isolation AS transaction_isolation, @@wait_timeout AS wait_timeout";

    }

}
