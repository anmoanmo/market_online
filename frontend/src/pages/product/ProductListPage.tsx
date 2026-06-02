import React, { useEffect, useState } from 'react';
import { Row, Col, Card, Input, Select, Pagination, Spin, Typography } from 'antd';
import { Link, useSearchParams } from 'react-router-dom';
import { productApi } from '../../api';
import type { ProductVO } from '../../types';

const { Title } = Typography;
const { Search } = Input;
const { Option } = Select;

const ProductListPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState<ProductVO[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [size] = useState(12);
  const [search, setSearch] = useState(searchParams.get('search') || '');
  const [sortBy, setSortBy] = useState('default');

  useEffect(() => {
    fetchProducts(search);
  }, [page, sortBy, search]);

  const fetchProducts = async (keyword?: string) => {
    setLoading(true);
    try {
      const kw = keyword ?? search;
      let res;
      if (kw) {
        res = await productApi.search(kw);
        if (res.code === 0) {
          setProducts(res.data || []);
          setTotal((res.data as any[])?.length || 0);
        }
      } else {
        res = await productApi.pageProduct(page, size);
        if (res.code === 0) {
          setProducts(res.data?.records || []);
          setTotal(res.data?.total || 0);
        }
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (value: string) => {
    setSearch(value);
    setPage(1);
    if (value) {
      setSearchParams({ search: value });
    } else {
      setSearchParams({});
    }
  };

  const formatPrice = (price: number) => `¥${price.toFixed(2)}`;

  const sortedProducts = [...products].sort((a, b) => {
    if (sortBy === 'price_asc') return a.price - b.price;
    if (sortBy === 'price_desc') return b.price - a.price;
    return 0;
  });

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto', padding: '24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0 }}>商品列表</Title>
        <div style={{ display: 'flex', gap: 16 }}>
          <Search
            placeholder="搜索商品"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            onSearch={handleSearch}
            style={{ width: 300 }}
            allowClear
          />
          <Select value={sortBy} onChange={setSortBy} style={{ width: 150 }}>
            <Option value="default">默认排序</Option>
            <Option value="price_asc">价格升序</Option>
            <Option value="price_desc">价格降序</Option>
          </Select>
        </div>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: 100 }}>
          <Spin size="large" />
        </div>
      ) : (
        <>
          <Row gutter={[16, 16]}>
            {sortedProducts.map((product) => (
              <Col xs={24} sm={12} md={8} lg={6} key={product.id}>
                <Link to={`/product/${product.id}`}>
                  <Card
                    hoverable
                    cover={
                      <img
                        alt={product.title}
                        src={product.coverImg || 'https://via.placeholder.com/300x200?text=No+Image'}
                        style={{ height: 200, objectFit: 'cover' }}
                      />
                    }
                  >
                    <Card.Meta
                      title={product.title}
                      description={
                        <div>
                          <span style={{ fontSize: 18, fontWeight: 'bold', color: '#ff4d4f' }}>
                            {formatPrice(product.price)}
                          </span>
                          {product.oldPrice !== product.price && (
                            <span style={{ textDecoration: 'line-through', color: '#999', marginLeft: 8, fontSize: 12 }}>
                              {formatPrice(product.oldPrice)}
                            </span>
                          )}
                          <div style={{ marginTop: 4, fontSize: 12, color: '#666' }}>
                            库存: {product.stock}
                          </div>
                        </div>
                      }
                    />
                  </Card>
                </Link>
              </Col>
            ))}
          </Row>

          {sortedProducts.length === 0 && (
            <div style={{ textAlign: 'center', padding: 100 }}>
              <Typography.Text type="secondary">未找到商品</Typography.Text>
            </div>
          )}

          {total > size && (
            <div style={{ textAlign: 'center', marginTop: 32 }}>
              <Pagination
                current={page}
                pageSize={size}
                total={total}
                onChange={setPage}
                showSizeChanger={false}
              />
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default ProductListPage;