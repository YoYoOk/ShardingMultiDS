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
public class userTest {
	
	@Autowired
	private UserRepository repository;
	
	@Test
	public void saveTest(){
		User user = new User("liaoyao");
		user.setId(1l);
		repository.save(user);
	}
	
}
