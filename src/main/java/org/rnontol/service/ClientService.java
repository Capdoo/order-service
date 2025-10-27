package org.rnontol.service;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.rnontol.dto.ClientDto;

@Path("/api/clients")
@RegisterRestClient(configKey = "client-service")
public interface ClientService {

    @GET
    @Path("/{id}")
    Uni<ClientDto> getClientById(@PathParam("id") long id);

}