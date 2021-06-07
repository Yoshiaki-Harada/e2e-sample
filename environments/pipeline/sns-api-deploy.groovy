def ENV=""
def NS=""
pipeline {
    agent any
    parameters {
        string(name: 'COMMIT_HASH', defaultValue: 'main', description: 'Git Hash')
        string(name: 'ENV', defaultValue: 'e2e', description: 'Helm Values file')
        string(name: 'NAMESPACE', defaultValue: 'sns-e2e', description: 'Deploy Namespace')
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
        stage("deploy to ${parms.ENV}") {
            parallel {
                stage('deploy api') {
                    steps {
                        dir('environments/app') {
                            script {
                                sh """helm template --set revision=${COMMIT_HASH} -f k8s/values-${ENV}.yaml k8s/ | kubectl --context minikube apply -f -"""
                                sh """kubectl wait --for=condition=ready pod -l name=sns-api -n ${NS} --timeout=120s"""
                            }
                        }
                    }
                }
                stage('deploy db') {
                    steps {
                        dir('environments/db') {
                            script {
                                sh """helm template --set revision=${COMMIT_HASH} k8s/values-${ENV}.yaml k8s/ | kubectl --context minikube apply -f -"""
                                sh """kubectl wait --for=condition=ready pod -l name=sns-db -n ${NS} --timeout=120s"""
                            }
                        }
                    }
                }
            }
        }
    }
}