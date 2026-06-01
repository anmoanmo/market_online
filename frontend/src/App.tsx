import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import { AuthProvider } from './context/AuthContext';
import AppLayout from './components/Layout/AppLayout';
import AdminAppLayout from './components/Layout/AdminAppLayout';
import HomePage from './pages/home/HomePage';
import LoginPage from './pages/user/LoginPage';
import ProductListPage from './pages/product/ProductListPage';
import ProductDetailPage from './pages/product/ProductDetailPage';
import CartPage from './pages/cart/CartPage';
import CheckoutPage from './pages/order/CheckoutPage';
import UserLayout from './pages/user/UserLayout';
import UserInfoPage from './pages/user/UserInfoPage';
import AddressPage from './pages/user/AddressPage';
import OrderListPage from './pages/user/OrderListPage';
import OrderDetailPage from './pages/user/OrderDetailPage';
import CouponListPage from './pages/user/CouponListPage';
import DashboardPage from './pages/admin/DashboardPage';
import AdminProductPage from './pages/admin/AdminProductPage';
import AdminOrderPage from './pages/admin/AdminOrderPage';
import AdminUserPage from './pages/admin/AdminUserPage';
import AdminCouponPage from './pages/admin/AdminCouponPage';
import AdminBannerPage from './pages/admin/AdminBannerPage';

const AdminGuard: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user } = useAuth();
  if (!user || user.admin !== 1) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
};

const UserOrAdminRoute: React.FC = () => {
  const { user } = useAuth();
  if (user?.admin === 1) {
    return <Navigate to="/admin" replace />;
  }
  return <AppLayout />;
};

const App: React.FC = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<UserOrAdminRoute />}>
            <Route index element={<HomePage />} />
            <Route path="products" element={<ProductListPage />} />
            <Route path="product/:id" element={<ProductDetailPage />} />
            <Route path="cart" element={<CartPage />} />
            <Route path="checkout" element={<CheckoutPage />} />
            <Route path="login" element={<LoginPage />} />

            <Route path="user" element={<UserLayout />}>
              <Route index element={<UserInfoPage />} />
              <Route path="address" element={<AddressPage />} />
              <Route path="orders" element={<OrderListPage />} />
              <Route path="orders/:outTradeNo" element={<OrderDetailPage />} />
              <Route path="coupons" element={<CouponListPage />} />
            </Route>
          </Route>

          <Route path="/admin" element={<AdminGuard><AdminAppLayout /></AdminGuard>}>
            <Route index element={<DashboardPage />} />
            <Route path="products" element={<AdminProductPage />} />
            <Route path="orders" element={<AdminOrderPage />} />
            <Route path="orders/:outTradeNo" element={<OrderDetailPage />} />
            <Route path="users" element={<AdminUserPage />} />
            <Route path="coupons" element={<AdminCouponPage />} />
            <Route path="banners" element={<AdminBannerPage />} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
};

export default App;