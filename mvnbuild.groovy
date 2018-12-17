@Library('deploy')
import deploy

def deployLib = new deploy()

def build(String mydir, String mvnOptions) {
    try {
		dir(mydir) {
			stage ('Build: ' + mydir) {
				// bruker private m2 repo i workspace slik at fungerer også uavhengig for PR branches
				sh 'mvn -B -s ../mvn-settings.xml --no-snapshot-updates -Dmaven.repo.local=../.m2 ' + mvnOptions + ' install' 
			}
		}
		
	} catch (error) {
		throw error
	}
}

return this;