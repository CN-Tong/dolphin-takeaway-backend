package com.tong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tong.dto.UserLoginDTO;
import com.tong.entity.User;

public interface UserService extends IService<User> {

    User wxLogin(UserLoginDTO userLoginDTO);
}
