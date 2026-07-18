package com.Passman.Manager.management_roles;

public record EntryKeyView(
    Long id,
    Long entryId,
     Long userId,
    String dekEnvelopeType,
    String encryptedDek,
    String dekIv
){

}
