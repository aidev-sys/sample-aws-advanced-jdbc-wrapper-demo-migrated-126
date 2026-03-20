package com.example.dao;

import com.example.config.DatabaseConfig;
import com.example.model.Order;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class OrderDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS orders (" +
                "id SERIAL PRIMARY KEY," +
                "customer_name VARCHAR(100) NOT NULL," +
                "product VARCHAR(100) NOT NULL," +
                "quantity INTEGER NOT NULL," +
                "total_amount NUMERIC(10,2) NOT NULL," +
                "status VARCHAR(50) DEFAULT 'PENDING'," +
                "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try {
            Query query = entityManager.createNativeQuery(sql);
            query.executeUpdate();
            log.info("Table 'orders' created or already exists");
        } catch (Exception e) {
            log.error("Error creating table", e);
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void createOrder(Order order) {
        log.info("WRITE OPERATION: Creating new order for customer: {}", order.getCustomerName());
        String sql = "INSERT INTO orders (customer_name, product, quantity, total_amount, status) VALUES (?, ?, ?, ?, ?)";

        try {
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, order.getCustomerName());
            query.setParameter(2, order.getProduct());
            query.setParameter(3, order.getQuantity());
            query.setParameter(4, order.getTotalAmount());
            query.setParameter(5, order.getStatus());

            query.executeUpdate();

            // Retrieve generated ID
            Query selectQuery = entityManager.createNativeQuery("SELECT LASTVAL()");
            Number lastVal = (Number) selectQuery.getSingleResult();
            order.setId(lastVal.longValue());

            log.info("Order created with ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Error creating order", e);
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void updateOrderStatus(Long orderId, String newStatus) {
        log.info("WRITE OPERATION: Updating order {} status to {}", orderId, newStatus);
        String sql = "UPDATE orders SET status = ? WHERE id = ?";

        try {
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, newStatus);
            query.setParameter(2, orderId);

            int updated = query.executeUpdate();
            log.info("Updated {} order(s)", updated);
        } catch (Exception e) {
            log.error("Error updating order status", e);
            throw new RuntimeException(e);
        }
    }

    public List<Order> getOrderHistory() {
        log.info("READ OPERATION: Getting order history");
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";

        try {
            Query query = entityManager.createNativeQuery(sql, Order.class);
            @SuppressWarnings("unchecked")
            List<Order> orders = query.getResultList();
            log.info("Found {} orders", orders.size());
            return orders;
        } catch (Exception e) {
            log.error("Error getting order history", e);
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> getSalesReport() {
        log.info("READ OPERATION: Generating sales report");
        String sql = "SELECT " +
                "COUNT(*) as total_orders, " +
                "SUM(total_amount) as total_revenue, " +
                "AVG(total_amount) as avg_order_value " +
                "FROM orders";

        Map<String, Object> report = new HashMap<>();

        try {
            Query query = entityManager.createNativeQuery(sql);
            Object[] result = (Object[]) query.getSingleResult();

            report.put("totalOrders", ((Number) result[0]).intValue());
            report.put("totalRevenue", ((Number) result[1]).doubleValue());
            report.put("avgOrderValue", ((Number) result[2]).doubleValue());

            log.info("Sales report generated: {}", report);
            return report;
        } catch (Exception e) {
            log.error("Error generating sales report", e);
            throw new RuntimeException(e);
        }
    }

    public List<Order> searchOrdersByCustomer(String customerName) {
        log.info("READ OPERATION: Searching orders for customer: {}", customerName);
        String sql = "SELECT * FROM orders WHERE customer_name ILIKE ? ORDER BY order_date DESC";

        try {
            Query query = entityManager.createNativeQuery(sql, Order.class);
            query.setParameter(1, "%" + customerName + "%");

            @SuppressWarnings("unchecked")
            List<Order> orders = query.getResultList();
            log.info("Found {} orders for customer: {}", orders.size(), customerName);
            return orders;
        } catch (Exception e) {
            log.error("Error searching orders", e);
            throw new RuntimeException(e);
        }
    }
}