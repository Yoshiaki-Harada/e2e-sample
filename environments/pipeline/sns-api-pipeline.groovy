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
                stage('deploy api') {
                    steps {
                        dir('environments') {
                            script {
                                sh """helm template app/k8s/ | kubectl --context minikube apply -f -"""
                                sh """kubectl wait --for=condition=ready pod -l name=sns-api -n sns --timeout=120s"""
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
            environment {
                E2E_CONTEXT_HOST = sh(script: "kubectl --context minikube get nodes -o=jsonpath=\'{.items[0].status.addresses[0].address}\'", returnStdout: true)
                API_PORT=svcPort("minikube", "sns", "sns-api-svc")
                API_URL="http://$E2E_CONTEXT_HOST:$API_PORT"
                DB_PORT=svcPort("minikube", "sns", "sns-db-svc")
                DB_URL="jdbc:postgresql://$E2E_CONTEXT_HOST:$DB_PORT/sns_db"
            }
            parallel {
                stage('sequential') {
                    steps {
                        dir("e2e") {
                            sh """echo $API_URL"""
                            sh """docker run --rm -e API_URL=$API_URL -e DB_URL=jdbc:postgresql://192.168.39.122:30158/sns_db -u gradle -v "$PWD":/home/gradle/project -w /home/gradle/project gauge-gradle gradle gauge"""
                        }
                    }
                }
                stage('parallel') {
                    steps {
                        sh """echo parallel e2e"""
                    }
                }
            }
        }
    }
}
