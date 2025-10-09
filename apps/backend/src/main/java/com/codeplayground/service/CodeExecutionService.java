package com.codeplayground.service;

import com.codeplayground.entity.CodeSnippet;
import com.codeplayground.entity.Execution;
import com.codeplayground.entity.enums.ExecutionStatus;
import com.codeplayground.repository.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 코드 실행 서비스 (교육용 모킹 구현)
 * 실제 프로덕션 환경에서는 Docker 컨테이너나 샌드박스 환경에서 코드를 실행해야 합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CodeExecutionService {

    private final ExecutionRepository executionRepository;
    private final Random random = new Random();

    /**
     * 코드 실행 (모킹 구현)
     */
    public Execution executeCode(CodeSnippet codeSnippet, String customCode, String input, Integer timeoutSeconds) {
        log.info("Executing code for snippet ID: {}", codeSnippet.getId());

        String codeToExecute = customCode != null ? customCode : codeSnippet.getCode();

        try {
            // 실행 시간 시뮬레이션 (100ms ~ 2초)
            long executionTime = 100 + random.nextInt(1900);
            Thread.sleep(Math.min(executionTime, 500)); // 최대 0.5초만 실제로 대기

            // 실행 결과 생성
            ExecutionResult result = generateMockExecutionResult(codeSnippet.getLanguage(), codeToExecute, input, executionTime);

            // Execution 엔티티 생성
            Execution execution = Execution.builder()
                    .codeSnippet(codeSnippet)
                    .status(result.status)
                    .output(result.output)
                    .errorMessage(result.errorMessage)
                    .executionTime(result.executionTime)
                    .memoryUsage(result.memoryUsage)
                    .build();

            // 저장
            execution = executionRepository.save(execution);

            log.info("Code execution completed with status: {}", result.status);
            return execution;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Code execution interrupted", e);

            return Execution.builder()
                    .codeSnippet(codeSnippet)
                    .status(ExecutionStatus.TIMEOUT)
                    .errorMessage("실행이 중단되었습니다.")
                    .executionTime(timeoutSeconds != null ? timeoutSeconds * 1000L : 10000L)
                    .memoryUsage(0L)
                    .build();
        } catch (Exception e) {
            log.error("Error during code execution", e);

            return Execution.builder()
                    .codeSnippet(codeSnippet)
                    .status(ExecutionStatus.ERROR)
                    .errorMessage("실행 중 오류가 발생했습니다: " + e.getMessage())
                    .executionTime(0L)
                    .memoryUsage(0L)
                    .build();
        }
    }

    /**
     * 모킹된 실행 결과 생성
     */
    private ExecutionResult generateMockExecutionResult(String language, String code, String input, long executionTime) {
        ExecutionResult result = new ExecutionResult();
        result.executionTime = executionTime;
        result.memoryUsage = 1024L * 1024 * (1 + random.nextInt(50)); // 1MB ~ 50MB

        // 코드 분석을 통한 스마트 모킹
        if (containsHelloWorld(code)) {
            result.status = ExecutionStatus.SUCCESS;
            result.output = generateHelloWorldOutput(language);
        } else if (containsPrintStatement(code, language)) {
            result.status = ExecutionStatus.SUCCESS;
            result.output = generatePrintOutput(code, language, input);
        } else if (containsLoop(code)) {
            result.status = ExecutionStatus.SUCCESS;
            result.output = generateLoopOutput(language);
        } else if (containsSyntaxError(code, language)) {
            result.status = ExecutionStatus.ERROR;
            result.errorMessage = generateSyntaxError(language);
            result.output = null;
        } else if (random.nextInt(100) < 10) { // 10% 확률로 타임아웃
            result.status = ExecutionStatus.TIMEOUT;
            result.errorMessage = "실행 시간이 초과되었습니다.";
            result.output = null;
        } else if (random.nextInt(100) < 20) { // 20% 확률로 에러
            result.status = ExecutionStatus.ERROR;
            result.errorMessage = generateRandomError(language);
            result.output = null;
        } else {
            result.status = ExecutionStatus.SUCCESS;
            result.output = generateDefaultOutput(language, input);
        }

        return result;
    }

    private boolean containsHelloWorld(String code) {
        String lowerCode = code.toLowerCase();
        return lowerCode.contains("hello") && lowerCode.contains("world");
    }

    private boolean containsPrintStatement(String code, String language) {
        if ("javascript".equalsIgnoreCase(language)) {
            return code.contains("console.log");
        } else if ("python".equalsIgnoreCase(language)) {
            return code.contains("print");
        } else if ("java".equalsIgnoreCase(language)) {
            return code.contains("System.out");
        } else {
            return false;
        }
    }

    private boolean containsLoop(String code) {
        return code.contains("for") || code.contains("while") || code.contains("forEach");
    }

    private boolean containsSyntaxError(String code, String language) {
        // 간단한 문법 오류 감지
        if ("javascript".equalsIgnoreCase(language)) {
            return !code.contains(";") && code.length() > 20;
        } else if ("python".equalsIgnoreCase(language)) {
            return code.contains("print(") && !code.contains(")");
        } else if ("java".equalsIgnoreCase(language)) {
            return !code.contains("{") || !code.contains("}");
        } else {
            return false;
        }
    }

    private String generateHelloWorldOutput(String language) {
        return "Hello, World!";
    }

    private String generatePrintOutput(String code, String language, String input) {
        StringBuilder output = new StringBuilder();

        // 간단한 출력 추출 로직
        if ("javascript".equalsIgnoreCase(language)) {
            Pattern jsPattern = Pattern.compile("console\\.log\\([\"'](.*?)[\"']\\)");
            Matcher jsMatcher = jsPattern.matcher(code);
            while (jsMatcher.find()) {
                output.append(jsMatcher.group(1)).append("\n");
            }
        } else if ("python".equalsIgnoreCase(language)) {
            Pattern pyPattern = Pattern.compile("print\\([\"'](.*?)[\"']\\)");
            Matcher pyMatcher = pyPattern.matcher(code);
            while (pyMatcher.find()) {
                output.append(pyMatcher.group(1)).append("\n");
            }
        } else if ("java".equalsIgnoreCase(language)) {
            Pattern javaPattern = Pattern.compile("System\\.out\\.println\\([\"'](.*?)[\"']\\)");
            Matcher javaMatcher = javaPattern.matcher(code);
            while (javaMatcher.find()) {
                output.append(javaMatcher.group(1)).append("\n");
            }
        }

        if (output.length() == 0) {
            output.append("코드가 성공적으로 실행되었습니다.");
        }

        if (input != null && !input.trim().isEmpty()) {
            output.append("\n입력값: ").append(input);
        }

        return output.toString().trim();
    }

    private String generateLoopOutput(String language) {
        return "1\n2\n3\n4\n5\n루프 실행 완료";
    }

    private String generateSyntaxError(String language) {
        if ("javascript".equalsIgnoreCase(language)) {
            return "SyntaxError: Unexpected token at line 1";
        } else if ("python".equalsIgnoreCase(language)) {
            return "SyntaxError: invalid syntax at line 1";
        } else if ("java".equalsIgnoreCase(language)) {
            return "Error: ';' expected at line 1";
        } else {
            return "Syntax error in code";
        }
    }

    private String generateRandomError(String language) {
        String[] errors = {
            "ReferenceError: 변수를 찾을 수 없습니다",
            "TypeError: 잘못된 타입입니다",
            "RuntimeError: 런타임 오류가 발생했습니다",
            "NullPointerException: Null 값을 참조했습니다"
        };
        return errors[random.nextInt(errors.length)];
    }

    private String generateDefaultOutput(String language, String input) {
        StringBuilder output = new StringBuilder();
        output.append("프로그램이 성공적으로 실행되었습니다.\n");

        if ("javascript".equalsIgnoreCase(language)) {
            output.append("JavaScript 코드 실행 완료");
        } else if ("python".equalsIgnoreCase(language)) {
            output.append("Python 코드 실행 완료");
        } else if ("java".equalsIgnoreCase(language)) {
            output.append("Java 코드 실행 완료");
        }

        if (input != null && !input.trim().isEmpty()) {
            output.append("\n입력값: ").append(input);
        }

        return output.toString();
    }

    /**
     * 실행 결과를 담는 내부 클래스
     */
    private static class ExecutionResult {
        ExecutionStatus status;
        String output;
        String errorMessage;
        Long executionTime;
        Long memoryUsage;
    }
}