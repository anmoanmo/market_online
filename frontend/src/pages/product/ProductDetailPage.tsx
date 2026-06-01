import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Row, Col, Card, Typography, Button, InputNumber, message, Spin, Breadcrumb } from 'antd';
import { ShoppingCartOutlined, ThunderboltOutlined } from '@ant-design/icons';
import { productApi, cartApi } from '../../api';
import { useAuth } from '../../context/AuthContext';
import type { ProductVO } from '../../types';

const { Title, Text, Paragraph } = Typography;

const ProductDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [product, setProduct] = useState<ProductVO | null>(null);
  const [loading, setLoading] = useState(true);
  const [count, setCount] = useState(1);
  const [adding, setAdding] = useState(false);

  useEffect(() => {
    if (id) {
      fetchProduct();
    }
  }, [id]);

  const fetchProduct = async () => {
    setLoading(true);
    try {
      const res = await productApi.getDetail(Number(id));
      if (res.code === 0) {
        setProduct(res.data);
      } else {
        message.error('商品不存在');
        navigate('/products');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = async () => {
    if (!user) {
      message.warning('请先登录');
      navigate('/login');
      return;
    }

    setAdding(true);
    try {
      const res = await cartApi.addCart({ productId: product!.id, buyNum: count });
      if (res.code === 0) {
        message.success('加入购物车成功');
      } else {
        message.error(res.msg || '加入购物车失败');
      }
    } finally {
      setAdding(false);
    }
  };

  const handleBuyNow = async () => {
    if (!user) {
      message.warning('请先登录');
      navigate('/login');
      return;
    }

    setAdding(true);
    try {
      await cartApi.addCart({ productId: product!.id, buyNum: count });
      navigate('/cart');
    } finally {
      setAdding(false);
    }
  };

  const formatPrice = (price: number) => `¥${price.toFixed(2)}`;

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!product) {
    return null;
  }

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto', padding: '24px' }}>
      <Breadcrumb
        items={[
          { title: <a href="/">首页</a> },
          { title: <a href="/products">商品</a> },
          { title: product.title },
        ]}
        style={{ marginBottom: 24 }}
      />

      <Row gutter={32}>
        <Col xs={24} md={12}>
          <Card cover={
            <img
              src={product.coverImg || 'https://via.placeholder.com/400x400?text=No+Image'}
              alt={product.title}
              style={{ width: '100%', height: 400, objectFit: 'cover' }}
            />
          } />
        </Col>
        <Col xs={24} md={12}>
          <div style={{ padding: '0 0 24px 0' }}>
            <Title level={2}>{product.title}</Title>

            <div style={{ background: '#f5f5f5', padding: 16, marginBottom: 24 }}>
              <Row gutter={16}>
                <Col span={12}>
                  <Text type="secondary">原价</Text>
                  <div>
                    <Text delete style={{ fontSize: 18 }}>{formatPrice(product.oldPrice)}</Text>
                  </div>
                </Col>
                <Col span={12}>
                  <Text type="secondary">现价</Text>
                  <div>
                    <Text strong style={{ fontSize: 28, color: '#ff4d4f' }}>{formatPrice(product.price)}</Text>
                  </div>
                </Col>
              </Row>
            </div>

            <div style={{ marginBottom: 24 }}>
              <Text type="secondary">库存: </Text>
              <Text>{product.stock} 件</Text>
            </div>

            <div style={{ marginBottom: 24 }}>
              <Text type="secondary">数量: </Text>
              <InputNumber
                min={1}
                max={product.stock}
                value={count}
                onChange={(value) => setCount(value || 1)}
                style={{ marginLeft: 8 }}
              />
            </div>

            <div style={{ display: 'flex', gap: 16 }}>
              <Button
                type="primary"
                size="large"
                icon={<ShoppingCartOutlined />}
                onClick={handleAddToCart}
                loading={adding}
                style={{ flex: 1 }}
              >
                加入购物车
              </Button>
              <Button
                type="default"
                size="large"
                icon={<ThunderboltOutlined />}
                onClick={handleBuyNow}
                loading={adding}
                style={{ flex: 1 }}
              >
                立即购买
              </Button>
            </div>
          </div>
        </Col>
      </Row>

      <Card title="商品详情" style={{ marginTop: 24 }}>
        <Paragraph style={{ whiteSpace: 'pre-wrap', marginBottom: 0 }}>
          {product.detail || '暂无商品详情'}
        </Paragraph>
      </Card>
    </div>
  );
};

export default ProductDetailPage;
