import React, { useEffect, useState } from 'react';
import { Card, Table, Tag, Button, Typography, Modal, Form, Input, message, Popconfirm } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { couponApi } from '../../api';
import type { CouponDO } from '../../types';

const { Title, Text } = Typography;

const AdminCouponPage: React.FC = () => {
  const [coupons, setCoupons] = useState<CouponDO[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [size] = useState(10);
  const [loading, setLoading] = useState(true);
  const [modalVisible, setModalVisible] = useState(false);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchCoupons();
  }, [page]);

  const fetchCoupons = async () => {
    setLoading(true);
    try {
      const res = await couponApi.pageCoupon(page, size);
      if (res.code === 0) {
        setCoupons(res.data?.records || []);
        setTotal(res.data?.total || 0);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    form.resetFields();
    setModalVisible(true);
  };

  const handleDelete = async (_id: number) => {
    message.info('删除功能需要后端支持');
  };

  const handlePublish = async (id: number) => {
    const res = await couponApi.addPromotion(id);
    if (res.code === 0) {
      message.success('发布成功');
      fetchCoupons();
    } else {
      message.error(res.msg || '发布失败');
    }
  };

  const columns: ColumnsType<CouponDO> = [
    {
      title: '优惠券名称',
      dataIndex: 'couponTitle',
      key: 'couponTitle',
      ellipsis: true,
    },
    {
      title: '优惠金额',
      dataIndex: 'price',
      key: 'price',
      render: (amount: number) => <Tag color="green">¥{amount.toFixed(2)}</Tag>,
    },
    {
      title: '使用条件',
      dataIndex: 'conditionPrice',
      key: 'conditionPrice',
      render: (amount: number) => amount === 0 ? '无门槛' : `满 ${amount} 可用`,
    },
    {
      title: '发放量/剩余量',
      key: 'stock',
      render: (_: any, record: CouponDO) => (
        <Text>{record.stock}/{record.publishCount}</Text>
      ),
    },
    {
      title: '时间',
      dataIndex: 'startTime',
      key: 'time',
      render: (_: any, record: CouponDO) => (
        <Text type="secondary">
          {record.startTime?.slice(0, 10)} ~ {record.endTime?.slice(0, 10)}
        </Text>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_: any, record: CouponDO) => (
        <>
          <Button type="link" icon={<EditOutlined />} onClick={() => message.info('编辑功能需要后端支持')}>
            编辑
          </Button>
          <Popconfirm
            title="确定发布该优惠券？"
            onConfirm={() => handlePublish(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link">发布</Button>
          </Popconfirm>
          <Popconfirm
            title="确定删除该优惠券？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={3} style={{ margin: 0 }}>优惠券管理</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          添加优惠券
        </Button>
      </div>

      <Card>
        <Table
          dataSource={coupons}
          columns={columns}
          rowKey="id"
          loading={loading}
          pagination={{
            current: page,
            pageSize: size,
            total,
            onChange: setPage,
            showTotal: (total) => `共 ${total} 条`,
          }}
        />
      </Card>

      <Modal
        title="添加优惠券"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="优惠券名称" rules={[{ required: true, message: '请输入优惠券名称' }]}>
            <Input style={{ width: '100%' }} placeholder="请输入优惠券名称" />
          </Form.Item>
          <Text type="secondary">添加优惠券功能需要后端支持</Text>
        </Form>
      </Modal>
    </div>
  );
};

export default AdminCouponPage;
