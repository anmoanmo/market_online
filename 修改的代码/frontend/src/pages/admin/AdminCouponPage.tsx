import React, { useEffect, useState } from 'react';
import { Card, Table, Tag, Button, Typography, Modal, Form, Input, InputNumber, DatePicker, message, Select, Space, Dropdown } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { MenuProps } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SendOutlined, UserAddOutlined, DownOutlined } from '@ant-design/icons';
import { couponApi } from '../../api';
import type { CouponDO } from '../../types';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

const AdminCouponPage: React.FC = () => {
  const [coupons, setCoupons] = useState<CouponDO[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [size] = useState(10);
  const [loading, setLoading] = useState(true);

  const [batchModalVisible, setBatchModalVisible] = useState(false);
  const [singleModalVisible, setSingleModalVisible] = useState(false);
  const [addModalVisible, setAddModalVisible] = useState(false);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [editingCoupon, setEditingCoupon] = useState<CouponDO | null>(null);

  const [form] = Form.useForm();
  const [singleForm] = Form.useForm();
  const [addForm] = Form.useForm();
  const [editForm] = Form.useForm();

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

  const handleBatchDistribute = () => {
    form.resetFields();
    setBatchModalVisible(true);
  };

  const handleSingleDistribute = () => {
    singleForm.resetFields();
    setSingleModalVisible(true);
  };

  const handleAdd = () => {
    addForm.resetFields();
    setAddModalVisible(true);
  };

  const handleEdit = (record: CouponDO) => {
    setEditingCoupon(record);
    editForm.setFieldsValue({
      couponTitle: record.couponTitle,
      price: record.price,
      conditionPrice: record.conditionPrice,
      category: record.category,
      userLimit: record.userLimit,
      startTime: record.startTime ? dayjs(record.startTime) : null,
      endTime: record.endTime ? dayjs(record.endTime) : null,
      publishCount: record.publishCount,
    });
    setEditModalVisible(true);
  };

  const handleBatchSubmit = async () => {
    const values = await form.validateFields();
    const res = await couponApi.distributeBatch(
      values.couponId,
      values.registerDate.format('YYYY-MM-DD'),
      values.endTime.format('YYYY-MM-DD HH:mm:ss')
    );
    if (res.code === 0) {
      message.success(res.data || '发放成功');
      setBatchModalVisible(false);
      fetchCoupons();
    } else {
      message.error(res.msg || '发放失败');
    }
  };

  const handleSingleSubmit = async () => {
    const values = await singleForm.validateFields();
    const res = await couponApi.distributeSingle(
      values.userId,
      values.couponId,
      values.endTime.format('YYYY-MM-DD HH:mm:ss')
    );
    if (res.code === 0) {
      message.success(res.data || '发放成功');
      setSingleModalVisible(false);
      fetchCoupons();
    } else {
      message.error(res.msg || '发放失败');
    }
  };

  const handleAddSubmit = async () => {
    const values = await addForm.validateFields();
    const data = {
      couponTitle: values.couponTitle,
      price: values.price,
      conditionPrice: values.conditionPrice ?? 0,
      category: values.category,
      userLimit: values.userLimit,
      startTime: values.startTime.format('YYYY-MM-DD HH:mm:ss'),
      endTime: values.endTime.format('YYYY-MM-DD HH:mm:ss'),
      publishCount: values.publishCount,
    };
    const res = await couponApi.addCoupon(data);
    if (res.code === 0) {
      message.success('添加成功');
      setAddModalVisible(false);
      fetchCoupons();
    } else {
      message.error(res.msg || '添加失败');
    }
  };

  const handleEditSubmit = async () => {
    const values = await editForm.validateFields();
    if (!editingCoupon) return;
    const data: CouponDO = {
      ...editingCoupon,
      couponTitle: values.couponTitle,
      price: values.price,
      conditionPrice: values.conditionPrice ?? 0,
      category: values.category,
      userLimit: values.userLimit,
      startTime: values.startTime.format('YYYY-MM-DD HH:mm:ss'),
      endTime: values.endTime.format('YYYY-MM-DD HH:mm:ss'),
      publishCount: values.publishCount,
    };
    const res = await couponApi.updateCoupon(data);
    if (res.code === 0) {
      message.success('编辑成功');
      setEditModalVisible(false);
      setEditingCoupon(null);
      fetchCoupons();
    } else {
      message.error(res.msg || '编辑失败');
    }
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

  const getActionMenu = (record: CouponDO): MenuProps['items'] => [
    {
      key: 'edit',
      icon: <EditOutlined />,
      label: '编辑',
      onClick: () => handleEdit(record),
    },
    {
      key: 'publish',
      label: '发布',
      onClick: () => handlePublish(record.id),
    },
    {
      key: 'batch',
      icon: <SendOutlined />,
      label: '批量发放',
      onClick: () => { form.setFieldsValue({ couponId: record.id }); setBatchModalVisible(true); },
    },
    {
      key: 'single',
      icon: <UserAddOutlined />,
      label: '单独发放',
      onClick: () => { singleForm.setFieldsValue({ couponId: record.id }); setSingleModalVisible(true); },
    },
    { type: 'divider' },
    {
      key: 'delete',
      icon: <DeleteOutlined />,
      label: '删除',
      danger: true,
      onClick: () => handleDelete(record.id),
    },
  ];

  const columns: ColumnsType<CouponDO> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 60,
    },
    {
      title: '优惠券名称',
      dataIndex: 'couponTitle',
      key: 'couponTitle',
      ellipsis: true,
    },
    {
      title: '类型',
      dataIndex: 'category',
      key: 'category',
      width: 90,
      render: (cat: string) => {
        const map: Record<string, string> = { NEW_USER: '新人', PROMOTION: '促销', TASK: '任务' };
        return <Tag>{map[cat] || cat}</Tag>;
      },
    },
    {
      title: '优惠金额',
      dataIndex: 'price',
      key: 'price',
      width: 100,
      render: (amount: number) => <Tag color="green">¥{amount.toFixed(2)}</Tag>,
    },
    {
      title: '使用条件',
      dataIndex: 'conditionPrice',
      key: 'conditionPrice',
      width: 100,
      render: (amount: number) => (amount === 0 ? '无门槛' : `满 ${amount}`),
    },
    {
      title: '库存',
      key: 'stock',
      width: 90,
      render: (_: any, record: CouponDO) => (
        <Text>{record.stock}/{record.publishCount}</Text>
      ),
    },
    {
      title: '有效期',
      key: 'time',
      width: 190,
      render: (_: any, record: CouponDO) => (
        <Text type="secondary">
          {record.startTime?.slice(0, 10)} ~ {record.endTime?.slice(0, 10)}
        </Text>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      fixed: 'right',
      render: (_: any, record: CouponDO) => (
        <Dropdown menu={{ items: getActionMenu(record) }} trigger={['click']}>
          <Button size="small">
            操作 <DownOutlined />
          </Button>
        </Dropdown>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16, flexWrap: 'wrap', gap: 8 }}>
        <Title level={3} style={{ margin: 0 }}>
          优惠券管理
        </Title>
        <Space wrap>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            添加优惠券
          </Button>
          <Button icon={<SendOutlined />} onClick={handleBatchDistribute}>
            批量发放
          </Button>
          <Button icon={<UserAddOutlined />} onClick={handleSingleDistribute}>
            单独发放
          </Button>
        </Space>
      </div>

      <Card>
        <Table
          dataSource={coupons}
          columns={columns}
          rowKey="id"
          loading={loading}
          scroll={{ x: 900 }}
          pagination={{
            current: page,
            pageSize: size,
            total,
            onChange: setPage,
            showTotal: (total) => `共 ${total} 条`,
          }}
        />
      </Card>

      {/* 批量发放 */}
      <Modal title="批量发放优惠券" open={batchModalVisible} onCancel={() => setBatchModalVisible(false)} onOk={handleBatchSubmit} okText="发放" destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="couponId" label="优惠券" rules={[{ required: true, message: '请选择优惠券' }]}>
            <Select placeholder="请选择优惠券">
              {coupons.map((c) => (
                <Select.Option key={c.id} value={c.id}>{c.couponTitle} (¥{c.price})</Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="registerDate" label="注册日期前" rules={[{ required: true, message: '请选择日期' }]}>
            <DatePicker style={{ width: '100%' }} placeholder="选择日期，此日期前注册的用户都将获得" />
          </Form.Item>
          <Form.Item name="endTime" label="优惠券过期时间" rules={[{ required: true, message: '请选择过期时间' }]}>
            <DatePicker showTime style={{ width: '100%' }} placeholder="选择过期时间" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 单独发放 */}
      <Modal title="单独发放优惠券" open={singleModalVisible} onCancel={() => setSingleModalVisible(false)} onOk={handleSingleSubmit} okText="发放" destroyOnClose>
        <Form form={singleForm} layout="vertical">
          <Form.Item name="couponId" label="优惠券" rules={[{ required: true, message: '请选择优惠券' }]}>
            <Select placeholder="请选择优惠券">
              {coupons.map((c) => (
                <Select.Option key={c.id} value={c.id}>{c.couponTitle} (¥{c.price})</Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="userId" label="用户ID" rules={[{ required: true, message: '请输入用户ID' }]}>
            <InputNumber min={1} style={{ width: '100%' }} placeholder="输入用户ID" />
          </Form.Item>
          <Form.Item name="endTime" label="优惠券过期时间" rules={[{ required: true, message: '请选择过期时间' }]}>
            <DatePicker showTime style={{ width: '100%' }} placeholder="选择过期时间" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 添加优惠券 */}
      <Modal title="添加优惠券" open={addModalVisible} onCancel={() => setAddModalVisible(false)} onOk={handleAddSubmit} okText="添加" width={600} destroyOnClose>
        <Form form={addForm} layout="vertical">
          <Form.Item name="couponTitle" label="优惠券名称" rules={[{ required: true, message: '请输入名称' }]}>
            <Input placeholder="请输入优惠券名称" />
          </Form.Item>
          <Space style={{ width: '100%' }} size="large">
            <Form.Item name="price" label="优惠金额(元)" rules={[{ required: true, message: '请输入金额' }]}>
              <InputNumber min={0.01} step={0.01} precision={2} placeholder="0.00" />
            </Form.Item>
            <Form.Item name="conditionPrice" label="使用条件(元)" initialValue={0}>
              <InputNumber min={0} step={0.01} precision={2} placeholder="0 表示无门槛" />
            </Form.Item>
          </Space>
          <Space style={{ width: '100%' }} size="large">
            <Form.Item name="category" label="类型" rules={[{ required: true, message: '请选择类型' }]} initialValue="PROMOTION">
              <Select style={{ width: 160 }}>
                <Select.Option value="PROMOTION">促销券</Select.Option>
                <Select.Option value="NEW_USER">新人券</Select.Option>
                <Select.Option value="TASK">任务券</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item name="userLimit" label="每人限领" rules={[{ required: true, message: '请输入限领数量' }]} initialValue={1}>
              <InputNumber min={1} precision={0} />
            </Form.Item>
          </Space>
          <Space style={{ width: '100%' }} size="large">
            <Form.Item name="startTime" label="开始时间" rules={[{ required: true, message: '请选择开始时间' }]}>
              <DatePicker showTime placeholder="选择开始时间" />
            </Form.Item>
            <Form.Item name="endTime" label="结束时间" rules={[{ required: true, message: '请选择结束时间' }]}>
              <DatePicker showTime placeholder="选择结束时间" />
            </Form.Item>
          </Space>
          <Form.Item name="publishCount" label="发放总量" rules={[{ required: true, message: '请输入发放总量' }]}>
            <InputNumber min={1} precision={0} style={{ width: '100%' }} placeholder="优惠券总数量" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 编辑优惠券 */}
      <Modal title="编辑优惠券" open={editModalVisible} onCancel={() => { setEditModalVisible(false); setEditingCoupon(null); }} onOk={handleEditSubmit} okText="保存" width={600} destroyOnClose>
        <Form form={editForm} layout="vertical">
          <Form.Item name="couponTitle" label="优惠券名称" rules={[{ required: true, message: '请输入名称' }]}>
            <Input placeholder="请输入优惠券名称" />
          </Form.Item>
          <Space style={{ width: '100%' }} size="large">
            <Form.Item name="price" label="优惠金额(元)" rules={[{ required: true, message: '请输入金额' }]}>
              <InputNumber min={0.01} step={0.01} precision={2} placeholder="0.00" />
            </Form.Item>
            <Form.Item name="conditionPrice" label="使用条件(元)" initialValue={0}>
              <InputNumber min={0} step={0.01} precision={2} placeholder="0 表示无门槛" />
            </Form.Item>
          </Space>
          <Space style={{ width: '100%' }} size="large">
            <Form.Item name="category" label="类型" rules={[{ required: true, message: '请选择类型' }]}>
              <Select style={{ width: 160 }}>
                <Select.Option value="PROMOTION">促销券</Select.Option>
                <Select.Option value="NEW_USER">新人券</Select.Option>
                <Select.Option value="TASK">任务券</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item name="userLimit" label="每人限领" rules={[{ required: true, message: '请输入限领数量' }]}>
              <InputNumber min={1} precision={0} />
            </Form.Item>
          </Space>
          <Space style={{ width: '100%' }} size="large">
            <Form.Item name="startTime" label="开始时间" rules={[{ required: true, message: '请选择开始时间' }]}>
              <DatePicker showTime placeholder="选择开始时间" />
            </Form.Item>
            <Form.Item name="endTime" label="结束时间" rules={[{ required: true, message: '请选择结束时间' }]}>
              <DatePicker showTime placeholder="选择结束时间" />
            </Form.Item>
          </Space>
          <Form.Item name="publishCount" label="发放总量" rules={[{ required: true, message: '请输入发放总量' }]}>
            <InputNumber min={1} precision={0} style={{ width: '100%' }} placeholder="优惠券总数量" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default AdminCouponPage;
