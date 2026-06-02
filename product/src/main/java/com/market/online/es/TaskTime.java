package com.market.online.es;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.market.online.model.ProductDO;
import com.market.online.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @className: TaskTime
 * @author: LZX
 * @date: 2025/4/19 21:22
 * @Version: 1.0
 * @description:
 */
@Component
@Slf4j
public class TaskTime {
    @Autowired
    private ProductService productService;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Scheduled(cron = "0/20 * * * * ?")
    public void taskTime(){
        try {
            //设置查询参数，避免一次性太多数据导致数据库崩溃
            int pageSize = 100;
            int pageNum = 1;
            boolean flag = true;
            while(flag){
                //分页查询最近一天创建的产品数据
                //创建分页器
                Page<ProductDO> page=new Page<>(pageNum,pageSize);
                LambdaQueryWrapper<ProductDO> wrapper=new LambdaQueryWrapper<>();
                //大于一天时间差的数据
                wrapper.ge(ProductDO::getCreateTime, LocalDateTime.now().minus(Duration.ofDays(1)));
                //分页查询
                Page<ProductDO> page1=productService.page(page,wrapper);
                //取出分页查询中的数据
                List<ProductDO> list = page1.getRecords();
                if (list.isEmpty()) {
                    //若集合中没有数据则结束循环
                    flag = false;
                } else {
                    //集合中有更新的商品信息开始循环取出
                    //查询结果中的产品id列表
                    List<Object> ids=list.stream().map(obj -> obj.getId().toString()).collect(Collectors.toList());
                    //获取es中索引库名称
                    IndexCoordinates indexCoordinatesFor = elasticsearchRestTemplate.getIndexCoordinatesFor(ProductDocument.class);
                    //es中批量查询
                    //构建批量查询条件构造器批量查询id
                    NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.termsQuery("id", ids)).build();
                    SearchHits<ProductDocument> search = elasticsearchRestTemplate.search(nativeSearchQuery, ProductDocument.class,indexCoordinatesFor);
                    //获取es批量查询的数据
                    List<ProductDocument> esDocuments=search.stream().map(SearchHit::getContent).collect(Collectors.toList());
                    Map<Long,ProductDocument> collect=esDocuments.stream().collect(Collectors.toMap(obj->obj.getId(), Function.identity()));
                    List<ProductDocument> documentList = new ArrayList<>();
                    for (ProductDO productDO : list) {
                        ProductDocument productDocument=collect.get(productDO.getId());
                        if (productDocument == null) {
                            ProductDocument p = new ProductDocument();
                            BeanUtils.copyProperties(productDO, p);
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd 'T' HH:mm:ss");
                            String format = simpleDateFormat.format(productDO.getCreateTime());
                            p.setCreateTime(format);
                            documentList.add(p);
                        }

                    }
                    if (documentList.size() > 0) {
                        elasticsearchRestTemplate.save(documentList, indexCoordinatesFor);
                    }

                    //查询下一页数据
                    pageNum++;

                }

            }
        }catch (Exception e){
            log.error("ES同步任务执行失败(若未部署ES可忽略): {}", e.getMessage());
        }
    }
}


