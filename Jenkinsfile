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
                bat '''
                    @echo off
                    echo Starting application on port 8800...
                    start /B java -jar target\\devops-pipeline-app-1.0.0.jar
                    echo Application process launched.
                '''
                bat '''
                    @echo off
                    echo Waiting 12 seconds for application to initialize...
                    ping -n 13 127.0.0.1 > nul

                    set RETRY=0
                    :HEALTHCHECK
                    echo Checking health endpoint (attempt %RETRY%)...
                    curl -s http://localhost:8800/health
                    if errorlevel 1 (
                        set /a RETRY+=1
                        if %RETRY% LSS 5 (
                            echo Retry %RETRY%/5 - waiting 5 more seconds...
                            ping -n 6 127.0.0.1 > nul
                            goto HEALTHCHECK
                        )
                        echo ERROR: Application did not start after 5 retries
                        exit /b 1
                    )
                    echo Application is running and healthy!
                '''
            }
        }

        stage('Selenium UI Test') {
            steps {
                bat '''
                    @echo off
                    echo Giving application extra time to stabilize...
                    ping -n 6 127.0.0.1 > nul

                    echo Verifying application is still responding...
                    curl -s http://localhost:8800/health
                    if errorlevel 1 (
                        echo ERROR: Application stopped responding before Selenium test
                        exit /b 1
                    )

                    echo Running Selenium smoke test...
                    mvn -B -ntp test -Dtest=SeleniumSmokeTest -Dselenium.enabled=true
                '''
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
                bat '''
                    @echo off
                    echo Checking Docker availability...
                    docker --version
                    if errorlevel 1 (
                        echo [WARNING] Docker is not available or daemon is not running.
                        echo [INFO] Start Docker Desktop and re-run the pipeline.
                        exit /b 0
                    )

                    echo Building Docker image: devops-pipeline-app:latest
                    docker build -t devops-pipeline-app:latest .
                    if errorlevel 1 (
                        echo [ERROR] Docker build failed.
                        exit /b 1
                    )

                    echo [SUCCESS] Docker image built successfully!
                    docker images devops-pipeline-app:latest
                '''
            }
        }

    }

    post {
        always {
            script {
                // Stop the application running on port 8800 (ignore errors if already stopped)
                bat '''
                    @echo off
                    echo Stopping application on port 8800...
                    for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| find ":8800" ^| find "LISTENING"') do (
                        echo Killing PID %%a
                        taskkill /F /PID %%a 2>nul
                    )
                    echo Cleanup complete.
                    exit /b 0
                '''
            }
        }
        success {
            echo 'Pipeline completed successfully! All stages passed.'
        }
        failure {
            echo 'Pipeline failed. Review the stage logs above for details.'
        }
    }
}
