def svcPort(context, ns, svc) {
    return sh(script: "kubectl --context ${context} get svc ${svc} -n ${ns} -o=jsonpath=\'{.spec.ports[0].nodePort}\'", returnStdout: true)
}

def COMMIT_HASH=""

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
                stage('build') {
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
        stage('deploy') {
            parallel {
                stage('deploy api') {
                    steps {
                        dir('environments') {
                            script {
                                sh """helm template --set namespace=sns-e2e --set revision=$COMMIT_HASH app/k8s/ | kubectl --context minikube apply -f -"""
                                sh """kubectl wait --for=condition=ready pod -l name=sns-api -n sns-e2e --timeout=120s"""
                            }
                        }
                    }
                }
                stage('deploy db') {
                    steps {
                        dir('environments') {
                            script {
                                sh """helm template --set namespace=sns-e2e --set revision=$COMMIT_HASH db/k8s/ | kubectl --context minikube apply -f -"""
                                sh """kubectl wait --for=condition=ready pod -l name=sns-db -n sns-e2e --timeout=120s"""
                            }
                        }
                    }
                }
            }
        }
        stage('e2e') {
            environment {
                E2E_CONTEXT_HOST = sh(script: "kubectl --context minikube get nodes -o=jsonpath=\'{.items[0].status.addresses[0].address}\'", returnStdout: true)
                E2E_IMAGE="haradayoshiaki/gauge-gradle:latest"
                API_PORT=svcPort("minikube", "sns-e2e", "sns-api-svc")
                API_URL="http://$E2E_CONTEXT_HOST:$API_PORT"
                DB_PORT=svcPort("minikube", "sns-e2e", "sns-db-svc")
                DB_URL="jdbc:postgresql://$E2E_CONTEXT_HOST:$DB_PORT/sns_db"
                E2E_ARGS="-e DB_URL=$DB_URL -u root -v /var/jenkins_home/workspace/sns-api-pipeline/e2e:/home/gradle/project -w /home/gradle/project"
            }
            parallel {
                stage('sequential') {
                    agent {
                        docker {
                            image E2E_IMAGE
                            args E2E_ARGS
                            reuseNode true
                        }
                    }
                    steps {
                        dir("e2e") {
                            sh """ gradle gauge -Ptags="sequential" """
                        }
                    }
                }
                // stage('parallel') {
                //     environment {
                //         API_PORT=svcPort("minikube", "sns", "sns-api-svc")
                //         API_URL="http://$E2E_CONTEXT_HOST:$API_PORT"
                //         DB_PORT=svcPort("minikube", "sns", "sns-db-svc")
                //         DB_URL="jdbc:postgresql://$E2E_CONTEXT_HOST:$DB_PORT/sns_db"
                //         E2E_ARGS="-e DB_URL=$DB_URL -u root -v /var/jenkins_home/workspace/sns-api-pipeline/e2e:/home/gradle/project -w /home/gradle/project"
                //     }
                //     agent {
                //         docker {
                //             image E2E_IMAGE
                //             args E2E_ARGS
                //             reuseNode true
                //         }
                //     }
                //     steps {
                //         dir("e2e") {
                //             sh """ gradle gauge -Ptags="!sequential" """
                //         }
                //     }
                // }
            }
        }
    }
}