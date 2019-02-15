package com.test.estimator.domain;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Company implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;

    @Column
    private String companyName;

    @Temporal(TemporalType.DATE)
    private Date bookedTillDate;

    @OneToMany(mappedBy = "company")
    private List<Developer> developers = new ArrayList<>();

}
