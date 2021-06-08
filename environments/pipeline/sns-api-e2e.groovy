def svcPort(context, ns, svc) {
    return sh(script: "kubectl --context ${context} get svc ${svc} -n ${ns} -o=jsonpath=\'{.spec.ports[0].nodePort}\'", returnStdout: true)
}

pipeline {
    agent any
    environment {
        E2E_CONTEXT_HOST = sh(script: "kubectl --context minikube get nodes -o=jsonpath=\'{.items[0].status.addresses[0].address}\'", returnStdout: true)
        E2E_IMAGE="haradayoshiaki/gauge-gradle:latest"
        API_PORT=svcPort("minikube", "sns-e2e", "sns-api-svc")
        API_URL="http://$E2E_CONTEXT_HOST:$API_PORT"
        DB_PORT=svcPort("minikube", "sns-e2e", "sns-db-svc")
        DB_URL="jdbc:postgresql://$E2E_CONTEXT_HOST:$DB_PORT/sns_db"
        E2E_ARGS="-e API_URL=$API_URL -e DB_URL=$DB_URL -u root -v /var/jenkins_home/workspace/sns-api-pipeline/e2e:/home/gradle/project -w /home/gradle/project"
    }
    stages {
        stage('e2e') {
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
            }
        }
    }
}