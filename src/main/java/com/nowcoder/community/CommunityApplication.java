package com.nowcoder.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {
	/*
	管理Bean生命周期
	 */
	@PostConstruct
	public void init(){
		/*
		解决Netty启动冲突问题(redis和elasticsearch)
		Netty4Utils+NettyRuntime
		 */
		System.setProperty("es.set.netty.runtime.available.processors","false");
	}


	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}
