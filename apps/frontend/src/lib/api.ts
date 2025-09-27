import axios from 'axios';
import {
  CodeSnippet,
  CodeSnippetRequest,
  ExecutionResult,
  ExecutionRequest,
  ShareInfo,
  ShareRequest,
  PageResponse,
  ApiError,
} from '@/types';

// Runtime configuration getter
const getRuntimeConfig = () => {
  if (typeof window !== 'undefined') {
    return (window as any).__RUNTIME_CONFIG__ || {};
  }
  return {};
};

// Axios 인스턴스 생성
const api = axios.create({
  baseURL: getRuntimeConfig().API_BASE_URL || process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api/v1',
  timeout: 30000, // 30초
  headers: {
    'Content-Type': 'application/json',
  },
});

// 요청 인터셉터
api.interceptors.request.use(
  (config) => {
    console.log(`[API] ${config.method?.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => {
    console.error('[API] Request error:', error);
    return Promise.reject(error);
  }
);

// 응답 인터셉터
api.interceptors.response.use(
  (response) => {
    console.log(`[API] ${response.status} ${response.config.url}`);
    return response;
  },
  (error) => {
    console.error('[API] Response error:', error);

    if (error.response) {
      const apiError: ApiError = error.response.data;
      console.error('[API] Error details:', apiError);
    }

    return Promise.reject(error);
  }
);

// 코드 스니펫 API
export const codeSnippetApi = {
  // 모든 코드 스니펫 조회
  getAll: async (page = 0, size = 20): Promise<PageResponse<CodeSnippet>> => {
    const response = await api.get(`/snippets?page=${page}&size=${size}`);
    return response.data;
  },

  // 코드 스니펫 상세 조회
  getById: async (id: number): Promise<CodeSnippet> => {
    const response = await api.get(`/snippets/${id}`);
    return response.data;
  },

  // 코드 스니펫 생성
  create: async (request: CodeSnippetRequest): Promise<CodeSnippet> => {
    const response = await api.post('/snippets', request);
    return response.data;
  },

  // 코드 스니펫 수정
  update: async (id: number, request: CodeSnippetRequest): Promise<CodeSnippet> => {
    const response = await api.put(`/snippets/${id}`, request);
    return response.data;
  },

  // 코드 스니펫 삭제
  delete: async (id: number): Promise<void> => {
    await api.delete(`/snippets/${id}`);
  },

  // 작성자별 조회
  getByAuthor: async (authorName: string, page = 0, size = 20): Promise<PageResponse<CodeSnippet>> => {
    const response = await api.get(`/snippets/author/${encodeURIComponent(authorName)}?page=${page}&size=${size}`);
    return response.data;
  },

  // 언어별 조회
  getByLanguage: async (language: string, page = 0, size = 20): Promise<PageResponse<CodeSnippet>> => {
    const response = await api.get(`/snippets/language/${language}?page=${page}&size=${size}`);
    return response.data;
  },

  // 키워드 검색
  search: async (keyword: string, page = 0, size = 20): Promise<PageResponse<CodeSnippet>> => {
    const response = await api.get(`/snippets/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`);
    return response.data;
  },

  // 인기 코드 스니펫 조회
  getPopular: async (page = 0, size = 20): Promise<PageResponse<CodeSnippet>> => {
    const response = await api.get(`/snippets/popular?page=${page}&size=${size}`);
    return response.data;
  },
};

// 코드 실행 API
export const executionApi = {
  // 코드 실행
  execute: async (request: ExecutionRequest): Promise<ExecutionResult> => {
    const response = await api.post('/executions/execute', request);
    return response.data;
  },

  // 실행 기록 조회
  getById: async (executionId: number): Promise<ExecutionResult> => {
    const response = await api.get(`/executions/${executionId}`);
    return response.data;
  },

  // 코드 스니펫의 실행 기록 조회
  getHistory: async (codeSnippetId: number, page = 0, size = 20): Promise<PageResponse<ExecutionResult>> => {
    const response = await api.get(`/executions/snippet/${codeSnippetId}?page=${page}&size=${size}`);
    return response.data;
  },

  // 최근 실행 기록 조회
  getLatest: async (codeSnippetId: number): Promise<ExecutionResult | null> => {
    try {
      const response = await api.get(`/executions/snippet/${codeSnippetId}/latest`);
      return response.data;
    } catch (error) {
      // 404인 경우 null 반환
      if (axios.isAxiosError(error) && error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  // 상태별 실행 기록 조회
  getByStatus: async (status: string, page = 0, size = 20): Promise<PageResponse<ExecutionResult>> => {
    const response = await api.get(`/executions/status/${status}?page=${page}&size=${size}`);
    return response.data;
  },
};

// 공유 API
export const shareApi = {
  // 공유 링크 생성
  create: async (request: ShareRequest): Promise<ShareInfo> => {
    const response = await api.post('/shares', request);
    return response.data;
  },

  // 공유 정보 조회
  getByShareId: async (shareId: string): Promise<ShareInfo> => {
    const response = await api.get(`/shares/${shareId}`);
    return response.data;
  },

  // 공유 비활성화
  deactivate: async (shareId: string): Promise<void> => {
    await api.delete(`/shares/${shareId}`);
  },

  // 코드 스니펫의 공유 목록 조회
  getByCodeSnippet: async (codeSnippetId: number, page = 0, size = 20): Promise<PageResponse<ShareInfo>> => {
    const response = await api.get(`/shares/snippet/${codeSnippetId}?page=${page}&size=${size}`);
    return response.data;
  },

  // 최근 공유 조회
  getRecent: async (page = 0, size = 20): Promise<PageResponse<ShareInfo>> => {
    const response = await api.get(`/shares/recent?page=${page}&size=${size}`);
    return response.data;
  },

  // 곧 만료될 공유 조회
  getExpiringSoon: async (page = 0, size = 20): Promise<PageResponse<ShareInfo>> => {
    const response = await api.get(`/shares/expiring-soon?page=${page}&size=${size}`);
    return response.data;
  },

  // 만료된 공유 정리
  cleanupExpired: async (): Promise<{ deactivatedCount: number; message: string }> => {
    const response = await api.post('/shares/cleanup-expired');
    return response.data;
  },

  // 공유 통계 조회
  getStatistics: async (): Promise<{
    totalShares: number;
    activeShares: number;
    expiredShares: number;
    permanentShares: number;
  }> => {
    const response = await api.get('/shares/statistics');
    return response.data;
  },
};

export default api;