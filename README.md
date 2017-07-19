# Firebase Test Plugin

Runs Android tests on Firebase, and stores result artifacts in the workspace.

This plugin is only compatible with Pipeline scripts.

## Setup

Supports the
[GCloud SDK Plugin](https://wiki.jenkins.io/display/JENKINS/GCloud+SDK+Plugin)
for automated tool installations.

Alternatively, manually install
the [Google Cloud SDK](https://cloud.google.com/sdk/downloads) and either ensure
the `gcloud` binary is on the system `$PATH` or provide a path to the binary in
the pipeline step definition.

Either set a system-wide Google API credential using `gcloud auth
login|activate-service-account`, or add a Google Robot credential. See the
[Google OAuth Plugin](https://wiki.jenkins.io/display/JENKINS/Google+OAuth+Plugin) for
documentation.

> Note: Only JSON-format service account keys are supported.

## Usage

In a pipeline stage:

```groovy
firebaseTest gcloud: {path to gcloud binary}, credentialsId: {id}, resultsDir: {path}, command: {command}
```

> Note: A GCloud SDK tool installation can be used like so:
> ```groovy
> firebaseTest gcloud: "${tool 'gcloud-tool-name'}/bin/gcloud", ...
> ```

Where `command` may be one of:

```groovy
instrumentation(
    app:             {path to app APK},
    test:            {path to test APK},

    // Optional parameters
    device:          [{device}, {device}, ...],
    testPackage:     {package id},
    testRunnerClass: {test runner class},
    testTargets:     "{target},{target},...",

    {common}
)
```

```groovy
robo(
    app:                {path to app APK},

    // Optional parameters
    device:             [{device}, {device}, ...],
    appInitialActivity: {activity},
    maxDepth:           {depth},
    maxSteps:           {steps},
    roboDirectives:     "{directive},{directive},...",

    {common}
)
```

Common options for both `instrumentation` and `robo` tests:

```groovy
    timeout:              {timeout},
    appPackage:           {package id},
    async:                {boolean},
    autoGoogleLogin:      {boolean},
    directoriesToPull:    "{path},{path},...",
    environmentVariables: "{var=val},{var=val},...",
    obbFiles:             "{path},{path},..."
    resultsBucket:        {cloud storage bucket id},
    resultsDir:           {path in bucket},
    resultsHistoryName:   {name}
```

Firebase test arguments can also be provided via
[argument files](https://cloud.google.com/sdk/gcloud/reference/topic/arg-files)
in the workspace:


```groovy
argfile(
    path:  {path to argfile},
    group: {argument group key}
)
```

See the
[gcloud documentation](https://cloud.google.com/sdk/gcloud/reference/firebase/test/android/run) for
additional information about test options.

The **Pipeline Syntax** snippet generator (`<jenkins-server>/pipeline-syntax/`)
is also helpful for authoring Firebase tests.

## Artifacts

Test artifacts are stored in `resultsDir`, relative to the workspace, which
defaults to `.firebase/`.  The artifacts pulled are:

- `junit-{device}.xml` - JUnit-style test results
- `logcat-{device}` - Raw logcat output from the device

The [JUnit Plugin](https://wiki.jenkins.io/display/JENKINS/JUnit+Plugin) can be
used to publish the test results:

```groovy
pipeline {
    stages {
        stage("Firebase test") {
            steps {
                firebase instrumentation(app: 'app.apk' test: 'app-test.apk')
            }
            post {
                always {
                    junit testResults: '.firebase/*.xml'
                }
            }
        }
    }
}
```

## License

```
Copyright 2017 Simple Finance Technology Corp

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
