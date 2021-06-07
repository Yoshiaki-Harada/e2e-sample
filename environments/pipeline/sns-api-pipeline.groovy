def svcPort(context, ns, svc) {
    return sh(script: "kubectl --context ${context} get svc ${svc} -n ${ns} -o=jsonpath=\'{.spec.ports[0].nodePort}\'", returnStdout: true)
}

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
                namespaces=["sns-e2e", "sns-e2e-seq"]
                namespaces.each {ns ->
                stage('deploy api to $ns') {
                    steps {
                        dir('environments') {
                            script {
                                sh """helm template --set namespace=$ns app/k8s/ | kubectl --context minikube apply -f -"""
                                sh """kubectl wait --for=condition=ready pod -l name=sns-api -n sns --timeout=120s"""
                            }
                        }
                    }
                }
                stage('deploy db to $ns') {
                    steps {
                        dir('environments') {
                            script {
                                sh """helm template --set namespace=$ns db/k8s/ | kubectl --context minikube apply -f -"""
                                sh """kubectl wait --for=condition=ready pod -l name=sns-db -n sns --timeout=120s"""
                            }
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
            }
            parallel {
                stage('sequential') {
                    environment {
                        API_PORT=svcPort("minikube", "sns-e2e", "sns-api-svc")
                        API_URL="http://$E2E_CONTEXT_HOST:$API_PORT"
                        DB_PORT=svcPort("minikube", "sns-e2e", "sns-db-svc")
                        DB_URL="jdbc:postgresql://$E2E_CONTEXT_HOST:$DB_PORT/sns_db"
                        E2E_ARGS="-e DB_URL=jdbc:postgresql://192.168.39.122:30158/sns_db -u root -v /var/jenkins_home/workspace/sns-api-pipeline/e2e:/home/gradle/project -w /home/gradle/project"
                    }
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
                stage('parallel') {
                    environment {
                        API_PORT=svcPort("minikube", "sns-e2e-seq", "sns-api-svc")
                        API_URL="http://$E2E_CONTEXT_HOST:$API_PORT"
                        DB_PORT=svcPort("minikube", "sns-e2e-seq", "sns-db-svc")
                        DB_URL="jdbc:postgresql://$E2E_CONTEXT_HOST:$DB_PORT/sns_db"
                        E2E_ARGS="-e DB_URL=jdbc:postgresql://192.168.39.122:30158/sns_db -u root -v /var/jenkins_home/workspace/sns-api-pipeline/e2e:/home/gradle/project -w /home/gradle/project"
                    }
                    agent {
                        docker {
                            image E2E_IMAGE
                            args E2E_ARGS
                            reuseNode true
                        }
                    }
                    steps {
                        dir("e2e") {
                            sh """ gradle gauge -Ptags="!sequential" """
                        }
                    }
                }
            }
        }
    }
}