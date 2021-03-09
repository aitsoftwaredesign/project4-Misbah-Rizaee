package com.example.springboot.docker.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.springboot.docker.model.Backup;
import com.example.springboot.docker.repository.BackupRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class BackupService {
	
	@Autowired 
	private BackupRepository backupRepository;
	
	public Backup addBackup(Backup backup) {
		// printing the data which I mock in the test to make sure it is working
//		ObjectMapper mapper = new ObjectMapper();
//		try {
//			System.out.println("Getting mock data for test(addBackup) : " + mapper.writeValueAsString(backup));
//		} catch (JsonProcessingException e) {
//			e.printStackTrace();
//		}
		
		return backupRepository.save(backup);
	}

	public List<Backup> getBackups() {
		List<Backup> backups = backupRepository.findAll();
		
		// printing the data which I mock in the test to make sure it is working
//		ObjectMapper mapper = new ObjectMapper();
//		try {
//			System.out.println("Getting mock data for test(getBackups) : " + mapper.writeValueAsString(backups));
//		} catch (JsonProcessingException e) {
//			e.printStackTrace();
//		}
		
		return backups;
	}
	
	public void deleteBackup(Backup backup) {
		// printing the data which I mock in the test to make sure it is working
//		ObjectMapper mapper = new ObjectMapper();
//		try {
//			System.out.println("Getting mock data for test(deleteBackup) : " + mapper.writeValueAsString(backup));
//		} catch (JsonProcessingException e) {
//			e.printStackTrace();
//		}
		
		backupRepository.delete(backup);
	}
	
	public Backup getBackupById(long id) {
		// printing the data which I mock in the test to make sure it is working
//		ObjectMapper mapper = new ObjectMapper();
//		try {
//			System.out.println("Getting mock data for test(getBackupById) : " + mapper.writeValueAsString(backupRepository.findById(id).get()));
//		} catch (JsonProcessingException e) {
//			e.printStackTrace();
//		}
		
		return backupRepository.findById(id).get();
	}
}
