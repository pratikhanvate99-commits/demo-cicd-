# CI/CD Live Demo — Mini Spring Boot App

A small Java + Maven web app with a complete 8-phase CI/CD example for a live walkthrough.

## Stack
- Java 21 + Spring Boot + Maven
- JUnit tests
- SonarQube quality analysis (GitHub Actions)
- Trivy filesystem and container image scanning
- Docker image build and push to Docker Hub
- Kubernetes Deployment + Service
- Smoke test after deployment
- Prometheus + Alertmanager example alert rules
- GitHub Actions workflow and a Jenkinsfile alternative

> Important: the pipeline is a demo template. To run the full cloud pipeline, configure repository secrets and a Kubernetes cluster first. The workflow will not deploy to a real cluster until the required secrets are set.

## App endpoints
- `/` — simple landing page with app status
- `/api/hello` — JSON response
- `/actuator/health` — health endpoint

## 8 phases to explain to Feroz Sir
1. **Checkout** — fetch source code from GitHub.
2. **Build & Unit Test** — Maven compiles the code and runs JUnit tests.
3. **Code Quality** — SonarQube checks bugs, code smells, and coverage.
4. **Security Scan** — Trivy scans the repository for vulnerable dependencies and misconfigurations.
5. **Containerize** — Docker packages the application into a portable image.
6. **Publish Image** — push the tagged image to Docker Hub / registry.
7. **Deploy to Kubernetes** — update the Deployment and wait for rollout.
8. **Verify & Alert** — smoke-test the live endpoint; Prometheus alert rules demonstrate how high error rate / pod unavailability can trigger alerts via Alertmanager.

## GitHub repository setup
1. Create a GitHub repository and upload these files.
2. In **Settings → Secrets and variables → Actions**, add:
   - `DOCKERHUB_USERNAME`
   - `DOCKERHUB_TOKEN` (Docker Hub access token)
   - `SONAR_TOKEN`
   - `SONAR_HOST_URL` (for example your SonarQube server URL)
   - `KUBE_CONFIG` (base64-encoded kubeconfig for your demo cluster)
   - `APP_BASE_URL` (optional; URL of the deployed app for smoke test)
3. Update `IMAGE_NAME` in `.github/workflows/cicd.yml` if needed.
4. Enable GitHub Actions and run the workflow by pushing to `main`.

### Create base64 kubeconfig (Linux/macOS)
```bash
base64 -w 0 ~/.kube/config
```
On macOS:
```bash
base64 < ~/.kube/config | tr -d '\n'
```
Paste the output as the `KUBE_CONFIG` secret. Use a dedicated demo namespace and least-privilege service account. Do not use a production kubeconfig.

## Run locally
Prerequisites: JDK 21 or newer. Maven Wrapper is included, so you do not need to install Maven separately.

Windows (PowerShell):
```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

macOS/Linux:
```bash
./mvnw clean verify
./mvnw spring-boot:run
```

Open `http://localhost:8080`. In VS Code, open this folder, install the Extension Pack for Java, open **Terminal → New Terminal**, and run the command for your operating system above. Stop the app with `Ctrl+C`.

## Docker
```bash
docker build -t cicd-live-demo:local .
docker run --rm -p 8080:8080 cicd-live-demo:local
```

## Kubernetes
```bash
kubectl apply -f k8s/
kubectl rollout status deployment/cicd-live-demo -n cicd-demo
kubectl port-forward service/cicd-live-demo 8080:80 -n cicd-demo
```
Then open `http://localhost:8080`.

## Monitoring / alerts — Prometheus + Grafana + Alertmanager
Install the complete monitoring stack with the example Helm values:
```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
kubectl create namespace monitoring
helm upgrade --install monitoring prometheus-community/kube-prometheus-stack --namespace monitoring -f monitoring/prometheus-values.yaml
kubectl -n monitoring port-forward svc/monitoring-grafana 3000:80
```
Open `http://localhost:3000` (demo credentials: `admin` / `change-me-before-demo`; change before sharing or using outside a local demo). Import `monitoring/grafana-demo-dashboard.json` in Grafana and select the Prometheus data source. To open Prometheus, check the service name using `kubectl get svc -n monitoring`, then port-forward its service to port 9090. The app includes Micrometer's Prometheus registry and exposes `/actuator/prometheus`.

Apply the PrometheusRule only in a cluster where Prometheus Operator is installed:
```bash
kubectl apply -f monitoring/prometheus-alert-rules.yaml
```
The example rules alert when pods are unavailable or the application returns elevated 5xx errors. Connect Alertmanager to email, Slack, or another receiver in its own configuration. The manifests are examples; metric names and labels may need adjustment for your Prometheus setup.

## What to say during the live demo
“First, a developer pushes code to GitHub. GitHub Actions starts the pipeline. Maven compiles the application and runs unit tests. SonarQube checks maintainability and quality gates. Trivy scans for known vulnerabilities. Docker builds an immutable image and publishes it to a registry. Kubernetes pulls that image and performs a rolling update. Finally, the pipeline checks the application endpoint, while Prometheus and Alertmanager can monitor runtime health and send alerts if the service or pods become unhealthy.”

## Demo limitations / honest explanation
- A successful YAML run is not proof of a real deployment unless cluster and registry secrets are configured.
- SonarQube requires a reachable server and valid token.
- Docker Hub push requires account credentials.
- Kubernetes deployment requires a valid kubeconfig and cluster permissions.
- Prometheus alert rules need a compatible Prometheus/Prometheus Operator installation and an Alertmanager receiver.
