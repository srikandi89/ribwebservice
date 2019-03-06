package com.intellinum.rib.webservice.modules.db;

import com.intellinum.rib.webservice.modules.models.Component;
import com.intellinum.rib.webservice.modules.models.Container;
import com.intellinum.rib.webservice.modules.models.Page;
import com.intellinum.rib.webservice.modules.models.Property;
import com.intellinum.rib.webservice.modules.utils.BeanToUnderscore;
import com.intellinum.rib.webservice.modules.utils.FieldExistenceChecker;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Otniel on 5/15/2015.
 */
public class QueryHandler {
    protected String query;
    protected Object object;
    protected List<?> list;

    public static ConnectionPool pool = ConnectionPool.getInstance();
    public Statement statement;
    public Connection connection;
    public ResultSet resultSet;

    public QueryHandler(){}

    public QueryHandler(String query, Object object){
        this.object = object;
        this.query = query;
    }

    public QueryHandler(Object object){
        this.object = object;
    }

    protected static QueryHandler getHandler(String query, Object... params){
        return new QueryHandler(query, params[0]);
    }

    protected static QueryHandler getHandler(Object object){
        return new QueryHandler(object);
    }

    public enum ModelCollection {
        COMPONENT,
        CONTAINER,
        PAGE,
        PROPERTY
    }

    public boolean isDefaultDataType(Field f){
        String dataType = f.getType().getSimpleName();
        if (f.getType().isPrimitive() || dataType.equals("String")){
            return true;
        }
        else{
            return false;
        }
    }

    public Object getTableObject(String tableName){
        Object tableObject = new Object();
        switch (tableName){
            case "page":
                Page page   = new Page();
                tableObject = page;
                break;
            case "container":
                Container container = new Container();
                tableObject         = container;
                break;
            case "component":
                Component component = new Component();
                tableObject         = component;
                break;
            case "property":
                Property property   = new Property();
                tableObject         = property;
                break;
        }

        return tableObject;
    }

    public List<?> setGenericList(ModelCollection models){
        switch(models){
            case PAGE:
                list = new ArrayList<Page>();
                break;
            case CONTAINER:
                list = new ArrayList<Container>();
                break;
            case COMPONENT:
                list = new ArrayList<Component>();
                break;
            case PROPERTY:
                list = new ArrayList<Property>();
                break;
        }

        return list;
    }

