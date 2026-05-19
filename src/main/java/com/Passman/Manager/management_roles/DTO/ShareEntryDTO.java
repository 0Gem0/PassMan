package com.Passman.Manager.management_roles.DTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShareEntryDTO {

    private Long entryId;
    private Long targetUserId;
    private String dekEnvelopeType;
    private String encryptedDek;
}