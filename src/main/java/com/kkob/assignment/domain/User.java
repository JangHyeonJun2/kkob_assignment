package com.kkob.assignment.domain;

import com.kkob.assignment.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class User extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String phoneNumber;

    @Embedded
    private Address address;
}
