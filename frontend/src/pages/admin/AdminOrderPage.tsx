import React, { useEffect, useState } from 'react';
import { Card, Table, Tag, Typography, Button } from 'antd';
import { useNavigate } from 'react-router-dom';
import type { ColumnsType } from 'antd/es/table';
import type { ProductOrderDO } from '../../types';
import { orderApi } from '../../api';

const { Title } = Typography;

const statusMap: Record<number, { color: string; text: string }> = {
  0: { color: 'orange', text: '待支付' },
  1: { color: 'green', text: '已支付' },
  3: { color: 'red', text: '已取消' },
};

const AdminOrderPage: React.FC = () => {
  const navigate = useNavigate();
  const [orders, setOrders] = useState<ProductOrderDO[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);

  useEffect(() => { fetchOrders(); }, [page]);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const res = await orderApi.adminPageOrder(page, 10);
      if (res.code === 0) {
        setOrders(res.data?.records || []);
        setTotal(res.data?.total || 0);
      }
    } finally { setLoading(false); }
  };

  const columns: ColumnsType<ProductOrderDO> = [
    { title: '订单号', dataIndex: 'outTradeNo', key: 'outTradeNo', width: 220 },
    { title: '用户', dataIndex: 'nickname', key: 'nickname' },
    { title: '金额', dataIndex: 'realPayAmount', key: 'realPayAmount', render: (v: number) => `¥${v.toFixed(2)}` },
    { title: '支付方式', dataIndex: 'payType', key: 'payType' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (s: number) => <Tag color={statusMap[s]?.color}>{statusMap[s]?.text || '未知'}</Tag> },
    { title: '时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
    {
      title: '操作', key: 'action',
      render: (_: any, record: ProductOrderDO) => (
        <Button type="link" onClick={() => navigate(`/admin/orders/${record.outTradeNo}`)}>查看</Button>
      ),
    },
  ];

  return (
    <div>
      <Title level={3} style={{ marginBottom: 16 }}>订单管理</Title>
      <Card>
        <Table dataSource={orders} columns={columns} rowKey="id" loading={loading}
          pagination={{ current: page, pageSize: 10, total, onChange: setPage, showTotal: t => `共 ${t} 条` }} />
      </Card>
    </div>
  );
};

export default AdminOrderPage;
