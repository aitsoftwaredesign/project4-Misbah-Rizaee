package com.example.springboot.docker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.text.PDFTextStripper;

import com.example.springboot.docker.model.Backup;
import com.example.springboot.docker.repository.BackupRepository;
import com.example.springboot.docker.service.BackupService;

import net.lingala.zip4j.ZipFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class BackupController {

	@Autowired // This means to get the bean called backupRepository Which is auto-generated by
				// Spring, we will use it to handle the data
	private BackupRepository backupRepository;
	
	@Autowired
	private BackupService service;

	List<Long> doneBackupList = new ArrayList<Long>();
	private static int backupsInProgress = 0;
	private static int backupsDone = 0;
	private static int backupsFailed = 0;

	// Spans with red & green highlights to put highlighted characters in HTML
	private static final String DELETION = "<span style=\"background-color: #FB504B\">${text}</span>";
	private static final String INSERTION = "<span style=\"background-color: #45EA85\">${text}</span>";
	private String left = "";
	private String right = "";

	@GetMapping("/list")
	public String backups(Model model) throws ParseException {
//		List<Backup> backups = backupRepository.findAll();
		List<Backup> backups = service.getBackups();
		
		backupsInProgress = 0;
		backupsDone = 0;
		backupsFailed = 0;
		doneBackupList.clear();
		
		Calendar calendar = Calendar.getInstance();
//		System.out.println(calendar.getTime());
		calendar.set(Calendar.SECOND, 0);
//		System.out.println(calendar.getTime()+"!!!!!!!!");
		calendar.add(Calendar.SECOND, -1);
//		System.out.println(calendar.getTime()+"!!!!!!!!");
		
		for (int i = 0; i < backups.size(); i++) {
			// check if a certain date is passed AND backup not taken THEN change status to FAILED
			if ((new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(backups.get(i).getTime().toString().replace("T", " ")).before(calendar.getTime())) && (backups.get(i).getStatus().equals("IN PROGRESS"))) {
//				Backup updateBackup = backupRepository.findById(backups.get(i).getId()).get();
				Backup updateBackup = service.getBackupById(backups.get(i).getId());
				updateBackup.setStatus("FAILED");
//				backupRepository.save(updateBackup);
				service.addBackup(updateBackup);
			}
			
			String checkExtension = backups.get(i).getFolderName();
			String theExtension = checkExtension.substring(checkExtension.lastIndexOf(".") + 1, checkExtension.length());
			if ((backups.get(i).getStatus().equals("DONE")) && (theExtension.equals("pdf") || theExtension.equals("txt") || theExtension.equals("java") || theExtension.equals("py") || theExtension.equals("docx"))) {
				doneBackupList.add(backups.get(i).getId());
			}
			
			if (backups.get(i).getStatus().equals("DONE")) {
				backupsDone += 1;
			} else if (backups.get(i).getStatus().equals("IN PROGRESS")) {
				backupsInProgress += 1;
			} else if (backups.get(i).getStatus().equals("FAILED")) {
				backupsFailed += 1;
			}
		}
		
		model.addAttribute("backups", backups);
		model.addAttribute("doneBackupList", doneBackupList);
		model.addAttribute("InProgress", backupsInProgress);
		model.addAttribute("Done", backupsDone);
		model.addAttribute("Failed", backupsFailed);
		return "index";
	}

	@GetMapping("showForm")
	public String showBackupForm(Backup backup) {
		return "add-backup";
	}

	@PostMapping("/add")
	public String addBackup(@RequestParam("file") MultipartFile file, @RequestParam("time") String time, @RequestParam("compress") String compress)
			throws ParseException {
		
		System.out.println(compress);

		Backup backup = new Backup();

		String fileName = file.getOriginalFilename();
		backup.setFolderName(fileName);
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDateTime now = LocalDateTime.now();
		backup.setTime(dtf.format(now)+"T"+time);
		
		backup.setStatus("IN PROGRESS");
		
		if (compress.equals("yes")) {
			backup.setCompression("Compressed");
		} else if (compress.equals("no")) {
			backup.setCompression("Not Compressed");
		}

//		backupRepository.save(backup);
		service.addBackup(backup);

		runScheduledBackup(backup.getTime(), backup.getId(), file, compress);

		return "redirect:list";
	}

	public void runScheduledBackup(String time, long id, MultipartFile file, String compress) throws ParseException {

		// the Date and time at which I want to execute
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date date = dateFormatter.parse(time.toString().replace("T", " "));

		// To create the time and schedule it
		Timer timer = new Timer();

		TimerTask myTask = new TimerTask() {
			@Override
			public void run() {

				try {
					// Update the backup status
//					Backup updateBackup = backupRepository.findById(id).get();
					Backup updateBackup = service.getBackupById(id);
					updateBackup.setStatus("DONE");
//					backupRepository.save(updateBackup);
					service.addBackup(updateBackup);

					String folderName = time.toString().replace(":", "-") + "-" + "ID" + id;
					String path = System.getProperty("user.home") + File.separator + "AutoBackup" + File.separator
							+ "Backups" + File.separator + folderName;
					File folder = new File(path);

					if (!folder.exists()) {
						try {
							System.out.println("Creating a directory for backups at (" + path + ")");
							Files.createDirectories(Paths.get(path));
						} catch (IOException e1) {
							System.out.println("Failed to create a directory for backups");
						}
					} else {
						System.out.println("A directory for backups already exists at (" + path + ")");
					}
					
					if (compress.equals("yes")) {
			            try {
			            	 // input file 
			            	InputStream inputStream =  new BufferedInputStream(file.getInputStream());

			                // out put file 
			                String fileNameWithOutExt = FilenameUtils.removeExtension(file.getOriginalFilename());
			                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(path + File.separator + fileNameWithOutExt+".zip"));

			                // name the file inside the zip  file 
			                out.putNextEntry(new ZipEntry(file.getOriginalFilename())); 

			                // buffer size
			                byte[] b = new byte[1024];
			                int count;

			                while ((count = inputStream.read(b)) > 0) {
			                    out.write(b, 0, count);
			                }
			                out.close();
			                inputStream.close();
			                
							String checkExtension = updateBackup.getFolderName();
							String theExtension = checkExtension.substring(checkExtension.lastIndexOf(".") + 1, checkExtension.length());
							if (theExtension.equals("pdf") || theExtension.equals("txt") || theExtension.equals("java") || theExtension.equals("py") || theExtension.equals("docx")) {
								doneBackupList.add(id);
							}
		
							System.out.println("The file \"" + folderName + "\" has been backed up at \"" + time);
				            
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else if (compress.equals("no")) {

						String fileName = file.getOriginalFilename();
						try {
							file.transferTo(new File(path + File.separator + fileName));
						} catch (IllegalStateException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						String checkExtension = updateBackup.getFolderName();
						String theExtension = checkExtension.substring(checkExtension.lastIndexOf(".") + 1, checkExtension.length());
						if (theExtension.equals("pdf") || theExtension.equals("txt") || theExtension.equals("java") || theExtension.equals("py") || theExtension.equals("docx")) {
							doneBackupList.add(id);
						}
	
						System.out.println("The file \"" + fileName + "\" has been backed up at \"" + time);
					}

				} catch (NoSuchElementException e) {
					System.out.println("Scheduled backup with ID " + id + " was deleted. File (" + file.getOriginalFilename()
							+ ") has not been backed up.");
				}
			}
		};

		// To schedule the task and Start it when the date is reached!
		timer.schedule(myTask, date);
	}

	@GetMapping("/delete/{id}")
	public String deleteBackup(@PathVariable("id") long id, Model model) {

//		Backup backup = this.backupRepository.findById(id)
//				.orElseThrow(() -> new IllegalArgumentException("Invalid backup id : " + id));
		Backup backup = this.service.getBackupById(id);

//		this.backupRepository.delete(backup);
		service.deleteBackup(backup);

		return "redirect:/list";
	}

	@PostMapping("compareForm")
	public String comparison(@RequestParam("file") MultipartFile file, @RequestParam("selectBackup") long id,
			Model model) throws IOException {
//		Backup backedUp = this.backupRepository.findById(id)
//				.orElseThrow(() -> new IllegalArgumentException("Invalid backup id : " + id));
		Backup backedUp = this.service.getBackupById(id);

		// Get the folder name where the backed up file is stored
		String time = backedUp.getTime();
		String folderName = time.toString().replace(":", "-") + "-" + "ID" + id;
		
		// Get the path to the Folder
		String pathToFolder = System.getProperty("user.home") + File.separator + "AutoBackup" + File.separator + "Backups"
				+ File.separator + folderName;
		
		// Get the path to the file. whether compressed or not
		String pathToNotCompressed = pathToFolder + File.separator + backedUp.getFolderName();
		
		String fileNameWithOutExt = FilenameUtils.removeExtension(backedUp.getFolderName());
		String pathToCompressed = pathToFolder + File.separator + fileNameWithOutExt+".zip";
		
		// Create a temporary folder for the selected file
		String tempPath = System.getProperty("user.home") + File.separator + "AutoBackup" + File.separator + "Temp";
		File tempFolder = new File(tempPath);
		if (!tempFolder.exists()) {
			try {
				System.out.println("Creating a directory for temporary files at (" + tempPath + ")");
				Files.createDirectories(Paths.get(tempPath));
			} catch (IOException e1) {
				System.out.println("Failed to create directory.");
			}
		} else {
			System.out.println("Directory for temporary files already exists at (" + tempPath + ")");
		}

		// Check if the backed up file exists
		File notCompressedFile = new File(pathToNotCompressed);
		File compressedFile = new File(pathToCompressed);
		
		if (notCompressedFile.exists()) {
			startComparing(file, backedUp, pathToNotCompressed, pathToCompressed, tempPath, notCompressedFile, model);
		} else if (compressedFile.exists()) {

            // Initiate ZipFile object with the path/name of the zip file.
            ZipFile zipFile = new ZipFile(pathToCompressed);

            // Extracts all files to the path specified
            zipFile.extractAll(pathToFolder);
            
			startComparing(file, backedUp, pathToNotCompressed, pathToCompressed, tempPath, notCompressedFile, model);
			
			// Delete the extracted file after comparing
			Path extractedFilePath = Paths.get(pathToNotCompressed);
			Files.delete(extractedFilePath);
		} else {
			System.out.println("Backup was DONE but file doesnt exist!!!!");
		} 
		
		return "compareFiles";
	}
	
	public Model startComparing(MultipartFile file, Backup backedUp, String pathToNotCompressed, String pathToCompressed, String tempPath, File backedUpFile, Model model) throws IOException {
		// LEFT SIDE TEXT
		String filename1 = backedUp.getFolderName();
		String extension1 = filename1.substring(filename1.lastIndexOf(".") + 1, filename1.length());
		StringBuilder leftTextBeforeHighlight = printHTML(extension1, backedUpFile, pathToNotCompressed);
		String lines1[] = leftTextBeforeHighlight.toString().split("\\r?\\n");

		String leftHeading = "Backed up file: " + filename1;
		model.addAttribute("leftHeading", leftHeading);

		// RIGHT SIDE TEXT
		File convFile = new File(tempPath, file.getOriginalFilename());
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();

		String filename2 = file.getOriginalFilename();
		String extension2 = filename2.substring(filename2.lastIndexOf(".") + 1, filename2.length());
		StringBuilder rightTextBeforeHighlight = printHTML(extension2, convFile, pathToNotCompressed);
		String lines2[] = rightTextBeforeHighlight.toString().split("\\r?\\n");

		String rightHeading = "Modified file: " + filename2;
		model.addAttribute("rightHeading", rightHeading);

		left = "";
		right = "";
		String leftTextAfterHighlight = deleteHighlight(lines1, lines2);
		String rightTextAfterHighlight = insertHighlight(lines2, lines1);
		model.addAttribute("leftText", leftTextAfterHighlight);
		model.addAttribute("rightText", rightTextAfterHighlight);
		
		// Delete the temporary file
		convFile.delete();
		
		return model;
	}

	public StringBuilder printHTML(String extension, File file, String path) throws IOException {
		if (extension.equals("txt") || extension.equals("java") || extension.equals("py")) {
			Scanner myReader = new Scanner(file);
			StringBuilder text = new StringBuilder();
			while (myReader.hasNextLine()) {
				text.append(myReader.nextLine() + " \n");
			}
			myReader.close();

			return text;
		} 
		else if (extension.equals("pdf")) {
			PDDocument pdDocument = PDDocument.load(file);
			PDFTextStripper pdfStripper = new PDFTextStripper();
			String leftText = pdfStripper.getText(pdDocument);

			StringBuilder text = new StringBuilder();
			Scanner scan = new Scanner(leftText);
			while (scan.hasNextLine()) {
				text.append(scan.nextLine() + " \n");
			}
			scan.close();
			pdDocument.close();

			return text;
		} 
		else if (extension.equals("docx")) {
			FileInputStream fis = new FileInputStream(file);
			XWPFDocument docx = new XWPFDocument(fis);
			List<XWPFParagraph> paragraphList = docx.getParagraphs();

			StringBuilder text = new StringBuilder();
			for (XWPFParagraph paragraph : paragraphList) {
				text.append(paragraph.getText() + " \n");
			}

			return text;
		}
		return null;
	}

	public String deleteHighlight(String[] lines1, String[] lines2) {

		int maxLs =  Math.max(lines1.length, lines2.length);
		for (int i = 0; i < maxLs; i++) { 

			if (i > lines2.length - 1) {
				String lineWords1[] = lines1[i].split("\\s");
				for (int z = 0; z < lineWords1.length; z++) {
					left = left + DELETION.replace("${text}", "" + lineWords1[z]) + " ";
				}
			} else if (i > lines1.length - 1) {
				System.out.println("End of file 1");
				break;
			}
			else {
				String lineWords1[] = lines1[i].split("\\s");
				String lineWords2[] = lines2[i].split("\\s");
				
				int maxWs =  Math.max(lineWords1.length, lineWords2.length);
				for (int y = 0; y < maxWs; y++) {
					if (y > lineWords2.length - 1) {
						left = left + DELETION.replace("${text}", "" + lineWords1[y]) + " ";
					} else if (y > lineWords1.length - 1) {
						break;
					}
					else {
						if (lineWords1[y].equalsIgnoreCase(lineWords2[y])) {
							left = left + lineWords1[y] + " ";
						} else if (lineWords1[y] != lineWords2[y]) {
							left = left + DELETION.replace("${text}", "" + lineWords1[y]) + " ";
						}
					}
				}
			}
			left = left + " <br>";
        } 
		return left;
	}

	public String insertHighlight(String[] lines1, String[] lines2) {

		int maxLs =  Math.max(lines1.length, lines2.length);
		for (int i = 0; i < maxLs; i++) { 

			if (i > lines2.length - 1) {
				String lineWords1[] = lines1[i].split("\\s");
				for (int z = 0; z < lineWords1.length; z++) {
					right = right + INSERTION.replace("${text}", "" + lineWords1[z]) + " ";
				}
			} else if (i > lines1.length - 1) {
				System.out.println("End of file 1");
				break;
			}
			else {
				String lineWords1[] = lines1[i].split("\\s");
				String lineWords2[] = lines2[i].split("\\s");
				
				int maxWs =  Math.max(lineWords1.length, lineWords2.length);
				for (int y = 0; y < maxWs; y++) {
					if (y > lineWords2.length - 1) {
						right = right + INSERTION.replace("${text}", "" + lineWords1[y]) + " ";
					} else if (y > lineWords1.length - 1) {
						break;
					}
					else {
						if (lineWords1[y].equalsIgnoreCase(lineWords2[y])) {
							right = right + lineWords1[y] + " ";
						} else if (lineWords1[y] != lineWords2[y]) {
							right = right + INSERTION.replace("${text}", "" + lineWords1[y]) + " ";
						}
					}
				}
			}
			right = right + " <br>";
        } 
		return right;
	}
}