#!/bin/sh

cd ..

# first clean all
./gradlew clean

# now build each one
./gradlew assembleTechnewsRelease
./gradlew assembleNerdistRelease

#./gradlew assembleNeurosoupRelease
#./gradlew assembleMaxkeiserRelease
#./gradlew assembleViceRelease
#./gradlew assembleCodeorgRelease
#./gradlew assembleKhanRelease
#./gradlew assembleAndroiddevsRelease
#./gradlew assembleRtRelease
#./gradlew assembleJoeroganRelease
#./gradlew assembleJetdaisukeRelease
#./gradlew assembleTopgearRelease
#./gradlew assembleBigthinkRelease
#./gradlew assemblePewdiepieRelease
#./gradlew assembleReasontvRelease
#./gradlew assembleXdaRelease
#./gradlew assembleThevergeRelease
#./gradlew assembleJustinbieberRelease


