package ro.itexpert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${auth.required}")
    private boolean AUTHENTICATION_REQUIRED;

    @Value("${auth.user}")
    private String USER;

    @Value("${auth.password}")
    private String PASSWORD;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if (AUTHENTICATION_REQUIRED) {
            http.authorizeRequests()
                    .antMatchers("/search").hasRole("USER")
                    .and()
                .httpBasic();
        }
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
            .withUser(USER).password(PASSWORD).roles("USER");
    }
}
