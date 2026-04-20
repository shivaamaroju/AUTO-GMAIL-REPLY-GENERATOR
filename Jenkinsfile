pipeline {
    agent any
    environment {
        DOCKER_USER = "shivaamaroju" 
        BACKEND_IMG = "${DOCKER_USER}/food-backend"
        FRONTEND_IMG = "${DOCKER_USER}/food-frontend"
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
                dir('backend') { // Updated folder name
                    sh "docker build -t ${BACKEND_IMG}:${BUILD_NUMBER} ."
                    sh "docker tag ${BACKEND_IMG}:${BUILD_NUMBER} ${BACKEND_IMG}:latest"
                    sh "docker push ${BACKEND_IMG}:latest"
                    sh "docker push ${BACKEND_IMG}:${BUILD_NUMBER}"
                }
            }
        }
        
        stage('Build & Push Frontend') {
            steps {
                dir('frontend') { // Updated folder name
                    sh "docker build -t ${FRONTEND_IMG}:${BUILD_NUMBER} ."
                    sh "docker tag ${FRONTEND_IMG}:${BUILD_NUMBER} ${FRONTEND_IMG}:latest"
                    sh "docker push ${FRONTEND_IMG}:latest"
                    sh "docker push ${FRONTEND_IMG}:${BUILD_NUMBER}"
                }
            }
        }
        
        stage('Deploy to AKS') {
            steps {
                withCredentials([file(credentialsId: 'aks-kubeconfig-file', variable: 'KUBECONFIG_PATH')]) {
                    // Apply all manifests in the k8s folder
                    sh 'kubectl apply -f k8s/ --kubeconfig="$KUBECONFIG_PATH"'
                    
                    // Restart to pull the new 'latest' images
                    sh 'kubectl rollout restart deployment backend --kubeconfig="$KUBECONFIG_PATH"'
                    sh 'kubectl rollout restart deployment frontend --kubeconfig="$KUBECONFIG_PATH"'
                }
            }
        }
    }
    
    post {
        success {
            echo "Successfully deployed to AKS!"
        }
        failure {
            echo "Deployment failed. Check Jenkins logs."
        }
    }
}
