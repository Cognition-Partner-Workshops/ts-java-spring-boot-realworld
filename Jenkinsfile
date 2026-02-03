pipeline {
    agent any

    tools {
        jdk 'JDK11'
    }

    environment {
        GRADLE_OPTS = '-Dorg.gradle.daemon=false'
        APP_NAME = 'spring-boot-realworld-example-app'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git --version'
                sh 'java -version'
            }
        }

        stage('Build') {
            steps {
                sh './gradlew clean compileJava --no-daemon'
            }
        }

        stage('Code Quality') {
            steps {
                sh './gradlew spotlessCheck --no-daemon'
            }
        }

        stage('Unit Tests') {
            steps {
                sh './gradlew test --no-daemon -x jacocoTestCoverageVerification'
            }
            post {
                always {
                    junit testResults: '**/build/test-results/test/*.xml', allowEmptyResults: true
                    publishHTML(target: [
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'build/reports/tests/test',
                        reportFiles: 'index.html',
                        reportName: 'Unit Test Report'
                    ])
                }
            }
        }

        stage('Code Coverage') {
            steps {
                sh './gradlew jacocoTestReport --no-daemon'
            }
            post {
                always {
                    jacoco(
                        execPattern: '**/build/jacoco/*.exec',
                        classPattern: '**/build/classes/java/main',
                        sourcePattern: '**/src/main/java',
                        exclusionPattern: '**/build/generated/**'
                    )
                    publishHTML(target: [
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'build/reports/jacoco/test/html',
                        reportFiles: 'index.html',
                        reportName: 'JaCoCo Coverage Report'
                    ])
                }
            }
        }

        stage('Build Docker Image') {
            when {
                anyOf {
                    branch 'master'
                    branch 'main'
                    buildingTag()
                }
            }
            steps {
                sh './gradlew bootBuildImage --imageName=${APP_NAME}:${BUILD_NUMBER} --no-daemon'
            }
        }

        stage('Deploy to Staging') {
            when {
                branch 'master'
            }
            steps {
                echo 'Deploying to staging environment...'
            }
        }

        stage('Deploy to Production') {
            when {
                buildingTag()
            }
            steps {
                input message: 'Deploy to production?', ok: 'Deploy'
                echo 'Deploying to production environment...'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed. Please check the logs for details.'
        }
        unstable {
            echo 'Pipeline is unstable. Some tests may have failed.'
        }
    }
}
