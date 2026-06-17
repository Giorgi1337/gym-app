package com.gym.utils;

import com.gym.dao.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsernameGeneratorTest {

    private UserDao userDao;
    private UsernameGenerator usernameGenerator;

    @BeforeEach
    void setUp() {
        userDao = mock(UserDao.class);
        usernameGenerator = new UsernameGenerator(userDao);
    }

    @Test
    void generate() {
        when(userDao.findUsernamesByPrefix("John.Doe"))
                .thenReturn(List.of(
                        "John.Doe",
                        "John.Doe1",
                        "John.Doe2"
                ));

        String res = usernameGenerator.generate("John", "Doe");

        assertThat(res).isEqualTo("John.Doe3");
    }

    @Test
    void returnBaseUserNameWhenBaseUsernameDoesNotExist() {
        when(userDao.findUsernamesByPrefix("John.Doe"))
                .thenReturn(List.of(
                        "John.Doe1",
                        "John.Doe2",
                        "John.Doe3"
                ));

        String res = usernameGenerator.generate("John", "Doe");

        assertThat(res).isEqualTo("John.Doe");
    }

}