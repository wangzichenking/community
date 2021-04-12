package com.nowcoder.community.service;

import com.nowcoder.community.dao.AlphaDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
//@Scope("prototype")
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    @Autowired
    private static final Logger LOGGER = LoggerFactory.getLogger(AlphaService.class);

    public AlphaService() {
//        System.out.println("实例化AlphaService");
    }

    @PostConstruct
    public void init() {
//        System.out.println("初始化AlphaService");
    }

    @PreDestroy
    public void destroy() {
//        System.out.println("销毁AlphaService");
    }

    public String find() {
        return alphaDao.select();
    }

    /*
    @Async：让该方法在多线程环境下，被异步的调用
     */
    @Async
    public void executor1(){
        LOGGER.debug("executor1");
    }

    /*
    @Scheduled：让该方法定时执行
     */
    @Scheduled(initialDelay = 10000,fixedRate = 1000)
    public void executor2(){
        LOGGER.debug("executor2");
    }

}
