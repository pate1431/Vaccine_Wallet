package com.cloud.vaccinewallet.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@Order(1)
public class AdminSecurityConfig extends WebSecurityConfigurerAdapter{

    @Autowired
    private LoggingAccessDeniedHandler accessDeniedHandlerAdmin;

    @Autowired
    UserDetailsServiceImpl userDetailsServiceAdmin;


    @Bean
    public BCryptPasswordEncoder passwordEncoderAdmin() {
        return new BCryptPasswordEncoder();
    }

    @Override protected void configure(HttpSecurity http) throws Exception {

        http.csrf().disable();
        http.headers().frameOptions().disable();

        http.authorizeRequests()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/register").permitAll()
                .antMatchers("/","/css/**","static/**","/image/**", "/js/**", "/**")
                .permitAll()
                .antMatchers("/h2-console/**").permitAll()
                .anyRequest()
                .authenticated()
                .and().formLogin()
                .loginPage("/login").permitAll()
                .and().logout()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout").permitAll()
                .and()
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandlerAdmin);
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception{
        auth.userDetailsService(userDetailsServiceAdmin).passwordEncoder(passwordEncoderAdmin());}
}