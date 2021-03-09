package com.example.springboot.docker.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.springboot.docker.model.Backup;
import com.example.springboot.docker.repository.BackupRepository;
import com.example.springboot.docker.service.BackupService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BackupServiceTest {

	@Autowired
	private BackupService service;

	@MockBean
	private BackupRepository backupRepository;
	
	@Test
	public void getBackupsTest() {
		when(backupRepository.findAll()).thenReturn(Stream
				.of(new Backup(1, "Essay.pdf", "2021-08-15T12:00", "IN PROGRESS", "Compressed"), new Backup(2, "Thesis.docx", "2021-05-25T23:59", "IN PROGRESS", "Not Compressed")).collect(Collectors.toList()));
		assertEquals(2, service.getBackups().size());
	}
	
	@Test
	public void getBackupByIdTest() {
		long id = 1;
		Backup backupToReturn = new Backup(1, "Essay.pdf", "2021-08-15T12:00", "IN PROGRESS", "Compressed"); 
		when(backupRepository.findById(id))
				.thenReturn(Optional.of(backupToReturn));
		service.getBackupById(id);
	}
	
	@Test
	public void saveBackupTest() {
		Backup backup = new Backup(3, "Assignment.pdf", "2020-11-15T11:59", "IN PROGRESS", "Not Compressed");
		when(backupRepository.save(backup)).thenReturn(backup);
		assertEquals(backup, service.addBackup(backup));
	}
	
	@Test
	public void deleteBackupTest() {
		Backup backup = new Backup(4, "Report.pdf", "2022-01-10T15:30", "IN PROGRESS", "Not Compressed");
		service.deleteBackup(backup);
		verify(backupRepository, times(1)).delete(backup);
	}
}
