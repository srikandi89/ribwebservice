package com.intellinum.rib.webservice.modules.controller;

import com.google.gson.*;
import com.intellinum.rib.webservice.modules.models.Page;

import java.util.List;

/**
 * Created by Otniel on 5/18/2015.
 */
public class PageLoad {

    public String getRibPageJsonList(){
        String pageListJson     = "";
        Page page               = new Page();
        JsonArray listJsonPages = new JsonArray();

        List<Page> pages        = page.findAll();

        for(int i=0; i<pages.size(); i++){
            JsonObject pageObject   = new JsonObject();
            JsonElement pageElement = new JsonParser().parse(pages.get(i).ribJson);
            pageObject.addProperty("page_id", pages.get(i).pageId);
            pageObject.addProperty("page_name", pages.get(i).pageName);
            pageObject.add("rib_json", pageElement);

            listJsonPages.add(pageObject);
        }

        pageListJson = listJsonPages.toString();

        return pageListJson;
    }
}
