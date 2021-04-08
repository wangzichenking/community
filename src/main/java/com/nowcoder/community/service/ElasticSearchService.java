package com.nowcoder.community.service;

import com.nowcoder.community.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ElasticSearchService {

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

    public void saveDiscussPost(DiscussPost post){
        discussPostRepository.save(post);
    }
    public void deleteDiscussPost(int id){
        discussPostRepository.deleteById(id);
    }

    public List<Object> searchDiscussPost(String keyword,int currentPage,int limit){
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword,"title","content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(currentPage,limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        SearchHits<DiscussPost> search = elasticsearchTemplate.search(query,DiscussPost.class);
        //得到查询结果返回的内容
        List<SearchHit<DiscussPost>> searchHits = search.getSearchHits();
        //设置一个需要返回的实体类集合
        List<DiscussPost> discussPosts = new ArrayList<>();
        //遍历返回的内容进行处理
        for (SearchHit<DiscussPost> searchHit : searchHits) {
            //高亮部分
            Map<String,List<String>> highLightFields = searchHit.getHighlightFields();
            searchHit.getContent().setTitle(
                    highLightFields.get("title") == null ? searchHit.getContent().getTitle() : highLightFields.get("title").get(0));
            searchHit.getContent().setTitle(
                    highLightFields.get("content") == null ? searchHit.getContent().getContent() : highLightFields.get("content").get(0));
            //放到实体类中
            discussPosts.add(searchHit.getContent());
        }
        List<Object> res = new ArrayList<>();
        res.add((int)search.getTotalHits());
        res.add(discussPosts);
        return res;
    }
}
