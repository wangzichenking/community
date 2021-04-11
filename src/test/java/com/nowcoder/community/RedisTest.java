package com.nowcoder.community;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTest {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testString(){
        String redisKey = "test:count";
        redisTemplate.opsForValue().set(redisKey,1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    @Test
    public void testHash(){
        String redisKey = "test:hash";
        redisTemplate.opsForHash().put(redisKey,"id",1);
        redisTemplate.opsForHash().put(redisKey,"name","wangzichen");
        System.out.println(redisTemplate.opsForHash().get(redisKey,"name"));
    }

    @Test
    public void testList(){
        String redisKey = "test:list";
        redisTemplate.opsForList().leftPush(redisKey,101);
        redisTemplate.opsForList().leftPush(redisKey,102);
        redisTemplate.opsForList().leftPush(redisKey,103);
        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey,1));
        System.out.println(redisTemplate.opsForList().range(redisKey,0,2));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }

    /*
    多次访问同一个key
     */
    @Test
    public void testBoundOperations(){
        String redisKey = "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.increment();
        System.out.println(operations.get());
    }

    /*
    编程式事务
     */
    @Test
    public void testTransactional(){
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey = "test:tx";
                //启动事务
                redisOperations.multi();
                redisOperations.opsForSet().add(redisKey,"wangzichen");
                redisOperations.opsForSet().add(redisKey,"xiaozhang");
                System.out.println(redisOperations.opsForSet().members(redisKey));
                return redisOperations.exec();
            }
        });
        System.out.println(obj);
    }

    /*
    统计20万个重复数据的独立总数
     */
    @Test
    public void testHyperLogLog(){
        String redisKey = "test:hll:01";
        for (int i = 1; i <= 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey,i);
        }
        for (int i = 1; i <= 100000; i++) {
            int random = (int)(Math.random()*100000+1);
            redisTemplate.opsForHyperLogLog().add(redisKey,random);
        }
        System.out.println(redisTemplate.opsForHyperLogLog().size(redisKey));
    }

    /*
    合并数据（7天数据）
     */
    @Test
    public void testHyperLogLogUnion(){
        String redisKey2 = "test:hll:02";
        for (int i = 1; i <= 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2,i);
        }
        String redisKey3 = "test:hll:03";
        for (int i = 5001; i <= 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3,i);
        }
        String redisKey4 = "test:hll:04";
        for (int i = 10001; i <= 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey4,i);
        }
        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey,redisKey2,redisKey3,redisKey4);
        System.out.println(redisTemplate.opsForHyperLogLog().size(unionKey));
    }

    /*
    Bitmaps
    统计一组数据的布尔值
     */
    @Test
    public void testBitMap(){
        String redisKey = "test:bm:01";
        //记录
        redisTemplate.opsForValue().setBit(redisKey,1,true);
        redisTemplate.opsForValue().setBit(redisKey,4,true);
        redisTemplate.opsForValue().setBit(redisKey,7,true);
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,2));

        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return redisConnection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(obj);
    }

    /*
    统计三组数据的布尔值，并对三组数据做OR运算
     */
    @Test
    public void testBitMapOperation(){
        String redisKey2 = "test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey2,0,true);
        redisTemplate.opsForValue().setBit(redisKey2,1,true);
        redisTemplate.opsForValue().setBit(redisKey2,2,true);

        String redisKey3 = "test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey3,2,true);
        redisTemplate.opsForValue().setBit(redisKey3,3,true);
        redisTemplate.opsForValue().setBit(redisKey3,4,true);

        String redisKey4 = "test:bm:04";
        redisTemplate.opsForValue().setBit(redisKey4,4,true);
        redisTemplate.opsForValue().setBit(redisKey4,5,true);
        redisTemplate.opsForValue().setBit(redisKey4,6,true);

        String redisKey = "test:bm:or";
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(),redisKey2.getBytes(),redisKey3.getBytes(),redisKey4.getBytes());
                return redisConnection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(obj);
        for (int i = 0; i < 7; i++) {
            System.out.println(redisTemplate.opsForValue().getBit(redisKey,i));
        }
    }
}
