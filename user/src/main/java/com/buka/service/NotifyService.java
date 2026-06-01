package com.buka.service;

import com.buka.util.JsonData;

public interface NotifyService {

    JsonData sendCode(String to);

    boolean checkCode(String to, String CODE);
}
