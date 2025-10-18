package com.ecommerce.manager.model;

import java.util.Objects;

import org.springframework.lang.NonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders") // Plurale per evitare conflitti
public class Order {

	@Id
	@GeneratedValue
	private Long id;

	@NonNull
	@Column(nullable = false)
	private Item item;
	private long price;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id")
	private User user;

	public Order() {
		// required for serialization/deserialization
	}

	public Order(Long id, Item item, long price, User user) {
		this.id = id;
		this.item = item;
		this.price = price;
		this.user = user;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public long getPrice() {
		return price;
	}

	public void setPrice(long price) {
		this.price = price;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "Order [id=" + id + ", item=" + item + ", price=" + price + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, item, price);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Order other = (Order) obj;
		return Objects.equals(id, other.id) && item == other.item && price == other.price;
	}
}
