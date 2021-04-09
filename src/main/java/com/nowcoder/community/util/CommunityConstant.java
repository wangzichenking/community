package com.nowcoder.community.util;

public interface CommunityConstant {

    /*
    激活成功
     */
    int ACTIVATION_SUCCESS = 0;
    /*
    重复激活
     */
    int ACTIVATION_REPEAT = 1;
    /*
    激活失败
     */
    int ACTIVATION_FALSE = 2;

    /*
    默认状态登录凭证超时时间
     */
    int DEFAUTL_EXPIRED_SECONDS = 3600 * 12;

    /*
    记住状态下的登录凭证超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;

    /*
    实体类型：帖子
     */
    int ENTITY_TYPE_POST = 1;

    /*
    实体类型：评论
     */
    int ENTITY_TYPE_COMMENT = 2;

    /*
    实体类型：评论
     */
    int ENTITY_TYPE_USER = 3;

    /*
    kafka主题：评论
     */
    String TOPIC_COMMENT = "comment";

    /*
    kafka主题：评论
     */
    String TOPIC_LIKE = "like";

    /*
    kafka主题：评论
     */
    String TOPIC_FOLLOW = "follow";

    /*
    主题：发帖
     */
    String TOPIC_PUBLISH = "publish";

    /*
    系统用户ID
     */
    int SYSTEM_USER_ID = 1;

    /*
    权限：普通用户
     */
    String AUTHENTICATION_USER = "user";

    /*
    权限：管理员
     */
    String AUTHENTICATION_ADMIN = "admin";

    /*
    权限：版主
     */
    String AUTHENTICATION_MODERATOR = "moderator";
}
