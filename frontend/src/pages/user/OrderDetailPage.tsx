import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Descriptions, Table, Tag, Button, Spin, Typography, Divider } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { orderApi } from '../../api';
import type { ProductOrderDO, ProductOrderItemDO } from '../../types';

const { Title, Text } = Typography;

const statusMap: Record<number, { color: string; text: string }> = {
  0: { color: 'orange', text: '待支付' },
  1: { color: 'green', text: '已支付' },
  2: { color: 'blue', text: '已完成' },
  3: { color: 'red', text: '已取消' },
};

const payTypeMap: Record<string, string> = {
  ALIPAY: '支付宝',
  WECHAT: '微信支付',
  BANK: '银行卡',
};

const OrderDetailPage: React.FC = () => {
  const { outTradeNo } = useParams<{ outTradeNo: string }>();
  const navigate = useNavigate();
  const [order, setOrder] = useState<ProductOrderDO | null>(null);
  const [items, setItems] = useState<ProductOrderItemDO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (outTradeNo) fetchDetail();
  }, [outTradeNo]);

  const fetchDetail = async () => {
    setLoading(true);
    try {
      const res = await orderApi.detail(outTradeNo!);
      if (res.code === 0) {
        setOrder(res.data!.order);
        setItems(res.data!.items || []);
      }
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div style={{ textAlign: 'center', padding: 80 }}><Spin size="large" /></div>;
  if (!order) return <div style={{ textAlign: 'center', padding: 80 }}>订单不存在</div>;

  const itemColumns = [
    { title: '商品', dataIndex: 'productName', key: 'productName' },
    { title: '数量', dataIndex: 'buyNum', key: 'buyNum' },
    { title: '单价', dataIndex: 'amount', key: 'amount', render: (v: number) => `¥${v.toFixed(2)}` },
    { title: '小计', dataIndex: 'totalAmount', key: 'totalAmount', render: (v: number) => `¥${v.toFixed(2)}` },
  ];

  return (
    <div style={{ maxWidth: 900, margin: '0 auto', padding: 24 }}>
      <Button type="link" onClick={() => navigate('/user/orders')} icon={<ArrowLeftOutlined />} style={{ marginBottom: 16 }}>
        返回订单列表
      </Button>
      <Card>
        <Title level={4}>订单详情</Title>
        <Descriptions column={2} bordered size="small">
          <Descriptions.Item label="订单号">{order.outTradeNo}</Descriptions.Item>
          <Descriptions.Item label="订单状态">
            <Tag color={statusMap[order.status]?.color}>{statusMap[order.status]?.text}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="总金额">¥{order.totalAmount.toFixed(2)}</Descriptions.Item>
          <Descriptions.Item label="实付金额">
            <Text strong style={{ color: '#cf1322' }}>¥{order.realPayAmount.toFixed(2)}</Text>
          </Descriptions.Item>
          <Descriptions.Item label="支付方式">{payTypeMap[order.payType] || order.payType}</Descriptions.Item>
          <Descriptions.Item label="创建时间">{order.createTime}</Descriptions.Item>
        </Descriptions>
        <Divider />
        <Title level={5}>商品列表</Title>
        <Table dataSource={items} columns={itemColumns} rowKey="productId" pagination={false} />
      </Card>
    </div>
  );
};

export default OrderDetailPage;
