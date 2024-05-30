package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.repository.OracleUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {

    @Autowired
    private OracleUserRepository oracleUserRepository;

    public OracleUser authenticate(Long userId) {
        Optional<OracleUser> user = oracleUserRepository.findById(userId);
        return user.orElse(null);
    }

    public boolean isManager(OracleUser user) {
        return "Manager".equals(user.getUserRole());
    }

    public boolean isDeveloper(OracleUser user) {
        return "Developer".equals(user.getUserRole());
    }
}
