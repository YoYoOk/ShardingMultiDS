package com.yj.multids.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yj.multids.entity.Info;

@Repository
public interface InfoRepository extends JpaRepository<Info, Long> {
	
}
