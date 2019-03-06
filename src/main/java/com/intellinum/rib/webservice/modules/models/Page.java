package com.intellinum.rib.webservice.modules.models;

import com.intellinum.rib.webservice.modules.db.Model;

import java.util.List;

/**
 * Created by Otniel on 5/15/2015.
 */
public class Page extends Model {
    public int pageId;
    public String pageName;
    public String ribJson;

    public List<Property> property;

    public Page(){}

    public Page(int pageId, String pageName, String ribJson){
        this.pageId     = pageId;
        this.pageName   = pageName;
        this.ribJson    = ribJson;
    }

    public Page getPage(int pageId, String pageName, String ribJson){
        Page p = new Page(pageId, pageName, ribJson);

        return p;
    }

    public List<Page> findAll(){
        return Page.findAll(this).fetch();
    }
}

