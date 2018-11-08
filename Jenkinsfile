pipeline {
  agent any
  stages {
    stage('Compile') {
      steps {
        sh './gradlew compileDebugSources'
      }
    }
//  stage('Unit test') {
//    steps {
//      sh './gradlew testDebugUnitTest testDebugUnitTest'
//      junit '**/TEST-*.xml'
//    }
//  }
    stage('Build APK') {
      steps {
        sh './gradlew assembleDebug'
      }
    }
    stage('Static analysis') {
      steps {
        sh './gradlew lintDebug'
        androidLint(pattern: '**/lint-results-*.xml')
      }
    }
    stage('Deploy') {

      when {
        // Only execute this stage when building from the `master` branch
        branch 'master'
      }
      steps {
        sh './gradlew assembleRelease'
        signAndroidApks(archiveSignedApks: true, keyAlias: 'losungen_final', keyStoreId: 'losungen-cert', apksToSign: '**/*-unsigned.apk')
        androidApkUpload(apkFilesPattern: '**/*-release.apk', googleCredentialsId: 'Google play', trackName: 'beta')
      }
    }
  }
  options {
    skipStagesAfterUnstable()
  }
}
