package com.techshop.dto;


import com.techshop.enums.CustomerType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private CustomerType customerType;
}
