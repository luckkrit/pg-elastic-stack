package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Orderdetails;
import com.example.demo.model.OrderdetailsId;

@Repository
public interface OrderDetailRepository extends JpaRepository<Orderdetails, OrderdetailsId> {
    @Query("select d from Orderdetails d where d.orders.ordernumber = :ordernumber order by d.orderlinenumber")
    List<Orderdetails> findById_Ordernumber(Integer ordernumber);
}
