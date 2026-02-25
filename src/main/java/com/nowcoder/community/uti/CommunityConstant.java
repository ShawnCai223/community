package com.nowcoder.community.uti;

public interface CommunityConstant {
    int ACTIVATION_SUCCESS = 0;

    int ACTIVATION_REPEAT = 1;

    int ACTIVATION_FAILURE = 2;

    int DEFAULT_EXPIRED_SECONDS = 3600 * 12; // 12H

    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100; // ABOUT 3 MONTHS
}
