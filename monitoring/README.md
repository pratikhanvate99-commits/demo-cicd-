# Prometheus + Grafana Monitoring Demo

This folder includes example alert rules and a Helm values file for the `kube-prometheus-stack` chart, which bundles Prometheus, Grafana, Alertmanager, kube-state-metrics, and node-exporter.

## 1. Install monitoring stack (demo cluster)
Make sure Helm and kubectl are configured for your Kubernetes demo cluster.

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
kubectl create namespace monitoring
helm upgrade --install monitoring prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  -f monitoring/prometheus-values.yaml
```

For a real environment, do not use the demo Grafana password. Use a Kubernetes Secret or your organization's secret manager, and configure persistent storage / resource limits as appropriate.

## 2. Access Grafana
```bash
kubectl -n monitoring port-forward svc/monitoring-grafana 3000:80
```
Open `http://localhost:3000`. The demo values file uses `admin` / `change-me-before-demo`; change this immediately before sharing the cluster or using beyond a local demo.

## 3. Access Prometheus
Service names can vary with Helm release/chart version. Check them with:
```bash
kubectl get svc -n monitoring
```
Then port-forward the Prometheus service, commonly:
```bash
kubectl -n monitoring port-forward svc/monitoring-kube-prometheus-prometheus 9090:9090
```
Open `http://localhost:9090`.

## 4. Apply example app alert rules
```bash
kubectl apply -f monitoring/prometheus-alert-rules.yaml
```
The example rules depend on kube-state-metrics and on application HTTP metrics with labels matching the query. The pod availability rule is usually the easiest first alert to demonstrate. The HTTP 5xx rule may need adjustment depending on how application metrics are scraped and labeled.

## 5. Show in the live demo
- **Prometheus:** collects time-series metrics and evaluates alert rules.
- **Grafana:** dashboards visualize pod availability, CPU/memory, request rate, and errors.
- **Alertmanager:** groups, routes, and delivers alerts to configured receivers (email/Slack/webhook). Installing it does not automatically send notifications; configure a receiver securely.

For application-specific metrics, configure a ServiceMonitor or scrape config for the app. The application exposes Spring Boot Actuator endpoints; Prometheus scraping may require an appropriate Micrometer Prometheus registry dependency and a ServiceMonitor. This starter project exposes Actuator health but does not claim an application `/actuator/prometheus` endpoint until that registry integration is added.
