import React, { useEffect, useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, message, Popconfirm, Tag, Typography } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { addressApi } from '../../api';
import type { AddressDO } from '../../types';

const { Title } = Typography;
const { Option } = Select;

const AddressPage: React.FC = () => {
  const [addresses, setAddresses] = useState<AddressDO[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingAddress, setEditingAddress] = useState<AddressDO | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchAddresses();
  }, []);

  const fetchAddresses = async () => {
    setLoading(true);
    try {
      const res = await addressApi.list();
      if (res.code === 0) {
        setAddresses(res.data || []);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingAddress(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (address: AddressDO) => {
    setEditingAddress(address);
    form.setFieldsValue({
      name: address.name,
      mobile: address.mobile,
      province: address.province,
      city: address.city,
      district: address.district,
      address: address.address,
    });
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    const res = await addressApi.delete(id);
    if (res.code === 0) {
      message.success('删除成功');
      fetchAddresses();
    } else {
      message.error(res.msg || '删除失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      let res;
      if (editingAddress) {
        res = await addressApi.update(editingAddress.id, values);
      } else {
        res = await addressApi.add(values);
      }
      if (res.code === 0) {
        message.success(editingAddress ? '修改成功' : '添加成功');
        setModalVisible(false);
        fetchAddresses();
      } else {
        message.error(res.msg || '操作失败');
      }
    } catch (error) {
      console.error(error);
    }
  };

  const columns = [
    {
      title: '收货人',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record: AddressDO) => (
        <span>
          {name} {record.defaultStatus === 1 && <Tag color="green">默认</Tag>}
        </span>
      ),
    },
    { title: '手机号', dataIndex: 'mobile', key: 'mobile' },
    {
      title: '地址',
      key: 'address',
      render: (_: any, record: AddressDO) => (
        <span>{record.province} {record.city} {record.district} {record.address}</span>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_: any, record: AddressDO) => (
        <>
          <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Popconfirm
            title="确定删除该地址？"
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
        <Title level={4} style={{ margin: 0 }}>收货地址</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          新增地址
        </Button>
      </div>

      <Table
        dataSource={addresses}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={false}
      />

      <Modal
        title={editingAddress ? '编辑地址' : '新增地址'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        okText="保存"
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="收货人" rules={[{ required: true, message: '请输入收货人' }]}>
            <Input placeholder="请输入收货人" />
          </Form.Item>
          <Form.Item name="mobile" label="手机号" rules={[{ required: true, message: '请输入手机号' }]}>
            <Input placeholder="请输入手机号" />
          </Form.Item>
          <Form.Item name="province" label="省份" rules={[{ required: true, message: '请选择省份' }]}>
            <Select placeholder="请选择省份">
              <Option value="北京市">北京市</Option>
              <Option value="上海市">上海市</Option>
              <Option value="广东省">广东省</Option>
              <Option value="浙江省">浙江省</Option>
              <Option value="江苏省">江苏省</Option>
            </Select>
          </Form.Item>
          <Form.Item name="city" label="城市" rules={[{ required: true, message: '请选择城市' }]}>
            <Select placeholder="请选择城市">
              <Option value="北京市">北京市</Option>
              <Option value="上海市">上海市</Option>
              <Option value="广州市">广州市</Option>
              <Option value="深圳市">深圳市</Option>
              <Option value="杭州市">杭州市</Option>
              <Option value="南京市">南京市</Option>
            </Select>
          </Form.Item>
          <Form.Item name="district" label="区县" rules={[{ required: true, message: '请输入区县' }]}>
            <Input placeholder="请输入区县" />
          </Form.Item>
          <Form.Item name="address" label="详细地址" rules={[{ required: true, message: '请输入详细地址' }]}>
            <Input.TextArea placeholder="请输入详细地址" rows={2} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default AddressPage;