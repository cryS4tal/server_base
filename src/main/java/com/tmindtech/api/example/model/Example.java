package com.tmindtech.api.example.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by RexQian on 2017/5/10.
 */
@Table(name = "t_example")
public class Example {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String name;

    public Boolean enabled;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
