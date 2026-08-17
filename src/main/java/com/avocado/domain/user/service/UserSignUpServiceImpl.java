package com.avocado.domain.user.service;

import com.avocado.domain.user.domain.User;
import com.avocado.domain.user.domain.UserRole;
import com.avocado.domain.user.domain.UserStatus;
import com.avocado.domain.user.domain.UserType;
import com.avocado.domain.user.dto.request.EmailCheckRequestDto;
import com.avocado.domain.user.dto.request.UserSignUpRequestDto;
import com.avocado.domain.user.dto.response.EmailCheckResponseDto;
import com.avocado.domain.user.dto.response.UserSignUpResponseDto;
import com.avocado.domain.user.mapper.UserMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.global.util.EmailNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSignUpServiceImpl implements UserSignUpService {

    // 초대 코드는 대문자와 숫자 6자리 (36^6 ≈ 21.7억 가지)
    private static final String INVITE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 6;

    // 초대 코드가 겹칠 확률은 낮지만, 겹치더라도 무한히 다시 뽑지는 않는다.
    private static final int INVITE_CODE_MAX_ATTEMPTS = 5;

    // UNIQUE 제약 위반 메시지에서 인덱스 이름 앞에 붙는 부분
    private static final String DUPLICATE_KEY_MARKER = "for key";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // SecureRandom은 스레드 세이프하므로 요청마다 새로 만들지 않는다.
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 회원을 등록하고, 가입 직후 이동할 화면을 정하는 데 필요한 정보를 반환한다.
     *
     * @param request 회원가입 요청 (이름, 이메일, 비밀번호, 전화번호, 생년월일, 회원 타입)
     * @return 가입한 회원 정보
     * @throws BusinessException 이메일 또는 전화번호가 이미 사용 중인 경우
     */
    @Override
    @Transactional
    public UserSignUpResponseDto signUp(UserSignUpRequestDto request) {
        // TODO: 이메일이 아이디로 대체되면 지우기 (정규화 없이 request 값을 그대로 저장)
        String email = EmailNormalizer.normalize(request.getEmail());
        String phone = normalizePhone(request.getPhone());

        // 이메일과 전화번호는 UNIQUE라 탈퇴한 회원이 쓰던 값도 다시 쓸 수 없다.
        if (userMapper.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        if (userMapper.existsByPhone(phone)) {
            throw new BusinessException(ErrorCode.DUPLICATE_PHONE);
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(phone)
                .birth(request.getBirth())
                .userType(request.getType())
                // 가입 단계에서는 권한을 선택할 수 없다. 요청 값을 받지 않고 USER로 고정한다.
                .role(UserRole.USER)
                // 부모는 계좌 연결, 아이는 가족 연결을 마쳐야 ACTIVE가 된다.
                .status(UserStatus.PENDING)
                // 초대 코드는 가족을 초대하는 부모에게만 발급한다.
                .inviteCode(request.getType() == UserType.PARENT ? generateInviteCode() : null)
                .build();

        insertUser(user);

        return UserSignUpResponseDto.builder()
                .userId(user.getId())
                .name(user.getName())
                .type(user.getUserType())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }

    /**
     * 가입 전에 이메일이 이미 쓰이고 있는지 미리 알려준다.

     * @param request 확인할 이메일
     * @return 확인한 이메일과 사용 가능 여부
     */
    @Override
    public EmailCheckResponseDto checkEmail(EmailCheckRequestDto request) {
        // TODO: 이메일이 아이디로 대체되면 지우기 (정규화 없이 request 값을 그대로 조회)
        String email = EmailNormalizer.normalize(request.getEmail());

        return EmailCheckResponseDto.builder()
                // 어떤 값으로 확인했는지 그대로 돌려줘야 화면이 입력값과 대조할 수 있다.
                // TODO: 이메일이 아이디로 대체되면 응답에서 email 필드를 빼도 된다.
                .email(email)
                .available(!userMapper.existsByEmail(email))
                .build();
    }

    // 하이픈은 화면에서 보기 좋으라고 넣는 표시용이라 숫자만 남겨서 저장한다.
    // 표기만 다른 같은 번호가 UNIQUE를 통과해 두 번 가입되는 것을 막는다.
    private String normalizePhone(String phone) {
        return phone.replaceAll("\\D", "");
    }

    // invite_code는 UNIQUE라 이미 쓰이는 값이면 다시 뽑는다.
    private String generateInviteCode() {
        for (int attempt = 0; attempt < INVITE_CODE_MAX_ATTEMPTS; attempt++) {
            String inviteCode = randomInviteCode();

            if (!userMapper.existsByInviteCode(inviteCode)) {
                return inviteCode;
            }
        }

        // 여기까지 왔다면 사용자가 다시 시도해서 풀 수 있는 문제가 아니다.
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private String randomInviteCode() {
        StringBuilder inviteCode = new StringBuilder(INVITE_CODE_LENGTH);

        for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
            inviteCode.append(
                    INVITE_CODE_CHARS.charAt(secureRandom.nextInt(INVITE_CODE_CHARS.length()))
            );
        }

        return inviteCode.toString();
    }

    // 중복 검사와 INSERT 사이에 같은 값으로 가입 요청이 들어오면 UNIQUE 제약에 걸린다.
    // 이 경우에도 500이 아니라 중복 안내로 응답해야 사용자가 다음 행동을 알 수 있다.
    private void insertUser(User user) {
        try {
            userMapper.insertUser(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(resolveDuplicateErrorCode(e));
        }
    }

    // 어떤 값이 겹쳤는지는 MySQL이 예외 메시지에 담아주는 인덱스 이름으로 판단한다.
    // 메시지 형식: Duplicate entry 'a@b.com' for key 'users.email'
    private ErrorCode resolveDuplicateErrorCode(DuplicateKeyException e) {
        String message = e.getMessage() == null
                ? ""
                : e.getMessage().toLowerCase(Locale.ROOT);

        // 입력값이 메시지 앞부분에 그대로 실리므로(myphone@example.com 등)
        // 인덱스 이름이 있는 뒷부분만 잘라서 본다.
        int keyPosition = message.lastIndexOf(DUPLICATE_KEY_MARKER);
        String duplicatedKey = keyPosition < 0
                ? ""
                : message.substring(keyPosition);

        if (duplicatedKey.contains("phone")) {
            return ErrorCode.DUPLICATE_PHONE;
        }

        if (duplicatedKey.contains("email")) {
            return ErrorCode.DUPLICATE_EMAIL;
        }

        // 초대 코드 충돌처럼 사용자가 입력을 고쳐서 해결할 수 없는 경우
        return ErrorCode.INTERNAL_SERVER_ERROR;
    }
}
