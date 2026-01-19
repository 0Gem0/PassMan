package com.Passman.Manager.Vault.DTO;

import com.Passman.Manager.Auth.POJO.KdfParams;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CryptoDTO {
    private final boolean access;
    private final KdfParams kdfParams;
    private final byte[] encryptedDek;
    private final byte[] encryptedDekIv;

    public CryptoDTO(boolean access, KdfParams kdfParams, byte[] encryptedDek, byte[] encryptedDekIv) {
        this.access = access;
        this.kdfParams = kdfParams;
        this.encryptedDek = encryptedDek;
        this.encryptedDekIv = encryptedDekIv;
    }


}
