package com.yj.multids;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.yj.multids.dao.InfoRepository;
import com.yj.multids.dao.UserRepository;
import com.yj.multids.entity.Info;
import com.yj.multids.entity.User;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InfoTest {
	
	@Autowired
	private InfoRepository repository;
	
//	@Test
	public void saveTest(){
		Info info = new Info();
		info.setAge(25);
		info.setGender("0");
		info.setWeight(46.1);
		repository.save(info);
	}
	
//	@Test
	public void updateTest(){
		Info info = new Info();
		info.setId(1l);
		info.setAge(12);
		info.setGender("0");
		repository.save(info);
	}
	
//	@Test
	public void deleteTest(){
		repository.delete(3l);
	}
	
	@Test
	public void getList(){
		System.out.println(repository.findAll());
	}
	
}
