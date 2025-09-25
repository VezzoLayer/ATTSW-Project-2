package com.ecommerce.manager.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class User {

	@Id
	@GeneratedValue
	private Long userId;
	private String username;
	private String name;
	private String email;
	private long balance;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Order> orders = new ArrayList<>();

	public User() {
		// required for serialization/deserialization
	}

	public User(Long id, String username, String name, String email, long balance) {
		this.userId = id;
		this.username = username;
		this.name = name;
		this.email = email;
		this.balance = balance;
	}

	public Long getId() {
		return userId;
	}

	public void setId(Long id) {
		this.userId = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public long getBalance() {
		return balance;
	}

	public void setBalance(long balance) {
		this.balance = balance;
	}

	@Override
	public String toString() {
		return "User [id=" + userId + ", username=" + username + ", name=" + name + ", email=" + email + ", balance="
				+ balance + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(balance, email, userId, name, username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		return balance == other.balance && Objects.equals(email, other.email) && Objects.equals(userId, other.userId)
				&& Objects.equals(name, other.name) && Objects.equals(username, other.username);
	}
}
