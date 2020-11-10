package com.example.uploadingfiles;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.example.uploadingfiles.cookies.CookieServiceImpl;
import com.example.uploadingfiles.model.FileModel;
import com.example.uploadingfiles.model.FilesModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.uploadingfiles.storage.StorageFileNotFoundException;
import com.example.uploadingfiles.storage.StorageService;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;

@AutoConfigureMockMvc
@SpringBootTest
public class FileUploadTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private StorageService storageService;

    @Test
    public void shouldListAllFiles() throws Exception {
        String username = "username";
        given(this.storageService.getAllFiles(username))
                .willReturn(Stream.of(Paths.get("first.txt"), Paths.get("second.txt")));

        MockHttpServletRequestBuilder requestBuilder = get("/api/files").cookie(new Cookie(CookieServiceImpl.USER_NAME, username));

        MvcResult result = this.mvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();
        MockHttpServletResponse response = result.getResponse();
        String content = response.getContentAsString();
        assertNotNull(content);
        ObjectMapper om = new ObjectMapper();
        FilesModel files = om.readValue(content, FilesModel.class);
        assertEquals(2, files.getFiles().size());

        List<String> all = files.getFiles().stream().map(f -> f.getFilename()).collect(Collectors.toList());
        assertTrue(all.containsAll(Arrays.asList("first.txt", "second.txt")));
    }

    @Test
    public void shouldSaveUploadedFile() throws Exception {
        given(this.storageService.store(any(String.class), any(MultipartFile.class), any(Function.class))).willReturn(Paths.get("filename"));

        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt",
                "text/plain", "Spring Framework".getBytes());

        FileModel file = new FileModel().filename("test.txt").setUrl("/api/files/filename");
        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(file);

        String username = "username";
        this.mvc.perform(multipart("/api/file").file(multipartFile).cookie(new Cookie(CookieServiceImpl.USER_NAME, username)))
                .andExpect(status().isOk())
                .andExpect(content().json(json))
                .andReturn();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should404WhenMissingFile() throws Exception {

        given(this.storageService.getFile(any(String.class), any(String.class)))
                .willThrow(StorageFileNotFoundException.class);

        this.mvc.perform(get("/api/files/test.txt")).andExpect(status().isNotFound());
    }

}
