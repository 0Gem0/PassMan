package com.Passman.Manager.vault;

import java.util.List;

public interface VaultApi {

    List<EntryView> findAll();

    List<EntryView> findAccessibleEntries(Long userId);

    EntryView getEntryView(Long entryId);

    boolean entryExists(Long entryId);
}
