package com.vibecoding.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 코드 실행 엔진 서비스.
 * 다양한 프로그래밍 언어의 코드를 실행하는 역할을 담당합니다.
 *
 * TODO: 실제 코드 실행 엔진 구현 필요 (Docker 컨테이너 기반)
 */
@Service
@Slf4j
public class CodeExecutorService {

    /**
     * 코드를 실행합니다.
     *
     * @param code           실행할 코드
     * @param language       프로그래밍 언어
     * @param input          입력 데이터 (stdin)
     * @param timeoutSeconds 타임아웃 (초)
     * @return 실행 결과 출력
     * @throws Exception 실행 중 오류 발생 시
     */
    public String executeCode(final String code,
                              final String language,
                              final String input,
                              final Integer timeoutSeconds) throws Exception {

        log.info("Executing {} code with timeout: {}s", language, timeoutSeconds);
        log.debug("Code to execute:\n{}", code);

        // TODO: 실제 코드 실행 로직 구현
        // 현재는 모의(Mock) 구현으로 대체
        return executeCodeMock(code, language, input, timeoutSeconds);
    }

    /**
     * 코드 실행 모의 구현.
     * 실제 구현 전까지 사용할 임시 메서드입니다.
     *
     * @param code           실행할 코드
     * @param language       프로그래밍 언어
     * @param input          입력 데이터
     * @param timeoutSeconds 타임아웃
     * @return 모의 실행 결과
     */
    private String executeCodeMock(final String code,
                                   final String language,
                                   final String input,
                                   final Integer timeoutSeconds) {

        // 간단한 모의 응답 생성
        return switch (language.toLowerCase()) {
            case "javascript" -> {
                if (code.contains("console.log")) {
                    yield "Hello from JavaScript!\n실행 시간: 42ms";
                }
                yield "JavaScript 코드가 성공적으로 실행되었습니다.";
            }
            case "python" -> {
                if (code.contains("print")) {
                    yield "Hello from Python!\n실행 시간: 38ms";
                }
                yield "Python 코드가 성공적으로 실행되었습니다.";
            }
            case "java" -> {
                if (code.contains("System.out.println")) {
                    yield "Hello from Java!\n실행 시간: 156ms";
                }
                yield "Java 코드가 성공적으로 컴파일 및 실행되었습니다.";
            }
            default -> language + " 코드가 실행되었습니다.\n(모의 실행 결과)";
        };
    }

    /**
     * 지원하는 프로그래밍 언어인지 확인합니다.
     *
     * @param language 프로그래밍 언어
     * @return 지원 여부
     */
    public boolean isLanguageSupported(final String language) {
        return switch (language.toLowerCase()) {
            case "javascript", "python", "java" -> true;
            default -> false;
        };
    }

    /**
     * 언어별 기본 타임아웃을 반환합니다.
     *
     * @param language 프로그래밍 언어
     * @return 기본 타임아웃 (초)
     */
    public int getDefaultTimeout(final String language) {
        return switch (language.toLowerCase()) {
            case "java" -> 15; // 컴파일 시간 고려
            case "python" -> 10;
            case "javascript" -> 5;
            default -> 10;
        };
    }
}