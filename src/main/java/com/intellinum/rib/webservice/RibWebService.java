package com.intellinum.rib.webservice;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.intellinum.rib.webservice.modules.controller.PageDelete;
import com.intellinum.rib.webservice.modules.controller.PageLoad;
import com.intellinum.rib.webservice.modules.controller.PageSave;

/**
 * Created by Otniel on 5/18/2015.
 */
@Path("/service")
public class RibWebService {

    @POST
    @Path("save")
    @Consumes({"application/xml", "application/json", "text/plain", "text/html"})
    @Produces({"application/xml", "application/json", "text/plain", "text/html"})
    public Response savePage(@QueryParam("json") String json){
        String output = "success";

        PageSave page = new PageSave();
        page.savePage(json);

        return Response.status(200).entity(output).build();
    }

    @POST
    @Path("saveWithProject")
    @Consumes({"application/xml", "application/json", "text/plain", "text/html"})
    @Produces({"application/xml", "application/json", "text/plain", "text/html"})
    public Response savePageWithProject(@QueryParam("json") String json, @QueryParam("ribJsonProject") String ribJsonProject){
        String output = "success";

        PageSave page = new PageSave();
        page.savePage(json, ribJsonProject);

        return Response.status(200).entity(output).build();
    }

    @GET
    @Path("load")
    @Consumes({"application/xml", "application/json", "text/plain", "text/html"})
    @Produces({"application/xml", "application/json", "text/plain", "text/html"})
    public Response loadPage(){
        PageLoad page   = new PageLoad();
        String output   = page.getRibPageJsonList();

        return Response.status(200).entity(output).type(MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("delete")
    @Consumes({"application/xml", "application/json", "text/plain", "text/html"})
    @Produces({"application/xml", "application/json", "text/plain", "text/html"})
    public Response deletePage(@QueryParam("pageName") String pageName){
        String output = "success";

        PageDelete delete = new PageDelete();
        delete.pageDelete(pageName);

        return Response.status(200).entity(output).type(MediaType.APPLICATION_JSON).build();
    }
}