package com.cloud.vaccinewallet.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import com.cloud.vaccinewallet.beans.User;

public interface UserRepository extends JpaRepository<User, Long> {

    public User findByUsername(String username);

}
