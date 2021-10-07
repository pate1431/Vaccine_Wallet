package com.cloud.vaccinewallet.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cloud.vaccinewallet.beans.Role;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {

    public Role findByRolename(String rolename);

}