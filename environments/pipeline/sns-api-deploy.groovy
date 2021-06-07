def ENV=""
def NS=""
pipeline {
    agent any
    parameters {
        string(name: 'COMMIT_HASH', defaultValue: 'main', description: 'Git Hash')
        string(name: 'ENV', defaultValue: 'sns', description: 'Helm Values file')
        string(name: 'NAMESPACE', defaultValue: 'sns', description: 'Deploy Namespace')
    }
    stages {
        stage('checkout scm') {
            steps {
                checkout([
                        $class           : "GitSCM",
                        userRemoteConfigs: [[
                                                    url: "git@github.com:Yoshiaki-Harada/e2e-sample.git"
                                            ]],
                        branches         : [[
                                                    name: params.COMMIT_HASH
                                            ]]
                ])

            }
        }
        stage('envronment and namespace') {
            steps {
                script {
                   ENV=params.ENV
                   NS=params.NAMESPACE
                }
            }
        }
        stage('deploy to ${parms.ENV}') {
            parallel {
                stage('deploy api') {
                    steps {
                        dir('environments') {
                            script {
                                sh """helm template --set namespace=sns-e2e --set revision=${COMMIT_HASH} -f values-${ENV} app/k8s/ | kubectl --context minikube apply -f -"""
                                sh """kubectl wait --for=condition=ready pod -l name=sns-api -n ${NS} --timeout=120s"""
                            }
                        }
                    }
                }
                stage('deploy db') {
                    steps {
                        dir('environments') {
                            script {
                                sh """helm template --set namespace=sns-e2e --set revision=$COMMIT_HASH values-${ENV} db/k8s/ | kubectl --context minikube apply -f -"""
                                sh """kubectl wait --for=condition=ready pod -l name=sns-db -n ${NS} --timeout=120s"""
                            }
                        }
                    }
                }
            }
        }
    }
}