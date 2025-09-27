'use client';

import { useState, useEffect } from 'react';
import { useCodeStore } from '@/store/codeStore';
import { LANGUAGE_CONFIG } from '@/types';

interface SnippetsListProps {
  className?: string;
}

export default function SnippetsList({ className = '' }: SnippetsListProps) {
  const {
    snippets,
    loadSnippets,
    loadSnippet,
    deleteSnippet,
    searchSnippets,
    isLoading,
    currentSnippet
  } = useCodeStore();

  const [searchQuery, setSearchQuery] = useState('');
  const [showDeleteConfirm, setShowDeleteConfirm] = useState<number | null>(null);

  useEffect(() => {
    loadSnippets();
  }, [loadSnippets]);

  const handleSearch = (query: string) => {
    setSearchQuery(query);
    if (query.trim()) {
      searchSnippets(query);
    } else {
      loadSnippets();
    }
  };

  const handleLoadSnippet = async (id: number) => {
    await loadSnippet(id);
  };

  const handleDeleteSnippet = async (id: number) => {
    await deleteSnippet(id);
    setShowDeleteConfirm(null);
    // Reload the list after deletion
    if (searchQuery.trim()) {
      searchSnippets(searchQuery);
    } else {
      loadSnippets();
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('ko-KR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const truncateCode = (code: string, maxLength: number = 100) => {
    if (code.length <= maxLength) return code;
    return code.substring(0, maxLength) + '...';
  };

  return (
    <>
      <div className={`bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg ${className}`}>
        <div className="p-4 border-b border-gray-200 dark:border-gray-700">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
              코드 스니펫
            </h3>
            <button
              onClick={() => loadSnippets()}
              disabled={isLoading}
              className="px-3 py-1.5 text-sm bg-gray-100 hover:bg-gray-200 dark:bg-gray-700
                       dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300 rounded-md
                       transition-colors disabled:opacity-50"
            >
              {isLoading ? '로딩...' : '새로고침'}
            </button>
          </div>

          {/* Search */}
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <svg className="h-4 w-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => handleSearch(e.target.value)}
              className="block w-full pl-10 pr-3 py-2 border border-gray-300 dark:border-gray-600
                       rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100
                       focus:ring-2 focus:ring-primary-500 focus:border-primary-500
                       placeholder-gray-400 dark:placeholder-gray-500 text-sm"
              placeholder="제목이나 작성자로 검색..."
            />
          </div>
        </div>

        {/* Snippets List */}
        <div className="max-h-96 overflow-y-auto scrollbar-thin">
          {isLoading ? (
            <div className="p-8 text-center">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-500 mx-auto mb-4"></div>
              <p className="text-gray-600 dark:text-gray-400">로딩 중...</p>
            </div>
          ) : snippets.length === 0 ? (
            <div className="p-8 text-center">
              <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">
                {searchQuery ? '검색 결과가 없습니다' : '저장된 코드 스니펫이 없습니다'}
              </p>
            </div>
          ) : (
            <div className="divide-y divide-gray-200 dark:divide-gray-700">
              {snippets.map((snippet) => (
                <div
                  key={snippet.id}
                  className={`p-4 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors cursor-pointer
                    ${currentSnippet?.id === snippet.id ? 'bg-primary-50 dark:bg-primary-900/20 border-l-4 border-primary-500' : ''}`}
                  onClick={() => handleLoadSnippet(snippet.id)}
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center space-x-2 mb-2">
                        <h4 className="text-sm font-medium text-gray-900 dark:text-gray-100 truncate">
                          {snippet.title}
                        </h4>
                        <div className="flex items-center space-x-1">
                          <div
                            className="w-3 h-3 rounded-full border border-gray-300 dark:border-gray-600"
                            style={{ backgroundColor: LANGUAGE_CONFIG[snippet.language]?.color || '#666666' }}
                            title={LANGUAGE_CONFIG[snippet.language]?.name || snippet.language}
                          />
                          <span className="text-xs text-gray-500 dark:text-gray-400">
                            {LANGUAGE_CONFIG[snippet.language]?.name || snippet.language}
                          </span>
                        </div>
                      </div>

                      <p className="text-xs text-gray-600 dark:text-gray-400 mb-2">
                        by {snippet.authorName} • {formatDate(snippet.createdAt)}
                      </p>

                      <pre className="text-xs text-gray-700 dark:text-gray-300 font-mono bg-gray-100 dark:bg-gray-800
                                     p-2 rounded border overflow-hidden">
                        {truncateCode(snippet.code)}
                      </pre>

                      <div className="flex items-center space-x-4 mt-2 text-xs text-gray-500 dark:text-gray-400">
                        <span className="flex items-center space-x-1">
                          <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                            <path fillRule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clipRule="evenodd" />
                          </svg>
                          <span>실행 {snippet.executionCount}회</span>
                        </span>
                        <span className="flex items-center space-x-1">
                          <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                            <path d="M8 12a1 1 0 01-.707-.293l-2-2a1 1 0 111.414-1.414L8 9.586l2.293-2.293a1 1 0 111.414 1.414l-2 2A1 1 0 018 12z"/>
                            <path fillRule="evenodd" d="M3 17a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm3.293-7.707A1 1 0 017 9h3V3a1 1 0 112 0v6h3a1 1 0 01.707 1.707l-4 4a1 1 0 01-1.414 0l-4-4z" clipRule="evenodd"/>
                          </svg>
                          <span>공유 {snippet.shareCount}회</span>
                        </span>
                      </div>
                    </div>

                    <div className="flex items-center space-x-2 ml-4">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          setShowDeleteConfirm(snippet.id);
                        }}
                        className="p-1 text-gray-400 hover:text-red-600 transition-colors"
                        title="삭제"
                      >
                        <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                          <path fillRule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clipRule="evenodd" />
                        </svg>
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Delete Confirmation Modal */}
      {showDeleteConfirm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-md w-full">
            <div className="p-6">
              <div className="flex items-center space-x-3 mb-4">
                <div className="flex-shrink-0">
                  <svg className="w-8 h-8 text-red-600" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                  </svg>
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                    코드 스니펫 삭제
                  </h3>
                  <p className="text-sm text-gray-600 dark:text-gray-400">
                    정말로 이 코드 스니펫을 삭제하시겠습니까?
                  </p>
                </div>
              </div>

              <div className="flex justify-end space-x-3">
                <button
                  onClick={() => setShowDeleteConfirm(null)}
                  className="px-4 py-2 bg-gray-200 hover:bg-gray-300 dark:bg-gray-600 dark:hover:bg-gray-500
                           text-gray-800 dark:text-gray-200 text-sm font-medium rounded transition-colors"
                >
                  취소
                </button>
                <button
                  onClick={() => handleDeleteSnippet(showDeleteConfirm)}
                  className="px-4 py-2 bg-red-500 hover:bg-red-600 text-white text-sm font-medium rounded transition-colors"
                >
                  삭제
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}