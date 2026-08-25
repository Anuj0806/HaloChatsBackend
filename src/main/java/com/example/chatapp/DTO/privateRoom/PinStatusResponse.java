package com.example.chatapp.DTO.privateRoom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PinStatusResponse {
    private boolean hasPin;

    /** True while locked out from too many wrong guesses. */
    private boolean locked;

    /** How long the caller still has to wait, if locked. */
    private long lockedForSeconds;
}
