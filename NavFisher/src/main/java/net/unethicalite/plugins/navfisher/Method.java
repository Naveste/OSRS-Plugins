package net.unethicalite.plugins.navfisher;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum Method {

    AERIAL_FISHING("Aerial fishing");

    private final String name;
}
