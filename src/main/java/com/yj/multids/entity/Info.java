package com.yj.multids.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Info implements Serializable{
	
	private static final long serialVersionUID = 1036000912117631594L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column
    private int age;
    
    @Column
    private String gender;
    
    @Column
    private double weight;
    
	public Info() {
	}

	public Info(int age) {
		super();
		this.age = age;
	}

	public Info(int age, String gender, double weight) {
		super();
		this.age = age;
		this.gender = gender;
		this.weight = weight;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return "Info [id=" + id + ", age=" + age + ", gender=" + gender + ", weight=" + weight + "]";
	}
	
}
