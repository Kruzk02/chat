package org.server;

import java.sql.Timestamp;

public class User {
  private long id;
  private String username;
  private String password;
  private Timestamp createdAt;

  public User() {
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    if (id > 0) this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    if (username != null && !username.isEmpty()) this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    if (password != null && !password.isEmpty()) this.password = password;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
  }
}
