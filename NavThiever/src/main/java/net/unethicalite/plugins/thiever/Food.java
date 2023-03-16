package net.unethicalite.plugins.thiever;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum Food {

    LOBSTER("Lobster",379),
    TUNA_POTATO("Tuna potato",7060),
    SHARK("Shark",385),
    SALMON("Salmon",329),
    MANTA_RAY("Manta ray", 391),
    ANGLERFISH("Anglerfish", 13441);
    private final String name;
    private final int id;
}
