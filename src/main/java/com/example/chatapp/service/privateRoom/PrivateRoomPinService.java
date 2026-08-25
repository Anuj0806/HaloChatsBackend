package com.example.chatapp.service.privateRoom;

import com.example.chatapp.DTO.privateRoom.PinStatusResponse;

/**
 * The four-digit lock in front of the Private Room tab.
 *
 * Purely an access-control gate - see PrivateRoomPin's Javadoc for
 * why this must never be used as cryptographic key material for the
 * sealed chat itself.
 */
public interface PrivateRoomPinService {

    PinStatusResponse status(String phone);

    /**
     * @param currentPin required (and checked) if the account already
     *                   has a PIN; ignored for first-time setup
     */
    void setPin(String phone, String currentPin, String newPin);

    /** Throws with a friendly message on a wrong PIN or while locked out. */
    void verify(String phone, String pin);

    /**
     * Recovers a forgotten PIN using the account password instead of
     * the old PIN - the account password is the stronger credential
     * the person used to sign in with in the first place, so it's a
     * legitimate way to reset a weaker, secondary lock on top of it.
     * Clears any active lockout.
     */
    void resetWithAccountPassword(String phone, String accountPassword, String newPin);
}
