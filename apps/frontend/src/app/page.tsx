'use client';

import { useState } from 'react';
import CodeEditor from '@/components/CodeEditor';
import LanguageSelector from '@/components/LanguageSelector';
import ExecutionPanel from '@/components/ExecutionPanel';
import SharePanel from '@/components/SharePanel';
import SnippetsList from '@/components/SnippetsList';
import { useCodeStore } from '@/store/codeStore';

export default function HomePage() {
  const {
    currentTitle,
    currentAuthor,
    setTitle,
    setAuthor,
    saveSnippet,
    resetEditor,
    isLoading,
    error,
    clearError
  } = useCodeStore();

  const [showMetadata, setShowMetadata] = useState(false);
  const [activeTab, setActiveTab] = useState<'code' | 'snippets'>('code');

  const handleSaveSnippet = async () => {
    await saveSnippet();
  };

  const handleNewCode = () => {
    resetEditor();
  };

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      {/* Header */}
      <header className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center space-x-4">
              <h1 className="text-xl font-bold text-gray-900 dark:text-gray-100">
                Vibe Coding
              </h1>
              <span className="text-sm text-gray-500 dark:text-gray-400">
                코드 작성, 실행, 공유 플랫폼
              </span>
            </div>

            <div className="flex items-center space-x-4">
              <LanguageSelector />

              {/* Tab Navigation */}
              <div className="flex items-center space-x-2">
                <button
                  onClick={() => setActiveTab('code')}
                  className={`px-3 py-1.5 text-sm rounded-md transition-colors ${
                    activeTab === 'code'
                      ? 'bg-primary-500 text-white'
                      : 'bg-gray-100 hover:bg-gray-200 dark:bg-gray-700 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300'
                  }`}
                >
                  코드 작성
                </button>
                <button
                  onClick={() => setActiveTab('snippets')}
                  className={`px-3 py-1.5 text-sm rounded-md transition-colors ${
                    activeTab === 'snippets'
                      ? 'bg-primary-500 text-white'
                      : 'bg-gray-100 hover:bg-gray-200 dark:bg-gray-700 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300'
                  }`}
                >
                  저장된 코드
                </button>
              </div>

              <button
                onClick={() => setShowMetadata(!showMetadata)}
                className="px-3 py-1.5 text-sm bg-gray-100 hover:bg-gray-200 dark:bg-gray-700
                         dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300 rounded-md
                         transition-colors"
              >
                메타데이터 {showMetadata ? '숨기기' : '보기'}
              </button>

              <button
                onClick={handleNewCode}
                className="px-3 py-1.5 text-sm bg-gray-100 hover:bg-gray-200 dark:bg-gray-700
                         dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300 rounded-md
                         transition-colors"
              >
                새 코드
              </button>

              <button
                onClick={handleSaveSnippet}
                disabled={isLoading}
                className="px-4 py-2 bg-primary-500 hover:bg-primary-600 disabled:bg-primary-300
                         text-white text-sm font-medium rounded-md transition-colors
                         disabled:cursor-not-allowed"
              >
                {isLoading ? '저장 중...' : '코드 저장'}
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Error Banner */}
      {error && (
        <div className="bg-red-50 dark:bg-red-900/50 border-l-4 border-red-400 p-4">
          <div className="flex justify-between items-center max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
              </div>
              <div className="ml-3">
                <p className="text-sm text-red-800 dark:text-red-200">{error}</p>
              </div>
            </div>
            <button
              onClick={clearError}
              className="text-red-400 hover:text-red-500 transition-colors"
            >
              <span className="sr-only">닫기</span>
              <svg className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
              </svg>
            </button>
          </div>
        </div>
      )}

      {/* Metadata Section */}
      {showMetadata && (
        <div className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label htmlFor="title" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  제목
                </label>
                <input
                  type="text"
                  id="title"
                  value={currentTitle}
                  onChange={(e) => setTitle(e.target.value)}
                  className="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600
                           rounded-md bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100
                           focus:ring-2 focus:ring-primary-500 focus:border-primary-500
                           placeholder-gray-400 dark:placeholder-gray-500"
                  placeholder="코드의 제목을 입력하세요"
                />
              </div>
              <div>
                <label htmlFor="author" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  작성자
                </label>
                <input
                  type="text"
                  id="author"
                  value={currentAuthor}
                  onChange={(e) => setAuthor(e.target.value)}
                  className="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600
                           rounded-md bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100
                           focus:ring-2 focus:ring-primary-500 focus:border-primary-500
                           placeholder-gray-400 dark:placeholder-gray-500"
                  placeholder="작성자명을 입력하세요"
                />
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {activeTab === 'code' ? (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Code Editor - Main Area */}
            <div className="lg:col-span-2">
              <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden">
                <div className="p-6">
                  <div className="mb-4">
                    <h2 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                      코드 에디터
                    </h2>
                    <p className="text-sm text-gray-600 dark:text-gray-400">
                      코드를 작성하고 실행해보세요
                    </p>
                  </div>

                  <CodeEditor
                    height="500px"
                    className="border border-gray-200 dark:border-gray-700 rounded-md overflow-hidden"
                  />
                </div>
              </div>
            </div>

            {/* Side Panel */}
            <div className="space-y-6">
              {/* Execution Panel */}
              <ExecutionPanel />

              {/* Share Panel */}
              <SharePanel />
            </div>
          </div>
        ) : (
          /* Snippets View */
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2">
              <SnippetsList />
            </div>
            <div className="space-y-6">
              <ExecutionPanel />
              <SharePanel />
            </div>
          </div>
        )}
      </main>
    </div>
  );
}