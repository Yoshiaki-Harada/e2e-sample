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
                resources = ['db', 'app']
                resources.each { resource ->
                    stage(resource) {
                        steps {
                            dir('environments') {
                                script {
                                    sh """helm template $resource/k8s/ | kubectl --context minikube apply -f -"""
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
