package com.example.springboot.docker.model;

import javax.persistence.*;

@Entity // This tells Hibernate to make a table out of this class. Hibernate automatically translates the entity into a table.
@Table(name = "backup")
public class Backup {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String folderName;
    private String time;
    
    public Backup() {
		
	}

    public Backup(String folderName, String time) {
		this.folderName = folderName;
		this.time = time;
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
}

