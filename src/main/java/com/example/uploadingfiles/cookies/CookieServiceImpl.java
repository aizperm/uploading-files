package com.example.uploadingfiles.cookies;

import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CookieServiceImpl implements CookieService {
    public static final String USER_NAME = "username";

    @Override
    public String generateCookie(HttpServletRequest request, HttpServletResponse response) {
        Optional<Cookie[]> cookiesOptional = Optional.ofNullable(request.getCookies());
        Optional<Cookie> findedOpt = Arrays.stream(cookiesOptional
                .orElse(new Cookie[0])).filter(cookie -> USER_NAME.equals(cookie.getName())).findAny();
        if (!findedOpt.isPresent()) {
            String uuid = UUID.randomUUID().toString();
            Cookie cookie = new Cookie(USER_NAME, uuid);
            cookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(2));
            cookie.setPath("/");
            response.addCookie(cookie);
            return uuid;
        }
        return findedOpt.get().getValue();
    }
}
