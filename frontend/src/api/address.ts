import request from '../utils/request';
import type { JsonData, AddressDO, AddressAddDto } from '../types';

export const addressApi = {
  list(): Promise<JsonData<AddressDO[]>> {
    return request.get('/user-service/api/address/v1/list');
  },

  add(data: AddressAddDto): Promise<JsonData> {
    return request.post('/user-service/api/address/v1/add', data);
  },

  find(addressId: number): Promise<JsonData<AddressDO>> {
    return request.get(`/user-service/api/address/v1/find/${addressId}`);
  },

  delete(addressId: number): Promise<JsonData> {
    return request.delete(`/user-service/api/address/v1/del/${addressId}`);
  },

  update(addressId: number, data: AddressAddDto): Promise<JsonData> {
    return request.put(`/user-service/api/address/v1/update/${addressId}`, data);
  },
};