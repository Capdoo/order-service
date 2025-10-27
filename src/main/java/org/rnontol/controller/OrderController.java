package org.rnontol.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.rnontol.entity.Order;
import org.rnontol.service.ClientService;

import java.util.List;

@Path("/api/orders")
public class OrderController {

    @Inject
    @RestClient
    ClientService clientService;

    @Inject
    @Channel("orders")
    Emitter<String> orderEmitter;

    @POST
    @WithTransaction
    public Uni<Response> registrarOrden(Order order) {
        Long idCliente = order.clientId;

        return clientService.getClientById(order.clientId)
                .onItem().ifNotNull().transformToUni(client -> {
                    order.state = "PENDING";
                    return Panache.withTransaction(order::persist)
                            .invoke(() -> {
                                ObjectMapper mapper = new ObjectMapper();
                                String payload;
                                try {
                                    payload = mapper.writeValueAsString(order);
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                                orderEmitter.send(payload);
                            })
                            .replaceWith(Response.status(Response.Status.CREATED).entity(order).build());
                })
                .onItem().ifNull().failWith(() ->
                        new NotFoundException("Client with id " + order.clientId + " not found")
                );

    }

    @GET
    @WithSession
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<Order>> getOrders() {
        return Order.listAll();
    }

    @GET
    @Path("/{id}")
    @WithSession
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Order> getOrder(@PathParam("id") long id) {
        return Order.findById(id);
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Order updateOrder(@PathParam("id") long id, Order order) {
        Order entity = (Order) Order.findById(id);
        if (entity == null) {
            throw new NotFoundException();
        }

        String state = order.state.isEmpty()? entity.state : order.state;
        String description = order.description.isEmpty()? entity.description : order.description;

        entity.description = description;
        entity.state = state;

        Order.persist(entity);

        return entity;
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    public void deleteOrder(@PathParam("id") long id) {
        Order entity = (Order) Order.findById(id);
        if (entity == null) {
            throw new NotFoundException("Order not found with id: " + id);
        }

        Order.deleteById(entity.id);
    }

}
