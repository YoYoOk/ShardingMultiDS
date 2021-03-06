package com.yj.multids;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.yj.multids.dao.UserRepository;
import com.yj.multids.entity.User;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserTest {
	
	@Autowired
	private UserRepository repository;
	
//	@Test
	public void saveTest(){
		User user = new User("save1");
		user.setId(1l);
		repository.save(user);
	}
	
//	@Test
	public void updateTest(){
		User user = new User();
		user.setId(242220579398029312l);
		user.setName("update");
		repository.save(user);
	}
	
//	@Test
	public void deleteTest(){
		repository.delete(242220579398029312l);
	}
	
	@Test
	public void getList(){
		System.out.println(repository.findAll());
	}
	
}
