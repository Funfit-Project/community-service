package funfit.community.post.entity;

import funfit.community.exception.ErrorCode;
import funfit.community.exception.customException.BusinessException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum Category {

    QUESTION("질문"),
    FREE("자유"),
    INFO("정보");

    private final String name;

    public static Category find(String name) {
        return Arrays.stream(Category.values())
                .filter(category -> category.name.equals(name))
                .findAny()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CATEGORY));
    }
}
