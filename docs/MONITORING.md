# Vibe Coding Monitoring Stack

## Overview
Complete observability solution for Vibe Coding platform with metrics, logs, and distributed tracing.

## Components

### Metrics (Prometheus Stack)
- **Prometheus**: Time-series metrics collection
- **Grafana**: Visualization and dashboards
- **AlertManager**: Alert routing and management
- **Node Exporter**: System metrics
- **cAdvisor**: Container metrics
- **PostgreSQL Exporter**: Database metrics
- **Redis Exporter**: Cache metrics

### Logs (ELK Stack)
- **Elasticsearch**: Log storage and search
- **Logstash**: Log processing pipeline
- **Kibana**: Log visualization and analysis
- **Filebeat**: Log shipping

### Tracing
- **Jaeger**: Distributed tracing UI
- **OpenTelemetry Collector**: Trace collection and processing
- **Tempo**: Trace storage backend

## Quick Start

### Start All Services
```bash
./scripts/start-monitoring.sh
```

### Individual Stack Commands
```bash
# Metrics
docker-compose -f docker-compose.monitoring.yml up -d

# Logs
cd elk && docker-compose -f docker-compose.elk.yml up -d

# Tracing
cd tracing && docker-compose -f docker-compose.tracing.yml up -d
```

## Access URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| Grafana | http://localhost:3001 | admin/vibecoding2024 |
| Prometheus | http://localhost:9090 | - |
| AlertManager | http://localhost:9093 | - |
| Kibana | http://localhost:5601 | - |
| Jaeger UI | http://localhost:16686 | - |
| Elasticsearch | http://localhost:9200 | - |

## Configuration

### Application Integration

#### Spring Boot Backend
Add to `application.yml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  metrics:
    export:
      prometheus:
        enabled: true
  tracing:
    sampling:
      probability: 0.1
  otlp:
    tracing:
      endpoint: http://otel-collector:4318/v1/traces

logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  level:
    root: INFO
```

#### Next.js Frontend
Install dependencies:
```bash
npm install @opentelemetry/api @opentelemetry/sdk-node prom-client
```

Add instrumentation:
```javascript
// instrumentation.ts
import { NodeSDK } from '@opentelemetry/sdk-node';
import { getNodeAutoInstrumentations } from '@opentelemetry/auto-instrumentations-node';
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-http';

const sdk = new NodeSDK({
  traceExporter: new OTLPTraceExporter({
    url: 'http://otel-collector:4318/v1/traces',
  }),
  instrumentations: [getNodeAutoInstrumentations()]
});

sdk.start();
```

## Dashboards

### Grafana Dashboards
- **System Overview**: Overall system health and performance
- **Backend Metrics**: Spring Boot application metrics
- **Frontend Metrics**: Next.js performance metrics
- **Database Metrics**: PostgreSQL performance
- **Business Metrics**: Code execution and user activity

### Kibana Views
- **Application Logs**: Centralized log viewing
- **Error Analysis**: Error tracking and patterns
- **Access Logs**: Nginx access patterns
- **Container Logs**: Docker container logs

### Jaeger Traces
- **Request Flow**: End-to-end request tracing
- **Service Dependencies**: Service interaction map
- **Performance Analysis**: Latency breakdown

## Alerts

### Critical Alerts
- Service down (Backend/Frontend/Database)
- High error rate (>5%)
- Database connection exhaustion
- Disk space low (<20%)

### Warning Alerts
- High response time (>1s)
- High memory usage (>85%)
- Slow database queries (>1s)
- High CPU usage (>80%)

### Business Alerts
- Low user activity
- High code execution failure rate
- API rate limit exceeded

## Maintenance

### Backup
```bash
# Elasticsearch snapshots
curl -X PUT "localhost:9200/_snapshot/backup" -H 'Content-Type: application/json' -d'
{
  "type": "fs",
  "settings": {
    "location": "/backup"
  }
}'

# Prometheus snapshots
curl -X POST http://localhost:9090/api/v1/admin/tsdb/snapshot
```

### Cleanup
```bash
# Remove old indices
curator --config curator.yml delete indices --older-than 30 --prefix vibe-coding-

# Clean Docker volumes
docker volume prune
```

### Scaling
```yaml
# Increase Elasticsearch heap
ES_JAVA_OPTS: "-Xms1g -Xmx1g"

# Add Prometheus replicas
prometheus:
  deploy:
    replicas: 2
```

## Troubleshooting

### Common Issues

1. **Elasticsearch not starting**
   ```bash
   # Increase vm.max_map_count
   sudo sysctl -w vm.max_map_count=262144
   ```

2. **Grafana dashboards missing**
   ```bash
   # Restart Grafana to reload provisioned dashboards
   docker-compose restart grafana
   ```

3. **Jaeger traces not appearing**
   ```bash
   # Check OTLP collector connection
   curl http://localhost:4318/v1/traces
   ```

4. **High memory usage**
   ```bash
   # Adjust memory limits in docker-compose
   mem_limit: 512m
   ```

## Performance Tuning

### Prometheus
- Adjust scrape intervals based on requirements
- Use recording rules for frequently queried metrics
- Implement federation for large deployments

### Elasticsearch
- Optimize index settings for write/read patterns
- Use appropriate shard sizes (20-40GB)
- Implement hot-warm-cold architecture for historical data

### Jaeger
- Adjust sampling rate for production (0.1-1%)
- Use adaptive sampling for high-traffic services
- Configure appropriate retention policies

## Security

### Authentication
- Enable Grafana OAuth/LDAP integration
- Configure Elasticsearch security features
- Implement Kibana spaces and roles

### Network Security
- Use internal Docker networks
- Implement TLS for all connections
- Restrict external access to monitoring services

### Data Protection
- Encrypt data at rest
- Implement backup encryption
- Regular security updates

## Contact
For issues or questions, contact the DevOps team at devops@vibecoding.com