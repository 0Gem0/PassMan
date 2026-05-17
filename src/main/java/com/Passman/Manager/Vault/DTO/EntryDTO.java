package com.Passman.Manager.Vault.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EntryDTO {

    private Long id;

    private String title;
    private String website;
    private String email;
    private String categoryName;
    private String note;

    /**
     * Зашифрованный пароль записи (base64)
     */
    private String passwordCipher;

    /**
     * IV для passwordCipher (base64)
     */
    private String passwordIv;

    /**
     * === НОВОЕ ===
     * Зашифрованный DEK записи (base64)
     */
    private String encryptedDek;

    /**
     * IV для DEK (только если KEK)
     */
    private String dekIv;

    /**
     * Тип envelope:
     * KEK / PRIVATE_KEY
     */
    private String dekEnvelopeType;

    private boolean canView;
    private boolean canEdit;

    private List<EntryRoleAccessDTO> roleAccesses;
    private List<EntryUserAccessDTO> userAccesses;

    public EntryDTO() {
    }
}