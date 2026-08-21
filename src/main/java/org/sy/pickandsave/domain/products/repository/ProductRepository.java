package org.sy.pickandsave.domain.products.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.sy.pickandsave.domain.products.entity.Product;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
  Optional<Product> findByCoupangProductId(Long coupangProductId);

  boolean existsByCoupangProductId(Long coupangProductId);
}
