pipeline {
    agent any
    options {
        skipDefaultCheckout()
    }
    stages {
        stage('checkout scm for repository') {
            steps {
                checkout scm
            }
        }
        stage('deploy') {
            parallel {
                stage('deploy') {
                    dir('environments') {
                        script {
                            sh """helm template app/k8s/ | kubectl --context minikube apply -f -"""
                        }
                    }
                }
            }
        }
    }
}
