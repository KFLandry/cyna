package com.cyna.products.repositories;

import com.cyna.products.models.Carousel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarouselRepo extends JpaRepository<Carousel, Long> {
}
