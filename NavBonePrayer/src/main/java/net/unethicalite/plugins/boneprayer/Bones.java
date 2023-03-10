package net.unethicalite.plugins.boneprayer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum Bones
{
	NORMAL_BONES("Bones", 526),
	BIG_BONES("Big bones", 532),
	WYVERN_BONES("Wyvern bones", 6812);
	private final String name;
	private final int ID;
}
