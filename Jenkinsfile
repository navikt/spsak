#!/usr/bin/env groovy
//
// for each layer we test first whether to run at all, then determine what to build exactly.
// note that each layer needs to encode dependency info on previous layer's libs.
// then do it using "parallel"
def modules = [:]

pipeline {
    agent any

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
                script {
					modules.felles = load "./felles/Jenkinsfile"
					modules.felles.call({})
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
                script {
					modules.kontrakter = load "./kontrakter/Jenkinsfile"
					modules.kontrakter.call({})
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
                script {
					modules.saksbehandling = load "./saksbehandling/Jenkinsfile"
					modules.saksbehandling.call({})
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
                script {
					modules.vtpmock = load "./vtp-mock/Jenkinsfile"
					modules.vtpmock.call({})
				}
            }
        }
    }
}