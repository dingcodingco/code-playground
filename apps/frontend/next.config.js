/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  swcMinify: true,
  output: 'standalone',

  // API 라우트 설정 (운영환경에서는 비활성화)
  async rewrites() {
    // 운영환경에서는 rewrites 사용하지 않음 (직접 API 호출)
    if (process.env.NODE_ENV === 'production') {
      return [];
    }

    // 개발환경에서만 localhost proxy 사용
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8080/api/:path*',
      },
    ]
  },

  // CORS 설정
  async headers() {
    return [
      {
        source: '/api/:path*',
        headers: [
          { key: 'Access-Control-Allow-Credentials', value: 'true' },
          { key: 'Access-Control-Allow-Origin', value: '*' },
          { key: 'Access-Control-Allow-Methods', value: 'GET,OPTIONS,PATCH,DELETE,POST,PUT' },
          { key: 'Access-Control-Allow-Headers', value: 'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version' },
        ]
      }
    ]
  },

  // 환경 변수
  env: {
    NEXT_PUBLIC_API_BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api/v1',
    NEXT_PUBLIC_APP_NAME: process.env.NEXT_PUBLIC_APP_NAME || 'Vibe Coding',
  },

  // 개발 환경 설정
  experimental: {
    optimizePackageImports: ['lucide-react'],
  },

  // Monaco Editor를 위한 설정
  webpack: (config) => {
    config.module.rules.push({
      test: /\.worker\.js$/,
      use: { loader: 'worker-loader' }
    });

    return config;
  },
}

module.exports = nextConfig