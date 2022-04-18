## Summary 
Authenticalls Flashcall Android SDK implements a wrapper to automatically intercept Authenticalls calls and give the feedback to your app. 
Therefore, the Android application developers do not need to invest time and effort into researching how to intercept calls and get the phone number that was used for the authenticalls
Demo application.

All the instructions about how to import and use the Authenticalls Flashcall Android SDK will be easier to understand by looking at our demo application. In order to start the demo application you have to follow these steps:

1. Download the latest version of the demo application code from here.
2. Unzip the downloaded archive and create a file called ```secrets.properties``` in the root directory (the directory that contains files build.gradle, settings.gradle, etc.) 
3. Open the empty secrets.properties file in a text editor and add the following lines: 
```
AUTHENTICALLS_GITHUB_USERNAME=authenticalls 
AUTHENTICALLS_GITHUB_READ_TOKEN=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
``` 
5. Open the folder of the demo app in Android Studio and run gradle sync (it should sync automatically at open)
6. That’s it! Now you can navigate the demo application code, build it, run it in an emulator, etc.
Importing and initializing the SDK 

Authenticalls Flashcall Android SDK is distributed as a library via a private maven repository. In order to import the SDK, first you need to configure your gradle project to know about our maven repository, by adding the following lines in your ```build.gradle``` (application) file: 

```
def secretProps = new Properties() 
secretProps.load(new FileInputStream(rootProject.file("secrets.properties"))) repositories { 
maven { 
    name = "AuthenticallsSDKPackages" 
    url = uri("https://maven.pkg.github.com/authenticalls/pay_android_app") 
        credentials { 
            username = secretProps['AUTHENTICALLS_GITHUB_USERNAME'] 
            password = secretProps['AUTHENTICALLS_GITHUB_READ_TOKEN'] 
        } 
    } 
}
```

As you can see, the credentials part uses some variables located in a file called ```secrets.properties```. You should create this text file in the root directory of your application (the same directory of files like build.gradle, settings.gradle, etc.). If you are using a version control system such as GIT, we recommend you to avoid adding this file to your repository (i.e. add it to .gitignore). The content of the file should look similar to this: 
```
GITHUB_USERNAME=authenticalls 
GITHUB_READ_TOKEN=167f17abb0fbe21bf7835227ad3baf51111adbc2
``` 

The second step is to simply add the dependency line in your ```build.gradle``` (application), which should look like this: 
```
dependencies { 
    // all your other application dependencies should be here as well // ... 
    // add Flashcall SDK dependency: 
    implementation "com.autenticalls.libraries:flashcall_sdk:1.0.0-rc01" 
} 
```
You can replace ```1.0.0``` with another version of the SDK, but we recommend you to always use the latest version. You can find the list of versions in the Changelog section. The final import step is to Sync your project with the Grade files. 

## Workflow 

The SDK is built around Android MVVM (Model, View, ViewModel) Design Pattern. More exactly, it exposes ViewModels, Fragments and Activities that contain all the data and business logic required to facilitate the payments made via our system. The available ViewModels/Fragments/Activities are: 

➔ ```FlashcallViewModel``` (```com.authenticalls.pay_sdk.ui.FlashcallViewModel```) 
    
➔ ```addListener``` - Used to add listener for calls

➔ ```removeListener``` - Used to remove listener

➔ ```resetStatus``` - Used to reset the flashcall observed or cancel it
There are also some data classes exposed by the SDK, such as ```FlashcallSDKStatus``` and ```FlashcallCDR```. These are explained in more detail at section ```Exposed Data Classes```.

## Initiating the SDK

The application user should be connected to the internet. The application developer is expected to navigate to ```FlashcallFragment```. 
An example of Kotlin code is presented in the ```PhoneVerificationFragment``` of our demo application: 
```
    // Go to Flashcall SDK fragment 
    view.findNavController().navigate(R.id.action_PhoneVerificationFragment_to_flashcallFragment) 
```

We recommend using a parent activity that should be able to navigate between your checkout or product fragments/activities and the Flashcall SDK fragment, because when the payment is confirmed the SDK will search for a NavController instance and, if present, will go back to the previous page, which you should update accordingly to the confirmed status of the payment. Otherwise, the user will have to press the back button after the payment is complete.

## Exposed Data Classes 

The data classes that the SDK exposes will help you create transactions and update your application based on the transaction status.

Most of the ones located in “com.authenticalls.flashcall_sdk.network.models” are based on our API Integration Manual. In this section we will explain each of them: 


```CallDetails```: DTO of the needed call details.

➔ ```msisdn```: ```String```

➔ ```duration```: ```String```

➔ ```timestamp```: ```String```


```FlashcallSDKStatus```

➔ Instantiated by ``flashcallViewModel`` and available as LiveData (```flashcallViewModel.flashcallSDKStatus```). 

➔ Initialized as ```WAITING_FOR_FLASHCALL``` at the start of the application.

➔ Changes to ```CALL_RINGING``` when phone starts to ring 

➔ Changes to ```CALL_HANGUP``` when the call is ended. 

➔ Changes to ```CALL_FINISHED``` After the call details are available and stored in callDetails variable in ```FlashcallViewModel```

## Changelog 

1.0.0 

➔ First public version of the SDK
