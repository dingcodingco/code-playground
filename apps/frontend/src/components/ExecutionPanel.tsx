'use client';

import { useState } from 'react';
import { useCodeStore } from '@/store/codeStore';
import { ExecutionStatus } from '@/types';

interface ExecutionPanelProps {
  className?: string;
}

export default function ExecutionPanel({ className = '' }: ExecutionPanelProps) {
  const {
    executionResult,
    isExecuting,
    executeCode,
    currentSnippet,
    loadExecutionHistory,
    executionHistory
  } = useCodeStore();

  const [input, setInput] = useState('');
  const [showHistory, setShowHistory] = useState(false);

  const handleExecute = async () => {
    await executeCode(undefined, input);
  };

  const handleLoadHistory = async () => {
    if (currentSnippet) {
      await loadExecutionHistory(currentSnippet.id);
      setShowHistory(true);
    }
  };

  const getStatusColor = (status: ExecutionStatus) => {
    switch (status) {
      case 'SUCCESS':
        return 'text-green-600 bg-green-50 border-green-200';
      case 'ERROR':
        return 'text-red-600 bg-red-50 border-red-200';
      case 'TIMEOUT':
        return 'text-yellow-600 bg-yellow-50 border-yellow-200';
      default:
        return 'text-gray-600 bg-gray-50 border-gray-200';
    }
  };

  const getStatusIcon = (status: ExecutionStatus) => {
    switch (status) {
      case 'SUCCESS':
        return (
          <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
          </svg>
        );
      case 'ERROR':
        return (
          <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
          </svg>
        );
      case 'TIMEOUT':
        return (
          <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L10 9.586V6z" clipRule="evenodd" />
          </svg>
        );
      default:
        return null;
    }
  };

  return (
    <div className={`bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg ${className}`}>
      <div className="p-4 border-b border-gray-200 dark:border-gray-700">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
            코드 실행
          </h3>
          <div className="flex items-center space-x-2">
            {currentSnippet && (
              <button
                onClick={handleLoadHistory}
                className="px-3 py-1.5 text-sm bg-gray-100 hover:bg-gray-200 dark:bg-gray-700
                         dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300 rounded-md
                         transition-colors"
              >
                실행 기록
              </button>
            )}
            <button
              onClick={handleExecute}
              disabled={isExecuting || !currentSnippet}
              className="px-4 py-2 bg-green-500 hover:bg-green-600 disabled:bg-green-300
                       text-white text-sm font-medium rounded-md transition-colors
                       disabled:cursor-not-allowed flex items-center space-x-2"
            >
              {isExecuting ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                  <span>실행 중...</span>
                </>
              ) : (
                <>
                  <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z" clipRule="evenodd" />
                  </svg>
                  <span>실행</span>
                </>
              )}
            </button>
          </div>
        </div>

        {/* Input for code execution */}
        <div className="mt-4">
          <label htmlFor="execution-input" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            입력 (선택사항)
          </label>
          <textarea
            id="execution-input"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            className="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600
                     rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100
                     focus:ring-2 focus:ring-green-500 focus:border-green-500
                     placeholder-gray-400 dark:placeholder-gray-500 resize-none"
            rows={3}
            placeholder="코드에 전달할 입력값을 입력하세요 (예: 사용자 입력, 테스트 데이터)"
          />
        </div>
      </div>

      {/* Execution Result */}
      {executionResult && (
        <div className="p-4">
          <div className={`p-3 rounded-md border ${getStatusColor(executionResult.status)}`}>
            <div className="flex items-center space-x-2 mb-2">
              {getStatusIcon(executionResult.status)}
              <span className="font-medium text-sm">
                {executionResult.status === 'SUCCESS' ? '실행 성공' :
                 executionResult.status === 'ERROR' ? '실행 오류' :
                 '실행 타임아웃'}
              </span>
              <span className="text-xs text-gray-500">
                ({executionResult.executionTime}ms)
              </span>
              {executionResult.memoryUsage && (
                <span className="text-xs text-gray-500">
                  ({Math.round(executionResult.memoryUsage / 1024 / 1024)}MB)
                </span>
              )}
            </div>

            {/* Output */}
            {executionResult.output && (
              <div className="mt-3">
                <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">출력:</h4>
                <pre className="text-sm bg-gray-100 dark:bg-gray-900 p-3 rounded border
                               font-mono whitespace-pre-wrap overflow-x-auto scrollbar-thin">
                  {executionResult.output}
                </pre>
              </div>
            )}

            {/* Error Message */}
            {executionResult.errorMessage && (
              <div className="mt-3">
                <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">오류:</h4>
                <pre className="text-sm bg-gray-100 dark:bg-gray-900 p-3 rounded border
                               font-mono whitespace-pre-wrap overflow-x-auto scrollbar-thin text-red-600 dark:text-red-400">
                  {executionResult.errorMessage}
                </pre>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Execution History Modal */}
      {showHistory && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-4xl w-full max-h-[80vh] overflow-hidden">
            <div className="p-4 border-b border-gray-200 dark:border-gray-700">
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">실행 기록</h3>
                <button
                  onClick={() => setShowHistory(false)}
                  className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                >
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
                  </svg>
                </button>
              </div>
            </div>
            <div className="p-4 max-h-96 overflow-y-auto scrollbar-thin">
              {executionHistory.length === 0 ? (
                <p className="text-gray-500 dark:text-gray-400 text-center py-8">
                  실행 기록이 없습니다.
                </p>
              ) : (
                <div className="space-y-4">
                  {executionHistory.map((execution) => (
                    <div key={execution.id} className={`p-3 rounded border ${getStatusColor(execution.status)}`}>
                      <div className="flex items-center justify-between mb-2">
                        <div className="flex items-center space-x-2">
                          {getStatusIcon(execution.status)}
                          <span className="font-medium text-sm">
                            {execution.status === 'SUCCESS' ? '성공' :
                             execution.status === 'ERROR' ? '오류' :
                             '타임아웃'}
                          </span>
                        </div>
                        <div className="text-xs text-gray-500">
                          {new Date(execution.createdAt).toLocaleString('ko-KR')}
                        </div>
                      </div>
                      {execution.output && (
                        <pre className="text-sm bg-gray-100 dark:bg-gray-900 p-2 rounded
                                       font-mono whitespace-pre-wrap max-h-32 overflow-y-auto scrollbar-thin">
                          {execution.output}
                        </pre>
                      )}
                      {execution.errorMessage && (
                        <pre className="text-sm bg-gray-100 dark:bg-gray-900 p-2 rounded
                                       font-mono whitespace-pre-wrap max-h-32 overflow-y-auto scrollbar-thin text-red-600 dark:text-red-400">
                          {execution.errorMessage}
                        </pre>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}