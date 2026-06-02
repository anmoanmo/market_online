import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { userApi } from '../api';
import type { LoginUser } from '../types';
import { message } from 'antd';

interface AuthContextType {
  user: LoginUser | null;
  token: string | null;
  login: (token: string) => void;
  logout: () => void;
  updateUser: (user: LoginUser) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<LoginUser | null>(null);
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));

  useEffect(() => {
    if (token && !user) {
      userApi.getUserInfo().then((res) => {
        if (res.code === 0) {
          setUser(res.data);
        } else if (res.code === 222222 || res.code === -1) {
          // 未登录或系统异常，不清除token，下次重试
        } else {
          logout();
        }
      }).catch(() => {
        // 网络异常不退出登录
      });
    }
  }, [token, user]);

  const login = (newToken: string) => {
    localStorage.setItem('token', newToken);
    setToken(newToken);
  };

  const logout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
    message.success('已退出登录');
  };

  const updateUser = (userData: LoginUser) => {
    setUser(userData);
  };

  return (
    <AuthContext.Provider value={{ user, token, login, logout, updateUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}