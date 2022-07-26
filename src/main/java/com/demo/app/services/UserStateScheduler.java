package com.demo.app.services;

import com.demo.app.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserStateScheduler {
    private final UserService userService;

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteNotEnabledUsers() {
        List<User> disabledUsers = userService.getDisabledUsers();
        log.info("About to delete {} not enabled users", disabledUsers.size());
        userService.removeUsers(disabledUsers);
        log.info("Not enabled users have been deleted");
    }
}
