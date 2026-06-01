import React from 'react';
import { Layout, Menu, Input, Badge, Dropdown, Avatar, Button } from 'antd';
import { ShoppingCartOutlined, UserOutlined, LogoutOutlined, ShopOutlined } from '@ant-design/icons';
import { Link, useNavigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const { Header, Content } = Layout;

const AppLayout: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [cartCount] = React.useState(0);

  const userMenuItems = [
    { key: '/user', label: '个人中心', icon: <UserOutlined /> },
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
        background: '#1a1a2e',
        boxShadow: '0 2px 12px rgba(0,0,0,0.15)',
        position: 'sticky',
        top: 0,
        zIndex: 100,
        padding: '0 24px',
        height: 64,
      }}>
        <div style={{ display: 'flex', alignItems: 'center', flex: 1 }}>
          <Link to="/" style={{ fontSize: 20, fontWeight: 700, color: '#cf1322', marginRight: 48, display: 'flex', alignItems: 'center', letterSpacing: 1 }}>
            <ShopOutlined style={{ marginRight: 8, fontSize: 24 }} />
            奇异市场
          </Link>
          <Menu
            mode="horizontal"
            selectedKeys={[location.pathname]}
            style={{ border: 'none', flex: 1, background: 'transparent' }}
            theme="dark"
            items={[
              { key: '/', label: <Link to="/" style={{ color: 'rgba(255,255,255,0.75)' }}>首页</Link> },
              { key: '/products', label: <Link to="/products" style={{ color: 'rgba(255,255,255,0.75)' }}>商品</Link> },
            ]}
          />
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
          <Input.Search placeholder="搜索商品" style={{ width: 220 }} onSearch={(value) => navigate(`/products?search=${value}`)} />

          <Link to="/cart">
            <Badge count={cartCount} size="small">
              <Button type="text" icon={<ShoppingCartOutlined style={{ fontSize: 22, color: 'rgba(255,255,255,0.75)' }} />} />
            </Badge>
          </Link>

          {user ? (
            <Dropdown menu={{ items: userMenuItems, onClick: handleUserMenuClick }} placement="bottomRight">
              <div style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
                <Avatar style={{ backgroundColor: '#cf1322', color: '#fff', boxShadow: '0 2px 8px rgba(207,19,34,0.3)' }}>
                  {user.name?.[0] || user.mail?.[0]?.toUpperCase() || 'U'}
                </Avatar>
                <span style={{ marginLeft: 8, color: 'rgba(255,255,255,0.85)' }}>{user.name || user.mail}</span>
              </div>
            </Dropdown>
          ) : (
            <Link to="/login">
              <Button type="primary" ghost style={{ borderColor: '#cf1322', color: '#cf1322' }}>登录</Button>
            </Link>
          )}
        </div>
      </Header>

      <Content style={{ background: '#f0f2f5', minHeight: 'calc(100vh - 64px)' }}>
        <Outlet />
      </Content>
    </Layout>
  );
};

export default AppLayout;
