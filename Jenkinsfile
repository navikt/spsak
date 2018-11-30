#!/usr/bin/env groovy
//
// for each layer we test first whether to run at all, then determine what to build exactly.
// note that each layer needs to encode dependency info on previous layer's libs.
// then do it using "parallel"

pipeline {
    agent any
	
	 tools {
        maven "maven-3.6.0"
    }

    triggers {
        pollSCM "* * * * *"
    }
	
	options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {
        stage('init') {
            steps {
                script {
                    def scmVars = checkout scm
                    env.MY_GIT_PREVIOUS_SUCCESSFUL_COMMIT = scmVars.GIT_PREVIOUS_SUCCESSFUL_COMMIT ?: "--"
                }
            }
        }
        stage('felles') {
            when {
                expression {
                    matches = sh(returnStatus:true, script: "git diff --name-only $MY_GIT_PREVIOUS_SUCCESSFUL_COMMIT|egrep -q '^felles'")
					return !fileExists("felles/target") || !matches
                }
            }
            steps {
				dir('felles') {
					script {
						def module = load "./Jenkinsfile"
						module.call({})
					}
				}
            }
        }
		stage('kontrakter') {
            when {
                expression {
                    matches = sh(returnStatus:true, script: "git diff --name-only $MY_GIT_PREVIOUS_SUCCESSFUL_COMMIT|egrep -q '^kontrakter'")
                    return !fileExists("kontrakter/target") || !matches
                }
            }
            steps {
                dir('kontrakter') {
					script {
						def module = load "./Jenkinsfile"
						module.call({})
					}
				}
            }
        }
        stage('saksbehandling') {
            when {
                expression {
                    matches = sh(returnStatus: true, script: "git diff --name-only $MY_GIT_PREVIOUS_SUCCESSFUL_COMMIT|egrep -q '^saksbehandling'")
                    return !fileExists("saksbehandling/target") || !matches
                }
            }
            steps {
                dir('saksbehandling') {
					script {
						def module = load "./Jenkinsfile"
						module.call({})
					}
				}
            }
        }
        stage('vtp-mock') {
            when {
                expression {
                    matches = sh(returnStatus: true, script: "git diff --name-only $MY_GIT_PREVIOUS_SUCCESSFUL_COMMIT|egrep -q '^vtp-mock'")
                    return !fileExists("vtp-mock/target") || !matches
                }
            }
            steps {
                dir('vtp-mock') {
					script {
						def module = load "./Jenkinsfile"
						module.call({})
					}
				}
            }
        }
    }
}