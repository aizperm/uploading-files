package com.example.uploadingfiles.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Stream;

public interface StorageService {

	void init();

	Path store(String username, MultipartFile file, Function<InputStream, byte[]> modifyFile);

	Stream<Path> getAllFiles(String username);

	Path getFile(String username, String filename);

	Resource getFileAsResource(String username, String filename);

	void deleteAll();

    void deleteFile(String username, String filename);
}
