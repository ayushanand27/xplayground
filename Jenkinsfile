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
        
        stage('Unit Tests') {
            steps {
                // Run unit tests (Selenium test is disabled by default)
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
                bat 'mvn -B -ntp package -DskipTests'
            }
            post {
                success {
                   archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }
        
        stage('Start Application') {
            steps {
                script {
                    // Start app in background on port 8800
                    bat 'start /B java -jar target\\devops-pipeline-app-1.0.0.jar > app.log 2>&1'
                    
                    // Wait for app to start and check health endpoint
                    bat '''
                        @echo off
                        echo Waiting for application to start on port 8800...
                        timeout /t 5 /nobreak >nul
                        curl http://localhost:8800/health
                        if errorlevel 1 (
                            echo ERROR: Application failed to start
                            exit /b 1
                        )
                        echo Application is running!
                    '''
                }
            }
        }
        
        stage('Selenium UI Test') {
            steps {
                script {
                    // Run Selenium test against the deployed app
                    bat 'mvn -B -ntp test -Dselenium.enabled=true'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Docker Build') {
            when {
                expression { return fileExists('Dockerfile') }
            }
            steps {
                bat 'docker --version'
                bat 'docker build -t devops-pipeline-app:latest .'
            }
        }
    }
    
    post {
        always {
            script {
                // Stop the application
                bat '''
                    @echo off
                    for /f "tokens=5" %%a in ('netstat -aon ^| find ":8800" ^| find "LISTENING"') do taskkill /F /PID %%a
                    echo Application stopped
                '''
            }
        }
        success {
            echo 'Pipeline completed successfully! All tests passed.'
        }
        failure {
            echo 'Pipeline failed. Check logs for details.'
        }
    }
}
