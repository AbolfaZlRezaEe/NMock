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

We implemented three repositories that help us manage the requests from **ViewModels**. This section is essential because it makes and controlled all of the processes. 
We receive data and convert them into a standard model so that **ViewModels** can use and parse them for **Views**. 

The following image, shows the difference between the two of them:

![architecture models](https://user-images.githubusercontent.com/73066290/172200304-0e7baeb1-7ae0-462c-97a8-cea06299aee0.png)

> The code on the left represents the data structure we receive from the server, and the code on the right indicates the format we need for our view.


In ViewModels, we have two different approaches to accessing the views. Each ViewModel has two outputs that can be used by Views to manage their views and actions:
- The first one is a **Stateflow** which represents the state of the view. This flow contains all the information that the view needs to display (MVI approach)
- The second one works with a **Sharedflow** and sends some actions that can either contain a message or be empty. View, then parse these actions and perform several actions, like displaying an error, for example. 

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

One of the important actions that Sharedflows can do upon receiving an action from the ViewModel is to parse the action and undo something in view. For example, imagine that you sent a request to the server to retrieve data, but the server did not respond; now, we should send an action and hide or disable something that was shown or enabled before. The following image shows the flow:

![error architecture](https://user-images.githubusercontent.com/73066290/172200355-c851739b-7dbd-4ce2-a419-22a66a0b2bf3.png)

## :arrow_left::arrow_right: Mock Import/Export file structure

Sometimes you need to share your trip data with your friends or colleagues. We have implemented a feature allowing you to share your mocked data in JSON format easily.

### Export

We store your mock trip data and its state in **JSON** format. We use the same format to export this data. The following JSON is how we structure this data to export:

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

|Name| Type | Description |
|--|--|--|
| **file_created_at** | String | Creation Date/Time of the exported JSON file
|**file_owner**|String|User UUID
|**version_code**|Number| Application version code
|**mock_information**|Object| Necessary mock data
|**route_information**|Object| Route information and line. **route_lines** is the most important field in this object. It is an array containing a list of positions (nodes) to draw lines on the map. Every array item has a **latitude** and **longitude** for its position.


### Import

As you see in the previous section, you can export and share your mock data with your friends and colleagues in JSON format. To import any data to your project, you can easily import this file using the import feature. ( We have plans in the near future to add other methods to import data )

We store this imported JSON data like standard mock data in a different database table. We add the following additional column to each row:
- fileCreatedAt
- fileOwner
- versionCode

We separated the imported data so we could manage them easily. These are the reasons that give us the ability to do so:

- We may have less imported data in the future.
- We have a plan to implement a separate UI to display imported data.

The following image indicates the current structure of the **Mock** model section of the application.

![mock model instruction](https://user-images.githubusercontent.com/73066290/183247981-327e3280-d966-4908-aa95-49f2ca74f5be.png)

## :bookmark_tabs: Helpful tips

These are some helpful tips for developers:

- Before starting any contribution, please create an issue and describe your idea and then assign the issue to me. I will take a look as soon as possible.
- Soon, we will publish this application in the Google play store. But for now, you can install it from the [release](https://github.com/AbolfaZlRezaEe/NMock/releases) section of this repository. 

