![NMock Banner](https://user-images.githubusercontent.com/73066290/198220049-159f1118-b181-415d-bc61-c9a0d7b1bf87.png)
## :collision: What is NMock?
Ever wondered how Android developers test map features and functionalities in their projects? Do they ride their cars or bikes to do so, or do they walk in the streets? The answer is no. 

There are applications in Android called Mock applications. They help us generate fake locations and trips to test map features. 

**NMock** is a tool that helps you generate fake locations and trips to test the map feature in your product. It enables you to share them with others as well. We've built this application with the help of the following products and services:
- **[Google Map SDK](https://developers.google.com/maps/documentation)**
-  **[Kotlin Language](https://kotlinlang.org/)** 
- **[Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)** 
- **[Dagger-Hilt Dependency Injection](https://dagger.dev/hilt/)**. 


This document is a guide on how the application works. It also covers the required steps on how to use it.

## :v: Contribution
Make me happy by contributing to this project:blush:! You can help me fix bugs, add features and resolve issues so NMock can grow.
To start your contribution, submit new issues and create pull requests. You can also check out the list of problems in the **[Issues Section](https://github.com/AbolfaZlRezaEe/NMock/issues)**.

No knowledge of programming? Don’t worry; you can contribute by asking questions and reporting issues in the **[Issues Section](https://github.com/AbolfaZlRezaEe/NMock/issues)**.

## :triangular_ruler: Architecture

I started this project based on the experiences I gained in previous projects. Private and public projects like **[Niky](https://github.com/AbolfaZlRezaEe/NikY)**. I learned a lot of helpful tips and tricks that played essential roles in doing this project.

I used the MVVM, and the MVI approaches to implement this project. The whole architecture can be summarized in the following image:

![architecture picture](https://user-images.githubusercontent.com/73066290/172200195-27916ce9-b467-42d3-b0f4-b650682bd1ea.png)

### Data source

As you can see in the above image, there are two sources of data in this application:
- Remote API
- Local database

Using the remote API, you can 
- Retrieve location information (reverse Geocoding)
- Retrieve routing information for your trip.
  
Then you can store all this information in your local database. 

> **[Neshan API](https://platform.neshan.org/)** is one of the APIs we use to retrieve information. Check out their documentation as well.

### Repositories

Then, we have three repositories that help us to manage these requests from **ViewModels**. This section is important because all of the processes make and control here. so, we receive data and convert that to a model that **ViewModels** can use and parse it for **Views**. in the picture below, you can see the difference between the two of them:

![architecture models](https://user-images.githubusercontent.com/73066290/172200304-0e7baeb1-7ae0-462c-97a8-cea06299aee0.png)

> The code on the left represents the data structure we receive from the server, and the code on the right indicates the structure we need for our view.


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

## :arrow_left::arrow_right: Mock Import/Export file structure

In some situations, you need to share your trip with your friends or your team workers. we have a feature that gives access you, to share your mock and send it everywhere you want. but what's the structure of this export file?

### Exporting Structure

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
      },
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

### Importing structure

as you can see in exporting section, you can share a `.json` file to your friends everywhere you want. for importing it, we can **Only** receive this `.json` file in the import section of the application(for the future, We have a plan for importing mocks in many ways!).

We save this `Json` file information like normal mocks but in a different table in the application database. We also save some extra information like: `fileCreatedAt`, `fileOwner` and `versionCode` in imported mock table. the reason that We split these mocks from normal mocks that you create in the application is, maybe the information of imported mocks can be less than normal mocks in the future. also, We have a plan for a new UI to show imported mocks in a different place in the application. so we create a new place for saving these mocks to managing them very easily in the future.

The structure of **Mock** model section of application is now like this:

![mock model instruction](https://user-images.githubusercontent.com/73066290/183247981-327e3280-d966-4908-aa95-49f2ca74f5be.png)

## :bookmark_tabs: Tips that help you...

for developing this application, we have some tips that maybe can help you:

- If you want to Contribute, before starting that, please create an issue and describe your idea and assign that to me. I will check that as soon as possible.
- We publish this application in GooglePlay as soon as possible. so for now, you can install application from [release section](https://github.com/AbolfaZlRezaEe/NMock/releases) in repository.

