# Dev notes

## Release new version

Example release 2.2.0 and prepare for next version 2.3.0.

1. Run `./scripts/release.sh 2.2.0`
2. `gren release --draft`
3. Review and accept release
4. Run `./scripts/update_changelog.sh 2.2.0`
4. Run `./scripts/prepare_next_version.sh 2.3.0`


## How to's in travis

Travis client install process: https://github.com/travis-ci/travis.rb#installation

### Create secure variable

* Generate secure variable called `MY_SECRET_ENV` with the value `super_secret`:
```
travis encrypt MY_SECRET_ENV=super_secret
```
* Then add it to .travis.yml

### Generate secure file

* Login into travis (supports two-factor authentication)
```
travis login --org
```
* Generate secure file from file called settings.xml
```
travis encrypt-file settings.xml
```
* Follow the instructions given as result of the previous command execution, which will add stuff into .travis.yml


## Redeploy on docker

* Delete all stopped containers
```
docker ps -q -f status=exited | xargs --no-run-if-empty docker rm
```
* Delete all dangling (unused) images
```
docker images -q -f dangling=true | xargs --no-run-if-empty docker rmi
```
* Use bash in running container
```
docker exec -i -t CONTAINER_ID /bin/bash
```

## Known problems

* Problems may arise when using GWT Dev Mode and having in the classpath a different jetty version
* Problems with maven dependencies that are fat-jars can be solved, even if this is hard to maintain, through repackaging and package name relocation.
	For example, if ApacheDS dependency contains bouncycastle and it colides with a certain but needed version of bouncycastle, one might use maven-shade plugin to solve the problem. Steps:
	* Download source code
	```
	wget http://mirrors.fe.up.pt/pub/apache//directory/apacheds/dist/2.0.0-M21/apacheds-parent-2.0.0-M21-source-release.zip
	```
	* Edit file apacheds-parent-2.0.0-M21/all/pom.xml as follows
	```
	     <plugin>
	       <groupId>org.apache.maven.plugins</groupId>
	       <artifactId>maven-shade-plugin</artifactId>
	       <executions>
	         <execution>
	           <phase>package</phase>
	           <goals>
	             <goal>shade</goal>
	           </goals>
	           <configuration>
	             <promoteTransitiveDependencies>true</promoteTransitiveDependencies>
	             <filters>
	               <filter>
	                 <artifact>org.bouncycastle:bcprov-jdk15on</artifact>
	                 <excludes>
	                   <exclude>META-INF/BCKEY.SF</exclude>
	                   <exclude>META-INF/BCKEY.DSA</exclude>
	                 </excludes>
	               </filter>
	             </filters>
	             <relocations>
	               <relocation>
	                 <pattern>org.bouncycastle</pattern>
	                 <shadedPattern>org.bouncycastle.shaded</shadedPattern>
	               </relocation>
	             </relocations>
	           </configuration>
	         </execution>
	       </executions>
	     </plugin>
	```
	* Recompile and grab the artifact
	```
	mvn clean package -Dmaven.test.skip
	cp all/target/apacheds-all-2.0.0-M21.jar ...
	```

* Tomcat7 behaves oddly when using the following code, throwing an exception (closed stream exception), whereas tomcat8 performs well/correctly
 ```
 StreamingOutput streamingOutput = new StreamingOutput() {

 @Override
 public void write(OutputStream os) throws IOException, WebApplicationException {
   InputStream retrieveFile = null;
   try {
     retrieveFile =  RodaCoreFactory.getFolderMonitor().retrieveFile(transferredResource.getFullPath());
     IOUtils.copy(retrieveFile, os);
     } catch (NotFoundException | RequestNotValidException | GenericException e) {
       // do nothing
     } finally {
       IOUtils.closeQuietly(retrieveFile);
     }
   }
 };
```
## Misc

* Executing worker (compiled with -Proda-core-jar):
	* `java -Droda.node.type=worker -jar roda-core/roda-core/target/roda-core-2.0.0-SNAPSHOT-jar-with-dependencies.jar`
	* `java -cp roda-core/roda-core/target/roda-core-2.0.0-SNAPSHOT-jar-with-dependencies.jar -Droda.node.type=worker org.roda.core.RodaCoreFactory`

* Solr HTTP (create cores from RODA install dir in ~/.roda/)
```
cd SOLR_INSTALL_DIR
bin/solr start
for i in $(ls ~/.roda/config/index/);do if [ -d ~/.roda/config/index/$i ]; then bin/solr create -c $i -d ~/.roda/config/index/$i/conf/ ; fi; done
```
OR
```
INDEXES_PATH=~/roda/roda-core/roda-core/src/main/resources/config/index/; SOLR_BIN=/apps/KEEPS/solr-single-node-tests/solr-5.5.0/bin/solr ; for i in $(find $INDEXES_PATH -mindepth 1 -maxdepth 1 -type d); do COLLECTION="$(basename $i)"; $SOLR_BIN create -c "$COLLECTION" -d "$INDEXES_PATH/$COLLECTION/conf/" -p 8984 ; done
```
* Analyze RODA dependencies for version update
```
mvn versions:display-dependency-updates
```

## Run CLI on docker or using tomcat libs

```
java -cp "webapps/ROOT/WEB-INF/lib/*:lib/*" org.roda.core.RodaCoreFactory migrate model
```
