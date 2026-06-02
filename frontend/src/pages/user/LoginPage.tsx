import React, { useState, useEffect } from 'react';
import { Form, Input, Button, Card, message, Tabs, Divider } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined, ReloadOutlined } from '@ant-design/icons';
import { useNavigate, Link } from 'react-router-dom';
import { userApi } from '../../api';
import { useAuth } from '../../context/AuthContext';

const LoginPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('login');
  const [captchaUrl, setCaptchaUrl] = useState('');
  const [codeSending, setCodeSending] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const navigate = useNavigate();
  const { login } = useAuth();
  const [form] = Form.useForm();

  useEffect(() => {
    if (activeTab === 'register') {
      refreshCaptcha();
    }
  }, [activeTab]);

  const refreshCaptcha = () => {
    setCaptchaUrl(userApi.getCaptcha());
  };

  const onLogin = async (values: { mail: string; pwd: string }) => {
    setLoading(true);
    try {
      const res = await userApi.login(values);
      if (res.code === 0) {
        message.success('登录成功');
        login(res.data as unknown as string);
        navigate('/');
      } else {
        message.error(res.msg || '登录失败');
      }
    } catch (error) {
      message.error('登录失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  const sendCode = async () => {
    const mail = form.getFieldValue('mail');
    const captcha = form.getFieldValue('captcha');
    if (!mail) { message.warning('请先输入邮箱'); return; }
    if (!captcha) { message.warning('请先输入图片验证码'); return; }
    setCodeSending(true);
    try {
      const res = await userApi.sendCode(mail, captcha);
      if (res.code === 0) {
        message.success('验证码已发送到邮箱');
        let sec = 60;
        setCountdown(sec);
        const timer = setInterval(() => {
          sec--;
          setCountdown(sec);
          if (sec <= 0) { clearInterval(timer); setCountdown(0); }
        }, 1000);
      } else {
        message.error(res.msg || '发送失败');
      }
    } catch {
      message.error('发送失败，请稍后重试');
    } finally {
      setCodeSending(false);
    }
  };

  const onRegister = async (values: { name: string; mail: string; pwd: string; captcha: string; code: string }) => {
    setLoading(true);
    try {
      const res = await userApi.register({
        name: values.name,
        mail: values.mail,
        pwd: values.pwd,
        code: values.code,
      });
      if (res.code === 0) {
        message.success('注册成功，请登录');
        setActiveTab('login');
      } else {
        message.error(res.msg || '注册失败');
        refreshCaptcha();
      }
    } catch (error) {
      message.error('注册失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)',
      position: 'relative',
      overflow: 'hidden',
    }}>
      <div style={{
        position: 'absolute', top: -100, right: -100, width: 400, height: 400,
        borderRadius: '50%', background: 'radial-gradient(circle, rgba(207,19,34,0.08) 0%, transparent 70%)',
      }} />
      <div style={{
        position: 'absolute', bottom: -80, left: -80, width: 300, height: 300,
        borderRadius: '50%', background: 'radial-gradient(circle, rgba(207,19,34,0.06) 0%, transparent 70%)',
      }} />
      <Card style={{
        width: 420, borderRadius: 12,
        boxShadow: '0 20px 60px rgba(0,0,0,0.3)',
        border: '1px solid rgba(255,255,255,0.06)',
        background: 'rgba(255,255,255,0.96)',
        backdropFilter: 'blur(20px)',
      }}>
        <div style={{ textAlign: 'center', marginBottom: 28 }}>
          <div style={{
            width: 56, height: 56, borderRadius: 14,
            background: 'linear-gradient(135deg, #cf1322, #820014)',
            display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
            marginBottom: 16, boxShadow: '0 4px 14px rgba(207,19,34,0.3)',
          }}>
            <span style={{ color: '#fff', fontSize: 28, fontWeight: 'bold' }}>奇</span>
          </div>
          <h1 style={{ fontSize: 24, fontWeight: 700, color: '#1a1a2e', margin: 0, letterSpacing: 2 }}>
            奇异市场
          </h1>
          <p style={{ color: '#888', marginTop: 6, fontSize: 14 }}>欢迎回来</p>
        </div>
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          centered
          items={[
            {
              key: 'login',
              label: '登录',
              children: (
                <Form onFinish={onLogin} layout="vertical">
                  <Form.Item
                    name="mail"
                    rules={[{ required: true, message: '请输入邮箱' }, { type: 'email', message: '请输入有效邮箱' }]}
                  >
                    <Input prefix={<MailOutlined />} placeholder="邮箱" size="large" />
                  </Form.Item>
                  <Form.Item
                    name="pwd"
                    rules={[{ required: true, message: '请输入密码' }]}
                  >
                    <Input.Password prefix={<LockOutlined />} placeholder="密码" size="large" />
                  </Form.Item>
                  <Form.Item>
                    <Button type="primary" htmlType="submit" loading={loading} block size="large">
                      登录
                    </Button>
                  </Form.Item>
                </Form>
              ),
            },
            {
              key: 'register',
              label: '注册',
              children: (
                <Form form={form} onFinish={onRegister} layout="vertical">
                  <Form.Item
                    name="name"
                    label="昵称"
                    rules={[{ required: true, message: '请输入昵称' }]}
                  >
                    <Input prefix={<UserOutlined />} placeholder="昵称" size="large" />
                  </Form.Item>
                  <Form.Item
                    name="mail"
                    rules={[{ required: true, message: '请输入邮箱' }, { type: 'email', message: '请输入有效邮箱' }]}
                  >
                    <Input prefix={<MailOutlined />} placeholder="邮箱" size="large" />
                  </Form.Item>
                  <Form.Item
                    name="pwd"
                    rules={[{ required: true, message: '请输入密码' }, { min: 6, message: '密码至少6位' }]}
                  >
                    <Input.Password prefix={<LockOutlined />} placeholder="密码" size="large" />
                  </Form.Item>
                  <Form.Item label="图片验证码" required>
                    <div style={{ display: 'flex', gap: 8 }}>
                      <Form.Item name="captcha" noStyle rules={[{ required: true, message: '请输入图片验证码' }]}>
                        <Input placeholder="图片验证码" size="large" />
                      </Form.Item>
                      <img
                        src={captchaUrl}
                        alt="验证码"
                        style={{ width: 100, height: 40, cursor: 'pointer', border: '1px solid #d9d9d9', borderRadius: 4 }}
                        onClick={refreshCaptcha}
                      />
                      <Button icon={<ReloadOutlined />} onClick={refreshCaptcha} size="large" />
                    </div>
                  </Form.Item>
                  <Form.Item label="邮箱验证码" required>
                    <div style={{ display: 'flex', gap: 8 }}>
                      <Form.Item name="code" noStyle rules={[{ required: true, message: '请输入邮箱验证码' }]}>
                        <Input placeholder="邮箱验证码" size="large" style={{ width: 180 }} />
                      </Form.Item>
                      <Button
                        size="large"
                        disabled={countdown > 0}
                        loading={codeSending}
                        onClick={sendCode}
                      >
                        {countdown > 0 ? `${countdown}s` : '获取验证码'}
                      </Button>
                    </div>
                  </Form.Item>
                  <Form.Item>
                    <Button type="primary" htmlType="submit" loading={loading} block size="large">
                      注册
                    </Button>
                  </Form.Item>
                </Form>
              ),
            },
          ]}
        />
        <Divider plain style={{ margin: '16px 0' }}>
          <Link to="/" style={{ color: '#999' }}>返回首页</Link>
        </Divider>
      </Card>
    </div>
  );
};

export default LoginPage;
