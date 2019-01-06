#!/usr/bin/env groovy
//
// for each layer we test first whether to run at all, then determine what to build exactly.
// note that each layer needs to encode dependency info on previous layer's libs.
// then do it using "parallel"

def incrementalBuild = false
def dbImage = null

def mvnBuild(String mydir) {
	dir(mydir) {
		// bruker private m2 repo i workspace slik at fungerer også uavhengig for PR branches
		sh 'mvn -B -s ../mvn-settings.xml --no-snapshot-updates -Dmaven.repo.local=../.m2 clean install' 
	}
}

def shouldRunStage(Boolean incrementalBuild, String projectPath, String checkFileExists) {
	if(!incrementalBuild || !fileExists(checkFileExists) || !fileExists(".m2")){
		return true
	} else {
		return 0==sh(returnStatus:true, script: "git diff --name-only $MY_GIT_PREVIOUS_SUCCESSFUL_COMMIT|egrep -q '^" + projectPath + "'")
	}
}

pipeline {
    agent any
	
	 tools {
        maven "maven-3.6.0"
		jdk "11"
    }

    triggers {
        pollSCM "* * * * *"
    }
	
	parameters {
        booleanParam(defaultValue: true, description: 'Incremental build', name: 'incrementalBuild')
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
					env.POSTGRES_IMAGE="postgres:11-alpine"
					env.DB_CONTAINER="spsak-postgres-" + "${env.JOB_NAME}".replaceAll("[^a-zA-Z0-9_-]", '_').toLowerCase()
					env.POSTGRES_USER="spsak"
					env.POSTGRES_PASSWORD="spsak"
					sh "java -version"
					sh "mvn --version"
					sh "echo $PATH"
					incrementalBuild = params.incrementalBuild
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
            steps {
				script {
					if(shouldRunStage(incrementalBuild, "felles", "felles/target")) {
						mvnBuild('felles')
						incrementalBuild=false
					}
				}
            }
        }
		stage('kontrakter') {
            steps {
                script {
					if(shouldRunStage(incrementalBuild, "kontrakter", "kontrakter/.flattened")) {
						mvnBuild('kontrakter')
						incrementalBuild=false
					}
				}
            }
        }
        stage('saksbehandling') {
            steps {
                script {
					if(shouldRunStage(incrementalBuild, "saksbehandling", "saksbehandling/target")) {
						mvnBuild('saksbehandling')
					}
				}
            }
        }
        stage('vtp-mock') {
            steps {
                script {
					if(shouldRunStage(incrementalBuild, "vtp-mock", "vtp-mock/.flattened")) {
						mvnBuild('vtp-mock')
					}
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
