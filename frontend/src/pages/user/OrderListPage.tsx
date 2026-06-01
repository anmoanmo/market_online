import React, { useEffect, useState, useCallback } from 'react';
import { Card, Table, Tag, Button, Typography, Tabs, Empty, message, Modal, Space } from 'antd';
import { ReloadOutlined, CloseCircleOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import type { ColumnsType } from 'antd/es/table';
import type { ProductOrderDO } from '../../types';
import { orderApi } from '../../api';
import { isPaymentHtml, submitPaymentHtml } from '../../utils/payment';

const { Title, Text } = Typography;

const statusMap: Record<number, { color: string; text: string }> = {
  0: { color: 'orange', text: '待支付' },
  1: { color: 'green', text: '已支付' },
  2: { color: 'blue', text: '已完成' },
  3: { color: 'red', text: '已取消' },
};

const payTypeMap: Record<string, { text: string }> = {
  ALIPAY: { text: '支付宝' },
  WECHAT: { text: '微信支付' },
  BANK: { text: '银行卡' },
};

const COUNTDOWN_MINUTES = 30;

const OrderListPage: React.FC = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('all');
  const [orders, setOrders] = useState<ProductOrderDO[]>([]);
  const [loading, setLoading] = useState(false);
  const [now, setNow] = useState(Date.now());

  const fetchOrders = useCallback(async () => {
    setLoading(true);
    try {
      const res = await orderApi.pageOrder(1, 100);
      if (res.code === 0) {
        setOrders(res.data?.records || []);
      }
    } catch {
      // use empty list
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  useEffect(() => {
    const timer = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(timer);
  }, []);

  const getRemainingTime = (createTime: string): number | null => {
    const created = new Date(createTime).getTime();
    const deadline = created + COUNTDOWN_MINUTES * 60 * 1000;
    const remaining = deadline - now;
    return remaining > 0 ? remaining : null;
  };

  const formatTime = (ms: number): string => {
    const totalSec = Math.floor(ms / 1000);
    const min = Math.floor(totalSec / 60);
    const sec = totalSec % 60;
    return `${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`;
  };

  const handleCancel = (outTradeNo: string) => {
    Modal.confirm({
      title: '确认取消订单？',
      content: '取消后优惠券将自动退回',
      onOk: async () => {
        const res = await orderApi.cancelOrder(outTradeNo);
        if (res.code === 0) {
          message.success('订单已取消');
          fetchOrders();
        } else {
          message.error(res.msg || '取消失败');
        }
      },
    });
  };

  const handleRepay = async (outTradeNo: string) => {
    const res = await orderApi.repay(outTradeNo);
    if (res.code === 0) {
      const payData = res.data;
      if (isPaymentHtml(payData) && !submitPaymentHtml(payData)) {
        message.error('支付表单无效，请重新下单');
      }
    } else {
      message.error('支付链接已过期，请重新下单');
    }
  };

  const columns: ColumnsType<ProductOrderDO> = [
    {
      title: '订单号',
      dataIndex: 'outTradeNo',
      key: 'outTradeNo',
      width: 220,
    },
    {
      title: '实付金额',
      dataIndex: 'realPayAmount',
      key: 'realPayAmount',
      render: (amount: number) => <Text strong>¥{amount.toFixed(2)}</Text>,
    },
    {
      title: '支付方式',
      dataIndex: 'payType',
      key: 'payType',
      render: (type: string) => payTypeMap[type]?.text || type,
    },
    {
      title: '订单状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: number, record: ProductOrderDO) => (
        <Space>
          <Tag color={statusMap[status]?.color || 'default'}>
            {statusMap[status]?.text || '未知'}
          </Tag>
          {status === 0 && record.createTime && (
            <Tag color="orange">
              剩余 {formatTime(getRemainingTime(record.createTime) || 0)}
            </Tag>
          )}
        </Space>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 180,
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_: any, record: ProductOrderDO) => (
        <Space>
          {record.status === 0 && (
            <>
              <Button type="primary" size="small" onClick={() => handleRepay(record.outTradeNo)}>
                去支付
              </Button>
              <Button
                size="small"
                danger
                icon={<CloseCircleOutlined />}
                onClick={() => handleCancel(record.outTradeNo)}
              >
                取消
              </Button>
            </>
          )}
          {record.status === 1 && (
            <Button type="link" size="small" icon={<ReloadOutlined />} onClick={() => navigate(`/user/orders/${record.outTradeNo}`)}>
              查看详情
            </Button>
          )}
          {record.status === 3 && (
            <Button type="link" size="small" onClick={() => navigate(`/user/orders/${record.outTradeNo}`)}>
              查看详情
            </Button>
          )}
        </Space>
      ),
    },
  ];

  const filteredOrders = activeTab === 'all'
    ? orders
    : orders.filter(order => order.status === Number(activeTab));

  return (
    <div>
      <Title level={4}>我的订单</Title>
      <Card>
        <Tabs activeKey={activeTab} onChange={setActiveTab} items={[
          { key: 'all', label: '全部订单' },
          { key: '0', label: '待支付' },
          { key: '1', label: '已支付' },
          { key: '3', label: '已取消' },
        ]} />
        {filteredOrders.length === 0 && !loading ? (
          <Empty description="暂无订单" />
        ) : (
          <Table
            dataSource={filteredOrders}
            columns={columns}
            rowKey="id"
            loading={loading}
            pagination={{ pageSize: 10 }}
          />
        )}
      </Card>
    </div>
  );
};

export default OrderListPage;
