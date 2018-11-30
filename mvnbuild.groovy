@Library('deploy')
import deploy

def deployLib = new deploy()

def call(String dir) {
    try {
		dir(dir) {
			stage ('Build: ' + dir) {
				sh 'mvn -s ../mvn-settings.xml install -DskipTests' 
			}
		}
		
	} catch (error) {
		throw error
	}
}

return this;