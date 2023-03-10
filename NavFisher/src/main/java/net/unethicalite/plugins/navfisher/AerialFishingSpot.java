package net.unethicalite.plugins.navfisher;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum AerialFishingSpot {

    EAST("East"),
    WEST("West");

    private final String name;
}
