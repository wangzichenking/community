package com.nowcoder.community.quartz;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticSearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, CommunityConstant {
    @Autowired
    private static final Logger LOGGER = LoggerFactory.getLogger(PostScoreRefreshJob.class);
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    private static final Date epoch;

    /*
    静态块：在类初次被加载时执行且会被执行一次，会按照static块的顺序来执行每个static块
     */
    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败！");
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        /*
        BoundSetOperations：就是一个绑定key的对象，可以通过这个对象进行与key相关的操作
         */
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);
        if (operations.size() == 0){
            LOGGER.info("任务取消，无需要刷新的帖子！");
            return;
        }
        LOGGER.info("任务开始，正在刷新帖子分数："+operations.size());
        while (operations.size() > 0){
            this.refresh((Integer)operations.pop());
        }
        LOGGER.info("任务结束，帖子分数刷新完毕！");
    }

    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if (post == null){
            LOGGER.error("该帖子不存在：id = "+postId);
            return;
        }
        //是否精华帖
        boolean wonderful = post.getStatus() == 1;
        //评论数量
        int commentCount = post.getCommentCount();
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,postId);

        /*
        计算权重
         */
        double weight = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        //分数 = 帖子权重+距离天数
        double score = Math.log10(Math.max(weight,1))+
                (post.getCreateTime().getTime() - epoch.getTime())/(1000*3600*24);
        //更新帖子分数
        discussPostService.updateScore(postId,score);
        //同步搜索数据
        post.setScore(score);
        elasticSearchService.saveDiscussPost(post);
    }
}
