package com.cyna.products.repositories;

import com.cyna.products.models.Category;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Repository
public interface CategoryRepo extends CrudRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.name LIKE %:name%")
    Set<Category> findByName(@Param("name") String name);

}
