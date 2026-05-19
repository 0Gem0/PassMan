package com.Passman.Manager.management_roles.internal.Models;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
        name = "entry_keys",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_entry_keys_entry_user", columnNames = {"entry_id", "user_id"})
        }
)
public class EntryKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Запись, к которой относится ключ
     */

    @Column(name = "entry_id", nullable = false)
    private Long entryId;

    /**
     * Пользователь, для которого зашифрован entryDEK
     */

    @Column(name = "user_id", nullable = false)
    private Long userId;


    /**
     * Тип envelope:
     * KEK / PRIVATE_KEY
     */
    @Column(name = "dek_envelope_type", nullable = false, length = 32)
    private String dekEnvelopeType;

    /**
     * Зашифрованный ключ записи
     */
    @Column(name = "encrypted_dek", nullable = false, columnDefinition = "TEXT")
    private String encryptedDek;

    /**
     * IV для AES-GCM envelope
     * Для PRIVATE_KEY может быть null
     */
    @Column(name = "dek_iv")
    private String dekIv;


}