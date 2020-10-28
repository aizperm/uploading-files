/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.uploadingfiles.storage;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dave Syer
 */
public class FileSystemStorageServiceTests {

    private StorageProperties properties = new StorageProperties();
    private FileSystemStorageService service;

    @BeforeEach
    public void init() {
        properties.setLocation("target/files/" + Math.abs(new Random().nextLong()));
        service = new FileSystemStorageService(properties);
        service.init();
    }

    @AfterEach
    public void deleteAll() {
        service.deleteAll();
    }

    @Test
    public void loadNonExistent() {
        String username = "username";
        assertThat(service.getFile(username, "foo.txt")).doesNotExist();
    }

    @Test
    public void saveAndLoad() {
        String username = "username";
        service.store(username, new MockMultipartFile("foo", "foo.txt", MediaType.TEXT_PLAIN_VALUE,
                "Hello, World".getBytes()));
        assertThat(service.getFile(username, "foo.txt")).exists();
    }

    @Test
    public void saveRelativePathNotPermitted() {
        String username = "username";
        assertThrows(StorageException.class, () -> {
            service.store(username, new MockMultipartFile("foo", "../foo.txt",
                    MediaType.TEXT_PLAIN_VALUE, "Hello, World".getBytes()));
        });
    }

    @Test
    public void saveAbsolutePathNotPermitted() {
        String username = "username";
        assertThrows(StorageException.class, () -> {
            service.store(username, new MockMultipartFile("foo", "/etc/passwd",
                    MediaType.TEXT_PLAIN_VALUE, "Hello, World".getBytes()));
        });
    }

    @Test
    @EnabledOnOs({OS.LINUX})
    public void saveAbsolutePathInFilenamePermitted() {
        //Unix file systems (e.g. ext4) allows backslash '\' in file names.
        String fileName = "\\etc\\passwd";
        String username = "username";
        service.store(username, new MockMultipartFile(fileName, fileName,
                MediaType.TEXT_PLAIN_VALUE, "Hello, World".getBytes()));
        assertTrue(Files.exists(
                Paths.get(properties.getLocation()).resolve(Paths.get(username)).resolve(Paths.get(fileName))));
    }

    @Test
    public void savePermitted() {
        String username = "username";
        service.store(username, new MockMultipartFile("foo", "bar/../foo.txt",
                MediaType.TEXT_PLAIN_VALUE, "Hello, World".getBytes()));
    }

}
