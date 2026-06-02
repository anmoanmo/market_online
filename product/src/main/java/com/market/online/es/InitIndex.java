package com.market.online.es;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;

import javax.annotation.PostConstruct;


@Configuration
@Slf4j
public class InitIndex {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @PostConstruct
    public void initIndex(){
        log.info("初始化索引");
        try {
            IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(ProductDocument.class);
            if (!indexOperations.exists()){
                indexOperations.create();
                indexOperations.putMapping(indexOperations.createMapping());
            }
        }catch (Exception e){
            log.error("初始化ES索引失败(若未部署ES可忽略): {}", e.getMessage());
        }
    }
}
