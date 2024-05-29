package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.repository.OracleUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OracleUserService {

    @Autowired
    private OracleUserRepository oracleUserRepository;

    public OracleUser updateUser(OracleUser user) {
        return oracleUserRepository.save(user);
    }
}
