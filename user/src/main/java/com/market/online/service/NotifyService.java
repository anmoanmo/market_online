package com.market.online.service;

import com.market.online.util.JsonData;

public interface NotifyService {

    JsonData sendCode(String to);

    boolean checkCode(String to, String CODE);
}
