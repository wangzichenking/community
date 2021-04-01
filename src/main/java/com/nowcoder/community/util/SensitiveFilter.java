package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符
    private static final String REPLACE = "***";

    //初始化root
    private TrieNode root = new TrieNode();


    /*
    @PostConstruct注解：会在服务器加载servlet的时候运行，并且只会被服务器执行一次
     */
    @PostConstruct
    public void init(){
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                ){
            String keyword;
            while ((keyword = reader.readLine()) != null){
                //添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (Exception e) {
            LOGGER.error("加载敏感词文件异常："+e.getMessage());
        }
    }

    //将敏感词添加到前缀树中
    private void addKeyword(String keyword) {

        TrieNode tempNode = root;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null){
                //初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            //指针指向子节点，进入下一轮循环
            tempNode = subNode;
            //设置结束标识
            if (i == keyword.length() - 1){
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     * @param text 待过滤文本
     * @return 过滤后的文本
     */
    public String filter(String text){
        if (StringUtils.isBlank(text)){
            return null;
        }

        //指针1
        TrieNode tempNode = root;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //结果
        StringBuilder stringBuilder = new StringBuilder();

        while (position < text.length()){
            char c = text.charAt(position);
            //跳过符号
            if (isSymbol(c)){
                //若指针1处于root，将此符号计入结果，让指针2向下走一步
                if (tempNode == root){
                    stringBuilder.append(c);
                    begin++;
                }
                //无论符号在开头或中间，指针3都向下走一步
                position++;
                continue;
            }
            //检测下一层节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null){
                //以begin开头的字符串不是敏感词
                stringBuilder.append(text.charAt(begin));
                //进入下一个位置
                position = ++begin;
                //重新指向root
                tempNode = root;
            }else if (tempNode.isKeyWordEnd()){
                //发现敏感词，将begin到position这段的字符串替换掉
                stringBuilder.append(REPLACE);
                //进入下一个位置
                begin = ++position;
                //重新指向root
                tempNode = root;
            }else {
                //检查下一个字符
                position++;
            }
        }
        //将最后一批字符计入结果
        stringBuilder.append(text.substring(begin));
        return stringBuilder.toString();
    }

    /*
    判断是否为符号
     */
    private boolean isSymbol(Character c){
        //0x2E80~0x9FFF 东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    /*
    前缀树信息
     */
    private class TrieNode{

        //关键词结束标识
        private boolean isKeyWordEnd = false;

        //子节点(key是下级字符，value是下级节点)
        private Map<Character,TrieNode> subNodes = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        //添加子节点
        public void addSubNode(Character c,TrieNode node){
            subNodes.put(c,node);
        }
        //获取子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }
}
