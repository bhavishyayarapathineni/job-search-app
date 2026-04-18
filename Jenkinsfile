pipeline {
    agent any
    tools { maven 'Maven' }
    environment { SONAR_TOKEN = credentials('sonar-token') }
    stages {
        stage('Checkout') {
            steps { git branch: 'main', url: 'https://github.com/bhavishyayarapathineni/job-search-app.git' }
        }
        stage('Build') {
            steps { dir('backend') { sh 'mvn clean compile -DskipTests' } }
        }
        stage('Test') {
            steps { dir('backend') { sh 'mvn test' } }
            post {
                always {
                    dir('backend') {
                        junit 'target/surefire-reports/*.xml'
                        jacoco(execPattern: 'target/jacoco.exec', classPattern: 'target/classes', sourcePattern: 'src/main/java')
                    }
                }
            }
        }
        stage('SonarQube') {
            steps {
                dir('backend') {
                    sh "mvn sonar:sonar -Dsonar.projectKey=bhavishyayarapathineni_job-search-app -Dsonar.host.url=https://sonarcloud.io -Dsonar.token=${SONAR_TOKEN}"
                }
            }
        }
        stage('Package') {
            steps { dir('backend') { sh 'mvn package -DskipTests' } }
            post { success { dir('backend') { archiveArtifacts artifacts: 'target/*.jar', fingerprint: true } } }
        }
    }
    post {
        success { echo 'Pipeline SUCCESS!' }
        failure { echo 'Pipeline FAILED!' }
    }
}
