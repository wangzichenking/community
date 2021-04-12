package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticSearchTest {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate template;

    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList(){
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134,0,100,0));

    }

    @Test
    public void testUpdate(){
        DiscussPost post = discussPostRepository.save(discussPostMapper.selectDiscussPostById(231));
        post.setContent("老人灌水");
        discussPostRepository.save(post);
    }

    @Test
    public void testDelete(){
        discussPostRepository.deleteById(231);
    }

    @Test
    public void testDeleteAll(){
        discussPostRepository.deleteAll();
    }

    @Test
    public void testSearchByRepository(){
        /*
        SearchQuery
         */
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        SearchHits<DiscussPost> search = template.search(query,DiscussPost.class);
        List<SearchHit<DiscussPost>> searchHits = search.getSearchHits();
        List<DiscussPost> discussPosts = new ArrayList<>();
        for (SearchHit<DiscussPost> searchHit : searchHits) {
            Map<String,List<String>> highLightFields = searchHit.getHighlightFields();
            searchHit.getContent().setTitle(
                    highLightFields.get("title") == null ? searchHit.getContent().getTitle() : highLightFields.get("title").get(0));
            searchHit.getContent().setTitle(
                    highLightFields.get("content") == null ? searchHit.getContent().getContent() : highLightFields.get("content").get(0));
            discussPosts.add(searchHit.getContent());
        }
        System.out.println(discussPosts.size());
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }
    }

}
