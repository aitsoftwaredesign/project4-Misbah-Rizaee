package com.example.springboot.docker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.springboot.docker.model.Backup;
import com.example.springboot.docker.repository.BackupRepository;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;

@Controller
public class BackupController {
	
	@Autowired // This means to get the bean called backupRepository Which is auto-generated by Spring, we will use it to handle the data
    private BackupRepository backupRepository;
	
	@GetMapping("/list")
	public String backups(Model model)
	{
		List<Backup> backups = backupRepository.findAll();
		model.addAttribute("backups", backups);
		return "index";
	}
	
	@GetMapping("showForm")
	public String showBackupForm(Backup backup) {
		return "add-backup";
	}
	
	@PostMapping("/add")
	public String addBackup(@RequestParam("file") MultipartFile file,
    		@RequestParam("time") String time,
    		@RequestParam("storageType") String storageType) throws ParseException {
		
		Backup backup = new Backup();
		
		String fileName = file.getOriginalFilename();
		backup.setFolderName(fileName);
		backup.setTime(time);
		backup.setStorage(storageType);
		backup.setStatus("IN PROGRESS");
		
		backupRepository.save(backup);
		
		runScheduledBackup(backup.getTime(), backup.getFolderName(), backup.getId(), file);

		return "redirect:list";
	}
	
	public void runScheduledBackup(String time, String folderName, long id, MultipartFile file) throws ParseException {
        
		//the Date and time at which I want to execute
	    DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	    Date date = dateFormatter .parse(time.toString().replace("T", " "));
        
	    //To create the time and schedule it
	    Timer timer = new Timer();
	    
		TimerTask myTask = new TimerTask () {
		    @Override
		    public void run () {
		    	
		    	try
		    	{
					// Update the backup status
					Backup updateBackup = backupRepository.findById(id).get(); 
					updateBackup.setStatus("DONE"); 
					backupRepository.save(updateBackup);
					
					String fileName = file.getOriginalFilename();
			    	
			    	File folder = new File("C:\\AutoBackup");
			    	if(!folder.exists()) {
			    		if(folder.mkdir()) {
			    			System.out.println("Directory is created.");
			    		} else {
			    			System.out.println("Failed to create directory.");
			    		}
			    	} else {
		    			System.out.println("Directory already exists.");
		    		}
					
					try {
						file.transferTo(new File("C:\\AutoBackup\\"+fileName));
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println("File uploaded successfully");
			    	
			        System.out.println("The file \""+ folderName +"\" has been backed up at \""+time);
					
		        } catch (NoSuchElementException e) {
		            // Output expected NoSuchElementExceptions.
		        	System.out.println("Scheduled backup process with ID "+id+" was terminated. File ("+folderName+") has not been backed up.");
		        } 
		    }
		};

		// To schedule the task and Start it when the date is reached!
		timer.schedule(myTask, date);
	}
	
	@GetMapping("/delete/{id}")
		public String deleteBackup(@PathVariable ("id") long id, Model model) {
		
		Backup backup = this.backupRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid backup id : " + id));
		
		this.backupRepository.delete(backup);
		model.addAttribute("backups", this.backupRepository.findAll());
		return "redirect:/list";
	}
}