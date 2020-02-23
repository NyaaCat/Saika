
pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh './gradlew build'
                WarnError('This is a canary build!'){
                    sh './gradlew -q checkRelease'
                }
            }
        }

    }
    post {
        always {
            archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
            cleanWs()
        }
    }
}