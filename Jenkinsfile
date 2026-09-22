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
                sh '''
                    for i in $(seq 1 40); do
                        if curl -f https://swyp-ontheway.duckdns.org/health; then
                            exit 0
                        fi
                        echo "앱이 아직 안 떴습니다. 3초 대기 후 재시도... ($i/40)"
                        sleep 3
                    done
                    echo "120초 넘게 health check 실패"
                    exit 1
                '''
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