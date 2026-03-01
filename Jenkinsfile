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
                    bat '''
                        @echo off
                        echo Starting application in background...
                        start /B java -jar target\devops-pipeline-app-1.0.0.jar
                        echo Application started, waiting for initialization...
                    '''
                    
                    // Wait for app to start and check health endpoint with retries
                    bat '''
                        @echo off
                        echo Waiting for application to start on port 8800...
                        timeout /t 10 /nobreak
                        
                        REM Retry health check up to 5 times
                        set RETRY=0
                        :HEALTHCHECK
                        curl -s http://localhost:8800/health
                        if errorlevel 1 (
                            set /a RETRY+=1
                            if %RETRY% LSS 5 (
                                echo Retry %RETRY%/5: Waiting for app...
                                timeout /t 3 /nobreak
                                goto HEALTHCHECK
                            )
                            echo ERROR: Application failed to start after 5 retries
                            exit /b 1
                        )
                        echo Application is running and healthy!
                    '''
                }
            }
        }
        
        stage('Selenium UI Test') {
            steps {
                script {
                    // Run Selenium test against the deployed app
                    bat '''
                        @echo off
                        echo Waiting for application to be ready...
                        timeout /t 5 /nobreak
                        
                        echo Verifying app is running...
                        curl -s http://localhost:8800/health
                        if errorlevel 1 (
                            echo ERROR: Application not responding
                            exit /b 1
                        )
                        
                        echo Running Selenium tests...
                        mvn -B -ntp test -Dtest=SeleniumSmokeTest -Dselenium.enabled=true
                    '''
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
                script {
                    // Try to build Docker image
                    bat '''
                        @echo off
                        echo Checking for Docker...
                        docker --version
                        if errorlevel 1 (
                            echo [WARNING] Docker not found or not running
                            echo [INFO] To enable Docker builds:
                            echo [INFO] 1. Install Docker Desktop: https://www.docker.com/products/docker-desktop
                            echo [INFO] 2. Start Docker Desktop
                            echo [INFO] 3. Verify with: docker ps
                            exit /b 0
                        )
                        
                        echo Building Docker image...
                        docker build -t devops-pipeline-app:latest .
                        if errorlevel 1 (
                            echo [ERROR] Docker build failed
                            exit /b 1
                        )
                        
                        echo [SUCCESS] Docker image built successfully!
                        docker images devops-pipeline-app:latest
                    '''
                }
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
