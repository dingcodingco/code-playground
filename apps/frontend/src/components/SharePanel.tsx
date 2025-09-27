'use client';

import { useState } from 'react';
import { useCodeStore } from '@/store/codeStore';

interface SharePanelProps {
  className?: string;
}

export default function SharePanel({ className = '' }: SharePanelProps) {
  const {
    shareCode,
    shareInfo,
    isSharing,
    currentSnippet
  } = useCodeStore();

  const [expirationDays, setExpirationDays] = useState<number | undefined>(7);
  const [showShareModal, setShowShareModal] = useState(false);
  const [copied, setCopied] = useState(false);

  const handleShare = async () => {
    if (!currentSnippet) return;

    const result = await shareCode(expirationDays);
    if (result) {
      setShowShareModal(true);
    }
  };

  const copyToClipboard = async (text: string) => {
    try {
      await navigator.clipboard.writeText(text);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      // Fallback for browsers that don't support clipboard API
      const textArea = document.createElement('textarea');
      textArea.value = text;
      document.body.appendChild(textArea);
      textArea.select();
      document.execCommand('copy');
      document.body.removeChild(textArea);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const formatExpirationDate = (dateString?: string) => {
    if (!dateString) return '만료되지 않음';
    return new Date(dateString).toLocaleString('ko-KR');
  };

  return (
    <>
      <div className={`bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg ${className}`}>
        <div className="p-4 border-b border-gray-200 dark:border-gray-700">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
            코드 공유
          </h3>
          <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
            코드를 다른 사람들과 공유해보세요
          </p>
        </div>

        <div className="p-4">
          {!currentSnippet ? (
            <div className="text-center py-8">
              <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.367 2.684 3 3 0 00-5.367-2.684z" />
              </svg>
              <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">
                먼저 코드를 저장해주세요
              </p>
            </div>
          ) : (
            <div className="space-y-4">
              {/* Expiration Settings */}
              <div>
                <label htmlFor="expiration" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  만료 기간
                </label>
                <select
                  id="expiration"
                  value={expirationDays || ''}
                  onChange={(e) => setExpirationDays(e.target.value ? parseInt(e.target.value) : undefined)}
                  className="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600
                           rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100
                           focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                >
                  <option value="">영구</option>
                  <option value="1">1일</option>
                  <option value="7">7일</option>
                  <option value="30">30일</option>
                  <option value="90">90일</option>
                </select>
              </div>

              {/* Share Button */}
              <button
                onClick={handleShare}
                disabled={isSharing}
                className="w-full px-4 py-2 bg-blue-500 hover:bg-blue-600 disabled:bg-blue-300
                         text-white text-sm font-medium rounded-md transition-colors
                         disabled:cursor-not-allowed flex items-center justify-center space-x-2"
              >
                {isSharing ? (
                  <>
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                    <span>공유 링크 생성 중...</span>
                  </>
                ) : (
                  <>
                    <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M8 12a1 1 0 01-.707-.293l-2-2a1 1 0 111.414-1.414L8 9.586l2.293-2.293a1 1 0 111.414 1.414l-2 2A1 1 0 018 12z"/>
                      <path fillRule="evenodd" d="M3 17a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm3.293-7.707A1 1 0 017 9h3V3a1 1 0 112 0v6h3a1 1 0 01.707 1.707l-4 4a1 1 0 01-1.414 0l-4-4z" clipRule="evenodd"/>
                    </svg>
                    <span>공유 링크 생성</span>
                  </>
                )}
              </button>

              {/* Current Share Info */}
              {shareInfo && (
                <div className="mt-4 p-3 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-md">
                  <div className="flex items-center space-x-2 mb-2">
                    <svg className="w-5 h-5 text-green-600 dark:text-green-400" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd"/>
                    </svg>
                    <span className="text-sm font-medium text-green-800 dark:text-green-200">
                      공유 링크가 생성되었습니다
                    </span>
                  </div>

                  <div className="space-y-2">
                    <div>
                      <label className="block text-xs font-medium text-green-700 dark:text-green-300 mb-1">
                        공유 URL
                      </label>
                      <div className="flex items-center space-x-2">
                        <input
                          type="text"
                          value={shareInfo.shareUrl}
                          readOnly
                          className="flex-1 px-3 py-1.5 text-sm bg-white dark:bg-gray-800 border border-green-300 dark:border-green-600
                                   rounded text-gray-900 dark:text-gray-100 font-mono"
                        />
                        <button
                          onClick={() => copyToClipboard(shareInfo.shareUrl)}
                          className={`px-3 py-1.5 text-xs font-medium rounded transition-colors ${
                            copied
                              ? 'bg-green-600 text-white'
                              : 'bg-green-100 hover:bg-green-200 dark:bg-green-800 dark:hover:bg-green-700 text-green-800 dark:text-green-200'
                          }`}
                        >
                          {copied ? '복사됨!' : '복사'}
                        </button>
                      </div>
                    </div>

                    <div>
                      <label className="block text-xs font-medium text-green-700 dark:text-green-300 mb-1">
                        공유 ID
                      </label>
                      <div className="flex items-center space-x-2">
                        <code className="flex-1 px-3 py-1.5 text-sm bg-white dark:bg-gray-800 border border-green-300 dark:border-green-600
                                       rounded text-gray-900 dark:text-gray-100 font-mono">
                          {shareInfo.shareId}
                        </code>
                        <button
                          onClick={() => copyToClipboard(shareInfo.shareId)}
                          className={`px-3 py-1.5 text-xs font-medium rounded transition-colors ${
                            copied
                              ? 'bg-green-600 text-white'
                              : 'bg-green-100 hover:bg-green-200 dark:bg-green-800 dark:hover:bg-green-700 text-green-800 dark:text-green-200'
                          }`}
                        >
                          {copied ? '복사됨!' : '복사'}
                        </button>
                      </div>
                    </div>

                    <div className="pt-2 border-t border-green-200 dark:border-green-800">
                      <div className="flex justify-between text-xs text-green-600 dark:text-green-400">
                        <span>생성일: {new Date(shareInfo.createdAt).toLocaleString('ko-KR')}</span>
                        <span>만료일: {formatExpirationDate(shareInfo.expiresAt)}</span>
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Share Success Modal */}
      {showShareModal && shareInfo && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-md w-full">
            <div className="p-6">
              <div className="flex items-center space-x-3 mb-4">
                <div className="flex-shrink-0">
                  <svg className="w-8 h-8 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd"/>
                  </svg>
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                    공유 링크 생성 완료
                  </h3>
                  <p className="text-sm text-gray-600 dark:text-gray-400">
                    코드가 성공적으로 공유되었습니다
                  </p>
                </div>
              </div>

              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    공유 URL
                  </label>
                  <div className="flex items-center space-x-2">
                    <input
                      type="text"
                      value={shareInfo.shareUrl}
                      readOnly
                      className="flex-1 px-3 py-2 text-sm bg-gray-100 dark:bg-gray-700 border border-gray-300 dark:border-gray-600
                               rounded text-gray-900 dark:text-gray-100 font-mono"
                    />
                    <button
                      onClick={() => copyToClipboard(shareInfo.shareUrl)}
                      className="px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white text-sm font-medium rounded transition-colors"
                    >
                      복사
                    </button>
                  </div>
                </div>

                <div className="flex justify-end space-x-3 pt-4">
                  <button
                    onClick={() => setShowShareModal(false)}
                    className="px-4 py-2 bg-gray-200 hover:bg-gray-300 dark:bg-gray-600 dark:hover:bg-gray-500
                             text-gray-800 dark:text-gray-200 text-sm font-medium rounded transition-colors"
                  >
                    닫기
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}