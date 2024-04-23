package funfit.community.post.entity;

import funfit.community.exception.ErrorCode;
import funfit.community.exception.customException.BusinessException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum Category {

    QUESTION("질문"), // 질문게시판
    WORKOUT("운동인증"), // 운동 인증
    DIET("식단인증"); // 식단 인증

    private final String name;

    public static Category find(String name) {
        return Arrays.stream(Category.values())
                .filter(category -> category.name.equals(name))
                .findAny()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CATEGORY));
    }
}
