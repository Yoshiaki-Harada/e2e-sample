def svcPort(context, ns, svc) {
    return sh(script: "kubectl --context ${context} get svc ${svc} -n ${ns} -o=jsonpath=\'{.spec.ports[0].nodePort}\'", returnStdout: true)
}

def COMMIT_HASH=""
def API_IMAGE="haradayoshiaki777/sns-db"
def DB_IMAGE="haradayoshiaki777/sns-api"
def DB_NS="sns"
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
                E2E_ARGS="-e API_URL=$API_URL -e DB_URL=$DB_URL -u root -v /var/jenkins_home/workspace/sns-api-pipeline/e2e:/home/gradle/project -w /home/gradle/project"
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
                            sh """ gradle clean :sns-api-e2e:gauge -Ptags=sequential """
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

        stage('push success docker image') {
            steps {
                script {
                    """ docker pull "$API_IMAGE:$COMMIT_HASH" """
                    """ docker tag "$API_IMAGE:$COMMIT_HASH" "$API_IMAGE:latest" """
                    """ docker push "$API_IMAGE:latest" """
                    """ docker pull "$DB_IMAGE:$COMMIT_HASH" """
                    """ docker tag "$DB_IMAGE:$COMMIT_HASH" "$API_IMAGE:latest" """
                    """ docker push "$DB_IMAGE:latest" """
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