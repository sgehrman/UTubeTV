#!/bin/sh

cd ..

# first clean all
./gradlew clean

# now build each one
./gradlew assembleTechnewsRelease
./gradlew assembleNerdistRelease
./gradlew assembleNeurosoupRelease
./gradlew assembleMaxkeiserRelease
./gradlew assembleAndroiddevsRelease
./gradlew assembleJoeroganRelease
./gradlew assembleJetdaisukeRelease
./gradlew assembleBigthinkRelease
./gradlew assembleThevergeRelease
./gradlew assembleReasontvRelease
./gradlew assembleViceRelease
./gradlew assembleCodeorgRelease
./gradlew assembleKhanRelease
./gradlew assembleRtRelease
./gradlew assembleTopgearRelease
./gradlew assemblePewdiepieRelease
#./gradlew assembleXdaRelease
#./gradlew assembleJustinbieberRelease


