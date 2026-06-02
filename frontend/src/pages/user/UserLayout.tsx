import React from 'react';
import { Layout, Menu, Avatar, Typography } from 'antd';
import { Link, Outlet, useLocation } from 'react-router-dom';
import { UserOutlined, EnvironmentOutlined, OrderedListOutlined, GiftOutlined } from '@ant-design/icons';
import { useAuth } from '../../context/AuthContext';

const { Sider, Content } = Layout;
const { Text } = Typography;

const UserLayout: React.FC = () => {
  const { user } = useAuth();
  const location = useLocation();

  const menuItems = [
    { key: '/user', icon: <UserOutlined />, label: '个人信息' },
    { key: '/user/address', icon: <EnvironmentOutlined />, label: '地址管理' },
    { key: '/user/orders', icon: <OrderedListOutlined />, label: '我的订单' },
    { key: '/user/coupons', icon: <GiftOutlined />, label: '我的优惠券' },
  ];

  return (
    <Layout style={{ minHeight: 'calc(100vh - 64px)' }}>
      <Sider width={220} style={{ background: '#fff', borderRight: '1px solid #f0f0f0' }}>
        <div style={{ padding: '32px 16px 24px', textAlign: 'center' }}>
          <Avatar size={72} style={{ backgroundColor: '#cf1322', color: '#fff', fontSize: 28, boxShadow: '0 4px 12px rgba(207,19,34,0.2)' }}>
            {user?.name?.[0] || user?.mail?.[0]?.toUpperCase() || 'U'}
          </Avatar>
          <div style={{ marginTop: 12 }}>
            <Text strong style={{ fontSize: 16 }}>{user?.name || user?.mail}</Text>
          </div>
          <Text type="secondary" style={{ fontSize: 12 }}>{user?.mail}</Text>
        </div>
        <Menu
          mode="inline"
          selectedKeys={[location.pathname]}
          style={{ borderRight: 'none', background: 'transparent' }}
          items={menuItems.map(item => ({
            ...item,
            label: <Link to={item.key}>{item.label}</Link>,
          }))}
        />
        <div style={{ padding: '16px', borderTop: '1px solid #f0f0f0' }}>
          <Link to="/">
            <Text type="secondary" style={{ fontSize: 12 }}>← 返回首页</Text>
          </Link>
        </div>
      </Sider>
      <Content style={{ padding: '24px', background: '#fff' }}>
        <Outlet />
      </Content>
    </Layout>
  );
};

export default UserLayout;