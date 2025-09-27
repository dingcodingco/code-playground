// 프로그래밍 언어 타입
export type ProgrammingLanguage = 'JAVASCRIPT' | 'PYTHON' | 'JAVA';

// 실행 상태 타입
export type ExecutionStatus = 'SUCCESS' | 'ERROR' | 'TIMEOUT';

// 코드 스니펫 타입
export interface CodeSnippet {
  id: number;
  title: string;
  code: string;
  language: ProgrammingLanguage;
  authorName: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  executionCount: number;
  shareCount: number;
}

// 코드 스니펫 생성 요청 타입
export interface CodeSnippetRequest {
  title: string;
  code: string;
  language: ProgrammingLanguage;
  authorName: string;
}

// 실행 결과 타입
export interface ExecutionResult {
  id: number;
  codeSnippetId: number;
  status: ExecutionStatus;
  output?: string;
  errorMessage?: string;
  executionTime: number;
  memoryUsage?: number;
  createdAt: string;
}

// 실행 요청 타입
export interface ExecutionRequest {
  codeSnippetId: number;
  customCode?: string;
  input?: string;
  timeoutSeconds?: number;
}

// 공유 정보 타입
export interface ShareInfo {
  id: number;
  codeSnippetId: number;
  shareId: string;
  shareUrl: string;
  expiresAt?: string;
  isActive: boolean;
  createdAt: string;
  codeSnippet: CodeSnippet;
}

// 공유 요청 타입
export interface ShareRequest {
  codeSnippetId: number;
  expirationDays?: number;
}

// 페이징 응답 타입
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

// API 에러 타입
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors?: Record<string, string>;
}

// 언어별 설정
export const LANGUAGE_CONFIG = {
  JAVASCRIPT: {
    name: 'JavaScript',
    extension: 'js',
    monacoLanguage: 'javascript',
    defaultCode: 'console.log("Hello, World!");',
    color: '#f7df1e',
  },
  PYTHON: {
    name: 'Python',
    extension: 'py',
    monacoLanguage: 'python',
    defaultCode: 'print("Hello, World!")',
    color: '#3776ab',
  },
  JAVA: {
    name: 'Java',
    extension: 'java',
    monacoLanguage: 'java',
    defaultCode: `public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}`,
    color: '#ed8b00',
  },
} as const;