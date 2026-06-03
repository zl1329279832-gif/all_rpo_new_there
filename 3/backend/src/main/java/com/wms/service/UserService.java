package com.wms.service;

import com.wms.dto.LoginDTO;
import com.wms.entity.User;

public interface UserService {

    User login(LoginDTO dto, String loginIp);

    User getById(Long id);

    User getByUsername(String username);
}
