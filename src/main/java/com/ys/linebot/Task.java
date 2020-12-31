package com.ys.linebot;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


import lombok.Data;

@Data
@Entity
@Table(name = "task")
public class Task {
	@Id
	@Column(name = "id", length = 36, nullable = false)
    private String id;
	
	@Column(name = "user_id", length = 255, nullable = false)
	private String userId;
	
	@Column(name = "title", length = 255, nullable = false)
	private String title;
	
	@Column(name = "specify_time", nullable = false)
	private LocalDateTime specify_time;
	
	@Column(name = "created_at", nullable = false)
	private LocalDateTime created_at;
}

