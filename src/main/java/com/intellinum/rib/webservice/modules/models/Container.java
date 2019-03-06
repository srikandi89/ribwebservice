package com.intellinum.rib.webservice.modules.models;

import com.intellinum.rib.webservice.modules.db.Model;

import java.util.List;

/**
 * Created by Otniel on 5/15/2015.
 */
public class Container extends Model {
    public int containerId;
    public String containerType;
    public int sequence;
    public int pageId;

    public List<Property> property;

    public Container(){}

    public Container(int containerId, String containerType, int sequence, int pageId){
        this.containerId    = containerId;
        this.containerType = containerType;
        this.sequence       = sequence;
        this.pageId         = pageId;
    }

    public Container getContainer(int containerId, String containerName, int sequence, int pageId){
        Container c = new Container(containerId, containerName, sequence, pageId);

        return c;
    }

    public List<Container> findAll(){
        return Container.findAll(this).fetch();
    }

    public List<Container> findByQuery(){
        return Container.find("SELECT * FROM container").fetchQuery();
    }
}

