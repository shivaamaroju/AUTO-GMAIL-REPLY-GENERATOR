pipeline {
    agent any
    environment {
        DOCKER_USER = "shivaamaroju" // Your Docker Hub username
        BACKEND_IMG = "${DOCKER_USER}/email-backend"
        FRONTEND_IMG = "${DOCKER_USER}/email-frontend"
    }
    stages {
        stage('Docker Login') {
            steps {
                // This securely logs Jenkins into your Docker Hub account
                withCredentials([usernamePassword(credentialsId: 'docker-hub-token', passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USER')]) {
                    sh "echo ${DOCKER_PASS} | docker login -u ${DOCKER_USER} --password-stdin"
                }
            }
        }
        stage('Build & Push Backend') {
            steps {
                dir('EmailWriter') {
                    sh "docker build -t ${BACKEND_IMG}:${BUILD_NUMBER} ."
                    sh "docker push ${BACKEND_IMG}:${BUILD_NUMBER}"
                    sh "docker tag ${BACKEND_IMG}:${BUILD_NUMBER} ${BACKEND_IMG}:latest"
                    sh "docker push ${BACKEND_IMG}:latest"
                }
            }
        }
        stage('Build & Push Frontend') {
            steps {
                dir('email-writer-react') {
                    sh "docker build -t ${FRONTEND_IMG}:${BUILD_NUMBER} ."
                    sh "docker push ${FRONTEND_IMG}:${BUILD_NUMBER}"
                    sh "docker tag ${FRONTEND_IMG}:${BUILD_NUMBER} ${FRONTEND_IMG}:latest"
                    sh "docker push ${FRONTEND_IMG}:latest"
                }
            }
        }
        stage('Deploy to AKS') {
            steps {
                withKubeConfig([credentialsId: 'aks-kubeconfig-file']) {
                    sh "kubectl apply -f k8s/deployment.yaml"
                    sh "kubectl apply -f k8s/service.yaml"
                    // Force the pods to restart and pick up the new images
                    sh "kubectl rollout restart deployment backend-deploy"
                    sh "kubectl rollout restart deployment frontend-deploy"
                }
            }
        }
    }
}