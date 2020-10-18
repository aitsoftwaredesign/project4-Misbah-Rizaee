package com.example.springboot.docker.mysql.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.springboot.docker.mysql.model.Backup;

//This is a repository that holds backup records

//This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
//CRUD refers Create, Read, Update, Delete

@Repository
public interface BackupRepository extends JpaRepository<Backup, Long> {

}

