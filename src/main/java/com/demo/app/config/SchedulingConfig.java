package com.demo.app.config;

import com.demo.app.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.concurrent.DelegatingSecurityContextScheduledExecutorService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.provisioning.UserDetailsManager;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.time.Instant.now;

@EnableScheduling
@Configuration
@RequiredArgsConstructor
public class SchedulingConfig implements SchedulingConfigurer {

    // Added just for consistency. Actually working without - just adding appropriate UsernamePasswordAuthenticationToken
    @Bean
    ApplicationRunner applicationRunner(UserDetailsManager userDetailsManager) {
        return args -> {
            User user = User.builder()
                    .username("admin")
                    .password("admin")
                    .createdAt(now())
                    .enabled(true)
                    .authorities(Set.of("ROLE_ADMIN"))
                    .build();
            userDetailsManager.createUser(user);
        };
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
    }

    @Bean
    public Executor taskExecutor() {
        ScheduledExecutorService delegateExecutor = Executors.newSingleThreadScheduledExecutor();
        SecurityContext schedulerContext = createSchedulerSecurityContext();
        return new DelegatingSecurityContextScheduledExecutorService(delegateExecutor, schedulerContext);
    }

    private SecurityContext createSchedulerSecurityContext() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_ADMIN");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "admin",
                "ROLE_ADMIN",
                authorities
        );
        context.setAuthentication(authentication);

        return context;
    }
}
