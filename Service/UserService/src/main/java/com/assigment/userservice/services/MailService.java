package com.assigment.userservice.services;

import com.assigment.userservice.dto.response.ResetMessage;
import com.assigment.userservice.dto.response.VerificationMessage;

public interface MailService {
    void handleVerification(VerificationMessage message);

    void handlePasswordReset(ResetMessage message);
}
