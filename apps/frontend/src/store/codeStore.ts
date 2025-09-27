import { create } from 'zustand';
import {
  CodeSnippet,
  CodeSnippetRequest,
  ExecutionResult,
  ExecutionRequest,
  ShareInfo,
  ShareRequest,
  ProgrammingLanguage,
  LANGUAGE_CONFIG,
} from '@/types';
import { codeSnippetApi, executionApi, shareApi } from '@/lib/api';
import toast from 'react-hot-toast';

interface CodeState {
  // 현재 코드 에디터 상태
  currentCode: string;
  currentLanguage: ProgrammingLanguage;
  currentTitle: string;
  currentAuthor: string;

  // 코드 스니펫 관련
  snippets: CodeSnippet[];
  currentSnippet: CodeSnippet | null;

  // 실행 관련
  executionResult: ExecutionResult | null;
  isExecuting: boolean;
  executionHistory: ExecutionResult[];

  // 공유 관련
  shareInfo: ShareInfo | null;
  isSharing: boolean;

  // UI 상태
  isLoading: boolean;
  error: string | null;
}

interface CodeActions {
  // 에디터 액션
  setCode: (code: string) => void;
  setLanguage: (language: ProgrammingLanguage) => void;
  setTitle: (title: string) => void;
  setAuthor: (author: string) => void;
  resetEditor: () => void;

  // 코드 스니펫 액션
  loadSnippets: () => Promise<void>;
  loadSnippet: (id: number) => Promise<void>;
  saveSnippet: () => Promise<void>;
  updateSnippet: (id: number) => Promise<void>;
  deleteSnippet: (id: number) => Promise<void>;
  searchSnippets: (keyword: string) => Promise<void>;

  // 실행 액션
  executeCode: (customCode?: string, input?: string) => Promise<void>;
  loadExecutionHistory: (snippetId: number) => Promise<void>;

  // 공유 액션
  shareCode: (expirationDays?: number) => Promise<ShareInfo | null>;
  loadSharedCode: (shareId: string) => Promise<void>;

  // 유틸리티 액션
  clearError: () => void;
  setError: (error: string) => void;
}

type CodeStore = CodeState & CodeActions;

const initialState: CodeState = {
  currentCode: LANGUAGE_CONFIG.JAVASCRIPT.defaultCode,
  currentLanguage: 'JAVASCRIPT',
  currentTitle: '새로운 코드',
  currentAuthor: '익명',
  snippets: [],
  currentSnippet: null,
  executionResult: null,
  isExecuting: false,
  executionHistory: [],
  shareInfo: null,
  isSharing: false,
  isLoading: false,
  error: null,
};

