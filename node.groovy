pipeline {
    agent any
    // tools {
    //     nodejs 'NodeJS-20'
    // }
    // environment {
    //     IMAGE_NAME = "node-demo-app"
    //     DOCKER_REPO = "mayurmwagh/node-demo-app"
    //     CONTAINER_NAME = "node-demo-container"
    // }
    stages {
        stage('checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/mayurmwagh/node-app.git'
            }
        }
    }

}