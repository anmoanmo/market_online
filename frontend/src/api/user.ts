import request, { API_BASE_URL } from '../utils/request';
import type { JsonData, PageResult, UserLoginDto, UserRegDto, LoginUser } from '../types';

export const userApi = {
  register(data: UserRegDto): Promise<JsonData> {
    return request.post('/user-service/api/user/v1/register', data);
  },

  login(data: UserLoginDto): Promise<JsonData<{ token: string; userInfo: LoginUser }>> {
    return request.post('/user-service/api/user/v1/login', data);
  },

  getUserInfo(): Promise<JsonData<LoginUser>> {
    return request.get('/user-service/api/user/v1/info');
  },

  upload(file: File): Promise<JsonData<string>> {
    const formData = new FormData();
    formData.append('file', file);
    return request.post('/user-service/api/user/v1/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  getCaptcha(): string {
    return `${API_BASE_URL}/user-service/api/notify/v1/captcha?t=${Date.now()}`;
  },

  sendCode(to: string, captcha: string): Promise<JsonData> {
    return request.get('/user-service/api/notify/v1/send_code', { params: { to, captcha } });
  },

  adminListUsers(page: number, size: number): Promise<JsonData<PageResult<any>>> {
    return request.get('/user-service/api/user/v1/admin_list', { params: { page, size } });
  },

  adminUpdatePwd(userId: number, newPwd: string): Promise<JsonData> {
    return request.put('/user-service/api/user/v1/admin_update_pwd', null, { params: { userId, newPwd } });
  },

  updateInfo(data: { name?: string; headImg?: string }): Promise<JsonData> {
    return request.put('/user-service/api/user/v1/update_info', data);
  },
};
