package com.xlq.service;

import com.xlq.po.User;

public interface UserService {

    User checkUser(String username, String password);
}
