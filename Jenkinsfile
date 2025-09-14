pipeline {
    agent any

    tools {
        maven 'maven'
    }

    environment {
        DEPLOY_FOLDER = "${WORKSPACE}/deploy"
        // Kubernetes cluster configuration
        PROJECT_ID = 'santos-435406'
        CLUSTER_NAME = "santosk8s"
        ZONE_KUBERNETES = "asia-east1-a"
        // Docker registry configuration
        ZONE_REPO = "asia-east2"
        DATA_SERVICE_REPO = "santos-repo"
        // Service account configuration
        SANTOS_REPO_SERVICE_ACCOUNT = credentials('santos-repo-account-service')
        COMPUTER_SERVICE_ACCOUNT = credentials('computer-engine-service-account')
    }

    stages {
        stage('Init Environment') {
            steps {
                script {
                    scmVars = checkout scm
                    env.BRANCH_NAME = scmVars.GIT_BRANCH.replaceAll('^origin/', '').replaceAll('/', '-').toLowerCase()
                    env.DATA_SERVICE_DEPLOYMENT_NAME = "data-service-${env.BRANCH_NAME}"
                    env.DATA_SERVICE_PORT = "8080"
                    env.DATA_SERVICE_REGISTRY_PATH = "${ZONE_REPO}-docker.pkg.dev/${PROJECT_ID}/${DATA_SERVICE_REPO}/${DATA_SERVICE_DEPLOYMENT_NAME}"
                }
            }
        }


        stage('Build and Test') {
            steps {
                script {
                    sh 'mvn -version'
                    sh 'java -version'
                    sh 'mvn clean package'
                }
            }
        }

        stage('Pushing Docker Image') {
            steps {
                script {
                    sh '''  
                            gcloud auth activate-service-account --key-file=${SANTOS_REPO_SERVICE_ACCOUNT}
                            gcloud auth configure-docker ${ZONE_REPO}-docker.pkg.dev
                       '''
                    sh 'docker build -t ${DATA_SERVICE_REGISTRY_PATH}:1.0.2 .'
                    sh 'docker push ${DATA_SERVICE_REGISTRY_PATH}:1.0.2'
                }
            }
        }


        stage('Deploy to GKE') {
            steps {
                script {
                    dir(DEPLOY_FOLDER) {

                        sh '''
                            gcloud auth activate-service-account --key-file=${COMPUTER_SERVICE_ACCOUNT}
                            gcloud container clusters get-credentials ${CLUSTER_NAME} --zone ${ZONE_KUBERNETES} --project ${PROJECT_ID}
                        '''
                        sh '''
                            sed -e "s|\\\${DATA_SERVICE_DEPLOYMENT_NAME}|${DATA_SERVICE_DEPLOYMENT_NAME}|g" \
                                -e "s|\\\${DATA_SERVICE_PORT}|${DATA_SERVICE_PORT}|g" \
                                -e "s|\\\${ZONE_REPO}|${ZONE_REPO}|g" \
                                -e "s|\\\${PROJECT_ID}|${PROJECT_ID}|g" \
                                -e "s|\\\${DATA_SERVICE_REPO}|${DATA_SERVICE_REPO}|g" \
                                -e "s|\\\${DATA_SERVICE_REGISTRY_PATH}|${DATA_SERVICE_REGISTRY_PATH}|g" \
                            data-service-deployment.yaml > data-service-deployment-updated.yaml
                            
                            kubectl apply -f data-service-deployment-updated.yaml
                        '''
                    }
                }
            }
        }

//        stage('Deploy to GKE') {
//            steps {
//                script {
//                    dir(DEPLOY_FOLDER) {
//                       sh 'kubectl --kubeconfig=/home/quangnam130520/.kube/config apply -f data-service-deployment.yaml'
//                    }
//                }
//            }
//    }
    }

    post {
        // If the pipeline is successful, send a Slack notification.
//        success {
//            slackSend channel: '#general',
//                    color: 'good',
//                    message: "The pipeline ${currentBuild.fullDisplayName} has succeeded."
//        }

        // Cleanup workspace after the build is done
        always {
            cleanWs()
        }
    }
}
