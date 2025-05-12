package com.cyna.products.repositories;

import com.cyna.products.models.Category;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepo extends CrudRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.name LIKE %:name%")
    List<Category> findByName(@Param("name") String name);

}
