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
                stage('deploy api') {
                    steps {
                        dir('environments') {
                            script {
                                sh """helm template app/k8s/ | kubectl --context minikube apply -f -"""
                                sg """kubectl wait --for=condition=ready pod -l name=sns-api -n sns --timeout=30s"""
                            }
                        }
                    }
                }
                stage('deploy db') {
                    steps {
                        dir('environments') {
                            script {
                                sh """helm template db/k8s/ | kubectl --context minikube apply -f -"""
                            }
                        }
                    }
                }
            }
        }
        stage('e2e') {
            parallel {
                stage('sequential') {
                    steps {
                        sh """echo 'sequential e2e'"""
                    }
                }
                stage('parallel') {
                    steps {
                        sh """echo 'parallel e2e'"""
                    }
                }
            }
        }
    }
}
