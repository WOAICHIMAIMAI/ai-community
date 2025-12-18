package com.zheng.aicommunitybackend.domain.enums;

public enum PostCategoryEnum {
    ANNOUNCEMENT("公告"),
    SHARE("分享"),
    DYNAMIC("动态"),
    SUGGESTION("建议"),
    THANKS("感谢"),
    COMPLAINT("投诉"),
    HELP("求助"),
    ACTIVITY("活动"),
    WARNING("警示"),
    DISCUSSION("讨论"),
    IDLE("闲置");

    private String name;

    PostCategoryEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}