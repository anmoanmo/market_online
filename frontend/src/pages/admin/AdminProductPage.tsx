import React, { useEffect, useState } from 'react';
import { Card, Table, Button, Modal, Form, Input, InputNumber, message, Popconfirm, Typography, Image } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { productApi } from '../../api';
import type { ProductDO, ProductVO } from '../../types';

const { Title, Text } = Typography;
const { TextArea } = Input;

const AdminProductPage: React.FC = () => {
  const [products, setProducts] = useState<ProductVO[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [size] = useState(10);
  const [loading, setLoading] = useState(true);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingProduct, setEditingProduct] = useState<ProductDO | null>(null);
  const [form] = Form.useForm();
  const [coverImgUrl, setCoverImgUrl] = useState('');

  const placeholderImgs = [
    'https://picsum.photos/seed/p1/400/400',
    'https://picsum.photos/seed/p2/400/400',
    'https://picsum.photos/seed/p3/400/400',
    'https://picsum.photos/seed/p4/400/400',
    'https://picsum.photos/seed/p5/400/400',
  ];

  useEffect(() => {
    fetchProducts();
  }, [page]);

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const res = await productApi.pageProduct(page, size);
      if (res.code === 0) {
        setProducts(res.data?.records || []);
        setTotal(res.data?.total || 0);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingProduct(null);
    form.resetFields();
    setCoverImgUrl('');
    setModalVisible(true);
  };

  const handleEdit = (product: ProductVO) => {
    setEditingProduct(product);
    form.setFieldsValue({
      title: product.title,
      oldPrice: product.oldPrice,
      price: product.price,
      stock: product.stock,
      detail: product.detail,
    });
    setCoverImgUrl(product.coverImg || '');
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    const res = await productApi.deleteProduct(id);
    if (res.code === 0) {
      message.success('删除成功');
      fetchProducts();
    } else {
      message.error(res.msg || '删除失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const productData: ProductDO = {
        ...values,
        coverImg: values.coverImg || coverImgUrl || editingProduct?.coverImg || '',
        ...(editingProduct?.id && { id: editingProduct.id }),
      };

      const res = editingProduct
        ? await productApi.updateProduct(productData)
        : await productApi.addProduct(productData);
      if (res.code === 0) {
        message.success(editingProduct ? '修改成功' : '添加成功');
        setModalVisible(false);
        fetchProducts();
      } else {
        message.error(res.msg || '操作失败');
      }
    } catch (error: any) {
      console.error('Submit error:', error);
      message.error(error?.response?.data?.msg || error?.message || '操作失败');
    }
  };

  const columns: ColumnsType<ProductVO> = [
    {
      title: '商品图片',
      dataIndex: 'coverImg',
      key: 'coverImg',
      width: 100,
      render: (img: string) => <Image src={img} width={60} height={60} fallback="https://via.placeholder.com/60" />,
    },
    {
      title: '商品名称',
      dataIndex: 'title',
      key: 'title',
      ellipsis: true,
    },
    {
      title: '原价',
      dataIndex: 'oldPrice',
      key: 'oldPrice',
      render: (price: number) => `¥${price.toFixed(2)}`,
    },
    {
      title: '现价',
      dataIndex: 'price',
      key: 'price',
      render: (price: number) => <Text strong style={{ color: '#ff4d4f' }}>¥{price.toFixed(2)}</Text>,
    },
    {
      title: '库存',
      dataIndex: 'stock',
      key: 'stock',
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_: any, record: ProductVO) => (
        <>
          <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Popconfirm
            title="确定删除该商品？"
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
        <Title level={3} style={{ margin: 0 }}>商品管理</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          添加商品
        </Button>
      </div>

      <Card>
        <Table
          dataSource={products}
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
        title={editingProduct ? '编辑商品' : '添加商品'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        okText="保存"
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="title" label="商品名称" rules={[{ required: true, message: '请输入商品名称' }]}>
            <Input placeholder="请输入商品名称" />
          </Form.Item>

          <Form.Item label="封面图片 URL" name="coverImg">
            <Input placeholder="输入图片URL，留空自动使用随机图片" />
          </Form.Item>
          {coverImgUrl && (
            <div style={{ marginBottom: 16, textAlign: 'center' }}>
              <img src={coverImgUrl} alt="封面预览" style={{ maxWidth: 200, maxHeight: 120, borderRadius: 4 }} />
            </div>
          )}
          <Form.Item label="快速选择">
            <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
              {placeholderImgs.map((url, i) => (
                <img key={i} src={url} alt={`选择${i+1}`} style={{ width: 48, height: 48, objectFit: 'cover', cursor: 'pointer', borderRadius: 4, border: coverImgUrl === url ? '2px solid #cf1322' : '2px solid transparent' }} onClick={() => { setCoverImgUrl(url); form.setFieldsValue({coverImg: url}) }} />
              ))}
            </div>
          </Form.Item>

          <Form.Item name="oldPrice" label="原价" rules={[{ required: true, message: '请输入原价' }]}>
            <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入原价" />
          </Form.Item>

          <Form.Item name="price" label="现价" rules={[{ required: true, message: '请输入现价' }]}>
            <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入现价" />
          </Form.Item>

          <Form.Item name="stock" label="库存" rules={[{ required: true, message: '请输入库存' }]}>
            <InputNumber min={0} style={{ width: '100%' }} placeholder="请输入库存" />
          </Form.Item>

          <Form.Item name="detail" label="商品详情">
            <TextArea rows={4} placeholder="请输入商品详情（支持HTML）" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default AdminProductPage;
