package com.Passman.Manager.shared.util;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
