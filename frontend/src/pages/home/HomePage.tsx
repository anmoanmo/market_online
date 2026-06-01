import React, { useEffect, useState } from 'react';
import { Carousel, Card, Row, Col, Typography, Spin, Button } from 'antd';
import { Link } from 'react-router-dom';
import { RightOutlined, ShoppingOutlined, RocketOutlined, SafetyOutlined } from '@ant-design/icons';
import { productApi } from '../../api';
import type { BannerDO, ProductVO } from '../../types';

const { Title, Text } = Typography;

const HomePage: React.FC = () => {
  const [banners, setBanners] = useState<BannerDO[]>([]);
  const [products, setProducts] = useState<ProductVO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      productApi.listBanners(),
      productApi.pageProduct(1, 8),
    ]).then(([bannerRes, productRes]) => {
      if (bannerRes.code === 0) setBanners(bannerRes.data || []);
      if (productRes.code === 0) setProducts(productRes.data?.records || []);
    }).finally(() => setLoading(false));
  }, []);

  const formatPrice = (price: number) => `¥${price.toFixed(2)}`;

  if (loading) return <div style={{ textAlign: 'center', padding: 80 }}><Spin size="large" /></div>;

  return (
    <div>
      {banners.length > 0 && (
        <Carousel autoplay>
          {banners.map((banner) => (
            <div key={banner.id}>
              <Link to={banner.link || '/'}>
                <div style={{ width: '100%', height: 420, overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#1a1a2e' }}>
                  <img src={banner.img} alt={banner.name || 'banner'} style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
                </div>
              </Link>
            </div>
          ))}
        </Carousel>
      )}

      <div style={{ maxWidth: 1200, margin: '0 auto', padding: '48px 24px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 }}>
          <Title level={2} style={{ margin: 0 }}>
            <span style={{ borderLeft: '4px solid #cf1322', paddingLeft: 12 }}>热门商品</span>
          </Title>
          <Link to="/products">
            <Button type="link" style={{ color: '#cf1322' }}>查看更多 <RightOutlined /></Button>
          </Link>
        </div>

        <Row gutter={[20, 20]}>
          {products.map((product) => (
            <Col xs={24} sm={12} md={8} lg={6} key={product.id}>
              <Link to={`/product/${product.id}`}>
                <Card
                  hoverable
                  cover={
                    <div style={{ height: 200, overflow: 'hidden' }}>
                      <img
                        alt={product.title}
                        src={product.coverImg || 'https://via.placeholder.com/300x200?text=No+Image'}
                        style={{ height: '100%', width: '100%', objectFit: 'cover', transition: 'transform 0.3s' }}
                      />
                    </div>
                  }
                  style={{ height: '100%', borderRadius: 12, overflow: 'hidden' }}
                >
                  <Card.Meta
                    title={<span style={{ fontSize: 15 }}>{product.title}</span>}
                    description={
                      <div>
                        <Text delete style={{ fontSize: 13, color: '#999', marginRight: 8 }}>
                          {formatPrice(product.oldPrice)}
                        </Text>
                        <Text strong style={{ fontSize: 20, color: '#cf1322' }}>
                          {formatPrice(product.price)}
                        </Text>
                        <div style={{ marginTop: 8 }}>
                          <Text type="secondary" style={{ fontSize: 12 }}>库存: {product.stock}</Text>
                        </div>
                      </div>
                    }
                  />
                </Card>
              </Link>
            </Col>
          ))}
        </Row>
      </div>

      <div style={{ background: '#1a1a2e', padding: '60px 0' }}>
        <div style={{ maxWidth: 1200, margin: '0 auto', padding: '0 24px' }}>
          <Row gutter={48}>
            <Col xs={24} sm={8}>
              <div style={{ textAlign: 'center', color: '#fff' }}>
                <ShoppingOutlined style={{ fontSize: 48, color: '#cf1322', marginBottom: 16 }} />
                <Title level={4} style={{ color: '#fff' }}>精选商品</Title>
                <Text style={{ color: 'rgba(255,255,255,0.5)' }}>品质好物，放心购</Text>
              </div>
            </Col>
            <Col xs={24} sm={8}>
              <div style={{ textAlign: 'center', color: '#fff' }}>
                <RocketOutlined style={{ fontSize: 48, color: '#cf1322', marginBottom: 16 }} />
                <Title level={4} style={{ color: '#fff' }}>快速配送</Title>
                <Text style={{ color: 'rgba(255,255,255,0.5)' }}>下单即发货</Text>
              </div>
            </Col>
            <Col xs={24} sm={8}>
              <div style={{ textAlign: 'center', color: '#fff' }}>
                <SafetyOutlined style={{ fontSize: 48, color: '#cf1322', marginBottom: 16 }} />
                <Title level={4} style={{ color: '#fff' }}>安全支付</Title>
                <Text style={{ color: 'rgba(255,255,255,0.5)' }}>多种支付方式</Text>
              </div>
            </Col>
          </Row>
        </div>
      </div>
    </div>
  );
};

export default HomePage;
