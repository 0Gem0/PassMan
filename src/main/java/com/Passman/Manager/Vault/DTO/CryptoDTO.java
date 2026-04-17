package com.Passman.Manager.Vault.DTO;

import com.Passman.Manager.Auth.POJO.KdfParams;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CryptoDTO {
    private final boolean access;
    private final KdfParams cryptoKdfParams;
    private final byte[] cryptoSalt;
    private final byte[] encryptedDEK;
    private final byte[] encryptedDEK_iv;

    public CryptoDTO(boolean access, KdfParams cryptoKdfParams, byte[] encryptedDEK, byte[] encryptedDEK_iv, byte[] cryptoSalt) {
        this.access = access;
        this.cryptoKdfParams = cryptoKdfParams;
        this.encryptedDEK = encryptedDEK;
        this.encryptedDEK_iv= encryptedDEK_iv;
        this.cryptoSalt = cryptoSalt;
    }
}
