package com.techshop.service;

import com.techshop.DTO.AddressDTO;
import com.techshop.DTO.AddressUpdateDTO;
import com.techshop.model.Address;

import java.util.List;
import java.util.Optional;

public interface AddressService {
    List<Address> getAllAddresses();
    Optional<Address> getAddressById(Long id);
    Address saveAddress(AddressDTO addressDTO);
    Address updateAddress(AddressUpdateDTO addressUpdateDTO);
    void deleteAddress(Long id);
}
