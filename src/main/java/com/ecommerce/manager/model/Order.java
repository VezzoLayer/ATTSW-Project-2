package com.ecommerce.manager.model;

import java.util.Objects;

public class Order {
	
	private Long id;
	private Item item;
	private long price;
	
	public Order() {
		// required for serialization/deserialization
	}

	public Order(Long id, Item item, long price) {
		this.id = id;
		this.item = item;
		this.price = price;
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
