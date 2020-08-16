package xyz.taoqz.handler;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

/**
 * @author :almostTao
 * @date :Created in 2020/8/16 17:54
 */
public class BeanHandler<T> implements ResultSetHandler {

    private Class clazz;
    private List<T> list;
    public BeanHandler(Class clazz) {
        this.clazz = clazz;
        list = new ArrayList<>();
    }

    @Override
    public List<T> handler(ResultSet resultSet) throws Exception {
        Object bean;
        while (resultSet.next()){
            bean = clazz.newInstance();
            //拿到结果集元数据
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
                //获取到每列的列名
                String columnName = resultSetMetaData.getColumnName(i + 1);
                //获取到每列的数据
                Object columnData = resultSet.getObject(i + 1);
                //设置Bean属性
                Field field = clazz.getDeclaredField(columnName);

                field.setAccessible(true);
                field.set(bean,columnData);
            }
            list.add((T) bean);
        }
        return list;
    }
}
