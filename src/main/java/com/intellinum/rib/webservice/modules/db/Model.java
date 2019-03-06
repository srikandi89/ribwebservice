package com.intellinum.rib.webservice.modules.db;

/**
 * Created by Otniel on 5/15/2015.
 */
public class Model extends QueryHandler {
    protected static QueryHandler find(String query, Object... params){
        return QueryHandler.getHandler(query, params);
    }

    protected static QueryHandler findAll(Object params){
        return QueryHandler.getHandler(params);
    }

    public int save(Object object){
        return QueryHandler.getHandler(object).save();
    }

    public static int getIdCounter(){
        return QueryHandler.getHandler(null).getLatestId();
    }
}
