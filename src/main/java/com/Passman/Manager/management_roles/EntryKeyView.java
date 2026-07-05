package com.Passman.Manager.management_roles;

import jakarta.persistence.Column;

public record EntryKeyView(
    Long id,
    Long entryId,
     Long userId,
    String dekEnvelopeType,
    String encryptedDek,
    String dekIv
){

}
