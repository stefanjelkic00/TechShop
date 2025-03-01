package com.techshop.dto;


import com.techshop.enums.CustomerType;
import com.techshop.enums.Role;

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
public class UserDTO {
    private Long id;  // Dodato polje id
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private CustomerType customerType;
}
