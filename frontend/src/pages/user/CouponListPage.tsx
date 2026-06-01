import React, { useEffect, useState } from 'react';
import { Card, Table, Tag, Typography, Tabs, Empty, Spin } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { couponApi } from '../../api';
import type { CouponRecordDO } from '../../types';

const { Title } = Typography;

const useStateMap: Record<string, { color: string; text: string }> = {
  NEW: { color: 'blue', text: '未使用' },
  USED: { color: 'green', text: '已使用' },
  EXPIRED: { color: 'default', text: '已过期' },
};

const tabStateMap: Record<string, string> = {
  '0': 'NEW',
  '1': 'USED',
  '2': 'EXPIRED',
};

const CouponListPage: React.FC = () => {
  const [coupons, setCoupons] = useState<CouponRecordDO[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('0');

  useEffect(() => {
    fetchCoupons();
  }, []);

  const fetchCoupons = async () => {
    setLoading(true);
    try {
      const res = await couponApi.pageCouponRecord(1, 100);
      if (res.code === 0) {
        setCoupons(res.data?.records || []);
      }
    } finally {
      setLoading(false);
    }
  };

  const columns: ColumnsType<CouponRecordDO> = [
    {
      title: '优惠券名称',
      dataIndex: 'couponTitle',
      key: 'couponTitle',
    },
    {
      title: '优惠金额',
      dataIndex: 'price',
      key: 'price',
      render: (amount: number) => `¥${amount.toFixed(2)}`,
    },
    {
      title: '使用条件',
      dataIndex: 'conditionPrice',
      key: 'conditionPrice',
      render: (amount: number) => amount === 0 ? '无门槛' : `满 ${amount} 可用`,
    },
    {
      title: '状态',
      dataIndex: 'useState',
      key: 'useState',
      render: (state: string) => {
        const item = useStateMap[state] || { color: 'default', text: '未知' };
        return <Tag color={item.color}>{item.text}</Tag>;
      },
    },
    {
      title: '开始时间',
      dataIndex: 'startTime',
      key: 'startTime',
      render: (time: string) => time ? new Date(time).toLocaleDateString() : '-',
    },
    {
      title: '结束时间',
      dataIndex: 'endTime',
      key: 'endTime',
      render: (time: string) => time ? new Date(time).toLocaleDateString() : '-',
    },
  ];

  const tabItems = [
    { key: '0', label: '未使用' },
    { key: '1', label: '已使用' },
    { key: '2', label: '已过期' },
  ];

  const filteredCoupons = coupons.filter(coupon => coupon.useState === tabStateMap[activeTab]);

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 50 }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div>
      <Title level={4}>我的优惠券</Title>

      <Card>
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={tabItems}
        />

        {filteredCoupons.length === 0 ? (
          <Empty description="暂无优惠券" />
        ) : (
          <Table
            dataSource={filteredCoupons}
            columns={columns}
            rowKey="id"
            pagination={{ pageSize: 10 }}
          />
        )}
      </Card>
    </div>
  );
};

export default CouponListPage;
