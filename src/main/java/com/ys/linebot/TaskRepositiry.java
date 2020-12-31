package com.ys.linebot;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepositiry extends JpaRepository<Task, String> {
	
	List<Task> deleteByTitle(String title);
	List<Task> findByTitle(String title);
	List<Task> findByUserId(String title);
}
