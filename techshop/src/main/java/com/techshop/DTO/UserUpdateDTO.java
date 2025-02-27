package com.techshop.DTO;


import com.techshop.enums.CustomerType;
import com.techshop.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private CustomerType customerType;
    private Role role;


}
