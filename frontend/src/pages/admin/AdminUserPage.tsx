import React, { useEffect, useState } from 'react';
import { Card, Table, Tag, Typography, Button, Modal, Input, message } from 'antd';
import { LockOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { userApi } from '../../api';

const { Title } = Typography;

interface UserRow {
  id: number;
  name: string;
  mail: string;
  admin: number;
  createTime: string;
}

const AdminUserPage: React.FC = () => {
  const [users, setUsers] = useState<UserRow[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [editModal, setEditModal] = useState(false);
  const [editUser, setEditUser] = useState<UserRow | null>(null);
  const [newPwd, setNewPwd] = useState('');

  useEffect(() => { fetchUsers(); }, [page]);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await userApi.adminListUsers(page, 10);
      if (res.code === 0) {
        setUsers(res.data?.records || []);
        setTotal(res.data?.total || 0);
      }
    } finally { setLoading(false); }
  };

  const handleEdit = (user: UserRow) => {
    setEditUser(user);
    setNewPwd('');
    setEditModal(true);
  };

  const handleSavePwd = async () => {
    if (!newPwd || newPwd.length < 6) {
      message.warning('密码至少6位');
      return;
    }
    if (!editUser) return;
    const res = await userApi.adminUpdatePwd(editUser.id, newPwd);
    if (res.code === 0) {
      message.success('密码修改成功');
      setEditModal(false);
    } else {
      message.error(res.msg || '修改失败');
    }
  };

  const columns: ColumnsType<UserRow> = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '昵称', dataIndex: 'name', key: 'name' },
    { title: '邮箱', dataIndex: 'mail', key: 'mail' },
    {
      title: '角色', dataIndex: 'admin', key: 'admin',
      render: (v: number) => v === 1 ? <Tag color="red">管理员</Tag> : <Tag>普通用户</Tag>,
    },
    { title: '注册时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
    {
      title: '操作', key: 'action', width: 120,
      render: (_: any, record: UserRow) => (
        <Button type="link" size="small" icon={<LockOutlined />} onClick={() => handleEdit(record)}>
          修改密码
        </Button>
      ),
    },
  ];

  return (
    <div>
      <Title level={3} style={{ marginBottom: 16 }}>用户管理</Title>
      <Card>
        <Table dataSource={users} columns={columns} rowKey="id" loading={loading}
          pagination={{ current: page, pageSize: 10, total, onChange: setPage, showTotal: t => `共 ${t} 条` }} />
      </Card>

      <Modal title={`修改密码 - ${editUser?.name || ''}`} open={editModal}
        onOk={handleSavePwd} onCancel={() => setEditModal(false)} okText="保存">
        <p style={{ marginBottom: 12, color: '#888' }}>用户：{editUser?.mail}</p>
        <Input.Password prefix={<LockOutlined />} placeholder="输入新密码" value={newPwd}
          onChange={e => setNewPwd(e.target.value)} onPressEnter={handleSavePwd} />
      </Modal>
    </div>
  );
};

export default AdminUserPage;
