pipeline {
    agent any

    environment {
        DEPLOY_HOST = 'ubuntu@swyp-ontheway.duckdns.org'
        REMOTE_JAR  = '/home/ubuntu/app.jar'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew clean build -x test'
            }
        }

        stage('Deploy') {
            steps {
                sshagent(credentials: ['ec2-ssh-key']) {
                    sh """
                        JAR=\$(ls build/libs/*.jar | grep -v plain)
                        scp -o StrictHostKeyChecking=no \$JAR ${DEPLOY_HOST}:${REMOTE_JAR}
                        ssh -o StrictHostKeyChecking=no ${DEPLOY_HOST} 'sudo systemctl restart ontheway'
                    """
                }
            }
        }

        stage('Health Check') {
            steps {
                sh 'sleep 15'
                sh 'curl -f https://swyp-ontheway.duckdns.org/health'
            }
        }
    }

    post {
        success {
            echo '배포 성공'
        }
        failure {
            echo '배포 실패'
        }
    }
}