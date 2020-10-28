package com.example.uploadingfiles;

import com.example.uploadingfiles.model.FileModel;
import com.example.uploadingfiles.model.FilesModel;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.any;

import com.example.uploadingfiles.storage.StorageService;

import java.io.IOException;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FileUploadIntegrationTests {

    private TestRestTemplate restTemplate = testRestTemplate();

    @MockBean
    private StorageService storageService;

    @LocalServerPort
    private int port;

    @Test
    public void shouldUploadFile() throws Exception {

        ResponseEntity<? extends FilesModel> responseGet = this.restTemplate.getForEntity("http://localhost:" + this.port + "/", FilesModel.class);

        List<String> header = responseGet.getHeaders().get("Set-Cookie");
        String cookie = header.get(0);
        String[] split = cookie.split(";")[0].split("=");
        String username = split[1];

        ClassPathResource resource = new ClassPathResource("testupload.txt", getClass());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("file", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        HttpEntity<MultiValueMap> request = new HttpEntity<>(map, headers);

        ResponseEntity<FileModel> response = this.restTemplate.postForEntity("http://localhost:" + this.port + "/", request,
                FileModel.class);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();

        then(storageService).should().store(any(String.class), any(MultipartFile.class));
    }


    public static TestRestTemplate testRestTemplate() {
        RestTemplateBuilder restTemplate = new RestTemplateBuilder()
                .errorHandler(new ResponseErrorHandler() {
                    @Override
                    public boolean hasError(ClientHttpResponse response) throws IOException {
                        return false;
                    }

                    @Override
                    public void handleError(ClientHttpResponse response) throws IOException {

                    }
                });

        return new TestRestTemplate(restTemplate, "user", "password",
                TestRestTemplate.HttpClientOption.ENABLE_COOKIES);
    }
}
