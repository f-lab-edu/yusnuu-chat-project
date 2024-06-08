package com.example.websocektpratice.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
@Getter
@Setter
public class ChatRoom {
    private String id;
    private Set<String> users = new HashSet<>();
}