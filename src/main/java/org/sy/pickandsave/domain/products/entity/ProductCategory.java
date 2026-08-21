package org.sy.pickandsave.domain.products.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCT_CATEGORIES")
@Getter
@NoArgsConstructor
public class ProductCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE , generator = "product_categories_seq_gen")
	@SequenceGenerator(name = "product_categories_seq_gen", sequenceName = "PRODUCT_CATEGORIES_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "name", length = 100, nullable = false)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private ProductCategory parent;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void prePersist() {
		createdAt = LocalDateTime.now();
	}
}