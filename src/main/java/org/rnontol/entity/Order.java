package org.rnontol.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
public class Order extends PanacheEntity {
    public Long id;
    public Long clientId;
    public String description;
    public String state;
    public static Uni<Order> findByClientId(Long clientId) {
        return find("clientId", clientId).firstResult();
    }
}