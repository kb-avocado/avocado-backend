pipeline {
    agent any

    stages {

        stage('Build') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean war'
            }
        }

        stage('Transfer WAR') {
            steps {
                sshagent(credentials: ['avocado-backend-ssh']) {
                    sh '''
                        scp -o StrictHostKeyChecking=no \
                        build/libs/avocado-backend-1.0-SNAPSHOT.war \
                        ubuntu@172.31.37.52:/tmp/avocado-backend.war
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sshagent(credentials: ['avocado-backend-ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no \
                        ubuntu@172.31.37.52 '
                            set -e

                            if [ -f /opt/tomcat/webapps/ROOT.war ]; then
                                sudo cp \
                                    /opt/tomcat/webapps/ROOT.war \
                                    /opt/tomcat/webapps/ROOT.war.bak
                            fi

                            /opt/tomcat/bin/shutdown.sh || true

                            sleep 5

                            sudo rm -rf /opt/tomcat/webapps/ROOT

                            sudo cp \
                                /tmp/avocado-backend.war \
                                /opt/tomcat/webapps/ROOT.war

                            sudo chown ubuntu:ubuntu \
                                /opt/tomcat/webapps/ROOT.war

                            /opt/tomcat/bin/startup.sh
                        '
                    '''
                }
            }
        }

        stage('Health Check') {
            steps {
                sshagent(credentials: ['avocado-backend-ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no \
                        ubuntu@172.31.37.52 '
                            sleep 15

                            pgrep -f "org.apache.catalina.startup.Bootstrap" > /dev/null

                            HTTP_CODE=$(curl -s \
                                -o /tmp/avocado-health-response.txt \
                                -w "%{http_code}" \
                                http://localhost:8080/)

                            echo "HTTP status: $HTTP_CODE"
                            cat /tmp/avocado-health-response.txt

                            if [ "$HTTP_CODE" != "200" ] && [ "$HTTP_CODE" != "401" ]; then
                                echo "Application health check failed."
                                exit 1
                            fi

                            echo "Application is running."
                        '
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'Avocado backend deployment succeeded.'
        }

        failure {
            echo 'Avocado backend deployment failed.'
        }
    }
}
