@Library('deploy')
import deploy

def deployLib = new deploy()

def build(String mydir) {
    try {
		dir(mydir) {
			stage ('Build: ' + mydir) {
				sh 'mvn -s ../mvn-settings.xml install' 
			}
		}
		
	} catch (error) {
		throw error
	}
}

return this;