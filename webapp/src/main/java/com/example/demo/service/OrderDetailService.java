package com.example.demo.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Orderdetails;
import com.example.demo.model.OrderdetailsId;
import com.example.demo.model.Orders;
import com.example.demo.model.Products;
// adjust to where your repositories are; the names below are the ones from our earlier messages
import com.example.demo.repository.OrderDetailRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;

@Service
public class OrderDetailService {

    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderDetailService(OrderDetailRepository orderDetailRepository,
                              OrderRepository orderRepository,
                              ProductRepository productRepository) {
        this.orderDetailRepository = orderDetailRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public void changeQuantity(Integer orderNumber, String productCode, int delta) {
        OrderdetailsId id = new OrderdetailsId(orderNumber, productCode);
        Optional<Orderdetails> existing = orderDetailRepository.findById(id);

        int newQty = existing.map(Orderdetails::getQuantityordered).orElse(0) + delta;
        if (newQty <= 0) {
            existing.ifPresent(orderDetailRepository::delete);
            return;
        }
        Orderdetails line = existing.orElseGet(() -> newLine(id, orderNumber, productCode));
        line.setQuantityordered(newQty);
        orderDetailRepository.save(line);
    }

    private Orderdetails newLine(OrderdetailsId id, Integer orderNumber, String productCode) {
        Orders order = orderRepository.findById(orderNumber).orElseThrow();
        Products product = productRepository.findById(productCode).orElseThrow();

        int nextLine = orderDetailRepository.findById_Ordernumber(orderNumber).stream()
                .mapToInt(Orderdetails::getOrderlinenumber).max().orElse(0) + 1;

        Orderdetails d = new Orderdetails();
        d.setId(id);
        d.setOrders(order);
        d.setProducts(product);
        d.setPriceeach(product.getMsrp());        // see the note below
        d.setOrderlinenumber((short) nextLine);
        return d;
    }
}