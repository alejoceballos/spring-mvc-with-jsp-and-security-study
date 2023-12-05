package com.momo2x.study.springmvc.controller;

import com.momo2x.study.springmvc.model.UserData;
import com.momo2x.study.springmvc.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    public final UserService service;

    @GetMapping()
    public List<UserData> list() {
        return service.findUsers();
    }

}

