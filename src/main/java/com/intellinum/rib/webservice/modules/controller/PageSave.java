package com.intellinum.rib.webservice.modules.controller;

import com.google.gson.*;
import com.intellinum.rib.webservice.modules.db.Model;
import com.intellinum.rib.webservice.modules.models.Component;
import com.intellinum.rib.webservice.modules.models.Container;
import com.intellinum.rib.webservice.modules.models.Page;
import com.intellinum.rib.webservice.modules.models.Property;
import com.intellinum.rib.webservice.modules.parser.RibJsonParser;


import java.util.Map;

/**
 * Created by Otniel on 5/15/2015.
 */
public class PageSave {

    public RibJsonParser parser;
    public Gson gson;

    public PageSave(){
        parser  = new RibJsonParser();
        gson    = new GsonBuilder().setPrettyPrinting().create();
    }

    public void savePage(String ribJson){
        int latestId = Model.getIdCounter()+1;

        JsonObject page         = parser.parsePage(ribJson, latestId).getAsJsonObject();
        JsonObject properties   = page.get("properties").getAsJsonObject();
        JsonArray components    = parser.getComponentList();

        Page p      = new Page();
        p.pageId    = page.get("page_id").getAsInt();
        p.pageName  = page.get("page_name").getAsString();
        p.ribJson   = ribJson;
        p.save(p);

        saveContainer(ribJson, p.pageId);
        saveProperty(properties, p.pageId);
        saveComponent(components);
    }

    public void savePage(String ribJson, String ribJsonProject){
        int latestId = Model.getIdCounter()+1;

        JsonObject page         = parser.parsePage(ribJson, latestId).getAsJsonObject();
        JsonObject properties   = page.get("properties").getAsJsonObject();
        JsonArray components    = parser.getComponentList();

        Page p      = new Page();
        p.pageId    = page.get("page_id").getAsInt();
        p.pageName  = page.get("page_name").getAsString();
        p.ribJson   = ribJsonProject;
        p.save(p);

        saveContainer(ribJson, p.pageId);
        saveProperty(properties, p.pageId);
        saveComponent(components);
    }

    public void saveContainer(String ribJson, int pageId){
        JsonArray containers = parser.parseContainer(ribJson, pageId).getAsJsonArray();

        for(int i=0; i<containers.size(); i++){
            JsonObject object = containers.get(i).getAsJsonObject();

            Container c     = new Container();
            c.containerId   = object.get("container_id").getAsInt();
            c.containerType = object.get("container_type").getAsString();
            c.sequence      = object.get("sequence").getAsInt();
            c.pageId        = object.get("page_id").getAsInt();
            c.save(c);

            if(object.has("properties")){
                JsonObject properties = object.get("properties").getAsJsonObject();
                saveProperty(properties, c.containerId);
            }
        }
    }

    public void saveComponent(JsonArray components){
        for(int i=0; i<components.size(); i++){
            JsonObject object = components.get(i).getAsJsonObject();

            Component c     = new Component();
            c.componentId   = object.get("component_id").getAsInt();
            c.componentType = object.get("type").getAsString();
            c.sequence      = object.get("sequence").getAsInt();
            c.containerId   = object.get("container_id").getAsInt();
            c.save(c);

            if(object.has("properties")){
                JsonObject properties = object.get("properties").getAsJsonObject();
                saveProperty(properties, c.componentId);
            }
        }
    }

    public void saveProperty(JsonObject properties, int elementId){
        for(Map.Entry<String, JsonElement> entry : properties.entrySet()){
            String propertyName     = entry.getKey();
            String propertyValue    = entry.getValue().getAsString();

            Property property       = new Property();
            property.propertyId     = elementId;

            property.propertyName   = propertyName;
            property.propertyValue  = propertyValue;
            property.save(property);
        }
    }
}
