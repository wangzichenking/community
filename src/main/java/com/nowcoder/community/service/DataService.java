package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    /*
    将指定IP计入UV
     */
    public void recordUV(String ip){
        String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey,ip);
    }

    /*
    统计指定日期范围内的UV
     */
    public long calculateUV(Date startDate,Date endDate){
        if (startDate == null || endDate == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        //整理该日期范围内的key
        List<String> keyList = new ArrayList<>();
        //日期类
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        while (!calendar.getTime().after(endDate)){
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE,1);
        }
        //合并数据
        String redisKey = RedisKeyUtil.getUVKey(df.format(startDate),df.format(endDate));
        redisTemplate.opsForHyperLogLog().union(redisKey,keyList.toArray());

        //返回统计的结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    /*
    将指定用户计入DAYU
     */
    public void recordDAU(int userId){
        String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey,userId,true);
    }

    /*
    统计指定日期范围内的DAU
     */
    public long calculateDAU(Date startDate,Date endDate){
        if (startDate == null || endDate == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        //整理该日期范围内的key
        List<byte[]> keyList = new ArrayList<>();
        //日期类
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        while (!calendar.getTime().after(endDate)){
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE,1);
        }
        //进行OR运算
        return (long)redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                String rediskey = RedisKeyUtil.getDAUKey(df.format(startDate),df.format(endDate));
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR,
                        rediskey.getBytes(),keyList.toArray(new byte[0][0]));
                return redisConnection.bitCount(rediskey.getBytes());
            }
        });
    }
}
