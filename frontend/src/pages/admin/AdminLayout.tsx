import React from 'react';
import { Layout, Menu, Avatar, Typography, Dropdown } from 'antd';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import {
  DashboardOutlined,
  ShoppingOutlined,
  OrderedListOutlined,
  UserOutlined,
  GiftOutlined,
  PictureOutlined,
  LogoutOutlined,
  ShopOutlined,
} from '@ant-design/icons';
import { useAuth } from '../../context/AuthContext';

const { Header, Sider, Content } = Layout;
const { Text } = Typography;

const AdminLayout: React.FC = () => {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const menuItems = [
    { key: '/admin', icon: <DashboardOutlined />, label: '仪表盘', path: '/' },
    { key: '/admin/products', icon: <ShoppingOutlined />, label: '商品管理' },
    { key: '/admin/orders', icon: <OrderedListOutlined />, label: '订单管理' },
    { key: '/admin/users', icon: <UserOutlined />, label: '用户管理' },
    { key: '/admin/coupons', icon: <GiftOutlined />, label: '优惠券管理' },
    { key: '/admin/banners', icon: <PictureOutlined />, label: '轮播图管理' },
  ];

  const userMenuItems = [
    { key: '/', label: '返回首页', icon: <ShopOutlined /> },
    { type: 'divider' as const },
    { key: 'logout', label: '退出登录', icon: <LogoutOutlined /> },
  ];

  const handleUserMenuClick = ({ key }: { key: string }) => {
    if (key === 'logout') {
      logout();
      navigate('/');
    } else {
      navigate(key);
    }
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        background: '#001529',
        padding: '0 24px',
      }}>
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <Link to="/admin" style={{ color: '#fff', fontSize: 18, fontWeight: 'bold' }}>
            404商城管理后台
          </Link>
        </div>

        <Dropdown menu={{ items: userMenuItems, onClick: handleUserMenuClick }} placement="bottomRight">
          <div style={{ display: 'flex', alignItems: 'center', cursor: 'pointer', color: '#fff' }}>
            <Avatar style={{ backgroundColor: '#667eea' }}>
              {user?.name?.[0] || user?.mail?.[0]?.toUpperCase() || 'A'}
            </Avatar>
            <Text style={{ marginLeft: 8, color: '#fff' }}>{user?.name || user?.mail}</Text>
          </div>
        </Dropdown>
      </Header>

      <Layout>
        <Sider width={200} style={{ background: '#fff' }}>
          <Menu
            mode="inline"
            selectedKeys={[location.pathname]}
            items={menuItems.map(item => ({
              ...item,
              label: <Link to={item.path || item.key}>{item.label}</Link>,
            }))}
          />
        </Sider>

        <Content style={{ padding: '24px', background: '#f0f2f5' }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default AdminLayout;