package com.intellinum.rib.webservice.modules.models;

import com.intellinum.rib.webservice.modules.db.Model;

import java.util.List;

/**
 * Created by Otniel on 5/15/2015.
 */
public class Property extends Model {
    public int propertyId;
    public String propertyName;
    public String propertyValue;

    public List<Property> findAll(){
        return Property.findAll(this).fetch();
    }
}