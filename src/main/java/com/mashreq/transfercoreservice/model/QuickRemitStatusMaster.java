package com.mashreq.transfercoreservice.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "qr_status_ms")
public class QuickRemitStatusMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String desc;
    private String groupCode;
    private String gateway;
}
