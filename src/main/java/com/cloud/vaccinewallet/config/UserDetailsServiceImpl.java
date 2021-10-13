package com.cloud.vaccinewallet.config;

import java.util.ArrayList;
import java.util.List;

import com.cloud.vaccinewallet.beans.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cloud.vaccinewallet.beans.Role;
import com.cloud.vaccinewallet.repositories.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Find the user based on the user name
        com.cloud.vaccinewallet.beans.User user = userRepository.findByUsername(username);

        // If the user doesn't exist throw an exception
        if (user == null ) {
            System.out.println("User not found:" + username);
            throw new UsernameNotFoundException("User " + username + " was not found in the database");
        }
        if (user.getEnabled()==false ) {
            System.out.println("User access DENIED" + username);
            throw new UsernameNotFoundException("User " + username + " was not found in the database");
        }

        // Change the list of the user's roles into a list of GrantedAuthority
        List<GrantedAuthority> grantList= new ArrayList<GrantedAuthority>();
        for (Role role: user.getRoles()) {
            grantList.add(new SimpleGrantedAuthority(role.getRolename()));
        }

        // Create a user based on the information above.
        UserDetails userDetails= (UserDetails) new User(user.getUsername(), user.getEncryptedPassword(), grantList);
        return userDetails;
    }

}
