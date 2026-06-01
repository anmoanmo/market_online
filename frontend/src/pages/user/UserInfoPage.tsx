import React, { useState } from 'react';
import { Card, Form, Input, Button, Upload, Avatar, message, Typography } from 'antd';
import { UploadOutlined, UserOutlined } from '@ant-design/icons';
import { userApi } from '../../api';
import { useAuth } from '../../context/AuthContext';

const { Title, Text } = Typography;

const UserInfoPage: React.FC = () => {
  const { user, updateUser } = useAuth();
  const [uploading, setUploading] = useState(false);
  const [nickname, setNickname] = useState(user?.name || '');
  const [saving, setSaving] = useState(false);

  const handleAvatarChange = async (file: File) => {
    setUploading(true);
    try {
      const res = await userApi.upload(file);
      if (res.code === 0) {
        message.success('头像上传成功');
        if (user) updateUser({ ...user, headImg: res.data });
      } else {
        message.error(res.msg || '上传失败');
      }
    } finally {
      setUploading(false);
    }
    return false;
  };

  const handleSave = async () => {
    if (!user) return;
    setSaving(true);
    try {
      const res = await userApi.updateInfo({ name: nickname });
      if (res.code === 0) {
        message.success('保存成功');
        updateUser({ ...user, name: nickname });
      } else {
        message.error(res.msg || '保存失败');
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <div style={{ maxWidth: 600 }}>
      <Title level={4}>个人信息</Title>
      <Card>
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: 32 }}>
          <div style={{ marginRight: 24 }}>
            <Avatar size={80} src={user?.headImg} icon={<UserOutlined />} style={{ backgroundColor: '#667eea' }} />
          </div>
          <div>
            <Text strong>头像</Text>
            <br />
            <Upload showUploadList={false} beforeUpload={handleAvatarChange} accept="image/*">
              <Button loading={uploading} icon={<UploadOutlined />} style={{ marginTop: 8 }}>更换头像</Button>
            </Upload>
          </div>
        </div>

        <Form layout="vertical">
          <Form.Item label="邮箱">
            <Input value={user?.mail} disabled />
          </Form.Item>
          <Form.Item label="昵称">
            <Input value={nickname} onChange={e => setNickname(e.target.value)} placeholder="请输入昵称" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" loading={saving} onClick={handleSave}>保存修改</Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default UserInfoPage;
