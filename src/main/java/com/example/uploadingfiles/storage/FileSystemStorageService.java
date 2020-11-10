package com.example.uploadingfiles.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public Path store(String username, MultipartFile file, Function<InputStream, byte[]> modifyFile) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            Path dirPath = getUserDirPath(username);
            dirPath.toFile().mkdirs();

            Path destinationFile = dirPath.resolve(
                    Paths.get(file.getOriginalFilename()))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(dirPath.toAbsolutePath())) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                byte[] bytes = modifyFile.apply(inputStream);
                Files.copy(new ByteArrayInputStream(bytes), destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
            return destinationFile;
        } catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }

    private Path getUserDirPath(String username) {
        if (StringUtils.isEmpty(username))
            throw new StorageException("Failed to store empty username.");
        Path userdir = this.rootLocation.resolve(Paths.get(username));
        userdir.toFile().mkdirs();
        return userdir;
    }

    @Override
    public Stream<Path> getAllFiles(String username) {
        try {
            Path userDir = getUserDirPath(username);
            return Files.walk(userDir, 1)
                    .filter(path -> !path.equals(userDir))
                    .map(userDir::relativize);
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Path getFile(String username, String filename) {
        Path dirPath = getUserDirPath(username);
        return dirPath.resolve(filename);
    }

    @Override
    public Resource getFileAsResource(String username, String filename) {
        try {
            Path file = getFile(username, filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void deleteFile(String username, String filename) {
        Path file = getFile(username, filename);
        try {
            Files.delete(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
