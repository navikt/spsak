#!/usr/bin/env groovy
//
// for each layer we test first whether to run at all, then determine what to build exactly.
// note that each layer needs to encode dependency info on previous layer's libs.
// then do it using "parallel"

def dbImage = null;

def mvnOptions(String projectPath, String prevCommit) {
	 matches = sh(returnStdout: true, script: "git diff --diff-filter=DRBX --shortstat $prevCommit.. $projectPath")
	 println matches
	 return matches!=null && matches.length() >0 ? "clean" : ""
}

pipeline {
    agent any
	
	 tools {
        maven "maven-3.6.0"
		jdk "10"
    }

    triggers {
        pollSCM "* * * * *"
    }
	
	options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 15, unit: 'MINUTES')
		disableConcurrentBuilds()
    }

    stages {
        stage('init') {
            steps {
                script {
                    def scmVars = checkout scm
                    env.MY_GIT_PREVIOUS_SUCCESSFUL_COMMIT = scmVars.GIT_PREVIOUS_SUCCESSFUL_COMMIT ?: "--"
					env.LANG = "nb_NO.UTF-8"
					env.POSTGRES_IMAGE="postgres:10-alpine"
					env.DB_CONTAINER="spsak-postgres-" + "${env.JOB_NAME}".replaceAll("[^a-zA-Z0-9_-]", '_').toLowerCase()
					env.POSTGRES_USER="spsak"
					env.POSTGRES_PASSWORD="spsak"
					sh "java -version"
					sh "mvn --version"
					sh "echo $PATH"
                }
            }
        }
		stage('Database') {
			steps {
			    // TODO: bruk heller docker-compose under docker/localdev?
				script {
					sh 'docker ps -f name=$DB_CONTAINER -q | xargs --no-run-if-empty docker container stop'
					sh 'docker container ls -a -f name=$DB_CONTAINER -q | xargs -r docker container rm'
					sh 'docker pull $POSTGRES_IMAGE'
					sh 'docker run -p 5432:5432 -v "$(pwd)":/jenkins-workdir --name $DB_CONTAINER -e POSTGRES_PASSWORD=$POSTGRES_PASSWORD -e POSTGRES_USER=$POSTGRES_USER -d $POSTGRES_IMAGE'
					sh 'until docker exec $DB_CONTAINER psql -v ON_ERROR_STOP=1 --username $POSTGRES_USER -c "select 1"; do sleep 1; done'
					sh 'docker exec $DB_CONTAINER sh /jenkins-workdir/docker/localdev/initdb.sh'
				}
			}
		}
        stage('felles') {
            when {
                expression {
                    matches = sh(returnStatus:true, script: "git diff --name-only $MY_GIT_PREVIOUS_SUCCESSFUL_COMMIT|egrep -q '^felles'")
					return !fileExists("felles/target") || !fileExists(".m2") || matches==0
                }
            }
            steps {
				script {
					def module = load './mvnbuild.groovy'
					module.build('felles', mvnOptions('felles', "${env.MY_GIT_PREVIOUS_SUCCESSFUL_COMMIT}"))
				}
            }
        }
		stage('kontrakter') {
            when {
                expression {
                    matches = sh(returnStatus:true, script: "git diff --name-only $MY_GIT_PREVIOUS_SUCCESSFUL_COMMIT|egrep -q '^kontrakter'")
                    return !fileExists("kontrakter/.flattened") || !fileExists(".m2") || matches==0
                }
            }
            steps {
                script {
					def module = load './mvnbuild.groovy'
					module.build('kontrakter', mvnOptions('kontrakter', "${env.MY_GIT_PREVIOUS_SUCCESSFUL_COMMIT}"))
				}
            }
        }
        stage('saksbehandling') {
            when {
                expression {
                    matches = sh(returnStatus: true, script: "git diff --name-only $MY_GIT_PREVIOUS_SUCCESSFUL_COMMIT|egrep -q '^saksbehandling'")
                    return !fileExists("saksbehandling/target") || !fileExists(".m2") || matches==0
                }
            }
            steps {
                script {
					def module = load './mvnbuild.groovy'
					module.build('saksbehandling', mvnOptions('saksbehandling', "${env.MY_GIT_PREVIOUS_SUCCESSFUL_COMMIT}"))
				}
            }
        }
        stage('vtp-mock') {
            when {
                expression {
                    matches = sh(returnStatus: true, script: "git diff --name-only $MY_GIT_PREVIOUS_SUCCESSFUL_COMMIT|egrep -q '^vtp-mock'")
                    return !fileExists("vtp-mock/target") || !fileExists(".m2") || matches==0
                }
            }
            steps {
                script {
					def module = load './mvnbuild.groovy'
					module.build('vtp-mock', mvnOptions('vtp-mock', "${env.MY_GIT_PREVIOUS_SUCCESSFUL_COMMIT}"))
				}
            }
        }
    }
	
	post {
		always {
			script {
				sh 'docker stop $DB_CONTAINER'
			}
		}
	}
}