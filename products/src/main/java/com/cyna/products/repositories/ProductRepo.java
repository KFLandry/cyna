package com.cyna.products.repositories;

import com.cyna.products.models.Product;
import org.hibernate.annotations.Parameter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ProductRepo extends CrudRepository<Product, Long>{
    @Query(value = "SELECT * FROM product WHERE " +
            "MATCH(name, description, caracteristics, brand) " +
            "AGAINST(:word IN BOOLEAN MODE)",
            nativeQuery = true)
    Set<Product> findByText(@Param("text") String text);
}
