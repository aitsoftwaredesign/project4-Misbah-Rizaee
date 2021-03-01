package com.example.springboot.docker.model;

import javax.persistence.*;

@Entity // This tells Hibernate to make a table out of this class. Hibernate
		// automatically translates the entity into a table.
@Table(name = "backup")
public class Backup {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String folderName;
	private String time;
	private String status;
	private String compression;

	public Backup() {

	}

	public Backup(String folderName, String time, String status, String compression) {
		this.folderName = folderName;
		this.time = time;
		this.status = status;
		this.compression = compression;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCompression() {
		return compression;
	}

	public void setCompression(String compression) {
		this.compression = compression;
	}
}
