package com.example.uploadingfiles;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.example.uploadingfiles.cookies.CookieService;
import com.example.uploadingfiles.cookies.CookieServiceImpl;
import com.example.uploadingfiles.model.FileModel;
import com.example.uploadingfiles.model.FilesModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.uploadingfiles.storage.StorageFileNotFoundException;
import com.example.uploadingfiles.storage.StorageService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class FileUploadController {

    private final StorageService storageService;
    private final CookieService cookieService;

    @Autowired
    public FileUploadController(StorageService storageService,
                                CookieService cookieService) {
        this.storageService = storageService;
        this.cookieService = cookieService;
    }

    @GetMapping("/")
    public FilesModel listUploadedFiles(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = cookieService.generateCookie(request, response);
        List<FileModel> files = storageService.getAllFiles(username).map(
                path -> new FileModel().filename(path.getFileName().toString()))
                .collect(Collectors.toList());
        FilesModel filesModel = new FilesModel();
        filesModel.setFiles(files);
        return filesModel;
    }


    @PostMapping("/")
    public FileModel handleFileUpload(@CookieValue(name = CookieServiceImpl.USER_NAME, required = false) String username, @RequestParam("file") MultipartFile file) {
        if (StringUtils.isEmpty(username))
            return null;
        storageService.store(username, file);
        return new FileModel().filename(file.getOriginalFilename());
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
