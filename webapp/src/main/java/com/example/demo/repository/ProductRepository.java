package com.example.demo.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.demo.model.Products;

@Repository
public interface ProductRepository extends JpaRepository<Products, String> {
@Query("""
        select p from Products p
        where lower(p.productname) like lower(concat('%', :q, '%'))
           or lower(p.productdescription) like lower(concat('%', :q, '%'))
        """)
    List<Products> search(@Param("q") String q);
}
