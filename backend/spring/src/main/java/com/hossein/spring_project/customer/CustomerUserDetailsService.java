package com.hossein.spring_project.customer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomerUserDetailsService implements UserDetailsService{

    private final CustomerDAO customerDAO;

    public CustomerUserDetailsService(@Qualifier("jpa") CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }


    @Override
    public UserDetails loadUserByUsername(String username) 
        throws UsernameNotFoundException {
        return customerDAO.selectCustomerByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User with username: [%s] was not found!".formatted(username)));
    }

}
