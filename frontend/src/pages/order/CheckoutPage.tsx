import React, { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Card, Form, Radio, Button, Typography, Row, Col, List, Modal, message, Spin } from 'antd';
import { EnvironmentOutlined, GiftOutlined, AlipayCircleOutlined, WechatOutlined } from '@ant-design/icons';
import { addressApi, cartApi, couponApi, orderApi } from '../../api';
import { useAuth } from '../../context/AuthContext';
import { isPaymentHtml, submitPaymentHtml } from '../../utils/payment';
import type { AddressDO, CartItemVO, CouponRecordDO } from '../../types';

const { Title, Text } = Typography;

const CheckoutPage: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [addresses, setAddresses] = useState<AddressDO[]>([]);
  const [selectedAddressId, setSelectedAddressId] = useState<number | null>(null);
  const [cartItems, setCartItems] = useState<CartItemVO[]>([]);
  const [myCoupons, setMyCoupons] = useState<CouponRecordDO[]>([]);
  const [selectedCouponId, setSelectedCouponId] = useState<number | null>(null);
  const [payType, setPayType] = useState('ALIPAY');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [couponModalVisible, setCouponModalVisible] = useState(false);
  const [paymentHtml, setPaymentHtml] = useState<string | null>(null);
  const [countdown, setCountdown] = useState(5);
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const productIds: number[] = location.state?.productIds || [];

  useEffect(() => {
    if (!user) {
      message.warning('请先登录');
      navigate('/login');
      return;
    }
    if (productIds.length === 0) {
      message.warning('请选择商品');
      navigate('/cart');
      return;
    }
    fetchData();
  }, [user]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [addrRes, cartRes, couponRes] = await Promise.all([
        addressApi.list(),
        cartApi.confirmOrderCartItems(productIds),
        couponApi.pageCouponRecord(1, 100),
      ]);

      if (addrRes.code === 0) {
        setAddresses(addrRes.data || []);
        const defaultAddr = (addrRes.data || []).find((a: AddressDO) => a.defaultStatus === 1);
        setSelectedAddressId(defaultAddr?.id || (addrRes.data as AddressDO[])[0]?.id);
      }

      if (cartRes.code === 0) {
        setCartItems(cartRes.data || []);
      }

      if (couponRes.code === 0) {
        setMyCoupons((couponRes.data?.records || []).filter((c: CouponRecordDO) => c.useState === 'NEW'));
      }
    } finally {
      setLoading(false);
    }
  };

  const totalAmount = cartItems.reduce((sum, item) => sum + item.price * item.count, 0);
  const selectedCoupon = myCoupons.find(c => c.id === selectedCouponId);
  const discountAmount = selectedCoupon?.price || 0;
  const realPayAmount = Math.max(0, totalAmount - discountAmount);

  const handleSubmit = async () => {
    if (!selectedAddressId) {
      message.warning('请选择收货地址');
      return;
    }

    setSubmitting(true);
    try {
      const [tokenRes, freshCartRes] = await Promise.all([
        orderApi.getToken(),
        cartApi.confirmOrderCartItems(productIds),
      ]);
      const token = tokenRes.code === 0 ? tokenRes.data : '';

      const freshItems: CartItemVO[] = freshCartRes.code === 0 ? (freshCartRes.data || []) : cartItems;
      const freshTotal = freshItems.reduce((sum, item) => sum + item.price * item.count, 0);
      const freshCoupon = myCoupons.find(c => c.id === selectedCouponId);
      const freshDiscount = freshCoupon?.price || 0;
      const freshRealPay = Math.max(0, freshTotal - freshDiscount);

      const res = await orderApi.confirmOrder({
        couponRecordId: selectedCouponId || undefined,
        productIdList: productIds,
        payType,
        clientType: 'PC',
        addressId: selectedAddressId,
        totalAmount: freshTotal,
        realPayAmount: freshRealPay,
        token,
      });

      // 后端成功时直接返回支付宝 HTML 表单（非 JSON），失败时返回 JSON {code, msg}
      if (isPaymentHtml(res)) {
        setPaymentHtml(res);
        return;
      }

      if (typeof res === 'string') {
        message.error('订单创建失败');
        return;
      }

      if (res.code === 0) {
        const payData = res.data;
        if (isPaymentHtml(payData)) {
          setPaymentHtml(payData);
          return;
        } else {
          message.success('订单创建成功');
          navigate('/user/orders');
        }
      } else {
        message.error(res.msg || '订单创建失败');
      }
    } catch (error) {
      message.error('订单创建失败');
    } finally {
      setSubmitting(false);
    }
  };

  useEffect(() => {
    if (paymentHtml) {
      setCountdown(5);
      timerRef.current = setInterval(() => {
        setCountdown(prev => {
          if (prev <= 1) {
            if (timerRef.current) clearInterval(timerRef.current);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
      return () => {
        if (timerRef.current) clearInterval(timerRef.current);
      };
    }
  }, [paymentHtml]);

  const handlePayNow = () => {
    if (!paymentHtml || !submitPaymentHtml(paymentHtml)) {
      message.error('支付表单无效，请重新下单');
    }
  };

  useEffect(() => {
    if (countdown === 0 && paymentHtml) {
      handlePayNow();
    }
  }, [countdown]);

  const handleBackToOrders = () => {
    if (timerRef.current) clearInterval(timerRef.current);
    setPaymentHtml(null);
    navigate('/user/orders');
  };

  const formatPrice = (price: number) => `¥${price.toFixed(2)}`;

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto', padding: '24px' }}>
      <Title level={2}>确认订单</Title>

      <Row gutter={24}>
        <Col span={16}>
          <Card title={<><EnvironmentOutlined /> 收货地址</>} style={{ marginBottom: 16 }}>
            <Radio.Group
              value={selectedAddressId}
              onChange={(e) => setSelectedAddressId(e.target.value)}
              style={{ width: '100%' }}
            >
              <Row gutter={[16, 16]}>
                {addresses.map((addr) => (
                  <Col span={12} key={addr.id}>
                    <Radio value={addr.id} style={{ width: '100%' }}>
                      <Card size="small" style={{ marginTop: 8 }}>
                        <Text strong>{addr.name}</Text> <Text type="secondary">{addr.mobile}</Text>
                        <br />
                        <Text type="secondary">
                          {addr.province} {addr.city} {addr.district} {addr.address}
                        </Text>
                      </Card>
                    </Radio>
                  </Col>
                ))}
              </Row>
            </Radio.Group>
          </Card>

          <Card title="商品信息" style={{ marginBottom: 16 }}>
            <List
              dataSource={cartItems}
              renderItem={(item) => (
                <List.Item>
                  <div style={{ display: 'flex', alignItems: 'center', width: '100%' }}>
                    <img
                      src={item.productImg || 'https://via.placeholder.com/60?text=No+Image'}
                      alt={item.productName}
                      style={{ width: 60, height: 60, objectFit: 'cover', marginRight: 16 }}
                    />
                    <div style={{ flex: 1 }}>
                      <Text>{item.productName}</Text>
                    </div>
                    <div style={{ textAlign: 'right', marginRight: 24 }}>
                      <Text type="secondary">x {item.count}</Text>
                    </div>
                    <Text strong>{formatPrice(item.price * item.count)}</Text>
                  </div>
                </List.Item>
              )}
            />
          </Card>

          <Card title={<><GiftOutlined /> 优惠券</>}>
            <Button onClick={() => setCouponModalVisible(true)}>
              {selectedCoupon ? `已选择: ${selectedCoupon.couponTitle}` : '选择优惠券'}
            </Button>
            {selectedCoupon && (
              <Text type="secondary" style={{ marginLeft: 16 }}>
                减 ¥{discountAmount}
              </Text>
            )}
          </Card>
        </Col>

        <Col span={8}>
          <Card title="订单支付" style={{ position: 'sticky', top: 80 }}>
            <div style={{ marginBottom: 16 }}>
              <Text type="secondary">商品总价：</Text>
              <Text style={{ float: 'right' }}>{formatPrice(totalAmount)}</Text>
            </div>
            {discountAmount > 0 && (
              <div style={{ marginBottom: 16, color: '#ff4d4f' }}>
                <Text type="secondary">优惠券：</Text>
                <Text style={{ float: 'right' }}>-{formatPrice(discountAmount)}</Text>
              </div>
            )}
            <div style={{ marginBottom: 16, fontSize: 18 }}>
              <Text strong>实付款：</Text>
              <Text strong style={{ float: 'right', color: '#ff4d4f', fontSize: 24 }}>
                {formatPrice(realPayAmount)}
              </Text>
            </div>

            <Form.Item label="支付方式" required>
              <Radio.Group value={payType} onChange={(e) => setPayType(e.target.value)}>
                <Radio.Button value="ALIPAY">
                  <AlipayCircleOutlined /> 支付宝
                </Radio.Button>
                <Radio.Button value="WECHAT">
                  <WechatOutlined /> 微信
                </Radio.Button>
              </Radio.Group>
            </Form.Item>

            <Button
              type="primary"
              size="large"
              block
              loading={submitting}
              onClick={handleSubmit}
            >
              提交订单
            </Button>
          </Card>
        </Col>
      </Row>

      <Modal
        title="选择优惠券"
        open={couponModalVisible}
        onCancel={() => setCouponModalVisible(false)}
        footer={null}
      >
        <Radio.Group
          value={selectedCouponId}
          onChange={(e) => {
            setSelectedCouponId(e.target.value);
            setCouponModalVisible(false);
          }}
          style={{ width: '100%' }}
        >
          <Radio value={null} style={{ width: '100%', marginBottom: 8 }}>
            <Card size="small" style={{ width: '100%' }}>
              <Text>不使用优惠券</Text>
            </Card>
          </Radio>
          {myCoupons.map((coupon) => (
            <Radio key={coupon.id} value={coupon.id} style={{ width: '100%', marginBottom: 8 }}>
              <Card size="small" style={{ width: '100%' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <div>
                    <Text strong>{coupon.couponTitle}</Text>
                    <br />
                    <Text type="secondary">满 {coupon.conditionPrice} 减 {coupon.price}</Text>
                  </div>
                  <Text strong style={{ color: '#ff4d4f' }}>-¥{coupon.price}</Text>
                </div>
              </Card>
            </Radio>
          ))}
        </Radio.Group>
      </Modal>

      {paymentHtml && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: '#fff', zIndex: 9999, padding: 24, overflow: 'auto',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          <div style={{ maxWidth: 500, width: '100%', textAlign: 'center' }}>
            <Title level={3} style={{ marginBottom: 8 }}>即将跳转到支付宝支付</Title>
            <Text type="secondary" style={{ fontSize: 16 }}>支付金额：</Text>
            <div style={{ margin: '12px 0 24px' }}>
              <Text strong style={{ fontSize: 40, color: '#cf1322' }}>¥{realPayAmount.toFixed(2)}</Text>
            </div>
            <Button type="primary" size="large" block style={{ marginBottom: 12, height: 48, fontSize: 16 }}
              onClick={handlePayNow}>
              立即支付 ¥{realPayAmount.toFixed(2)}
            </Button>
            <div style={{ marginTop: 8 }}>
              <Text type="secondary">{countdown > 0 ? `${countdown}秒后自动跳转...` : '正在跳转...'}</Text>
            </div>
            <div style={{ marginTop: 16 }}>
              <Button type="link" onClick={handleBackToOrders}>返回订单列表</Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CheckoutPage;
