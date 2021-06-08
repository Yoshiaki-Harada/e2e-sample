
def COMMIT_HASH=""
def REGISTORY="haradayoshiaki777"
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
        stage('Commit Hash') {
            steps {
                script {
                    COMMIT_HASH = sh(returnStdout: true, script: 'echo -n `git rev-parse --short HEAD`')
                }
            }
        }
        stage('build') {
            parallel {
                stage('build api') {
                    steps {
                        dir('environments/app/docker') {
                            script {
                                sh "./build_and_push.sh $COMMIT_HASH"
                            }
                        }
                    }
                }
                stage("build db") {
                    steps {
                        dir('environments/db/docker') {
                            script {
                                sh "./build_and_push.sh $COMMIT_HASH"
                            }
                        }
                    }
                }
            }
        }
        stage('deploy to e2e') {
            steps {
                build(
                    job: "sns-api-deployment",
                    parameters: [[
                        $class: 'StringParameterValue',
                        name: 'COMMIT_HASH',
                        value: COMMIT_HASH
                    ],
                    [
                        $class: 'StringParameterValue',
                        name: 'ENV',
                        value: "e2e"
                    ],
                    [
                        $class: 'StringParameterValue',
                        name: 'NAMESPACE',
                        value: "sns-e2e"
                    ]]
                )
            }
        }
        stage('e2e') {
            steps {
                build(job: "sns-api-e2e")
            }
        }

        stage('push success docker image') {
            steps {
                script {
                    def apiImage = docker.image("$REGISTORY/sns-api:$COMMIT_HASH")
                    apiImage.pull()
                    apiImage.push("latest")
                    def dbImage = docker.image("$REGISTORY/sns-db:$COMMIT_HASH")
                    dbImage.pull()
                    dbImage.push("latest")
                }
            }
        }
        stage('deploy to dev') {
            parallel {
                stage('deploy api') {
                    steps {
                        dir('environments') {
                            script {
                                sh """helm template app/k8s/ | kubectl --context minikube apply -f -"""
                                sh """kubectl wait --for=condition=ready pod -l name=sns-api -n sns-e2e --timeout=120s"""
                            }
                        }
                    }
                }
                stage('deploy db') {
                    steps {
                        dir('environments') {
                            script {
                                sh """helm template db/k8s/ | kubectl --context minikube apply -f -"""
                                sh """kubectl wait --for=condition=ready pod -l name=sns-db -n sns-e2e --timeout=120s"""
                            }
                        }
                    }
                }
            }
        }
    }
}