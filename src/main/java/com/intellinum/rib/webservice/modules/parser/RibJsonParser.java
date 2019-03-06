package com.intellinum.rib.webservice.modules.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map;

/**
 * Created by Otniel on 5/15/2015.
 */
public class RibJsonParser {
    /**
     * Read json file from path, and read the json content
     *
     * @param path  json file location
     * @return      string of json content
     */

    public  int idCounter         = 1;
    public  int componentId       = 1;
    public JsonObject page       = new JsonObject();

    public JsonArray componentList         = new JsonArray();
    public  JsonArray containerList         = new JsonArray();
    public  JsonObject containerObject      = new JsonObject();
    public  JsonObject containerProperties  = new JsonObject();

    public  int contentSequence             = 1;
    public  int containerSequence           = 1;

    public RibJsonParser(){

    }


    public  String jsonReader(String path){
        BufferedReader reader = null;
        String line;
        String content = "";

        try{
            reader = new BufferedReader(new FileReader(path));

            while((line = reader.readLine()) != null){
                content += line;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return content;
    }

    public enum ModelCollection {
        COMPONENT,
        CONTAINER,
        PAGE,
        PROPERTY
    }

    public  boolean isContainer(JsonObject object){
        if(object.has("type")){
            if(object.get("type").toString().equals("\"Grid\"") || object.get("type").toString().equals("\"Div\"") || object.get("type").toString().equals("\"Form\"")){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }

    /**
     * Return the parsed component by models type which stated at ModelCollection enum
     *
     * @param models    which model to be returned from parsed json data structure
     * @param json      meta data structure from RIB
     * @return          parsed json file into preferred models from ModelCollection enum
     */
    public JsonElement getParsedElement(ModelCollection models, String json){
        JsonElement element = new JsonParser().parse(json);
        switch(models){
            case PAGE:
                break;
            case CONTAINER:
                break;
            case COMPONENT:
                break;
            case PROPERTY:
                break;
        }

        return element;
    }

    /**
     * Parse json to takes only meta data which corresponded to meta data pageId
     *
     * @param json      meta data structure from RIB
     * @param pageId
     * @return          pageId element which contained on the RIB's json
     */
    public  JsonElement parsePage(String json, int pageId){
        JsonElement element         = new JsonParser().parse(json) ;

        if(element.isJsonObject()){
            JsonObject pageObject = element.getAsJsonObject();
            if(pageObject.has("properties")){
                JsonElement properties = pageObject.get("properties");
                if(properties.isJsonObject()){
                    String pageName = properties.getAsJsonObject().get("id").getAsString();
                    page.addProperty("page_id", pageId);
                    page.addProperty("page_name", pageName);
                    page.add("properties", properties);

                    idCounter = pageId;
                }
            }
        }

        return page;
    }

    public  JsonElement parseContainer(String json, int pageId){
        JsonElement element             = new JsonParser().parse(json);

        if(element.isJsonObject()){
            JsonObject pageObject = element.getAsJsonObject();

            if(pageObject.has("children")){
                if(!isContainer(pageObject)){
                    parseContainer(pageObject.get("children").toString(), 1);
                }
                else{
                    parseContainer(pageObject.get("children").toString(), 1);

                    if(pageObject.has("properties")){
                        if(pageObject.get("properties").isJsonObject()){
                            JsonObject properties       = pageObject.get("properties").getAsJsonObject();

                            for(Map.Entry<String, JsonElement> entry : properties.entrySet()){
                                containerProperties.add(entry.getKey(), entry.getValue());
                            }

                            containerSequence++;

                            containerObject.add("properties", containerProperties);

                        }
                    }

                    JsonElement containerName   = pageObject.get("type");

                    idCounter++;

                    containerObject.add("container_type", containerName);
                    containerObject.addProperty("container_id", idCounter);
                    containerObject.addProperty("page_id", pageId);
                    containerObject.addProperty("sequence", containerSequence);

                    containerList.add(containerObject);

                    containerObject = new JsonObject();
                }
            }
            else{
                int containerId = idCounter+1;

                while(componentId <= containerId){
                    componentId++;
                }

                pageObject.addProperty("container_id", containerId);
                pageObject.addProperty("component_id", componentId++);
                pageObject.addProperty("sequence", contentSequence++);
                componentList.add(pageObject);
            }
        }

        if(element.isJsonArray()){
            JsonArray array = element.getAsJsonArray();
            for(int i=0; i < array.size(); i++) {
                parseContainer(array.get(i).toString(), 1);
            }
        }

        return containerList;
    }

    public  JsonArray getComponentList(){
        return componentList;
    }

    public  JsonElement getPropertyList(){
        return containerProperties;
    }
}
