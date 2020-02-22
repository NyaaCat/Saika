
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
            archiveArtifacts artifacts: 'build/libs/Saika-*.jar', fingerprint: true
            cleanWs()
        }
    }
}