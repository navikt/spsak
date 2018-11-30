@Library('deploy')
import deploy

def deployLib = new deploy()

def call(body) {
    try {
		def dir = body.dir
		
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