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
                    docker --version >nul 2>&1
                    if errorlevel 1 (
                        echo [WARNING] Docker CLI is not available on this Jenkins agent.
                        echo [INFO] Skipping Docker Build stage for this run.
                        exit /b 0
                    )

                    docker info >nul 2>&1
                    if errorlevel 1 (
                        echo [WARNING] Docker daemon is not running or not reachable.
                        echo [INFO] Start Docker Desktop and ensure Linux engine is running.
                        echo [INFO] Skipping Docker Build stage for this run.
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

        stage('Build Frontend') {
            when {
                expression { return fileExists('frontend/package.json') }
            }
            steps {
                bat '''
                    @echo off
                    echo Building frontend app in frontend/...
                    cd /d frontend
                    call npm install
                    if errorlevel 1 (
                        echo [ERROR] npm install failed.
                        exit /b 1
                    )

                    call npm run build
                    if errorlevel 1 (
                        echo [ERROR] npm run build failed.
                        exit /b 1
                    )

                    echo [SUCCESS] Frontend build completed!
                '''
            }
        }

        stage('Docker Compose Build') {
            when {
                expression { return fileExists('docker-compose.yml') }
            }
            steps {
                bat '''
                    @echo off
                    docker info >nul 2>&1
                    if errorlevel 1 (
                        echo [WARNING] Docker daemon is not running or not reachable.
                        echo [INFO] Skipping Docker Compose Build stage for this run.
                        exit /b 0
                    )

                    docker compose version >nul 2>&1
                    if errorlevel 1 (
                        echo [WARNING] docker compose command is not available.
                        echo [INFO] Skipping Docker Compose Build stage for this run.
                        exit /b 0
                    )

                    echo Running docker compose build...
                    docker compose build
                    if errorlevel 1 (
                        echo [ERROR] docker compose build failed.
                        exit /b 1
                    )

                    echo [SUCCESS] docker compose build completed!
                '''
            }
        }

        stage('Push to DockerHub') {
            when {
                expression { return fileExists('Dockerfile') }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')]) {
                    bat '''
                        @echo off
                        set PUSH_MAX_RETRIES=3

                        docker info >nul 2>&1
                        if errorlevel 1 (
                            echo [WARNING] Docker daemon is not running or not reachable.
                            echo [INFO] Skipping DockerHub push for this run.
                            exit /b 0
                        )

                        echo Logging in to DockerHub...
                        echo %DOCKERHUB_PASS% | docker login --username "%DOCKERHUB_USER%" --password-stdin
                        if errorlevel 1 (
                            echo [ERROR] DockerHub login failed.
                            exit /b 1
                        )

                        echo Tagging image as %DOCKERHUB_USER%/devops-pipeline-app:latest...
                        docker tag devops-pipeline-app:latest %DOCKERHUB_USER%/devops-pipeline-app:latest
                        if errorlevel 1 (
                            echo [ERROR] Failed to tag latest image.
                            exit /b 1
                        )

                        echo Tagging image as %DOCKERHUB_USER%/devops-pipeline-app:%BUILD_NUMBER%...
                        docker tag devops-pipeline-app:latest %DOCKERHUB_USER%/devops-pipeline-app:%BUILD_NUMBER%
                        if errorlevel 1 (
                            echo [ERROR] Failed to tag build-number image.
                            exit /b 1
                        )

                        set PUSH_RETRY=1
                        :PUSH_LATEST
                        echo Pushing %DOCKERHUB_USER%/devops-pipeline-app:latest (attempt %PUSH_RETRY%/%PUSH_MAX_RETRIES%)...
                        docker push %DOCKERHUB_USER%/devops-pipeline-app:latest
                        if errorlevel 1 (
                            if %PUSH_RETRY% GEQ %PUSH_MAX_RETRIES% (
                                echo [ERROR] Failed to push latest image after %PUSH_MAX_RETRIES% attempts.
                                exit /b 1
                            )
                            set /a PUSH_RETRY+=1
                            echo [WARNING] Push latest failed. Re-authenticating and retrying...
                            echo %DOCKERHUB_PASS% | docker login --username "%DOCKERHUB_USER%" --password-stdin
                            ping -n 6 127.0.0.1 > nul
                            goto PUSH_LATEST
                        )

                        set PUSH_RETRY=1
                        :PUSH_BUILD_NUMBER
                        echo Pushing %DOCKERHUB_USER%/devops-pipeline-app:%BUILD_NUMBER% (attempt %PUSH_RETRY%/%PUSH_MAX_RETRIES%)...
                        docker push %DOCKERHUB_USER%/devops-pipeline-app:%BUILD_NUMBER%
                        if errorlevel 1 (
                            if %PUSH_RETRY% GEQ %PUSH_MAX_RETRIES% (
                                echo [ERROR] Failed to push build-number image after %PUSH_MAX_RETRIES% attempts.
                                exit /b 1
                            )
                            set /a PUSH_RETRY+=1
                            echo [WARNING] Push build-number failed. Re-authenticating and retrying...
                            echo %DOCKERHUB_PASS% | docker login --username "%DOCKERHUB_USER%" --password-stdin
                            ping -n 6 127.0.0.1 > nul
                            goto PUSH_BUILD_NUMBER
                        )

                        echo [SUCCESS] DockerHub push completed!
                    '''
                }
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
