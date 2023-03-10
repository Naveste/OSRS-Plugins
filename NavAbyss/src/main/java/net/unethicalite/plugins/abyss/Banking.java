package net.unethicalite.plugins.abyss;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum Banking
{
    EDGEVILLE("Edgeville"),
    FEROX_ENCLAVE("Ferox Enclave"),
    CASTLE_WARS("Castle Wars");

    private final String name;
}
