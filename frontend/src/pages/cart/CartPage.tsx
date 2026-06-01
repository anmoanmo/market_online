import React, { useEffect, useState } from 'react';
import { Table, Button, InputNumber, Card, Typography, Row, Col, Checkbox, message, Popconfirm, Empty } from 'antd';
import { DeleteOutlined, ShoppingOutlined } from '@ant-design/icons';
import { Link, useNavigate } from 'react-router-dom';
import { cartApi } from '../../api';
import { useAuth } from '../../context/AuthContext';
import type { CartItemVO } from '../../types';

const { Title, Text } = Typography;

const CartPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [cartItems, setCartItems] = useState<CartItemVO[]>([]);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) {
      message.warning('请先登录');
      navigate('/login');
      return;
    }
    fetchCart();
  }, [user]);

  const fetchCart = async () => {
    setLoading(true);
    try {
      const res = await cartApi.getCart();
      if (res.code === 0) {
        setCartItems(res.data?.cartItem || []);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateCount = async (productId: number, count: number) => {
    const res = await cartApi.updateCart({ productId, buyNum: count });
    if (res.code === 0) {
      setCartItems(items =>
        items.map(item =>
          item.productId === productId ? { ...item, count } : item
        )
      );
    }
  };

  const handleDelete = async (productId: number) => {
    const res = await cartApi.deleteCartItem(productId);
    if (res.code === 0) {
      setCartItems(items => items.filter(item => item.productId !== productId));
      setSelectedIds(ids => ids.filter(id => id !== productId));
      message.success('删除成功');
    } else {
      message.error(res.msg || '删除失败');
    }
  };

  const handleClearCart = async () => {
    const res = await cartApi.clearCart();
    if (res.code === 0) {
      setCartItems([]);
      setSelectedIds([]);
      message.success('清空成功');
    }
  };

  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      setSelectedIds(cartItems.map(item => item.productId));
    } else {
      setSelectedIds([]);
    }
  };

  const handleSelectOne = (productId: number, checked: boolean) => {
    if (checked) {
      setSelectedIds(ids => [...ids, productId]);
    } else {
      setSelectedIds(ids => ids.filter(id => id !== productId));
    }
  };

  const selectedItems = cartItems.filter(item => selectedIds.includes(item.productId));
  const totalAmount = selectedItems.reduce((sum, item) => sum + item.price * item.count, 0);
  const totalCount = selectedItems.reduce((sum, item) => sum + item.count, 0);

  const handleCheckout = () => {
    if (selectedIds.length === 0) {
      message.warning('请选择商品');
      return;
    }
    navigate('/checkout', { state: { productIds: selectedIds } });
  };

  const columns = [
    {
      title: '全选',
      key: 'select',
      width: 60,
      render: (_: any, record: CartItemVO) => (
        <Checkbox
          checked={selectedIds.includes(record.productId)}
          onChange={(e) => handleSelectOne(record.productId, e.target.checked)}
        />
      ),
    },
    {
      title: '商品',
      key: 'product',
      render: (_: any, record: CartItemVO) => (
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <img
            src={record.productImg || 'https://via.placeholder.com/80?text=No+Image'}
            alt={record.productName}
            style={{ width: 80, height: 80, objectFit: 'cover', marginRight: 16 }}
          />
          <Link to={`/product/${record.productId}`}>{record.productName}</Link>
        </div>
      ),
    },
    {
      title: '单价',
      dataIndex: 'price',
      key: 'price',
      width: 120,
      render: (price: number) => `¥${price.toFixed(2)}`,
    },
    {
      title: '数量',
      key: 'count',
      width: 150,
      render: (_: any, record: CartItemVO) => (
        <InputNumber
          min={1}
          value={record.count}
          onChange={(value) => value && handleUpdateCount(record.productId, value)}
        />
      ),
    },
    {
      title: '小计',
      key: 'subtotal',
      width: 120,
      render: (_: any, record: CartItemVO) => (
        <Text strong style={{ color: '#ff4d4f' }}>
          ¥{(record.price * record.count).toFixed(2)}
        </Text>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      render: (_: any, record: CartItemVO) => (
        <Popconfirm
          title="确定删除该商品？"
          onConfirm={() => handleDelete(record.productId)}
          okText="确定"
          cancelText="取消"
        >
          <Button type="link" danger icon={<DeleteOutlined />}>
            删除
          </Button>
        </Popconfirm>
      ),
    },
  ];

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto', padding: '24px' }}>
      <Title level={2}>购物车</Title>

      <Card>
        {cartItems.length > 0 && (
          <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Checkbox
              checked={selectedIds.length === cartItems.length && cartItems.length > 0}
              onChange={(e) => handleSelectAll(e.target.checked)}
            >
              全选
            </Checkbox>
            <Popconfirm
              title="确定清空购物车？"
              onConfirm={handleClearCart}
              okText="确定"
              cancelText="取消"
            >
              <Button type="link">清空购物车</Button>
            </Popconfirm>
          </div>
        )}

        <Table
          dataSource={cartItems}
          columns={columns}
          rowKey={(record: CartItemVO) => record.productId}
          pagination={false}
          loading={loading}
          locale={{
            emptyText: (
              <Empty
                image={<ShoppingOutlined style={{ fontSize: 64, color: '#ccc' }} />}
                description="购物车是空的"
              >
                <Link to="/products">
                  <Button type="primary">去逛逛</Button>
                </Link>
              </Empty>
            ),
          }}
        />

        {cartItems.length > 0 && (
          <Card style={{ marginTop: 16, background: '#fafafa' }}>
            <Row gutter={16} align="middle">
              <Col span={16}>
                <Text type="secondary">
                  已选择 {selectedIds.length} 件商品，共 {cartItems.length} 件
                </Text>
              </Col>
              <Col span={8} style={{ textAlign: 'right' }}>
                <div style={{ marginBottom: 8 }}>
                  <Text type="secondary">合计：</Text>
                  <Text strong style={{ fontSize: 24, color: '#ff4d4f' }}>
                    ¥{totalAmount.toFixed(2)}
                  </Text>
                </div>
                <Button
                  type="primary"
                  size="large"
                  disabled={selectedIds.length === 0}
                  onClick={handleCheckout}
                >
                  结算 ({totalCount})
                </Button>
              </Col>
            </Row>
          </Card>
        )}
      </Card>
    </div>
  );
};

export default CartPage;