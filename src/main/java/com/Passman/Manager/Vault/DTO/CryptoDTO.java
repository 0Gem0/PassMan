package com.Passman.Manager.Vault.DTO;

import com.Passman.Manager.Auth.POJO.KdfParams;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CryptoDTO {

    private boolean access;

    private KdfParams cryptoKdfParams;

    private String cryptoSalt;

    private String publicKey;

    private String encryptedPrivateKey;

    private String privateKeyIv;

    public CryptoDTO(boolean access,
                     KdfParams cryptoKdfParams,
                     String cryptoSalt,
                     String publicKey,
                     String encryptedPrivateKey,
                     String privateKeyIv) {
        this.access = access;
        this.cryptoKdfParams = cryptoKdfParams;
        this.cryptoSalt = cryptoSalt;
        this.publicKey = publicKey;
        this.encryptedPrivateKey = encryptedPrivateKey;
        this.privateKeyIv = privateKeyIv;
    }
}
