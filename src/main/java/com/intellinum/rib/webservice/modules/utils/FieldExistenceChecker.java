package com.intellinum.rib.webservice.modules.utils;

import java.lang.reflect.Field;

/**
 * Created by Otniel on 5/15/2015.
 */
public class FieldExistenceChecker {
    public boolean isFieldExist(Object object, String fieldName){
        boolean exist = false;

        Field[] fields = object.getClass().getDeclaredFields();

        int counter = 0;

        while(!exist && counter < fields.length){
            if(fields[counter].getName().equals(fieldName)){
                exist = true;
            }
            counter++;
        }

        return exist;
    }
}