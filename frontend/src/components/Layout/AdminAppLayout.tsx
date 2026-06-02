import React from 'react';
import { Layout, Menu, Avatar, Dropdown } from 'antd';
import { LogoutOutlined, DashboardOutlined, AppstoreOutlined, ShoppingCartOutlined, TeamOutlined, GiftOutlined, PictureOutlined } from '@ant-design/icons';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const { Header, Sider, Content } = Layout;

const AdminAppLayout: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const menuItems = [
    { key: '/admin', icon: <DashboardOutlined />, label: '控制台' },
    { key: '/admin/products', icon: <AppstoreOutlined />, label: '商品管理' },
    { key: '/admin/orders', icon: <ShoppingCartOutlined />, label: '订单管理' },
    { key: '/admin/users', icon: <TeamOutlined />, label: '用户管理' },
    { key: '/admin/coupons', icon: <GiftOutlined />, label: '优惠券管理' },
    { key: '/admin/banners', icon: <PictureOutlined />, label: '轮播图管理' },
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider width={220} style={{ background: '#1a1a2e' }}>
        <div style={{ height: 64, display: 'flex', alignItems: 'center', justifyContent: 'center', borderBottom: '1px solid rgba(255,255,255,0.06)' }}>
          <span style={{ color: '#cf1322', fontSize: 20, fontWeight: 700, letterSpacing: 2 }}>奇异市场</span>
          <span style={{ color: 'rgba(255,255,255,0.4)', fontSize: 12, marginLeft: 8 }}>管理后台</span>
        </div>
        <Menu
          mode="inline"
          theme="dark"
          selectedKeys={[location.pathname]}
          style={{ background: 'transparent', borderRight: 'none', marginTop: 8 }}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header style={{ background: '#fff', padding: '0 24px', display: 'flex', alignItems: 'center', justifyContent: 'flex-end', boxShadow: '0 1px 4px rgba(0,0,0,0.05)' }}>
          <Dropdown menu={{ items: [
            { key: 'home', label: '返回前台', icon: <AppstoreOutlined /> },
            { type: 'divider' as const },
            { key: 'logout', label: '退出登录', icon: <LogoutOutlined /> },
          ], onClick: ({ key }) => {
            if (key === 'logout') { logout(); navigate('/login'); }
            else if (key === 'home') { navigate('/'); }
          } }} placement="bottomRight">
            <div style={{ display: 'flex', alignItems: 'center', cursor: 'pointer', gap: 8 }}>
              <Avatar style={{ backgroundColor: '#cf1322', verticalAlign: 'middle' }} size="small">
                {user?.name?.[0] || 'A'}
              </Avatar>
              <span>{user?.name || '管理员'}</span>
            </div>
          </Dropdown>
        </Header>
        <Content style={{ margin: 24, background: '#f0f2f5', minHeight: 'calc(100vh - 64px - 48px)' }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default AdminAppLayout;