    public int getLatestId(){
        int latestId = 0;

        try{
            connection  = pool.getConnection();
            statement   = connection.createStatement();
            resultSet   = statement.executeQuery("SELECT * FROM counter ORDER BY latest_id DESC LIMIT 1");

            while(resultSet.next()){
                latestId = resultSet.getInt(2);
            }

            connection.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return latestId;
    }

    public int save(){
        Class type                  = this.object.getClass();
        String tableName            = type.getSimpleName().toLowerCase();
        StringBuilder queryBuilder  = new StringBuilder();
        StringBuilder valueBuilder  = new StringBuilder();

        int status      = 0;
        int latestId    = 0;

        try{
            for(Field f : type.getDeclaredFields()){
                if(isDefaultDataType(f)){
                    if(f.getType().getSimpleName().equals("int")){
                        f.setAccessible(true);
                        int value = f.getInt(this.object);
                        valueBuilder.append(value + ",");
                    }
                    if(f.getType().getSimpleName().equals("String")){
                        f.setAccessible(true);
                        String value = (String)f.get(this.object);
                        valueBuilder.append("'" + value + "',");
                    }
                }
            }

            String values = valueBuilder.deleteCharAt(valueBuilder.length()-1).toString();

            queryBuilder.append("INSERT INTO "+tableName+" VALUES ("+values+")");

            String insertQuery  = queryBuilder.toString();
            String counterQuery = "";

            try{
                connection  = pool.getConnection();
                statement   = connection.createStatement();

                statement.addBatch(insertQuery);

                Field idAttribute = type.getDeclaredFields()[0];

                if(idAttribute.getType().getSimpleName().equals("int") && !type.getSimpleName().equals("Property")){
                    latestId        = idAttribute.getInt(this.object);
                    counterQuery    = "INSERT INTO counter (latest_id) VALUES("+latestId+")";

                    statement.addBatch(counterQuery);
                }

                statement.executeBatch();

                connection.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }

        }
        catch(Exception e){
            e.printStackTrace();
        }

        return status;
    }

    public <T> List<T> fetch(){
        Class type                  = this.object.getClass();
        String tableName            = type.getSimpleName().toLowerCase();
        List<T> list                = new ArrayList<T>();
        BeanToUnderscore convert    = new BeanToUnderscore();

        try{
            Field tableNameField    = type.getDeclaredFields()[0];

            convert.setBeanVariable(tableNameField.getName());

            String tableNameId              = convert.getUnderScoredVariable();
            List<Integer> listId            = new ArrayList<Integer>();
            List<Property> propertyList     = new ArrayList<>();
            List<List<Property>> listList   = new ArrayList<>();

            for(Field f : type.getDeclaredFields()){

                if(!isDefaultDataType(f)){
                    if(!f.getType().getSimpleName().equals("List") && !f.getType().getSimpleName().equals("ArrayList")){
                        StringBuilder queryBuilder  = new StringBuilder();
                        String relatedTable         = f.getType().getSimpleName().toLowerCase();

                        Object tableMemberObj       = getTableObject(relatedTable);
                        Field fieldTableMemberObj   = tableMemberObj.getClass().getDeclaredFields()[0];
                        Field[] fieldsTableMember   = tableMemberObj.getClass().getDeclaredFields();

                        StringBuilder selectedField = new StringBuilder();

                        for(int i=0; i<fieldsTableMember.length; i++){
                            if(isDefaultDataType(fieldsTableMember[i])) {
                                selectedField.append(relatedTable+"."+convert.getUnderScoredVariable(fieldsTableMember[i].getName()+","));
                            }
                        }

                        selectedField.deleteCharAt(selectedField.length()-1);

                        if(!relatedTable.equals("property")){
                            convert.setBeanVariable(fieldTableMemberObj.getName());

                            String tableMemberId = convert.getUnderScoredVariable();

                            queryBuilder.append("SELECT "+selectedField.toString()+" FROM "+relatedTable+" INNER JOIN "+tableName+" ON "+relatedTable+"."+tableMemberId+" = "+tableName+"."+tableMemberId+" GROUP BY "+tableMemberId);
                        }
                        else{
                            convert.setBeanVariable(fieldTableMemberObj.getName());

                            String tableMemberId = convert.getUnderScoredVariable();

                            queryBuilder.append("SELECT "+selectedField.toString()+" FROM "+relatedTable+" INNER JOIN "+tableName + " ON " + relatedTable+"."+tableMemberId+" = "+tableName+"."+tableNameId+" GROUP BY "+tableMemberId);
                        }

                        connection  = pool.getConnection();
                        statement   = connection.createStatement();
                        resultSet   = statement.executeQuery(queryBuilder.toString());

                        ResultSetMetaData rsmd  = resultSet.getMetaData();
                        int columnSize          = rsmd.getColumnCount();

                        while(resultSet.next()){
                            listId.add(resultSet.getInt(1));

                            for(int j=0; j<columnSize; j++){
                                if(rsmd.getColumnTypeName(j+1).toLowerCase().equals("int")){
                                    fieldsTableMember[j].setInt(tableMemberObj, resultSet.getInt(j+1));
                                }
                                else if(rsmd.getColumnTypeName(j+1).toLowerCase().equals("varchar") || rsmd.getColumnTypeName(j+1).toLowerCase().equals("text")){
                                    fieldsTableMember[j].set(tableMemberObj, resultSet.getString(j+1));
                                }
                            }
                        }

                        connection.close();
                    }
                    else{
                        ParameterizedType getListGenericClass = (ParameterizedType) f.getGenericType();
                        if(getListGenericClass != null){
                            List<Integer> tempListId    = new ArrayList<Integer>(listId);
                            StringBuilder listQuery     = new StringBuilder();
                            StringBuilder parentQuery   = new StringBuilder();

                            Class<?> genericClass = (Class<?>) getListGenericClass.getActualTypeArguments()[0];

                            if(genericClass.getSimpleName().toLowerCase().equals("property")){
                                Object generic = getTableObject(genericClass.getSimpleName().toLowerCase());

                                Field[] fields = generic.getClass().getDeclaredFields();

                                connection  = pool.getConnection();
                                statement   = connection.createStatement();

                                parentQuery.append("SELECT * FROM " + tableName);

                                resultSet = statement.executeQuery(parentQuery.toString());

                                while(resultSet.next()){
                                    tempListId.add(resultSet.getInt(1));
                                }

                                Property property = new Property();

                                List<Property> tempList = new ArrayList<>();

                                for(int i=0; i<tempListId.size(); i++){
                                    listQuery = new StringBuilder();
                                    listQuery.append("SELECT * FROM " + genericClass.getSimpleName().toLowerCase() + " WHERE " + convert.getUnderScoredVariable(fields[0].getName()) + "=" + tempListId.get(i));
                                    resultSet   = statement.executeQuery(listQuery.toString());

                                    while ((resultSet.next())){
                                        property = new Property();
                                        property.propertyId = resultSet.getInt(convert.getUnderScoredVariable(fields[0].getName()));
                                        property.propertyName = resultSet.getString(convert.getUnderScoredVariable(fields[1].getName()));
                                        property.propertyValue = resultSet.getString(convert.getUnderScoredVariable(fields[2].getName()));
                                        tempList.add(property);
                                    }
                                    listList.add(tempList);

                                    tempList = new ArrayList<>();
                                }

                                connection.close();
                            }
                        }
                    }

                    listId = new ArrayList<Integer>();
                }
            }

            connection  = pool.getConnection();
            statement   = connection.createStatement();

            StringBuilder parentQueryBuilder = new StringBuilder();

            parentQueryBuilder.append("SELECT * FROM "+tableName);

            resultSet = statement.executeQuery(parentQueryBuilder.toString());

            ResultSetMetaData rsmd = resultSet.getMetaData();

            int columnSize = rsmd.getColumnCount();

            Object parentTable;

            FieldExistenceChecker checkField = new FieldExistenceChecker();

            if(checkField.isFieldExist(object, "property")){
                object.getClass().getDeclaredField("property").setAccessible(true);

                int counter = 0;

                while(resultSet.next()){
                    parentTable = getTableObject(tableName);

                    Field[] parentFields = parentTable.getClass().getDeclaredFields();

                    for(int i=0; i<columnSize; i++){
                        if(rsmd.getColumnTypeName(i+1).toLowerCase().equals("int")){
                            parentFields[i].setInt(parentTable, resultSet.getInt(i+1));
                        }
                        else if(rsmd.getColumnTypeName(i+1).toLowerCase().equals("varchar") || rsmd.getColumnTypeName(i+1).toLowerCase().equals("text")){
                            parentFields[i].set(parentTable, resultSet.getString(i+1));
                        }
                    }

                    propertyList = new ArrayList<>(listList.get(counter));

                    for(int i=columnSize; i<parentFields.length; i++){
                        if(parentFields[i].getType().getSimpleName().equals("List") || parentFields[i].getType().getSimpleName().equals("ArrayList")){
                            ParameterizedType getListGenericClass = (ParameterizedType) parentFields[i].getGenericType();

                            if(getListGenericClass != null){
                                Class<?> generic = (Class<?>) getListGenericClass.getActualTypeArguments()[0];
                                if(generic.getSimpleName().equals("Property")){
                                    parentFields[i].set(parentTable, propertyList);
                                }
                            }
                        }
                    }
                    counter++;

                    list.add((T) parentTable);
                }
            }
            else{
                while(resultSet.next()){
                    parentTable = getTableObject(tableName);

                    Field[] parentFields = parentTable.getClass().getDeclaredFields();

                    for(int i=0; i<columnSize; i++){

                        if(rsmd.getColumnTypeName(i+1).toLowerCase().equals("int")){
                            parentFields[i].setInt(parentTable, resultSet.getInt(i+1));
                        }
                        else if(rsmd.getColumnTypeName(i+1).toLowerCase().equals("varchar") || rsmd.getColumnTypeName(i+1).toLowerCase().equals("text")){
                            parentFields[i].set(parentTable, resultSet.getString(i+1));
                        }
                    }

                    list.add((T) parentTable);

                }
            }

            connection.close();

        } catch (NoSuchFieldException ex) {
            System.out.println("Field doesn't exist");
        } catch(Exception e){
            e.printStackTrace();
        }

        return list;
    }

    public <T> List<T> fetchQuery(){
        List<T> list = new ArrayList<T>();
        if(!query.isEmpty()){
            if(query.toLowerCase().contains("join")){
                System.out.println("No join query allowed");
            }
            else{
                Class type                  = this.object.getClass();
                String tableName            = type.getSimpleName().toLowerCase();
                BeanToUnderscore convert    = new BeanToUnderscore();

                try{
                    Field tableNameField    = type.getDeclaredFields()[0];

                    convert.setBeanVariable(tableNameField.getName());

                    String tableNameId              = convert.getUnderScoredVariable();
                    List<Integer> listId            = new ArrayList<Integer>();
                    List<Property> propertyList     = new ArrayList<>();
                    List<List<Property>> listList   = new ArrayList<>();

                    for(Field f : type.getDeclaredFields()){

                        if(!isDefaultDataType(f)){
                            if(!f.getType().getSimpleName().equals("List") && !f.getType().getSimpleName().equals("ArrayList")){
                                StringBuilder queryBuilder  = new StringBuilder();
                                String relatedTable         = f.getType().getSimpleName().toLowerCase();

                                Object tableMemberObj       = getTableObject(relatedTable);
                                Field fieldTableMemberObj   = tableMemberObj.getClass().getDeclaredFields()[0];
                                Field[] fieldsTableMember   = tableMemberObj.getClass().getDeclaredFields();

                                StringBuilder selectedField = new StringBuilder();

                                for(int i=0; i<fieldsTableMember.length; i++){
                                    if(isDefaultDataType(fieldsTableMember[i])) {
                                        selectedField.append(relatedTable+"."+convert.getUnderScoredVariable(fieldsTableMember[i].getName()+","));
                                    }
                                }

                                selectedField.deleteCharAt(selectedField.length()-1);

                                if(!relatedTable.equals("property")){
                                    convert.setBeanVariable(fieldTableMemberObj.getName());

                                    String tableMemberId = convert.getUnderScoredVariable();

                                    queryBuilder.append("SELECT "+selectedField.toString()+" FROM "+relatedTable+" INNER JOIN "+tableName+" ON "+relatedTable+"."+tableMemberId+" = "+tableName+"."+tableMemberId+" GROUP BY "+tableMemberId);
                                }
                                else{
                                    convert.setBeanVariable(fieldTableMemberObj.getName());

                                    String tableMemberId = convert.getUnderScoredVariable();

                                    queryBuilder.append("SELECT "+selectedField.toString()+" FROM "+relatedTable+" INNER JOIN "+tableName + " ON " + relatedTable+"."+tableMemberId+" = "+tableName+"."+tableNameId+" GROUP BY "+tableMemberId);
                                }

                                connection  = pool.getConnection();
                                statement   = connection.createStatement();
                                resultSet   = statement.executeQuery(queryBuilder.toString());

                                ResultSetMetaData rsmd  = resultSet.getMetaData();
                                int columnSize          = rsmd.getColumnCount();

                                while(resultSet.next()){
                                    listId.add(resultSet.getInt(1));

                                    for(int j=0; j<columnSize; j++){
                                        if(rsmd.getColumnTypeName(j+1).toLowerCase().equals("int")){
                                            fieldsTableMember[j].setInt(tableMemberObj, resultSet.getInt(j+1));
                                        }
                                        else if(rsmd.getColumnTypeName(j+1).toLowerCase().equals("varchar") || rsmd.getColumnTypeName(j+1).toLowerCase().equals("text")){
                                            fieldsTableMember[j].set(tableMemberObj, resultSet.getString(j+1));
                                        }
                                    }
                                }
                            }
                            else{
                                ParameterizedType getListGenericClass = (ParameterizedType) f.getGenericType();
                                if(getListGenericClass != null){
                                    List<Integer> tempListId    = new ArrayList<Integer>(listId);
                                    StringBuilder listQuery     = new StringBuilder();
                                    StringBuilder parentQuery   = new StringBuilder();

                                    Class<?> genericClass = (Class<?>) getListGenericClass.getActualTypeArguments()[0];

                                    if(genericClass.getSimpleName().toLowerCase().equals("property")){
                                        Object generic = getTableObject(genericClass.getSimpleName().toLowerCase());

                                        Field[] fields = generic.getClass().getDeclaredFields();

                                        connection  = pool.getConnection();
                                        statement   = connection.createStatement();

                                        parentQuery.append(query);

                                        resultSet = statement.executeQuery(parentQuery.toString());

                                        while(resultSet.next()){
                                            tempListId.add(resultSet.getInt(1));
                                        }

                                        Property property = new Property();

                                        List<Property> tempList = new ArrayList<>();

                                        for(int i=0; i<tempListId.size(); i++){
                                            listQuery = new StringBuilder();
                                            listQuery.append("SELECT * FROM " + genericClass.getSimpleName().toLowerCase() + " WHERE " + convert.getUnderScoredVariable(fields[0].getName()) + "=" + tempListId.get(i));

                                            resultSet   = statement.executeQuery(listQuery.toString());

                                            while ((resultSet.next())){
                                                property = new Property();
                                                property.propertyId = resultSet.getInt(convert.getUnderScoredVariable(fields[0].getName()));
                                                property.propertyName = resultSet.getString(convert.getUnderScoredVariable(fields[1].getName()));
                                                property.propertyValue = resultSet.getString(convert.getUnderScoredVariable(fields[2].getName()));
                                                tempList.add(property);
                                            }
                                            listList.add(tempList);

                                            tempList = new ArrayList<>();
                                        }
                                    }
                                }
                            }

                            listId = new ArrayList<Integer>();
                        }
                    }

                    connection  = pool.getConnection();
                    statement   = connection.createStatement();

                    StringBuilder parentQueryBuilder = new StringBuilder();

                    parentQueryBuilder.append(query);

                    resultSet = statement.executeQuery(parentQueryBuilder.toString());

                    ResultSetMetaData rsmd = resultSet.getMetaData();

                    int columnSize = rsmd.getColumnCount();

                    Object parentTable;

                    FieldExistenceChecker checkField = new FieldExistenceChecker();

                    if(checkField.isFieldExist(object, "property")){
                        object.getClass().getDeclaredField("property").setAccessible(true);

                        int counter = 0;

                        while(resultSet.next()){
                            parentTable = getTableObject(tableName);

                            Field[] parentFields = parentTable.getClass().getDeclaredFields();

                            for(int i=0; i<columnSize; i++){
                                if(rsmd.getColumnTypeName(i+1).toLowerCase().equals("int")){
                                    parentFields[i].setInt(parentTable, resultSet.getInt(i+1));
                                }
                                else if(rsmd.getColumnTypeName(i+1).toLowerCase().equals("varchar") || rsmd.getColumnTypeName(i+1).toLowerCase().equals("text")){
                                    parentFields[i].set(parentTable, resultSet.getString(i+1));
                                }
                            }
                            propertyList = new ArrayList<>(listList.get(counter));

                            for(int i=columnSize; i<parentFields.length; i++){
                                if(parentFields[i].getType().getSimpleName().equals("List") || parentFields[i].getType().getSimpleName().equals("ArrayList")){
                                    ParameterizedType getListGenericClass = (ParameterizedType) parentFields[i].getGenericType();

                                    if(getListGenericClass != null){
                                        Class<?> generic = (Class<?>) getListGenericClass.getActualTypeArguments()[0];
                                        if(generic.getSimpleName().equals("Property")){
                                            parentFields[i].set(parentTable, propertyList);
                                        }
                                    }
                                }
                            }

                            counter++;

                            list.add((T) parentTable);
                        }
                    }
                    else{
                        while(resultSet.next()){
                            parentTable = getTableObject(tableName);

                            Field[] parentFields = parentTable.getClass().getDeclaredFields();

                            for(int i=0; i<columnSize; i++){

                                if(rsmd.getColumnTypeName(i+1).toLowerCase().equals("int")){
                                    parentFields[i].setInt(parentTable, resultSet.getInt(i+1));
                                }
                                else if(rsmd.getColumnTypeName(i+1).toLowerCase().equals("varchar") || rsmd.getColumnTypeName(i+1).toLowerCase().equals("text")){
                                    parentFields[i].set(parentTable, resultSet.getString(i+1));
                                }
                            }

                            list.add((T) parentTable);

                        }
                    }

                } catch (NoSuchFieldException ex) {
                    System.out.println("Field doesn't exist");
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    public void removePageElements(String pageName){
        try {
            ArrayList<Integer> pageIdList = new ArrayList<Integer>();
            ArrayList<Integer> containerIdList = new ArrayList<Integer>();
            ArrayList<Integer> componentIdList = new ArrayList<Integer>();

            connection  = pool.getConnection();
            statement   = connection.createStatement();

            String query;

            //get all match page by page_name
            query = "SELECT * FROM page WHERE page.page_name = '"+pageName+"'";
            resultSet = statement.executeQuery(query);

            while(resultSet.next()){
                pageIdList.add(resultSet.getInt(1));
                System.out.println("Page Name("+resultSet.getInt(1)+") :"+resultSet.getString(2));
            }

            //get all match page properties
            System.out.println();
            System.out.println("-- Page Properties --");
            for(int i=0; i<pageIdList.size(); i++){
                query = "DELETE * FROM property WHERE property.property_id = " + pageIdList.get(i);

                statement.executeUpdate(query);
            }

            //get all match container which related to page
            System.out.println();
            for (int i=0; i<pageIdList.size(); i++){
                query = "SELECT * FROM container WHERE container.page_id = "+pageIdList.get(i);

                resultSet = statement.executeQuery(query);

                while(resultSet.next()){
                    containerIdList.add(resultSet.getInt(1));
                    System.out.println("Container Type :"+resultSet.getString(2));
                }
            }

            //get all match container properties
            System.out.println();
            System.out.println("-- Container Properties --");
            for(int i=0; i<containerIdList.size(); i++){
                query = "DELETE * FROM property WHERE property.property_id = " + containerIdList.get(i);

                statement.executeUpdate(query);
            }

            //get all match component which related to component
            System.out.println();
            for(int i=0; i<containerIdList.size(); i++){
                query = "SELECT * FROM component WHERE component.container_id = "+containerIdList.get(i);

                resultSet = statement.executeQuery(query);

                while(resultSet.next()){
                    componentIdList.add(resultSet.getInt(1));
                    System.out.println("Component Type :"+resultSet.getString(2));
                }
            }

            //get all match component properties
            System.out.println();
            System.out.println("-- Component Properties --");
            for(int i=0; i<componentIdList.size(); i++){
                query = "DELETE * FROM property WHERE property.property_id = " + componentIdList.get(i);

                statement.executeUpdate(query);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setQuery(String query){
        this.query = query;
    }

    public String getQuery(){
        return query;
    }
}
