package com.cloud.vaccinewallet.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

public class CustomeSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String redirectUrl = null;
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority grantedAuthority : authorities) {
            System.out.println("role " + grantedAuthority.getAuthority());
            if (grantedAuthority.getAuthority().equals("ROLE_USER")) {
                redirectUrl = "user/index";
                break;
            }
            else if (grantedAuthority.getAuthority().equals("ROLE_ADMIN")) {
                redirectUrl = "admin/index";
                break;
            }
        }
        System.out.println("redirectUrl " + redirectUrl);


        if (redirectUrl == null) {
            throw new IllegalStateException(); } new DefaultRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }


}
