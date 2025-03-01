package com.techshop.controller;

import com.techshop.dto.AddressDTO;
import com.techshop.dto.AddressUpdateDTO;
import com.techshop.model.Address;
import com.techshop.service.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/addresses")
public class AddressControler {

    private final AddressService addressService;

    public AddressControler(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<List<AddressDTO>> getAllAddresses() {
        List<AddressDTO> addresses = addressService.getAllAddresses()
            .stream()
            .map(address -> new AddressDTO(
                address.getStreet(),
                address.getCity(),
                address.getPostalCode(),
                address.getCountry()
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long id) {
        return addressService.getAddressById(id)
            .map(address -> new AddressDTO(
                address.getStreet(),
                address.getCity(),
                address.getPostalCode(),
                address.getCountry()
            ))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    public ResponseEntity<Address> createAddress(@RequestBody AddressDTO addressDTO) {
        Address createdAddress = addressService.saveAddress(addressDTO);
        return ResponseEntity.ok(createdAddress);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(@PathVariable Long id, @RequestBody AddressUpdateDTO addressUpdateDTO) {
        addressUpdateDTO.setId(id);
        Address updatedAddress = addressService.updateAddress(addressUpdateDTO);
        return ResponseEntity.ok(updatedAddress);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }
}
