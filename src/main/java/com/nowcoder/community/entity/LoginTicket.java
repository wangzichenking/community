package com.nowcoder.community.entity;

import lombok.Data;

import java.util.Date;

@Data
public class LoginTicket {
    private int userId;
    private String ticket;
    private int status;
    private Date expired;
    private int id;
}
