#!/bin/sh

# Create runtime config with environment variables
cat > /app/public/config.js << EOF
// Runtime configuration - generated at container startup
window.__RUNTIME_CONFIG__ = {
  API_BASE_URL: '${API_BASE_URL:-http://localhost:8080/api/v1}'
};
EOF

echo "Generated runtime config:"
cat /app/public/config.js

# Start the application
exec "$@"