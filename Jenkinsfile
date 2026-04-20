pipeline {
    agent any
    environment {
        DOCKER_USER = "shivaamaroju" 
        BACKEND_IMG = "${DOCKER_USER}/email-backend"
        FRONTEND_IMG = "${DOCKER_USER}/email-frontend"
    }
    stages {
        stage('Docker Login') {
            steps {
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
                withCredentials([file(credentialsId: 'aks-kubeconfig-file', variable: 'KUBECONFIG_PATH')]) {
                    // Using single quotes '...' for the shell command prevents Groovy from messing up the filename path
                    sh 'kubectl apply -f k8s/deployment.yaml --kubeconfig="$KUBECONFIG_PATH"'
                    sh 'kubectl apply -f k8s/service.yaml --kubeconfig="$KUBECONFIG_PATH"'
                    
                    // Forces Kubernetes to pull the new 'latest' images we just pushed
                    sh 'kubectl rollout restart deployment backend-deploy --kubeconfig="$KUBECONFIG_PATH"'
                    sh 'kubectl rollout restart deployment frontend-deploy --kubeconfig="$KUBECONFIG_PATH"'
                }
            }
        }
    }
    
    post {
        success {
            echo "Successfully deployed to AKS!"
            withCredentials([file(credentialsId: 'aks-kubeconfig-file', variable: 'KUBECONFIG_PATH')]) {
                echo "Fetching your Website URL..."
                sh 'kubectl get svc frontend-service --kubeconfig="$KUBECONFIG_PATH"'
            }
        }
        failure {
            echo "Deployment failed. Please check the logs above."
        }
    }
}
