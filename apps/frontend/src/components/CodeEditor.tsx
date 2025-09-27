'use client';

import { Editor } from '@monaco-editor/react';
import { useCodeStore } from '@/store/codeStore';
import { LANGUAGE_CONFIG } from '@/types';
import { useState, useCallback } from 'react';

interface CodeEditorProps {
  height?: string;
  width?: string;
  className?: string;
}

export default function CodeEditor({
  height = '400px',
  width = '100%',
  className = ''
}: CodeEditorProps) {
  const {
    currentCode,
    currentLanguage,
    setCode
  } = useCodeStore();

  const [isLoading, setIsLoading] = useState(true);

  const handleEditorDidMount = useCallback(() => {
    setIsLoading(false);
  }, []);

  const handleEditorChange = useCallback((value: string | undefined) => {
    if (value !== undefined) {
      setCode(value);
    }
  }, [setCode]);

  const config = LANGUAGE_CONFIG[currentLanguage];

  return (
    <div className={`relative ${className}`}>
      {isLoading && (
        <div className="absolute inset-0 flex items-center justify-center bg-gray-50 dark:bg-gray-900 z-10">
          <div className="flex items-center space-x-2">
            <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary-500"></div>
            <span className="text-sm text-gray-600 dark:text-gray-400">에디터 로딩 중...</span>
          </div>
        </div>
      )}

      <Editor
        height={height}
        width={width}
        language={config.monacoLanguage}
        value={currentCode}
        onChange={handleEditorChange}
        onMount={handleEditorDidMount}
        theme="vs-dark"
        options={{
          minimap: { enabled: false },
          fontSize: 14,
          lineNumbers: 'on',
          renderWhitespace: 'selection',
          tabSize: 2,
          insertSpaces: true,
          wordWrap: 'on',
          automaticLayout: true,
          scrollBeyondLastLine: false,
          folding: true,
          lineDecorationsWidth: 10,
          lineNumbersMinChars: 3,
          glyphMargin: false,
          contextmenu: true,
          selectOnLineNumbers: true,
          roundedSelection: false,
          readOnly: false,
          cursorStyle: 'line',
          accessibilitySupport: 'auto',
          smoothScrolling: true,
          padding: { top: 16, bottom: 16 }
        }}
        loading={
          <div className="flex items-center justify-center h-full">
            <div className="animate-pulse text-gray-500">코드 에디터 준비 중...</div>
          </div>
        }
      />
    </div>
  );
}