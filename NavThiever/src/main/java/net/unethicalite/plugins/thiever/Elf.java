package net.unethicalite.plugins.thiever;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum Elf {

    LINDIR("Lindir"),
    CELEBRIAN("Celebrian");
    private final String name;
}
