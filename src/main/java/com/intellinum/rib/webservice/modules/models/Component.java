package com.intellinum.rib.webservice.modules.models;

import com.intellinum.rib.webservice.modules.db.Model;

import java.util.List;

/**
 * Created by Otniel on 5/15/2015.
 */
public class Component extends Model {
    public int componentId;
    public String componentType;
    public int sequence;
    public int containerId;

    public List<Property> property;

    public Component(){}

    public Component(int componentId, int sequence, String componentType, int containerId){
        this.componentId    = componentId;
        this.sequence       = sequence;
        this.componentType  = componentType;
        this.containerId    = containerId;
    }

    public Component getComponent(int componentId, int sequence, String componentType, int containerId){
        Component c = new Component(componentId, sequence, componentType, containerId);
        return c;
    }

    public List<Component> findAll(){
        List<Component> list = Component.findAll(this).fetch();

        return list;
    }

    /**
     * No join query allowed here
     * This function has already automatic join table query
     * @return
     */
    public List<Component> findByQuery(){
        return Component.find("SELECT * FROM component WHERE component.container_id = 2", this).fetchQuery();
    }
}

