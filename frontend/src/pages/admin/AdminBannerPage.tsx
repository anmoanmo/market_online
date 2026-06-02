import React, { useEffect, useState } from 'react';
import { Card, Table, Button, Typography, Modal, Form, Input, InputNumber, message, Popconfirm, Image, Space } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { productApi } from '../../api';
import type { BannerDO } from '../../types';

const { Title } = Typography;

const placeholderImages = [
  'https://picsum.photos/seed/b1/1200/400',
  'https://picsum.photos/seed/b2/1200/400',
  'https://picsum.photos/seed/b3/1200/400',
  'https://picsum.photos/seed/b4/1200/400',
];

const AdminBannerPage: React.FC = () => {
  const [banners, setBanners] = useState<BannerDO[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingBanner, setEditingBanner] = useState<BannerDO | null>(null);
  const [form] = Form.useForm();

  useEffect(() => { fetchBanners(); }, []);

  const fetchBanners = async () => {
    setLoading(true);
    try {
      const res = await productApi.listBanners();
      if (res.code === 0) setBanners(res.data || []);
    } finally { setLoading(false); }
  };

  const handleAdd = () => {
    setEditingBanner(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (banner: BannerDO) => {
    setEditingBanner(banner);
    form.setFieldsValue({ name: banner.name, img: banner.img, link: banner.link, type: banner.type });
    setModalVisible(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const data: BannerDO = { ...values, ...(editingBanner?.id && { id: editingBanner.id }) };
      const res = editingBanner ? await productApi.updateBanner(data) : await productApi.addBanner(data);
      if (res.code === 0) {
        message.success(editingBanner ? '修改成功' : '添加成功');
        setModalVisible(false);
        fetchBanners();
      } else {
        message.error(res.msg || '操作失败');
      }
    } catch { /* validation fail */ }
  };

  const handleDelete = async (id: number) => {
    const res = await productApi.deleteBanner(id);
    if (res.code === 0) { message.success('删除成功'); fetchBanners(); }
    else { message.error(res.msg || '删除失败'); }
  };

  const columns: ColumnsType<BannerDO> = [
    {
      title: '图片', dataIndex: 'img', key: 'img', width: 220,
      render: (img: string) => (
        <Image src={img} width={180} height={80} style={{ objectFit: 'cover', borderRadius: 4 }}
          fallback="https://via.placeholder.com/180x80?text=Error" />
      ),
    },
    { title: '名称', dataIndex: 'name', key: 'name' },
    { title: '类型', dataIndex: 'type', key: 'type', render: (t: number) => t === 1 ? '首页轮播' : '其他' },
    { title: '链接', dataIndex: 'link', key: 'link', ellipsis: true },
    {
      title: '操作', key: 'action', width: 160,
      render: (_: any, record: BannerDO) => (
        <Space>
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
          <Popconfirm title="确定删除？" onConfirm={() => handleDelete(record.id)} okText="确定" cancelText="取消">
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={3} style={{ margin: 0 }}>轮播图管理</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>添加轮播图</Button>
      </div>
      <Card>
        <Table dataSource={banners} columns={columns} rowKey="id" loading={loading} pagination={false} />
      </Card>

      <Modal title={editingBanner ? '编辑轮播图' : '添加轮播图'} open={modalVisible}
        onOk={handleSubmit} onCancel={() => setModalVisible(false)} okText="保存" width={560}>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入名称' }]}>
            <Input placeholder="轮播图名称" />
          </Form.Item>
          <Form.Item name="img" label="图片 URL" rules={[{ required: true, message: '请输入或选择图片 URL' }]}>
            <Input placeholder="输入图片 URL" />
          </Form.Item>
          <Form.Item label="快速选择">
            <Space wrap>
              {placeholderImages.map((url, i) => (
                <img key={i} src={url} alt={`b${i}`} style={{ width: 80, height: 50, objectFit: 'cover', cursor: 'pointer', borderRadius: 4, border: '1px solid #d9d9d9' }}
                  onClick={() => form.setFieldsValue({ img: url })} />
              ))}
            </Space>
          </Form.Item>
          <Form.Item name="link" label="跳转链接" rules={[{ required: true, message: '请输入跳转链接' }]}>
            <Input placeholder="点击轮播图跳转的链接" />
          </Form.Item>
          <Form.Item name="type" label="类型" initialValue={1}>
            <InputNumber min={0} style={{ width: '100%' }} placeholder="1=首页轮播" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default AdminBannerPage;
