package com.test.estimator.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
public class Developer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long developerId;

    @Column
    private String developerName;

    @Enumerated(EnumType.STRING)
    @Column
    private DeveloperType developerType;

    @ManyToOne
    @JsonIgnore
    private Company company;

}
