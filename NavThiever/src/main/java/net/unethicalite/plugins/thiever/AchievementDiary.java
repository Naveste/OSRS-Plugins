package net.unethicalite.plugins.thiever;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum AchievementDiary {

    NONE("None/Easy"),
    MEDIUM("Medium"),
    HARD("Hard"),
    ELITE("Elite");

    private final String diary;

}
