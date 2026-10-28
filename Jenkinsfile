pipeline {
  agent any
  environment {
    IMAGE_NAME = 'YOUR_DOCKERHUB_USERNAME/cicd-live-demo'
    IMAGE_TAG = "${BUILD_NUMBER}"
    SONAR_HOST_URL = credentials('sonar-host-url')
    SONAR_TOKEN = credentials('sonar-token')
    DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
  }
  stages {
    stage('1. Checkout') {
      steps { checkout scm }
    }
    stage('2. Build and Unit Test') {
      steps { sh 'mvn -B clean verify' }
      post { always { junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml' } }
    }
    stage('3. SonarQube Quality Gate') {
      steps {
        withEnv(["SONAR_HOST_URL=${SONAR_HOST_URL}", "SONAR_TOKEN=${SONAR_TOKEN}"]) {
          sh 'mvn -B sonar:sonar -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.token=$SONAR_TOKEN'
        }
      }
    }
    stage('4. Trivy Security Scan') {
      steps { sh 'trivy fs --exit-code 1 --severity HIGH,CRITICAL --ignore-unfixed .' }
    }
    stage('5. Docker Build') {
      steps { sh 'docker build -t $IMAGE_NAME:$IMAGE_TAG .' }
    }
    stage('6. Push Image') {
      steps {
        sh 'echo "$DOCKERHUB_CREDENTIALS_PSW" | docker login -u "$DOCKERHUB_CREDENTIALS_USR" --password-stdin'
        sh 'docker push $IMAGE_NAME:$IMAGE_TAG'
      }
    }
    stage('7. Kubernetes Deploy') {
      steps {
        withCredentials([file(credentialsId: 'kubeconfig-demo', variable: 'KUBECONFIG')]) {
          sh 'kubectl -n cicd-demo set image deployment/cicd-live-demo app=$IMAGE_NAME:$IMAGE_TAG'
          sh 'kubectl -n cicd-demo rollout status deployment/cicd-live-demo --timeout=180s'
        }
      }
    }
    stage('8. Smoke Test') {
      steps { sh 'curl --fail --retry 10 --retry-delay 5 --retry-connrefused "$APP_BASE_URL/"' }
    }
  }
  post {
    success { echo 'CI/CD completed successfully: build, quality, security, image, deployment and smoke test passed.' }
    failure { echo 'Pipeline failed. Review the failed stage and notify the team using your Jenkins mail/Slack integration.' }
    always { sh 'docker logout || true' }
  }
}
