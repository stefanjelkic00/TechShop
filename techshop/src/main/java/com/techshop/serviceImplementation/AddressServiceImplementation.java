package com.techshop.serviceImplementation;

import com.techshop.DTO.AddressDTO;
import com.techshop.DTO.AddressUpdateDTO;
import com.techshop.model.Address;
import com.techshop.repository.AddressRepository;
import com.techshop.service.AddressService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressServiceImplementation implements AddressService {

    private final AddressRepository addressRepository;

    public AddressServiceImplementation(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public List<Address> getAllAddresses() {
        return addressRepository.findAll();
    }

    @Override
    public Optional<Address> getAddressById(Long id) {
        return addressRepository.findById(id);
    }

    @Override
    public Address saveAddress(AddressDTO addressDTO) {
        Address address = new Address();
        address.setStreet(addressDTO.getStreet());
        address.setCity(addressDTO.getCity());
        address.setPostalCode(addressDTO.getPostalCode());
        address.setCountry(addressDTO.getCountry());

        return addressRepository.save(address);
    }
    @Override
    public Address updateAddress(AddressUpdateDTO addressUpdateDTO) {
        Optional<Address> existingAddress = addressRepository.findById(addressUpdateDTO.getId());
        
        if (existingAddress.isPresent()) {
            Address address = existingAddress.get();
            address.setStreet(addressUpdateDTO.getStreet());
            address.setCity(addressUpdateDTO.getCity());
            address.setPostalCode(addressUpdateDTO.getPostalCode());
            address.setCountry(addressUpdateDTO.getCountry());
            return addressRepository.save(address);
        } else {
            throw new RuntimeException("Address not found with id: " + addressUpdateDTO.getId());
        }
    }


    @Override
    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }

	
}
