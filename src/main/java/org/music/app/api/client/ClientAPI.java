package org.music.app.api.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.music.app.api.dto.response.AlbunsResponse;


@RegisterRestClient(baseUri = "http://localhost:9090")
@Path("/albuns")
public interface ClientAPI {

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    AlbunsResponse getById(@QueryParam("id") Long id);

}
