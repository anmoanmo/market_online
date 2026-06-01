export interface JsonData<T = any> {
  code: number;
  data: T;
  msg: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  pages: number;
  current: number;
  size: number;
}

export interface LoginUser {
  id: number;
  mail: string;
  name?: string;
  headImg?: string;
  token?: string;
  admin?: number;
}

export interface UserRegDto {
  mail: string;
  pwd: string;
  name?: string;
  code?: string;
}

export interface UserLoginDto {
  mail: string;
  pwd: string;
}

export interface ProductDO {
  id?: number;
  title: string;
  coverImg: string;
  detail: string;
  oldPrice: number;
  price: number;
  stock?: number;
}

export interface ProductVO extends ProductDO {
  id: number;
  stock: number;
}

export interface BannerDO {
  id: number;
  name: string;
  img: string;
  type: number;
  link: string;
}

export interface CartItemDto {
  productId: number;
  buyNum: number;
}

export interface CartItemVO {
  productId: number;
  productName: string;
  productImg: string;
  price: number;
  count: number;
  totalAmount?: number;
}

export interface CartDO {
  totalNum: number;
  totalAmount: number;
  realPayAmount: number;
  cartItem: CartItemVO[];
}

export interface AddressDO {
  id: number;
  userId: number;
  name: string;
  mobile: string;
  province: string;
  city: string;
  district: string;
  address: string;
  defaultStatus: number;
}

export interface AddressAddDto {
  name: string;
  mobile: string;
  province: string;
  city: string;
  district: string;
  address: string;
}

export interface CouponDO {
  id: number;
  category: string;
  publish: string;
  couponImg?: string;
  couponTitle: string;
  price: number;
  userLimit: number;
  startTime: string;
  endTime: string;
  publishCount: number;
  stock: number;
  createTime: string;
  conditionPrice: number;
}

export interface CouponRecordDO {
  id: number;
  couponId: number;
  userId: number;
  userName: string;
  couponTitle: string;
  price: number;
  conditionPrice: number;
  useState: string;
  startTime: string;
  endTime: string;
  orderId?: number;
}

export interface ProductOrderDO {
  id: number;
  outTradeNo: string;
  userId: number;
  totalAmount: number;
  realPayAmount: number;
  payType: string;
  status: number;
  state?: string;
  createTime: string;
  payTime?: string;
}

export interface ProductOrderItemDO {
  id: number;
  orderId: number;
  productId: number;
  productName: string;
  productImg: string;
  price: number;
  count: number;
}

export interface ConfirmOrderDto {
  couponRecordId?: number;
  productIdList: number[];
  payType: string;
  clientType: string;
  addressId: number;
  totalAmount: number;
  realPayAmount: number;
  token?: string;
}