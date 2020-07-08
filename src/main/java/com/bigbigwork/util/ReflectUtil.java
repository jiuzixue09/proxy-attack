package com.bigbigwork.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;


public class ReflectUtil {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface FieldControlAnno {

        boolean includeParentField();
    }

    public static Object getNewInstance(String clazz,String keyword) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        Class<?> c = Class.forName(clazz);
        Constructor<?> con = c.getConstructor(String.class);
        return con.newInstance(keyword);
    }

    public static Object executeStaticMethodWithoutParams(String clazz, String methodName) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class<?> threadClazz = Class.forName(clazz);
        Method method = threadClazz.getMethod(methodName, (Class<?>[]) null);


        Object object =  method.invoke(null, (Object[])null);
        return object;
    }

    public static Object getNewInstance(String clazz,String enumClass,String enumName,String keyword) throws ClassNotFoundException,NoSuchMethodException, InstantiationException,	IllegalAccessException, InvocationTargetException {
        Class<?> c;
        Constructor<?> con;
        Class<?> class1;

        class1 = Class.forName(clazz + "$" + enumClass);
        c = Class.forName(clazz);
        Object invokeTester = con = c.getConstructor(String.class,class1);
        //获取类的valueOf()方法
        Method getStrMethod = class1.getMethod("valueOf", String.class);
        //调用对象的valueOf()方法
        Object result = getStrMethod.invoke(invokeTester, enumName);
        return con.newInstance(keyword,result);

    }

    public static Object getNewInstance(String clazz) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<?> c = Class.forName(clazz);
        return c.newInstance();
    }



    /**
     * 将一条记录转成一个对象
     *
     * @param cls
     *            泛型类型
     * @param rs
     *            ResultSet对象
     * @return 泛型类型对象
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws SQLException
     */
    public static <T> T executeResultSet(Class<T> cls, ResultSet rs)
            throws InstantiationException, IllegalAccessException, SQLException {
        T obj = cls.newInstance();
        ResultSetMetaData rsm = rs.getMetaData();
        int columnCount = rsm.getColumnCount();
        // Field[] fields = cls.getFields();
        FieldControlAnno annotation = cls.getAnnotation(FieldControlAnno.class);

        boolean includeParentField = false;
        if(null != annotation) includeParentField = annotation.includeParentField();

        List<Field> fields = getFields(cls, includeParentField);

        for (Field field : fields) {
            String fieldName = field.getName();
            for (int j = 1; j <= columnCount; j++) {
                String columnName = rsm.getColumnLabel(j);
                if (fieldName.equalsIgnoreCase(columnName.replace("_", "")) || fieldName.equalsIgnoreCase(columnName)) {
                    Object value = rs.getObject(j);
                    field.setAccessible(true);
                    field.set(obj, value);
                    break;
                }
            }
        }
        return obj;
    }

    private static <T> List<Field> getFields(Class<T> cls, boolean includeParentField) {
        List<Field> fields = new ArrayList<>(Arrays.asList(cls.getDeclaredFields()));

        if(includeParentField && null != cls.getSuperclass() &&
                !cls.getSuperclass().toString().equals("class java.lang.Object")){
            fields.addAll(getFields(cls.getSuperclass(), includeParentField));
        }
        return fields;
    }


    /**
     *
     * @param t
     * @param name
     * @return
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws SQLException
     */
    public static <T> Map<String, Object> getMapByAttribute(T t, String name) throws IllegalArgumentException, IllegalAccessException {
        Field[] fields = t.getClass().getDeclaredFields();
        Map<String, Object> map = new HashMap<>();
        for (Field field : fields) {
            String fieldName = field.getName();

            if (fieldName.equalsIgnoreCase(name.replace("_", "")) || fieldName.equalsIgnoreCase(name)) {
                field.setAccessible(true);
                map.put("value", field.get(t));
                map.put("type", field.getType().getSimpleName());
                break;
            }

        }
        return map;
    }



    public static <T> T mapToObject(Class<T> cls, Map<String, Object> map) throws InstantiationException, IllegalAccessException {
        T obj = cls.newInstance();

        boolean includeParentField = false;
        FieldControlAnno annotation = cls.getAnnotation(FieldControlAnno.class);
        if(null != annotation) includeParentField = annotation.includeParentField();

        List<Field> fields = getFields(cls, includeParentField);

        for (Field field : fields) {
            String fieldName = field.getName();
            if (map.containsKey(fieldName)) {
                Object value = map.get(fieldName);
                field.setAccessible(true);
                field.set(obj, value);
            }
        }


        return obj;

    }

    public static Map<String, Object> objectToMap(Object obj, boolean isFilterNullValue) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        BeanInfo info = Introspector.getBeanInfo(obj.getClass());
        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            Method reader = pd.getReadMethod();
            if (reader != null) {
                if ((isFilterNullValue && null == reader.invoke(obj)) || pd.getName().equals("class")) continue;
                result.put(pd.getName(), reader.invoke(obj));
            }
        }
        return result;
    }



    /**
     * 利用反射实现对象之间属性复制
     * @param from
     * @param to
     */
    public static void copyProperties(Object from, Object to) throws Exception {
        copyPropertiesExclude(from, to, null);
    }

    /**
     * 复制对象属性
     * @param from
     * @param to
     * @param excludsArray 排除属性列表
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public static void copyPropertiesExclude(Object from, Object to, String[] excludsArray) throws Exception {
        List<String> excludesList = null;
        if (excludsArray != null && excludsArray.length > 0) {
            excludesList = Arrays.asList(excludsArray); //构造列表对象
        }
        Method[] fromMethods = from.getClass().getDeclaredMethods();
        Method[] toMethods = to.getClass().getDeclaredMethods();
        Method fromMethod = null, toMethod = null;
        String fromMethodName = null, toMethodName = null;
        for (int i = 0; i < fromMethods.length; i++) {
            fromMethod = fromMethods[i];
            fromMethodName = fromMethod.getName();
            if (!fromMethodName.contains("get"))
                continue;
            //排除列表检测
            if (excludesList != null && excludesList.contains(fromMethodName.substring(3).toLowerCase())) {
                continue;
            }
            toMethodName = "set" + fromMethodName.substring(3);
            toMethod = findMethodByName(toMethods, toMethodName);
            if (toMethod == null)
                continue;
            Object value = fromMethod.invoke(from);
            if (value == null)
                continue;
            //集合类判空处理
            if (value instanceof Collection) {
                Collection newValue = (Collection) value;
                if (newValue.size() <= 0)
                    continue;
            }
            toMethod.invoke(to, value);
        }
    }

    /**
     * 对象属性值复制，仅复制指定名称的属性值
     * @param from
     * @param to
     * @param includsArray
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public static void copyPropertiesInclude(Object from, Object to, String[] includsArray) throws Exception {
        List<String> includesList = null;
        if (includsArray != null && includsArray.length > 0) {
            includesList = Arrays.asList(includsArray); //构造列表对象
        } else {
            return;
        }
        Method[] fromMethods = from.getClass().getDeclaredMethods();
        Method[] toMethods = to.getClass().getDeclaredMethods();
        Method fromMethod = null, toMethod = null;
        String fromMethodName = null, toMethodName = null;
        for (int i = 0; i < fromMethods.length; i++) {
            fromMethod = fromMethods[i];
            fromMethodName = fromMethod.getName();
            if (!fromMethodName.contains("get"))
                continue;
            //排除列表检测
            String str = fromMethodName.substring(3);
            if (!includesList.contains(str.substring(0, 1).toLowerCase() + str.substring(1))) {
                continue;
            }
            toMethodName = "set" + fromMethodName.substring(3);
            toMethod = findMethodByName(toMethods, toMethodName);
            if (toMethod == null)
                continue;
            Object value = fromMethod.invoke(from);
            if (value == null)
                continue;
            //集合类判空处理
            if (value instanceof Collection) {
                Collection newValue = (Collection) value;
                if (newValue.size() <= 0)
                    continue;
            }
            toMethod.invoke(to, value);
        }
    }



    /**
     * 从方法数组中获取指定名称的方法
     *
     * @param methods
     * @param name
     * @return
     */
    public static Method findMethodByName(Method[] methods, String name) {
        for (int j = 0; j < methods.length; j++) {
            if (methods[j].getName().equals(name))
                return methods[j];
        }
        return null;
    }

}