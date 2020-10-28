package com.example.uploadingfiles.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

	void init();

	void store(String username, MultipartFile file);

	Stream<Path> getAllFiles(String username);

	Path getFile(String username, String filename);

	Resource getFileAsResource(String username, String filename);

	void deleteAll();

}
