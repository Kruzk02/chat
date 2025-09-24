package org.server;

import java.sql.Timestamp;

public class Chat {
  private long id;
  private User user;
  private String name;
  private String message;
  private Timestamp createdAt;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    if (id > 0) this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    if (user != null) this.user = user;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    if (name != null && !name.isEmpty()) this.name = name;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    if (message != null && !message.isEmpty()) this.message = message;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
  }
}
