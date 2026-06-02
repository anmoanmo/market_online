import React, { useEffect, useState } from 'react';
import { Row, Col, Card, Statistic, Typography, Spin } from 'antd';
import { useNavigate, Link } from 'react-router-dom';
import {
  ShoppingOutlined,
  UserOutlined,
  GiftOutlined,
  OrderedListOutlined,
} from '@ant-design/icons';
import { productApi, orderApi, userApi, couponApi } from '../../api';

const { Title } = Typography;

const DashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    productCount: 0,
    userCount: 0,
    couponCount: 0,
    orderCount: 0,
  });

  useEffect(() => {
    Promise.all([
      productApi.pageProduct(1, 1).catch(() => null),
      userApi.adminListUsers(1, 1).catch(() => null),
      couponApi.pageCoupon(1, 1).catch(() => null),
      orderApi.adminPageOrder(1, 1).catch(() => null),
    ]).then(([p, u, c, o]) => {
      setStats({
        productCount: (p as any)?.data?.total || 0,
        userCount: (u as any)?.data?.total || 0,
        couponCount: (c as any)?.data?.total || 0,
        orderCount: (o as any)?.data?.total || 0,
      });
    }).finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <div style={{ textAlign: 'center', padding: 100 }}><Spin size="large" /></div>;
  }

  const statistics = [
    { title: '商品总数', value: stats.productCount, icon: <ShoppingOutlined style={{ fontSize: 36, color: '#cf1322' }} />, color: '#cf1322' },
    { title: '用户总数', value: stats.userCount, icon: <UserOutlined style={{ fontSize: 36, color: '#1a1a2e' }} />, color: '#1a1a2e' },
    { title: '优惠券总数', value: stats.couponCount, icon: <GiftOutlined style={{ fontSize: 36, color: '#52c41a' }} />, color: '#52c41a' },
    { title: '订单总数', value: stats.orderCount, icon: <OrderedListOutlined style={{ fontSize: 36, color: '#faad14' }} />, color: '#faad14' },
  ];

  const quickLinks = [
    { label: '商品管理', path: '/admin/products' },
    { label: '订单管理', path: '/admin/orders' },
    { label: '用户管理', path: '/admin/users' },
    { label: '优惠券管理', path: '/admin/coupons' },
    { label: '轮播图管理', path: '/admin/banners' },
  ];

  return (
    <div>
      <Title level={3}>仪表盘</Title>

      <Row gutter={[16, 16]}>
        {statistics.map((stat, i) => (
          <Col xs={24} sm={12} lg={6} key={i}>
            <Card hoverable onClick={() => navigate(quickLinks[i].path)} style={{ cursor: 'pointer' }}>
              <Row gutter={16} align="middle">
                <Col span={12}>
                  <Statistic title={stat.title} value={stat.value} valueStyle={{ color: stat.color, fontWeight: 700 }} />
                </Col>
                <Col span={12} style={{ textAlign: 'right' }}>{stat.icon}</Col>
              </Row>
            </Card>
          </Col>
        ))}
      </Row>

      <Row gutter={16} style={{ marginTop: 24 }}>
        <Col span={12}>
          <Card title="快捷操作">
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {quickLinks.map((link) => (
                <Link to={link.path} key={link.path}
                  style={{ color: '#cf1322', fontSize: 15, padding: '6px 0', borderBottom: '1px solid #f0f0f0', display: 'flex', justifyContent: 'space-between' }}>
                  <span>{link.label}</span>
                  <span>→</span>
                </Link>
              ))}
            </div>
          </Card>
        </Col>
        <Col span={12}>
          <Card title="系统信息">
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              <p><strong>系统版本：</strong>1.0.0</p>
              <p><strong>技术栈：</strong>React + Spring Cloud</p>
              <p><strong>后端服务：</strong>奇异市场微服务</p>
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default DashboardPage;