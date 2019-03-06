package com.intellinum.rib.webservice.modules.controller;

import com.intellinum.rib.webservice.modules.db.QueryHandler;

/**
 * Created by Otniel on 5/27/2015.
 */
public class PageDelete {
    public void pageDelete(String pageName){
        QueryHandler handler = new QueryHandler();
        handler.removePageElements(pageName);
    }
}
