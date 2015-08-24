all:
	mvn install:install-file -Dfile=./berkeleylm.jar \
		-DgroupId=berkeleylm -DartifactId=berkeleylm -Dversion=1.1.5 -Dpackaging=jar \
		-DcreateChecksum=true -DlocalRepositoryPath=local_mvn_repo

clean:
	rm -rf ./local_mvn_repo

