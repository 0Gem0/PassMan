package com.Passman.Manager;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTest {

    @Test
    void verifiesModularStructure() {
        ApplicationModules modules = ApplicationModules.of(ManagerApplication.class);

        modules.verify();
    }
}