export const useCodeStore = create<CodeStore>((set, get) => ({
  ...initialState,

  // 에디터 액션
  setCode: (code) => set({ currentCode: code }),

  setLanguage: (language) => {
    const config = LANGUAGE_CONFIG[language];
    set({
      currentLanguage: language,
      currentCode: config.defaultCode,
    });
  },

  setTitle: (title) => set({ currentTitle: title }),

  setAuthor: (author) => set({ currentAuthor: author }),

  resetEditor: () => {
    const { currentLanguage } = get();
    const config = LANGUAGE_CONFIG[currentLanguage];
    set({
      currentCode: config.defaultCode,
      currentTitle: '새로운 코드',
      currentSnippet: null,
      executionResult: null,
      shareInfo: null,
      error: null,
    });
  },

  // 코드 스니펫 액션
  loadSnippets: async () => {
    set({ isLoading: true, error: null });
    try {
      const response = await codeSnippetApi.getAll(0, 50);
      set({ snippets: response.content, isLoading: false });
    } catch (error) {
      console.error('Failed to load snippets:', error);
      set({
        error: '코드 스니펫을 불러오는데 실패했습니다.',
        isLoading: false
      });
      toast.error('코드 스니펫을 불러오는데 실패했습니다.');
    }
  },

  loadSnippet: async (id) => {
    set({ isLoading: true, error: null });
    try {
      const snippet = await codeSnippetApi.getById(id);
      set({
        currentSnippet: snippet,
        currentCode: snippet.code,
        currentLanguage: snippet.language,
        currentTitle: snippet.title,
        currentAuthor: snippet.authorName,
        isLoading: false,
      });
    } catch (error) {
      console.error('Failed to load snippet:', error);
      set({
        error: '코드 스니펫을 불러오는데 실패했습니다.',
        isLoading: false
      });
      toast.error('코드 스니펫을 불러오는데 실패했습니다.');
    }
  },

  saveSnippet: async () => {
    const { currentCode, currentLanguage, currentTitle, currentAuthor } = get();

    if (!currentCode.trim()) {
      set({ error: '코드를 입력해주세요.' });
      toast.error('코드를 입력해주세요.');
      return;
    }

    set({ isLoading: true, error: null });
    try {
      const request: CodeSnippetRequest = {
        title: currentTitle,
        code: currentCode,
        language: currentLanguage,
        authorName: currentAuthor,
      };

      const snippet = await codeSnippetApi.create(request);

      set((state) => ({
        currentSnippet: snippet,
        snippets: [snippet, ...state.snippets],
        isLoading: false,
      }));

      toast.success('코드 스니펫이 저장되었습니다.');
    } catch (error) {
      console.error('Failed to save snippet:', error);
      set({
        error: '코드 스니펫 저장에 실패했습니다.',
        isLoading: false
      });
      toast.error('코드 스니펫 저장에 실패했습니다.');
    }
  },

  updateSnippet: async (id) => {
    const { currentCode, currentLanguage, currentTitle, currentAuthor } = get();

    set({ isLoading: true, error: null });
    try {
      const request: CodeSnippetRequest = {
        title: currentTitle,
        code: currentCode,
        language: currentLanguage,
        authorName: currentAuthor,
      };

      const updatedSnippet = await codeSnippetApi.update(id, request);

      set((state) => ({
        currentSnippet: updatedSnippet,
        snippets: state.snippets.map(s => s.id === id ? updatedSnippet : s),
        isLoading: false,
      }));

      toast.success('코드 스니펫이 수정되었습니다.');
    } catch (error) {
      console.error('Failed to update snippet:', error);
      set({
        error: '코드 스니펫 수정에 실패했습니다.',
        isLoading: false
      });
      toast.error('코드 스니펫 수정에 실패했습니다.');
    }
  },

  deleteSnippet: async (id) => {
    set({ isLoading: true, error: null });
    try {
      await codeSnippetApi.delete(id);

      set((state) => ({
        snippets: state.snippets.filter(s => s.id !== id),
        currentSnippet: state.currentSnippet?.id === id ? null : state.currentSnippet,
        isLoading: false,
      }));

      toast.success('코드 스니펫이 삭제되었습니다.');
    } catch (error) {
      console.error('Failed to delete snippet:', error);
      set({
        error: '코드 스니펫 삭제에 실패했습니다.',
        isLoading: false
      });
      toast.error('코드 스니펫 삭제에 실패했습니다.');
    }
  },

  searchSnippets: async (keyword) => {
    if (!keyword.trim()) {
      get().loadSnippets();
      return;
    }

    set({ isLoading: true, error: null });
    try {
      const response = await codeSnippetApi.search(keyword, 0, 50);
      set({ snippets: response.content, isLoading: false });
    } catch (error) {
      console.error('Failed to search snippets:', error);
      set({
        error: '검색에 실패했습니다.',
        isLoading: false
      });
      toast.error('검색에 실패했습니다.');
    }
  },

  // 실행 액션
  executeCode: async (customCode, input) => {
    const { currentSnippet, currentCode } = get();

    if (!currentSnippet) {
      set({ error: '먼저 코드 스니펫을 저장해주세요.' });
      toast.error('먼저 코드 스니펫을 저장해주세요.');
      return;
    }

    set({ isExecuting: true, error: null, executionResult: null });
    try {
      const request: ExecutionRequest = {
        codeSnippetId: currentSnippet.id,
        customCode: customCode || currentCode,
        input,
        timeoutSeconds: 10,
      };

      const result = await executionApi.execute(request);
      set({ executionResult: result, isExecuting: false });

      if (result.status === 'SUCCESS') {
        toast.success('코드가 성공적으로 실행되었습니다.');
      } else {
        toast.error('코드 실행 중 오류가 발생했습니다.');
      }
    } catch (error) {
      console.error('Failed to execute code:', error);
      set({
        error: '코드 실행에 실패했습니다.',
        isExecuting: false
      });
      toast.error('코드 실행에 실패했습니다.');
    }
  },

  loadExecutionHistory: async (snippetId) => {
    try {
      const response = await executionApi.getHistory(snippetId, 0, 20);
      set({ executionHistory: response.content });
    } catch (error) {
      console.error('Failed to load execution history:', error);
      toast.error('실행 기록을 불러오는데 실패했습니다.');
    }
  },

  // 공유 액션
  shareCode: async (expirationDays) => {
    const { currentSnippet } = get();

    if (!currentSnippet) {
      set({ error: '먼저 코드 스니펫을 저장해주세요.' });
      toast.error('먼저 코드 스니펫을 저장해주세요.');
      return null;
    }

    set({ isSharing: true, error: null });
    try {
      const request: ShareRequest = {
        codeSnippetId: currentSnippet.id,
        expirationDays,
      };

      const shareInfo = await shareApi.create(request);
      set({ shareInfo, isSharing: false });

      toast.success('공유 링크가 생성되었습니다.');
      return shareInfo;
    } catch (error) {
      console.error('Failed to share code:', error);
      set({
        error: '공유 링크 생성에 실패했습니다.',
        isSharing: false
      });
      toast.error('공유 링크 생성에 실패했습니다.');
      return null;
    }
  },

  loadSharedCode: async (shareId) => {
    set({ isLoading: true, error: null });
    try {
      const shareInfo = await shareApi.getByShareId(shareId);
      const snippet = shareInfo.codeSnippet;

      set({
        shareInfo,
        currentSnippet: snippet,
        currentCode: snippet.code,
        currentLanguage: snippet.language,
        currentTitle: snippet.title,
        currentAuthor: snippet.authorName,
        isLoading: false,
      });
    } catch (error) {
      console.error('Failed to load shared code:', error);
      set({
        error: '공유된 코드를 불러오는데 실패했습니다.',
        isLoading: false
      });
      toast.error('공유된 코드를 불러오는데 실패했습니다.');
    }
  },

  // 유틸리티 액션
  clearError: () => set({ error: null }),

  setError: (error) => set({ error }),
}));