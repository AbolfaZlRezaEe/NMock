![NMock Banner](https://user-images.githubusercontent.com/73066290/172200086-faa0e353-eae4-4745-9454-3530560a54c3.png)

> Maybe you think about how map android developers test their features and functionalities? should every person that wanna test the map applications have a car and go to the streets to test? actually, the answer is No! in Android, we have some applications that we call them "Mock", and these applications help us to generate fake locations or fake trips to test our Map application and for other purposes.

## :collision: What is the NMock?

**NMock** is an Application that helps you to generate fake locations, share fake trips to your friends, test your Map product, and so on... we build this application with **[Neshan SDK](https://platform.neshan.org/)**, **[Kotlin Language](https://kotlinlang.org/)**, **[Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)** and also **[Dagger-Hilt Dependency Injection](https://dagger.dev/hilt/)**. follow this document to know how this application developed and works!

## :v: Contribute

If you want to make me happy, you can contribute to this project:blush:! all of the time, we have some bugs, issues, and tasks that we can do in this project and help NMock to grow. so, if you want to contribute to this project, you can see the **[Issues Section](https://github.com/AbolfaZlRezaEe/NMock/issues)** or you can create a new issue and then create your pull request for that.

also, we have an opportunity for those people that don't have programming knowledge. if you haven't programming knowledge, you can ask, report, or do anything you want in **[Issues Section](https://github.com/AbolfaZlRezaEe/NMock/issues)**.

## :triangular_ruler: Architecture

after some experience that I've taken in some private or public projects like **[Niky](https://github.com/AbolfaZlRezaEe/NikY)**, I've learned some good things that help me to build this project.

at the first, I use MVVM and a piece of MVI architecture in this project. so, the application can be summarized in the chart below:

![architecture picture](https://user-images.githubusercontent.com/73066290/172200195-27916ce9-b467-42d3-b0f4-b650682bd1ea.png)

as you can see, we have two sources for our data in the application. remote API and Database. with remote API, we can request location information(reverse Geocoding) and also routing information for our trip. and if we want to save our trip, the database does that for us.

> also, we use **[Neshan API](https://platform.neshan.org/)** to receive these informations. you can check the documentation as well.

after that, we have three repositories that help us to manage these requests from **ViewModels**. this section is important because all of the processes make and control here. so, we receive data and convert that to a model that **ViewModels** can use and parse it for **Views**. in the picture below, you can see the difference between the two of them:

![architecture models](https://user-images.githubusercontent.com/73066290/172200304-0e7baeb1-7ae0-462c-97a8-cea06299aee0.png)

> the left model represents the data that we receive from the server. and the right picture represents the data that we need for our view.

in ViewModels, we have a different approach for giving access to the Views. every ViewModel can have two outputs and Views can use those for managing their views and actions. at the first, we have a **Stateflow** that represents the state for the view. this flow contains all of the information that the view needs to show(like MVI approach).

and the second output that works with **Sharedflow** sends some actions that can have a message or not. Views parse these actions and then, do an action in view like showing an error and so on... for example:

```kotlin
private fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mockEditorState.collect { state ->
                    showLoadingProgressbar(false)

                    state.originAddress?.let {
                        it.ifNotHandled { address -> processOriginAddress(address) }
                    }

                    state.destinationAddress?.let {
                        it.ifNotHandled { address -> processDestinationAddress(address) }
                    }

                    state.lineVector?.let {
                        it.ifNotHandled { lineVector -> processLineVector(lineVector) }
                    }

                    state.originLocation?.let {
                        it.ifNotHandled { location -> processMarker(true, location) }
                    }

                    state.destinationLocation?.let {
                        it.ifNotHandled { location -> processMarker(false, location) }
                    }
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.oneTimeEmitter.collect { processAction(it) }
        }
    }
```

an important thing that these Sharedflows can do is when an action receives from viewModel, it can be parsed and do an action to undo a thing in view. for example we call a function that requests receiving information from the server, and this request will be failed. for that, we send an action to view and cancel some stuff that we turned on before. you can see an example of that below:

![error architecture](https://user-images.githubusercontent.com/73066290/172200355-c851739b-7dbd-4ce2-a419-22a66a0b2bf3.png)

## Mock Export file structure

In some situations, you need to share your trip with your friends or your team workers. we have a feature that gives access you, to share your mock and send it everywhere you want. but what's the structure of this export file?

as you maybe know, we save your mock trip and the state of that. for exporting the mock trip, we use **JSON** as a format of the file. the structure of this file is like this:

```json
{
  "file_created_at": "Mon Jul 18 19:48:16 GMT+04:30 2022",
  "file_owner": "User UUID",
  "version_code": 7,
  "mock_information": {
    "type": "CUSTOM_CREATION",
    "name": "Hallo",
    "description": "Hallo description",
    "origin_address": "your mock origin address",
    "destination_address": "your mock destination address",
    "speed": 90,
    "bearing": 0,
    "accuracy": 1,
    "provider": "GPS_PROVIDER",
    "created_at": "Mon Jul 18 19:48:08 GMT+04:30 2022",
    "updated_at": "Mon Jul 18 19:48:08 GMT+04:30 2022"
  },
  "route_information": {
    "origin_location": "your mock origin location",
    "destination_location": "your mock destination address",
    "route_lines": [
      {
        "id": 87,
        "latitude": 35.71972,
        "longitude": 51.34778000000001,
        "time": 1658157488796,
        "elapsed_real_time": 1517906677658857
      },
      {
        "id": 88,
        "latitude": 35.719370000000005,
        "longitude": 51.34763,
        "time": 1658157488803,
        "elapsed_real_time": 1517906684490742
      },...
    ]
  }
}
```

1. **file_created_at(String)**: We save the Date/Time of the export file created in this field.

2. **file_owner(String)**: We save the User UUID in this field.

3. **version_code(Int)**: We save the application version code in this field.

4. **mock_information(Object)**: For saving mock information, we use an object that we call **mock_information**. as you can see, we save necessary information in that.

5. **route_information(Object)**: For saving route information and lines, we use this object for it. the important field of this object is **route_lines**. this object contains a list of positions that we should have for drawing lines on the map. every line has a **latitude** and **longitude** for its position.

we save this file in the format of `.json`. you can share it everywhere you want. maybe you think how can we import it?

## :bookmark_tabs: Tips that help you...

for developing this application, we have some tips that maybe can help you:

- If you want to Contribute, before starting that, please create an issue and describe your idea and assign that to me. I will check that as soon as possible.
- We publish this application in GooglePlay as soon as possible. so for now, you can install application from [release section](https://github.com/AbolfaZlRezaEe/NMock/releases) in repository.

