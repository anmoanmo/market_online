package com.buka.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buka.config.RabbitMQConfig;
import com.buka.enums.BizCodeEnum;
import com.buka.enums.ProductOrderStateEnum;
import com.buka.enums.StockTaskStateEnum;
import com.buka.es.ProductDocument;
import com.buka.exceptions.BizException;
import com.buka.feign.ProductOrderFeignService;
import com.buka.model.ProductDO;
import com.buka.mapper.ProductMapper;
import com.buka.model.ProductTaskDO;
import com.buka.mq.ProductMessage;
import com.buka.request.LockProductRequest;
import com.buka.request.OrderItemRequest;
import com.buka.service.ProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buka.service.ProductTaskService;
import com.buka.util.JsonData;
import com.buka.vo.ProductVO;
import com.mysql.cj.protocol.Message;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LZX
 * @since 2025-02-17
 */
@Service
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, ProductDO> implements ProductService {
    @Autowired
    private ProductTaskService productTaskService;
    @Autowired
    private RabbitMQConfig rabbitMQConfig;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ProductOrderFeignService productOrderFeignService;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * @description:分页查看商品
     * @author: LZX
     * @date: 2025/2/19 16:54
     * @param: [page, size]
     * @return: com.buka.util.JsonData
     **/
    @Override
    public JsonData pageProduct(Long page, Long size) {
        //设置分页
        Page<ProductDO> productPage = new Page<>(page, size);
        this.page(productPage);
        //获取页数和总条数
        long pages = productPage.getPages();
        long total = productPage.getTotal();
        //利用stream流复制将do类的数据复制到vo类当中
        List<ProductDO> productPageRecords = productPage.getRecords();
        List<ProductVO> collect = productPageRecords.stream().map(obj -> {
            ProductVO productVO = new ProductVO();
            BeanUtils.copyProperties(obj, productVO);
            productVO.setOldPrice(obj.getOldPrice());
            productVO.setPrice(obj.getPrice());
            return productVO;
        }).collect(Collectors.toList());
        //将三个数据封装成map返回
        Map<String, Object> map = new HashMap<>();
        map.put("total", total);
        map.put("records", collect);
        map.put("pages", pages);
        return JsonData.buildSuccess(map);
    }

    /**
     * @description:展示商品详情
     * @author: LZX
     * @date: 2025/2/19 16:54
     * @param: [productId]
     * @return: com.buka.util.JsonData
     **/
    @Override
    public JsonData detailProduct(Long productId) {
        ProductDO byId = this.getById(productId);
        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(byId, productVO);
        productVO.setOldPrice(byId.getOldPrice());
        productVO.setPrice(byId.getPrice());
        productVO.setStock(byId.getStock()-byId.getLockStock());
        return JsonData.buildSuccess(productVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JsonData lockProduct(LockProductRequest lockProductRequest) {
        String orderOutTradeNo = lockProductRequest.getOrderOutTradeNo();
        List<OrderItemRequest> orderItemRequest = lockProductRequest.getOrderItemRequest();
        for (OrderItemRequest orderItemRequest1 : orderItemRequest) {
            ProductDO productDO = getById(orderItemRequest1.getProductId());
            LambdaUpdateWrapper<ProductDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.set(ProductDO::getLockStock, productDO.getLockStock() + orderItemRequest1.getBuyNum());
            lambdaUpdateWrapper.eq(ProductDO::getId, orderItemRequest1.getProductId());
            boolean flag = update(lambdaUpdateWrapper);
            if (!flag) {
                throw new BizException(BizCodeEnum.STOCK_LOCK_FAIL);
            }
            ProductTaskDO productTaskDO = new ProductTaskDO();
            productTaskDO.setProductId(orderItemRequest1.getProductId());
            productTaskDO.setProductName(productDO.getTitle());
            productTaskDO.setBuyNum(orderItemRequest1.getBuyNum());
            productTaskDO.setLockState(StockTaskStateEnum.LOCK.name());
            productTaskDO.setCreateTime(new Date());
            productTaskDO.setOutTradeNo(orderOutTradeNo);
            productTaskService.save(productTaskDO);
            ProductMessage productMessage=new ProductMessage();
            productMessage.setOutTradeNo(orderOutTradeNo);
            productMessage.setTaskId(productTaskDO.getId());
            rabbitTemplate.convertAndSend(rabbitMQConfig.getEventExchange(),rabbitMQConfig.getStockReleaseDelayRoutingKey(),productMessage);

        }
        return JsonData.buildSuccess();
    }


    //添加商品信息并同步到es数据库中
    @Override
    public JsonData addProduct(ProductDO productDO) {
        //设置商品的创建时间和锁定库存数量
        productDO.setCreateTime(new Date());
        productDO.setLockStock(0);
        //保存商品信息
        save(productDO);
        //同步信息至es数据库
        saveES(productDO);

        return JsonData.buildSuccess();
    }

    @Override
    public JsonData updateProduct(ProductDO productDO) {
        if (productDO == null || productDO.getId() == null) {
            return JsonData.buildResult(BizCodeEnum.OPS_ERROR);
        }
        updateById(productDO);
        return JsonData.buildSuccess();
    }

    @Override
    public JsonData deleteProduct(Long productId) {
        removeById(productId);
        return JsonData.buildSuccess();
    }

    @Async//异步处理,异步执行，不会阻塞主线程
    public void saveES(ProductDO productDO) {
        try {
            ProductDocument productDocument = new ProductDocument();
            BeanUtils.copyProperties(productDO, productDocument);
            //设置时间戳格式
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd 'T' HH:mm:ss");
            String format = simpleDateFormat.format(productDO.getCreateTime());
            productDocument.setCreateTime(format);
            //将商品信息保存至es当中
            elasticsearchRestTemplate.save(productDocument);
        }catch (Exception e) {
            log.error("同步商品失败,productId={}", productDO.getId(), e);
        }


    }

    @Override
    public JsonData searchProducts(String keyword, BigDecimal minPrice, BigDecimal maxPrice) {
        try {
            BoolQueryBuilder builder = new BoolQueryBuilder();
            if (keyword != null && !keyword.isEmpty()) {
                builder.must(QueryBuilders.matchQuery("title", keyword));
            }
            if (minPrice != null) {
                builder.filter(QueryBuilders.rangeQuery("amount").gte(minPrice.doubleValue()));
            }
            if (maxPrice != null) {
                builder.filter(QueryBuilders.rangeQuery("amount").lte(maxPrice.doubleValue()));
            }
            HighlightBuilder highlightBuilder = new HighlightBuilder().field("title").preTags("<em>").postTags("</em>");
            NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(builder).withHighlightBuilder(highlightBuilder).build();
            SearchHits<ProductDocument> search = elasticsearchRestTemplate.search(searchQuery, ProductDocument.class);
            List<ProductDocument> documents = search.stream().map(obj -> {
                ProductDocument productDocument = obj.getContent();
                List<String> highlights = obj.getHighlightFields().get("title");
                if (highlights != null && !highlights.isEmpty()) {
                    productDocument.setTitle(highlights.get(0));
                }
                return productDocument;
            }).collect(Collectors.toList());
            return JsonData.buildSuccess(documents);
        } catch (Exception e) {
            log.warn("ES搜索失败，降级为MySQL搜索: {}", e.getMessage());
            LambdaQueryWrapper<ProductDO> wrapper = new LambdaQueryWrapper<>();
            if (keyword != null && !keyword.isEmpty()) {
                wrapper.like(ProductDO::getTitle, keyword);
            }
            if (minPrice != null) {
                wrapper.ge(ProductDO::getPrice, minPrice);
            }
            if (maxPrice != null) {
                wrapper.le(ProductDO::getPrice, maxPrice);
            }
            List<ProductDO> list = list(wrapper);
            return JsonData.buildSuccess(list);
        }
    }

    @Override
    public boolean releaseProductStock(ProductMessage productMessage) {
        //查询工作单是否存在
        Long taskId = productMessage.getTaskId();
        ProductTaskDO taskDO = productTaskService.getById(taskId);
        if (taskDO == null) {
            log.error("工作单不存在");
        }
        //工作单存在
        //检查工作单的状态
        if (taskDO.getLockState().equalsIgnoreCase(StockTaskStateEnum.LOCK.name())) {
            //工作单状态为LOCK
            //远程调用查询该订单状态
            JsonData jsonData = productOrderFeignService.queryProductOrderState(productMessage.getOutTradeNo());
            //判断查询结果是否成功
            if (jsonData.getCode() == 0) {
                String string = jsonData.getData().toString();
                //如果订单状态为NEW代表未支付需要重新投递该消息
                if (string.equalsIgnoreCase(ProductOrderStateEnum.NEW.name())) {
                    log.warn("订单状态是NEW，重新投递:{}", productMessage);
                    return false;
                }
                //若订单状态为PAY则代表已经支付，不需要回滚库存
                if (string.equalsIgnoreCase(ProductOrderStateEnum.PAY.name())) {
                    taskDO.setLockState(StockTaskStateEnum.FINISH.name());
                    productTaskService.updateById(taskDO);
                    return true;
                }

            }
            //如果订单不存在或者已经取消则需要回滚库存
            ProductDO productDO = getById(taskDO.getProductId());
            LambdaUpdateWrapper<ProductDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(ProductDO::getId, taskDO.getProductId());
            lambdaUpdateWrapper.set(ProductDO::getLockStock, productDO.getLockStock() - taskDO.getBuyNum());
            update(lambdaUpdateWrapper);
            //修改任务状态
            taskDO.setLockState(StockTaskStateEnum.CANCEL.name());
            productTaskService.updateById(taskDO);
            return true;
        }

        return false;
    }
}
