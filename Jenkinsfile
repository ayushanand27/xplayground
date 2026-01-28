// Declarative Pipeline for Windows Jenkins agents
// Tools: JDK 17 and Maven must be configured globally in Jenkins as 'JDK17' and 'Maven3'.

pipeline {
    agent any
    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }

    options {
        timestamps()
        ansiColor('xterm')
    }

    stages {
        stage('Checkout') {
            steps {
                // On multibranch pipelines, 'checkout scm' is used
                checkout scm
            }
        }
        stage('Build') {
            steps {
                bat 'mvn -v'
                bat 'mvn -B -ntp clean compile'
            }
        }
        stage('Test') {
            steps {
                // Run unit tests; Selenium tests are opt-in via -Dselenium.enabled=true
                bat 'mvn -B -ntp test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }
        stage('Package') {
            steps {
                bat 'mvn -B -ntp package'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*-shaded.jar', fingerprint: true
                }
            }
        }
        stage('Docker Build') {
            when {
                expression { return true } // Requires Docker available on the Windows agent
            }
            steps {
                bat 'docker --version'
                bat 'docker build -t devops-pipeline-app:latest .'
            }
        }
    }
}
