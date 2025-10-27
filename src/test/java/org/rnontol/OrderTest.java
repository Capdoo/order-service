package org.rnontol;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.rnontol.controller.OrderController;
import io.quarkus.panache.mock.PanacheMock;
import org.rnontol.entity.Order;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;

@QuarkusTest
public class OrderTest {

    @Inject
    OrderController orderController;

    @BeforeEach
    void setup() {
        PanacheMock.mock(Order.class);
    }

    @Test
    void testGetClient() {
        // * Arrange
        Order order = new Order(1L, 2L, "Compra regular","PENDING");
        //Mockito.when(customerRepository.findAllCustomers()).thenReturn(List.of(customer2));
        Mockito.when(Order.listAll()).thenReturn((Uni<List<PanacheEntityBase>>) List.of(order));

        List<Order> expected = List.of(order);

        // * Act
        List<Order> response = (List<Order>) orderController.getOrders();

        // * Assert
        assertAll(
                () -> Assertions.assertEquals(expected, response),
                () -> org.assertj.core.api.Assertions.assertThat(response).usingRecursiveComparison().isEqualTo(expected)
        );
    }

    @Test
    void testGetClientById() {
        // * Arrange
        Order order = new Order(1L, 3L, "Compra regular","PENDING");
        Mockito.when(Order.findById(anyLong())).thenReturn((Uni<PanacheEntityBase>) order);

        // * Act
        Order response = (Order) orderController.getOrder(1L);

        // * Assert
        assertAll(
                () -> assertThat(response).usingRecursiveComparison().isEqualTo(order)
        );
    }

    @Test
    void testDeleteClient() {
        // * Arrange
        Order order = new Order(1L, 3L, "Compra regular","PENDING");
        Mockito.when(Order.findById(1L)).thenReturn((Uni<PanacheEntityBase>) order);

        // * Act
        orderController.deleteOrder(1L);

        // * Assert
        PanacheMock.verify(Order.class, times(1)).deleteById(order.id);
    }


    @Test
    void testDeleteCustomerNotFound() {
        // * Arrange
        Mockito.when(Order.findById(1L)).thenReturn(null);

        // * Act & Assert
        // * Option 1
        try {
            orderController.deleteOrder(1L);
            fail();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(NotFoundException.class);
        }

        // * Option 2
        assertThrows(ClientErrorException.class, () -> orderController.deleteOrder(1L));
        assertThrowsExactly(NotFoundException.class, () -> orderController.deleteOrder(1L));

        // * Option 3
        assertThatThrownBy(() -> orderController.deleteOrder(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Order not found");

        // * Option 4
        Exception exception = assertThrows(NotFoundException.class, () -> orderController.deleteOrder(1L));
        assertAll(
                () -> assertThat(exception)
                        .isInstanceOf(NotFoundException.class)
                        .extracting(Exception::getMessage)
                        .isEqualTo("Order not found with id: 1")
        );

    }

}