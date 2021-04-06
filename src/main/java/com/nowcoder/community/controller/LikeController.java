package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(value = "/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId){
        //获取当前用户
        User user = hostHolder.getUser();
        //点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        //数量
        long likeCount = likeService.findEntityLikeCount(entityType,entityId);
        //状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(),entityType,entityId);

        //封装数据
        Map<String ,Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);

        //返回json数据
        return CommunityUtil.getJSONString(0,null,map);
    }
}
