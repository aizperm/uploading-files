package com.example.uploadingfiles;

import java.awt.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.example.uploadingfiles.cookies.CookieService;
import com.example.uploadingfiles.cookies.CookieServiceImpl;
import com.example.uploadingfiles.model.FileModel;
import com.example.uploadingfiles.model.FilesModel;
import com.example.uploadingfiles.sign.Config;
import com.example.uploadingfiles.sign.ISignUtil;
import com.google.common.io.ByteStreams;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import com.example.uploadingfiles.storage.StorageFileNotFoundException;
import com.example.uploadingfiles.storage.StorageService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(allowCredentials = "true")
public class FileUploadController {

    private final StorageService storageService;
    private final CookieService cookieService;
    private final ISignUtil signUtil;

    @Autowired
    public FileUploadController(StorageService storageService,
                                CookieService cookieService,
                                ISignUtil signUtil) {
        this.storageService = storageService;
        this.cookieService = cookieService;
        this.signUtil = signUtil;
    }

    @GetMapping("/api")
    public ModelAndView index(ModelAndView modelAndView, HttpServletRequest request, HttpServletResponse response) {
        cookieService.generateCookie(request, response);
        modelAndView.setViewName("index.html");
        return modelAndView;
    }

    @GetMapping("/api/files")
    public FilesModel listUploadedFiles(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = cookieService.generateCookie(request, response);
        List<FileModel> files = storageService.getAllFiles(username).map(
                path -> {
                    String name = path.getFileName().toString();
                    String url = getURL(name);
                    return new FileModel().filename(name).setUrl(url);
                })
                .collect(Collectors.toList());
        FilesModel filesModel = new FilesModel();
        filesModel.setFiles(files);
        return filesModel;
    }

    private String getURL(String name) {
        String url = null;
        try {
            url = "/api/files/" + URLEncoder.encode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    @GetMapping("/api/files/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = cookieService.generateCookie(request, response);
        Path file = storageService.getFile(username, URLDecoder.decode(id, "UTF-8"));
        byte[] data = Files.readAllBytes(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
                        + file.getFileName().toString() + "\"")
                .body(data);
    }

    @DeleteMapping("/api/files/{id}")
    public ResponseEntity<Object> deleteFile(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = cookieService.generateCookie(request, response);
        storageService.deleteFile(username, URLDecoder.decode(id, "UTF-8"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/file")
    public FileModel handleFileUpload(@CookieValue(name = CookieServiceImpl.USER_NAME, required = false) String username, @RequestParam("file") MultipartFile file) {
        if (StringUtils.isEmpty(username))
            return null;
        Path storedFile = storageService.store(username, file, in -> {
            try {
                Config config = new Config();
                return signUtil.drawText(in, config, "Лесная сказка");
            } catch (IOException | FontFormatException e) {
                try {
                    return ByteStreams.toByteArray(in);
                } catch (IOException ioException) {
                    throw new RuntimeException(e);
                }
            }
        });
        String name = storedFile.getFileName().toString();
        String url = getURL(name);
        return new FileModel().filename(file.getOriginalFilename()).setUrl(url);
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
