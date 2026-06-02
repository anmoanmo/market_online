package com.market.online.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.market.online.constant.CacheKey;
import com.market.online.dto.CartItemDto;
import com.market.online.enums.BizCodeEnum;
import com.market.online.exceptions.BizException;
import com.market.online.interceptor.LoginInterceptor;
import com.market.online.model.CartDO;
import com.market.online.model.CartItemDO;
import com.market.online.model.ProductDO;
import com.market.online.service.CartService;
import com.market.online.service.ProductService;
import com.market.online.util.JsonData;
import com.market.online.vo.LoginUser;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @className: CartServiceImpl
 * @author: LZX
 * @date: 2025/2/18 10:27
 * @Version: 1.0
 * @description:
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ProductService productService;
    @Override
    /**
     * @description:添加商品进购物车
     * @author: LZX
     * @date: 2025/2/19 16:52
     * @param: [cartItemDto]
     * @return: com.market.online.util.JsonData
     **/
    public JsonData addCart(CartItemDto cartItemDto) {
        //获取前端传来的json数据
        //取出商品id
        long productId = cartItemDto.getProductId();
        //取出购买数量
        Integer buyNum = cartItemDto.getBuyNum();
        //从redis当中取出当前登录用户的购物车
        BoundHashOperations<String, Object, Object> myCart = getMyCart();
        //从购物车中取出该商品id对应的商品
        Object object = myCart.get(productId+"");
        String result = "";
        if (object != null) {
            result = (String) object;
        }
        if (StringUtils.isBlank(result)) {
            //购物车中没有该种商品
            //创建一个购物项类
            CartItemDO cartItemDO = new CartItemDO();
            //根据传来的商品id查询商品
            ProductDO byId = productService.getById(productId);
            //判断数据库中有没有该商品存在
            if (byId == null) {
                //没有该商品抛出错误
                throw new BizException(BizCodeEnum.OPS_ERROR);
            }
            //有该商品存在，为do类设置值
            cartItemDO.setProductId(productId);
            cartItemDO.setBuyNum(buyNum);
            cartItemDO.setProductImage(byId.getCoverImg());
            cartItemDO.setProductTitle(byId.getTitle());
            cartItemDO.setProductPrice(byId.getPrice());
            //将设置好的商品类添加到redis购物车当中
            myCart.put(productId+"", JSON.toJSONString(cartItemDO));
        } else {
            //已有该种商品
            //利用json工具将result转换为购物项类
           CartItemDO cartItemDO = JSON.parseObject(result, CartItemDO.class);
           //重新赋值购买数量
            cartItemDO.setBuyNum(cartItemDO.getBuyNum() + buyNum);
            //传入redis中保存数据
            myCart.put(productId+"", JSON.toJSONString(cartItemDO));
        }
        return JsonData.buildSuccess();
    }
    /**
     * @description:清空购物车
     * @author: LZX
     * @date: 2025/2/18 14:17
     * @param: []
     * @return: com.market.online.util.JsonData
     **/
    @Override
    public JsonData clearCart() {
        String myCartKey = getMyCartKey();
        stringRedisTemplate.delete(myCartKey);
        return JsonData.buildSuccess();
    }

    @Override
    public JsonData getCart() {
        //获取购物车中的商品数据
        List<CartItemDO> cartItemDOList = getCartItemList(true);
        //将获取到的商品加入购物车当中
        CartDO cartDO = new CartDO();
        cartDO.setCartItem(cartItemDOList);
        return JsonData.buildSuccess(cartDO);
    }
   private List<CartItemDO> getCartItemList(boolean b) {
           //从redis取出购物车数据
           BoundHashOperations<String, Object, Object> myCart = getMyCart();
           List<Object> values = myCart.values();
           //创建一个list集合用于储存购物车中所有商品的id
           List<Long> ids=new ArrayList<>();
           //将redis取出的数据封装成CartItemDO对象并且加入list集合当中
           List<CartItemDO> cartItemList = values.stream().map(obj -> {
               CartItemDO cartItemDO = JSON.parseObject(obj.toString(), CartItemDO.class);
               //将所有商品的id存入list集合
               Long productId = cartItemDO.getProductId();
               ids.add(productId);
               return cartItemDO;
           }).collect(Collectors.toList());

       if (b) {
           //获取最新价格
           setProductNewPrice(cartItemList, ids);

       }
        return cartItemList;
   }

   /**
    * @description:更改购物车商品的购买数量
    * @author: LZX
    * @date: 2025/2/19 16:53
    * @param: [cartItemDto]
    * @return: com.market.online.util.JsonData
    **/
    @Override
    public JsonData changeCart(CartItemDto cartItemDto) {
        //获取购物车数据
        BoundHashOperations<String, Object, Object> myCart = getMyCart();
        Object object = myCart.get(cartItemDto.getProductId()+"");
        if (object == null) {
            throw new BizException(BizCodeEnum.CART_NOT);
        }
        //重新给buyNum赋值
        CartItemDO itemDO = JSON.parseObject(object.toString(), CartItemDO.class);
        itemDO.setBuyNum(cartItemDto.getBuyNum());
        myCart.put(cartItemDto.getProductId()+"", JSON.toJSONString(itemDO));
        return JsonData.buildSuccess();
    }

    /**
     * @description:删除购物车之中的一件商品
     * @author: LZX
     * @date: 2025/2/19 16:59
     * @param: [productId]
     * @return: com.market.online.util.JsonData
     **/
    @Override
    public JsonData delCart(Long productId) {
        BoundHashOperations<String, Object, Object> myCart = getMyCart();
        myCart.delete(productId+"");
        return JsonData.buildSuccess();
    }

    /**
     * @description:获取最新价格
     * @author: LZX
     * @date: 2025/2/19 16:59
     * @param: [cartItemList, ids]
     * @return: void
     **/
    private void setProductNewPrice(List<CartItemDO> cartItemList, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<ProductDO> productDOS = productService.listByIds(ids);
        Map<Long, ProductDO> productDOMap = productDOS.stream().collect(Collectors.toMap(ProductDO::getId, Function.identity()));
        //将最新价格赋值
        cartItemList.stream().forEach(cartItemDO -> {
            //通过商品id从刚封装的map取出对应id的商品
            ProductDO aDo = productDOMap.get(cartItemDO.getProductId());
            //赋值
            cartItemDO.setProductPrice(aDo.getPrice());
            cartItemDO.setProductTitle(aDo.getTitle());
            cartItemDO.setProductImage(aDo.getCoverImg());
        });

    }
    /**
     * @description:从redis获取购物车
     * @author: LZX
     * @date: 2025/2/19 17:00
     * @param: []
     * @return: org.springframework.data.redis.core.BoundHashOperations<java.lang.String,java.lang.Object,java.lang.Object>
     **/
    private BoundHashOperations<String, Object, Object> getMyCart() {
        //获取key
        String myCartKey = getMyCartKey();
        //从redis取出购物车
        BoundHashOperations<String, Object, Object> stringObjectObjectBoundHashOperations = stringRedisTemplate.boundHashOps(myCartKey);
        return stringObjectObjectBoundHashOperations;
    }

    /**
     * @description:生成储存进redis购物车的key
     * @author: LZX
     * @date: 2025/2/19 16:59
     * @param: []
     * @return: java.lang.String
     **/
    private String getMyCartKey() {
        //获取当前登录用户
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        String format = String.format(CacheKey.CART_KEY, loginUser.getId());
        return format;
    }

    @Override
    public JsonData clearByProductIds(List<Long> productIdList) {
        BoundHashOperations<String, Object, Object> myCart = getMyCart();
        productIdList.forEach(pid -> myCart.delete(String.valueOf(pid)));
        return JsonData.buildSuccess();
    }

    @Override
    public JsonData confirmOrderCartItems(List<Long> productIdList) {
        List<CartItemDO> cartItemList = getCartItemList(true);
        List<CartItemDO> collect = cartItemList.stream().filter(obk -> {
            if (productIdList.contains(obk.getProductId())) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        return JsonData.buildSuccess(collect);
    }
}


