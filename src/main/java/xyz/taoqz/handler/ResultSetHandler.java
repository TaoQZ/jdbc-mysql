package xyz.taoqz.handler;

import java.sql.ResultSet;
import java.util.List;

public interface ResultSetHandler<T> {

    List<T> handler(ResultSet resultSet) throws Exception;

}
