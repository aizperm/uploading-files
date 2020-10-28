package com.example.uploadingfiles.cookies;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface CookieService {
    String generateCookie(HttpServletRequest request, HttpServletResponse response);
}
