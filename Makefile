rs2fol:  ./src/main/*
	./gradlew installDist
	cp -r ./build/install/rs2fol/* ./
