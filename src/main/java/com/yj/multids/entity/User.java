package com.yj.multids.entity;


import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class User implements Serializable{
	private static final long serialVersionUID = -3939038092023198789L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column
    private String name;
    
    @Column(name = "create_time")
	private Date createTime;
    
    
    
    public User() {
	}

	public User(String name) {
		super();
		this.name = name;
		this.createTime = new Date();
	}
    
	public User(Long id, String name, Date createTime) {
		super();
		this.id = id;
		this.name = name;
		this.createTime = createTime;
	}

	public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", createTime=" + createTime + "]";
	}
    
}
