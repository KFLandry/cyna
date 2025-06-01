package com.cyna.products.repositories;

import com.cyna.products.models.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProductRepo extends CrudRepository<Product, Long>{
    @Query(value = "SELECT * FROM products " +
            "WHERE MATCH(name, description, brand) AGAINST(:text IN BOOLEAN MODE)" +
            "   OR name LIKE CONCAT('%', :text, '%')" +
            "   OR description LIKE CONCAT('%', :text, '%')" +
            "   OR brand LIKE CONCAT('%', :text, '%');",
            nativeQuery = true)
    List<Product> findByText(@Param("text") String text);


    @Query(
            value = "SELECT * FROM products " +
                    "WHERE (:categoryIds IS NULL OR :categoryIds = '' OR category_id IN (:categoryIds)) " +
                    "AND (:promoOnly IS NULL OR promo = :promoOnly) " +
                    "LIMIT :size OFFSET :offset",
            nativeQuery = true
    )
    List<Product> findProductByCategoryAndPromo(
            @Param("categoryIds") Set<Long> categoryIds,
            @Param("promoOnly") boolean promoOnly,
            @Param("size") int size,
            @Param("offset") int offset
    );

    @Query(
            value = "SELECT COUNT(*) FROM products " +
                    "WHERE (:categoryIds IS NULL OR :categoryIds = '' OR category_id IN (:categoryIds)) " +
                    "AND (:promoOnly IS NULL OR promo = :promoOnly)",
            nativeQuery = true
    )

    long countProductByCategoryAndPromo(
            @Param("categoryIds") Set<Long> categoryIds,
            @Param("promoOnly") boolean promoOnly
    );

@Query(value = "SELECT COUNT(*) FROM products " +
        "WHERE MATCH(name, description, brand) AGAINST(:text IN BOOLEAN MODE)" +
        "   OR name LIKE CONCAT('%', :text, '%')" +
        "   OR description LIKE CONCAT('%', :text, '%')" +
        "   OR brand LIKE CONCAT('%', :text, '%');",
            nativeQuery = true)
    long countByText(@Param("text") String text);
}
