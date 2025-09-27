'use client';

import { useCodeStore } from '@/store/codeStore';
import { ProgrammingLanguage, LANGUAGE_CONFIG } from '@/types';

interface LanguageSelectorProps {
  className?: string;
}

export default function LanguageSelector({ className = '' }: LanguageSelectorProps) {
  const { currentLanguage, setLanguage } = useCodeStore();

  const handleLanguageChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const language = event.target.value as ProgrammingLanguage;
    setLanguage(language);
  };

  return (
    <div className={`flex items-center space-x-2 ${className}`}>
      <label htmlFor="language-select" className="text-sm font-medium text-gray-700 dark:text-gray-300">
        언어:
      </label>
      <select
        id="language-select"
        value={currentLanguage}
        onChange={handleLanguageChange}
        className="block w-32 px-3 py-1.5 text-sm border border-gray-300 dark:border-gray-600 rounded-md
                   bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100
                   focus:ring-2 focus:ring-primary-500 focus:border-primary-500
                   hover:border-gray-400 dark:hover:border-gray-500 transition-colors"
      >
        {Object.entries(LANGUAGE_CONFIG).map(([key, config]) => (
          <option key={key} value={key}>
            {config.name}
          </option>
        ))}
      </select>

      {/* Language indicator */}
      <div
        className="w-3 h-3 rounded-full border border-gray-300 dark:border-gray-600"
        style={{ backgroundColor: LANGUAGE_CONFIG[currentLanguage]?.color || '#666666' }}
        title={LANGUAGE_CONFIG[currentLanguage]?.name || currentLanguage}
      />
    </div>
  );
}