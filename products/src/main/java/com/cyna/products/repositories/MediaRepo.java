package com.cyna.products.repositories;

import com.cyna.products.models.Media;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface MediaRepo extends CrudRepository<Media, Long> {
    Set<Media> findAllByIdIn(Set<Long> ids);
}
