package com.config;

import com.global.filter.JwtAuthenticationTokenFilter;
import com.util.JwtAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Autowired
    private UserDetailsService userDetailsService;

    @Value("${jwt.route.authentication.path}")
    private String authPath;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(this.userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        //allow Swagger URL to be accessed without authentication
        web.ignoring().antMatchers("/v2/api-docs",
                "/swagger-resources/configuration/ui",
                "/swagger-resources",
                "/swagger-resources/configuration/security",
                "/swagger-ui.html",
                "/webjars/**");
    }

    @Bean
    public JwtAuthenticationTokenFilter authenticationTokenFilterBean() throws Exception {
        return new JwtAuthenticationTokenFilter();
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable().exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
                .and()
                // don't create session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // allow anonymous resource requests
                .antMatchers(
//                        HttpMethod.GET,
                        "/favicon.ico",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js"
                ).permitAll()
                .antMatchers("/" + authPath,
                        "/login/init").permitAll()
                .anyRequest().authenticated();

        /**
         * 每次请求过来时, 我们将获取请求的Authorization头部存有的jwt, 并提取相应的信息,
         * 如果当前security的上下文还没有认证对应的用户信息并且token是有效的,
         * 那么就将认证成功所返回的信息设置在security的上下文中,
         * 最后再将请求传递给下一个过滤器
         */
        httpSecurity.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);
        // disable page caching
        httpSecurity.headers().cacheControl();

        // custom JSON based authentication by POST of {"username":"<name>","password":"<password>"} which sets the token header upon authentication
//        httpSecurity.addFilterBefore(loginFilter(), UsernamePasswordAuthenticationFilter.class);

        // custom Token based authentication based on the header previously given to the client
//        httpSecurity.addFilterBefore(new StatelessTokenAuthenticationFilter(tokenAuthenticationService), UsernamePasswordAuthenticationFilter.class);

    }
}