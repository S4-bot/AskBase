package com.ssq.askbase.common.util;

import com.ssq.askbase.common.enums.ErrorCode;
import com.ssq.askbase.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class UserContextHolder {

    private static final String ANONYMOUS_USER = "anonymousUser";

    private UserContextHolder() {
    }

    public static CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (principal == null || ANONYMOUS_USER.equals(principal) || !(principal instanceof CurrentUser)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        CurrentUser currentUser = (CurrentUser) principal;
        if (currentUser.getUserId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return currentUser;
    }

    public static Long getUserId() {
        return getCurrentUser().getUserId();
    }

    public static String getUsername() {
        return getCurrentUser().getUsername();
    }
}
