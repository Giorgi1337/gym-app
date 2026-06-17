package com.gym.utils;

import com.gym.dao.UserDao;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class UsernameGenerator {

    private static final Pattern SUFFIX_PATTERN = Pattern.compile("^\\d+$");

    private final UserDao userDao;

    public UsernameGenerator(UserDao userDao) {
        this.userDao = userDao;
    }

    public final String generate(final String firstName, final String lastName) {
        String base = firstName + "." + lastName;

        List<String> usernames = userDao.findUsernamesByPrefix(base);

        int maxSuffix = 0;
        boolean baseExists = false;

        for (String username : usernames) {
            if (base.equals(username)) {
                baseExists = true;
                continue;
            }

            if (!username.startsWith(base)) {
                continue;
            }

            String suffix = username.substring(base.length());

            if (isNumeric(suffix)) {
                int value = Integer.parseInt(suffix);
                if (value > maxSuffix) {
                    maxSuffix = value;
                }
            }
        }

        return baseExists ? base + (maxSuffix + 1) : base;
    }

    private boolean isNumeric(String value) {
        return value != null && SUFFIX_PATTERN.matcher(value).matches();
    }

}

