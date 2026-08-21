package org.sy.pickandsave.domain.userproduct.entity;

import jakarta.persistence.*;
import lombok.*;
import org.sy.pickandsave.domain.products.entity.Product;
import org.sy.pickandsave.domain.users.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(
		name = "USER_PRODUCTS",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_user_product",
						columnNames = {"user_id", "product_id"}
				)
		}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserProduct {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	public UserProduct(User user, Product product) {
		this.user = user;
		this.product = product;
		this.createdAt = LocalDateTime.now();
	}
}