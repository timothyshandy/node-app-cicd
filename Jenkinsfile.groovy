pipeline {
    agent {
        label 'node-agent'
    }
    
    tools {
        nodejs 'NodeJS'
    }
    environment {
        IMAGE_NAME = "node-app"
        DOCKER_REPO = "whoistimothyshandy/node-app"
        CONTAINER_NAME = "node-demo-container"
    }
    stages {
        stage('checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/timothyshandy/node-app-cicd.git'
            }
        }    
        stage('Verify Environment') {
            steps {
                sh '''
                echo "Node Version:"
                node -v
                echo "NPM Version:"
                npm -v
                echo "Docker Version:"
                docker --version
                '''
            }
        }
        stage('Install Dependencies') {
            steps {
                sh 'npm install' 
            }
        }
        stage('Run Tests') {
            steps {
                sh 'npm test'
            }
        }
        stage('Build Docker Image') {
            steps {
                sh '''
                docker build -t ${IMAGE_NAME}:${BUILD_NUMBER} .
                ''' 
            }
        }
        stage('Docker Login') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub_creds',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {
                    sh 'echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin'
                }
            }
        }
        stage('Push Docker Image') {
            steps {
                sh '''
                docker tag ${IMAGE_NAME}:${BUILD_NUMBER} ${DOCKER_REPO}:${BUILD_NUMBER}
                docker push ${DOCKER_REPO}:${BUILD_NUMBER}
                '''
            }
        }
	stage('Deploy to EKS') {
    steps {
        withCredentials([
            [
                $class: 'AmazonWebServicesCredentialsBinding',
                credentialsId: 'aws-creds'
            ]
        ]) {
            sh '''
            aws eks update-kubeconfig \
            --region ap-south-1 \
            --name node-app-cluster

            kubectl apply -f deploymentfiles/deployment.yaml
            kubectl apply -f deploymentfiles/service.yaml

            kubectl set image deployment/node-app \
            node-demo-container=${DOCKER_REPO}:${BUILD_NUMBER}

            kubectl rollout status deployment/node-app
            '''
     }
        }
}        
    }
}
